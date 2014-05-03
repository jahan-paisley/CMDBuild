(function() {

if (typeof CMDBuild.Management == "undefined") {
	CMDBuild.Management = {};
}

CMDBuild.Management.TemplateResolver = function(config) {
	Ext.apply(this, config);
};

var isComboField = function(field) {
	return field.getReadableValue;
};

CMDBuild.Management.TemplateResolver.prototype = {

	getVariable: function(variable, ctx) {
		var varQName = this.getQName(variable);
		var nsFunctionArray = {
				client: this.getActivityFormVariable,
				server: this.getActivityServerVariable,
				user: this.getCurrentUserInfo,
				group: this.getCurrentGroupInfo,
				xa: this.getExtendedAttributeVariable,
				js: this.getJSVariable,
				cql: this.getCQLVariable
			};
		if (varQName.namespace in {cql:"",js:""} && !ctx) {
			CMDBuild.log.error("No direct resolver for namespace " + varQName.namespace);
			return "";
		}
		var nsFunction = nsFunctionArray[varQName.namespace];
		if (nsFunction) {
			return nsFunction.call(this, varQName.localname, ctx);
		} else {
			CMDBuild.log.error("No resolver for namespace " + varQName.namespace);
			return "";
		}
	},

	/*** namespaces that can be resolved directly ***/

	// private
	getActivityFormVariable: function(varName) {
		var value;
		var splitLocalName = this.splitLocalName(varName);
		var field = this.findFormField(splitLocalName.name);
		if (field) {
			if (isComboField(field)) {
				// reference and lookup
				value = {
						"Id": function() { return field.getValue(); },
						"Description": function() { return field.getReadableValue(); },
						"": function() {
								CMDBuild.log.warn("It is best to specify a detail when using lookup and reference types");
								return field.getValue();
							}
					}[splitLocalName.detail]();
			} else {
				if (splitLocalName.detail) {
					CMDBuild.log.warn("Detail can only be specified for lookup and reference types");
					CMDBuild.Msg.warn(CMDBuild.Translation.errors.warning_message,
							CMDBuild.Translation.errors.template_error);
				}

				if (typeof field.getRawValue == "function") {
					value = field.getRawValue();
				} else {
					value = field.getValue();
				}
			}
		}
		CMDBuild.log.debug("Activity form variable " + varName + " = " + value);
		return value;
	},

	// private
	findFormField: function(varName) {

		/*
		 * Look also into the DisplayFields
		 */
		function lookForFieldName(f) {
			return f.name == varName; 
		}

		/*
		 * usually only the editable fields
		 * has a CMAttribute associated
		 */
		function lookForCMAttributeName(f) {
			if (!f.CMAttribute) {
				return false;
			} else {
				return f.CMAttribute.name == varName;
			}
		}

		var fields = this.getBasicForm().getFields();
		var field = fields.findBy(lookForCMAttributeName) || fields.findBy(lookForFieldName);

		return field;
	},

	getBasicForm: function() {
		return this.clientForm;
	},

	// private
	getActivityServerVariable: function(varName) {
		var splitLocalname = this.splitLocalName(varName);
		var sv = this.getServerVars();
		if (!sv) {
			CMDBuild.log.debug("Activity server variable " + varName + " = No server vars");
			return undefined;
		}

		var v = sv[splitLocalname.name];
		if (v != null && 
				typeof v == "object") {

			if (splitLocalname.detail == "Description") {
				v = v.description;
			} else {
				v = v.id;
			}
		}

		CMDBuild.log.debug("Activity server variable " + varName + " = " + v);
		return v;
	},

	getServerVars: function() {
		return this.serverVars;
	},

	// private
	splitLocalName: function(localname) {
		var splitIndex;
		if ((splitIndex = localname.indexOf(".")) > 0) {
			return {
					name: localname.slice(0,splitIndex),
					detail: localname.slice(splitIndex+1)
				};
		} else if ((splitIndex = localname.search("_value")) > 0) {
			// For backward compatibility,
			// AttributeName_value is treated like AttributeName.Description
			return {
				name: localname.slice(0,splitIndex),
				detail: "Description"
			};
		} else {
			return {
					name: localname,
					detail: ""
			};
		}
	},

	// private
	getCurrentUserInfo: function(varName) {
		var infoMap = {
			name: CMDBuild.Runtime.Username,
			id: CMDBuild.Runtime.UserId
		};
		return infoMap[varName];
	},

	// private
	getCurrentGroupInfo: function(varName) {
		var infoMap = {
			name: CMDBuild.Runtime.DefaultGroupName,
			id: CMDBuild.Runtime.DefaultGroupId
		};
		return infoMap[varName];
	},

	// private
	getExtendedAttributeVariable: function(varName) {
		var value = this.xaVars[varName];
		if (typeof value == 'string') {
			var templateValue = this.expandTemplate(value);
			CMDBuild.log.debug("Extended Attribute expanded template " + varName + " = " + templateValue);
			return templateValue;
		} else {
			// sometimes in an activity the extended attribute definition is not a string
			CMDBuild.log.debug("Extended Attribute variable " + varName + " = " + value);
			return value;
		}
	},

	/*** namespaces that need context ***/

	// private
	getJSVariable: function(varName, ctx) {
		return ctx.js[varName];
	},

	// private
	getCQLVariable: function(varName, ctx) {
		var cqlSplit = varName.split(".");
		if (cqlSplit.length > 1) {
			var cqlQueryName = cqlSplit[0];
			var cqlField = cqlSplit[1];
			var cqlRecord = ctx.cql[cqlQueryName];
			if (cqlRecord) {
				return cqlRecord[cqlField];
			} else {
				return undefined;
			}
		} else {
			CMDBuild.log.error("Can't determine field name for CQL variable " + varName);
			return "";
		}
	},

	/********************************
	 * template expansion functions *
	 ********************************/
	
	/*
	 * conf:
	 *   attributes: attribute name array
	 *   callback: callback to call after the templates have been resolved
	 *   scope: scope for the callback
	 */
	resolveTemplates: function(conf) {
		var callbackFn = conf.callback || Ext.emptyFn;
		if (conf.scope) {
			callbackFn = Ext.bind(callbackFn,conf.scope);
		}
		var templates = conf.attributes;
		var deps = this.getTemplateSetDeps(templates, this.xaVars);
		CMDBuild.log.debug("External template dependencies", deps);
		var topoList = this.topoSort(deps);
		CMDBuild.log.debug("Topological order", topoList);
		this.evalTemplates(topoList, function(ctx) {
			callbackFn(ctx.out, ctx);
		});
	},

	/*** resolve template dependencies ***/

	// private
	getTemplateSetDeps: function(attrList) {
		var externalDeps = {};
		var localDeps = {};
		var attrSet = {};
		for (var i=0,len=attrList.length; i<len; ++i) {
			var attrName = attrList[i];
			attrSet["xa:"+attrName] = null;
		}
		this.updateTemplateSetDeps(attrSet, externalDeps, localDeps);
		this.localDeps = localDeps; // local deps for the whole set
		this.externalDeps = externalDeps;
		return externalDeps;
	},

	// private
	updateTemplateSetDeps: function(attrSet, externalDeps, localDeps) {
		for (var attrName in attrSet) {
			if (typeof externalDeps[attrName] == "undefined") {
				externalDeps[attrName] = null; // to detect cycles
				this.updateTemplateDeps(attrName, externalDeps, localDeps);
			}
		}
	},

	// private
	updateTemplateDeps: function(fullAttrName, externalDeps, localDeps) {
		var qName = this.getQName(fullAttrName);
		var template = this.xaVars[qName.localname];
		if (typeof template == "string") {
			var recursionVars = this.extractRecursionVarsAndUpdateLocalDeps(template, localDeps);
			externalDeps[fullAttrName] = recursionVars;
			this.updateTemplateSetDeps(recursionVars, externalDeps, localDeps);
		}
	},

	/*
	 * Extact only variables that are templates themselves
	 */
	// private
	extractRecursionVarsAndUpdateLocalDeps: function(template, localDeps) {
		var templateParts = this.splitTemplate(template);
		var templateVars = {};
		for (var i=0, len=templateParts.length; i<len; ++i) {
			var part = templateParts[i];
			if (part.qvar) {
				var ns = part.qvar.namespace;
				function addRecursionVar(qvar) {
					// remove cql attribute specifier
					var nsVar = part.qvar.raw.split('.')[0];
					templateVars[nsVar] = true;
				};
				function addLocalVar(qvar) {
					var varOnly = part.qvar.localname.split('.')[0];
					localDeps[varOnly] = true;
				};
				var fnMap = {
					cql: addRecursionVar,
					js: addRecursionVar,
                                        xa: addRecursionVar,
					client: addLocalVar
				};
				if (ns in fnMap) {
					fnMap[ns](part.qvar);
				}
			}
		}
		return templateVars;
	},

	/*** topological sort of the dependencies ***/

	//private
	topoSort: function (deps) {
	      var dead = {};
	      var list = [];
	      for (var d in deps)
	            dead[d] = false;
	      for (var d in deps)
	            this.topoVisit(deps, d, list, dead);
	      return list;
	},

	//private
	topoVisit: function (deps, d, list, dead) {
	      if (dead[d])
	            return;
	      dead[d] = true;
	      for (var child in deps[d])
	            this.topoVisit(deps, child, list, dead);
	      list.push(d);
	},

	/*** ***/

	//private
	evalTemplates: function(topoList, callback, ctx, i) {
		if (!ctx) ctx = { cql: {}, js: {}, out: {} };
		if (!i) i = 0;
		if (i < topoList.length) {
			var templateName = topoList[i];
			var t = this;
			this.evalTemplate(templateName, ctx, function() {
				t.evalTemplates(topoList, callback, ctx, ++i);
			});
		} else {
			callback(ctx);
		}
	},

	//private
	evalTemplate: function(templateName, ctx, callback) {
		var me = this;
		this.waitForBusyDeps(function() {
			CMDBuild.log.info("Evaluating template " + templateName);
			var qName = me.getQName(templateName);
			var localname = qName.localname;
			var ns = qName.namespace;
			var template = me.xaVars[localname];
			CMDBuild.log.debug("Template", template);
			CMDBuild.log.debug("Current context", ctx);
			if (typeof template == "string") {
				if (ns == "cql") {
					me.executeCQLTemplate(localname, template, ctx, callback);
				} else if (ns == "js") {
					ctx.js[localname] = me.evalJSTemplate(localname, template, ctx);
					callback(ctx);
				} else {
					ctx.out[localname] = me.expandTemplate(template, ctx);
					callback(ctx);
				}
			} else {
				ctx.out[localname] = template;
				callback(ctx);
			}
		});
	},

	waitForBusyDeps: function(cb) {
		var deps = this.getLocalDepsAsField();
		var busy = false;
		for (var fieldName in deps) {
			var field = deps[fieldName];
			if (field && field.templateResolverBusy) {
				busy = true;
				break;
			}
		}

		if (busy) {
			var me = this;
			Ext.Function.createDelayed(function(cb) {
				me.waitForBusyDeps(cb);
			}, 200)(cb);
		} else {
			cb();
		}
	},

	//private
	executeCQLTemplate: function(templateName, cqlQuery, ctx, callback) {
		var queryParams = this.buildCQLQueryParameters(cqlQuery, ctx);
		if (queryParams) {
			CMDBuild.Ajax.request({
				url: "services/json/management/modcard/getcardlist",
				params: queryParams,
				success: function(response, options, decoded) {
					var row = decoded.rows[0];
					ctx.cql[templateName] = row;
					callback(ctx);
				}
			});
		} else {
			callback(ctx);
		}
	},

	// should be private, but it is needed by LinkCards
	buildCQLQueryParameters: function(cqlQuery, ctx) {
		if (!cqlQuery) {
			return undefined;
		}
		var params = {
			CQL: ""
		};
		var openSlashes = 0; // 0 or 1
		var templateParts = this.splitTemplate(cqlQuery);
		for (var i=0, len=templateParts.length; i<len; ++i) {
			var item = templateParts[i];
			if (item.qvar) {
				var escapedVarName = "p" + i;
				var value = this.getVariable(item.qvar, ctx);
				if (value !== 0 && !value) { // NOTE: CQL is undefined if any of its variables is undefined OR EMPTY
					return undefined;
				}
				if (openSlashes == 0) {
					params.CQL += "{"+escapedVarName+"}";
					params[escapedVarName] = value;
				} else {
					params.CQL += value;
				}
			}
			if (item.text) {
				params.CQL += item.text;
				openSlashes ^= (item.text.match(/\//g) || []).length % 2;
			}
		}
		return params;
	},

	/*
	 * Fills the template substituting the variables with string
	 * constants with their value 
	 */
	//private
	evalJSTemplate: function(templateName, template, ctx) {
		CMDBuild.log.debug("JS Eval template " + template);
		var jsExpr = "";
		var parts = this.splitTemplate(template);
		for (var i=0,len=parts.length; i<len; ++i) {
			var part = parts[i];
			if (part.qvar) {
				var value = this.getVariable(part.qvar, ctx);
				var escapedVal;
				if (typeof value == "undefined") {
					escapedVal = "undefined";
				} else {
					var stringVal = value + '';
					escapedVal = "'" + Ext.String.escape(stringVal) + "'";
				}
				jsExpr += escapedVal;
			}
			if (part.text) { jsExpr += part.text; }
		}

		return this.safeJSEval(jsExpr);
	},

	/*
	 * Fills the template expanding its variables
	 * (already in the context or that can be immediately resolved)
	 */
	//private
	expandTemplate: function(template, ctx) {
		CMDBuild.log.debug("Resolving template " + template);
		var out = "";
		var parts = this.splitTemplate(template);
		for (var i=0,len=parts.length; i<len; ++i) {
			var part = parts[i];
			if (part.qvar) {
				var value = this.getVariable(part.qvar, ctx);
				if (typeof value == "undefined") {
					return undefined;
				}
				out += value;
			}
			if (part.text) { out += part.text; }
		}
		return out;
	},

	/*** utility functions ***/

	// private
	splitTemplate: function(template) {
		if (template) {
			var templateParts = [];
			var halfSplit = template.split("{");
			templateParts.push({
				text: halfSplit[0]
			});
			for (var i=1, len=halfSplit.length; i<len; ++i) {
				var subSplit = halfSplit[i].split("}");
				if (subSplit.length != 2) {
					CMDBuild.log.error("Unable to parse template " + template);
					return undefined;
				}
				templateParts.push({
					qvar: this.getQName(subSplit[0]),
					text: subSplit[1]
				});
			}
			return templateParts;
		} else {
			return {
					text: template
				};
		}
	},

	/*
	 * Splits the full name in namespace and local name
	 */
	// private
	getQName: function(variable) {
		if (variable instanceof Object) { // already a qname
			return variable;
		}
		var nsSplitIndex = variable.indexOf(":");
		if (nsSplitIndex == -1) {
			nsSplitIndex = variable.indexOf("#"); // try cql var namespace character
		}
		if (nsSplitIndex == -1) {
			return {
				raw: variable,
				namespace: "server", // default namespace
				localname: variable
			};
		} else {
			return {
				raw: variable,
				namespace: variable.slice(0,nsSplitIndex),
				localname: variable.slice(nsSplitIndex+1)
			};
		}
	},

	getLocalDepsAsField: function() {
		var out = {};
		for (var i in this.localDeps) {
			var f = this.findFormField(i);
			out[i] = f;
		}
		return out;
	},

	bindLocalDepsChange: function(callback, scope) {
		var ld = this.getLocalDepsAsField(),
			callback = callback || Ext.empltyFn,
			scope = scope || this;

		for (var i in ld) {
			//before the blur if the value is changed
			var field = ld[i];

			if (field) {
				// For check-box and HTMLEditor, call directly the
				// callback. For other attributes set the field
				// as changed, and call the callback at blur
				field.mon(field, "change", function(f) {
					if (Ext.getClassName(f) == "Ext.ux.form.XCheckbox" ||
							Ext.getClassName(f) == "CMDBuild.view.common.field.CMHtmlEditorField"
						) {

						callback.call(scope);
					} else {
						f.changed = true;
					}
				}, this);

				field.mon(field, "blur", function(f) {
					if (f.changed) {
						f.changed = false;

						callback.call(scope);
					}
				}, this);
			}
		}
	},

	safeJSEval: function(stringTOEvaluate) {
		var resultOfEval = "";

		try {
			resultOfEval = eval(stringTOEvaluate);
		} catch (e) {
			/*
			 * happens that some jsExpr contains
			 * characters that break the eval()
			 * so try again replacing the
			 * characters that was already identified
			 * as problematic
			*/
			try {
				resultOfEval = eval(stringTOEvaluate.replace(/(\r\n|\r|\n|\u0085|\u000C|\u2028|\u2029)/g,""));
			} catch (ee) {
				_debug("Error evaluating javascript expression", stringTOEvaluate);
			}
		}

		return resultOfEval;
	}
};

})();
