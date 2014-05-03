(function() {

	Ext.define("CMDBuild.DummyModel", {
		extend: "Ext.data.Model",
		fields:[],

		// in recent past, ExtJs add any data passed to the
		// constructor to the new model. In ExtJs 4.1 this is
		// not true. So use the setFields at the costructor to
		// set the fields of the DummyModel every time that it
		// is created
		constructor: function(data) {
			data = data || {};
			CMDBuild.DummyModel.setFields(Ext.Object.getKeys(data));
			this.callParent(arguments);
		}
	});

	Ext.define("CMDBuild.cache.CMLookupTypeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "text",type: 'string'},
			{name: "parent",type: 'string'},
			{name: "type",type: 'string'}
		]
	});

	Ext.define("CMDBuild.cache.CMEntryTypeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "text",type: 'string'},
			{name: "superclass",type: 'boolean'},
			{name: "active",type: 'boolean'},
			{name: "parent",type: 'string'},
			{name: "tableType",type: 'string'},
			{name: "type",type: 'string'},
			{name: "name",type: 'string'},
			{name: "priv_create",type: 'boolean'},
			{name: "priv_write",type: 'boolean'},
			{name: "meta", type:"auto"},
			// Process only
			{name: "userstoppable",type: 'boolean'},
			{name: "startable", type: "boolean"}
		],

		constructor: function() {
			this.callParent(arguments);
			this._widgets = [];
		},

		isSuperClass: function() {
			return this.get("superclass");
		},

		getTableType: function() {
			return this.get("tableType");
		},

		isProcess: function() {
			return this.get("type") == "processclass";
		},

		isUserStoppable: function() {
			return this.get("userstoppable");
		},

		isStartable: function() {
			return this.get("startable");
		},

		setWidgets: function(widgets) {
			this._widgets = widgets || [];
		},

		getWidgets: function() {
			return this._widgets;
		},

		addWidget: function(w) {
			this._widgets.push(w);
		},

		removeWidgetById: function(id) {
			var ww = this._widgets;
			for (var i=0, l=ww.length; i<l; ++i) {
				var widget = ww[i];
				if (widget.id == id) {
					delete ww[i];
					ww.splice(i, 1);
					return;
				}
			}
		},

		getName: function() {
			return this.get("name");
		},

		getDescription: function() {
			return this.get("text");
		},

		// Attachment metadata management

		/*
		 * In the meta could be a map called attachments.
		 * Here are stored the rules to autocomplete the
		 * attachments metadata. The aspected structure is:
		 * ...
		 * meta: {
		 * 		...
		 * 		attachments: {
		 * 			...
		 * 			autocompletion: {
		 * 				groupName: {
		 * 					metadataName: rule,
		 * 					metadataName: rule,
		 * 					....
		 * 				},
		 * 				groupName: {
		 * 					...
		 *				}
		 * 			}
		 * 		}
		 * }
		 */
		getAttachmentAutocompletion: function() {
			var meta = this.get("meta");
			var out = {};
			if (meta
					&& meta.attachments) {

				out = meta.attachments.autocompletion || {};
			}

			return out;
		},

		getAttachmentCopletionRuleByGropAndMetadataName: function(groupName, metaDataName) {
			var rulesByGroup = this.getAttachmentAutocompletion();
			var groupRules = rulesByGroup[groupName];
			var rule = null;

			if (groupRules) {
				rule = groupRules[metaDataName] || null;
			}

			return rule;
		},

		toString: function() {
			return this.get("name");
		}
	});

	Ext.define("CMDBuild.cache.CMDomainModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "active", type: "boolean"},
			{name: "attributes", type: "auto"},
			{name: "cardinality", type: "string"},
			{name: "nameClass1", type: "string"},
			{name: "nameClass2", type: "string"},
			{name: "idClass1", type: "string"},
			{name: "idClass2", type: "string"},
			{name: "classType", type: "string"},
			{name: "name", type: "string"},
			{name: "createPrivileges", type: "boolean"},
			{name: "writePrivileges", type: "boolean"},
			{name: "isMasterDetail", type: "boolean"},
			{name: "description", type: "stirng"},
			{name: "descr_1", type: "stirng"},
			{name: "descr_2", type: "stirng"},
			{name: "md_label", type: "string"}
		],

		getAttributes: function() {
			var a = null;
			if (this.raw) {
				a = this.raw.attributes;
			}

			return a || this.data.attributes || [];
		},

		hasCreatePrivileges: function() {
			if (this.raw) {
				return this.raw.createPrivileges;
			} else {
				return this.data.createPrivileges;
			}
		},

		getSourceClassId: function() {
			return this.get("idClass1");
		},

		getDestinationClassId: function() {
			return this.get("idClass2");
		},

		getNSideIdInManyRelation: function() {
			var cardinality = this.get("cardinality");
			if (cardinality == "1:N") {
				return this.getDestinationClassId();
			}

			if (cardinality == "N:1") {
				return this.getSourceClassId();
			}

			return null;
		},

		getName: function() {
			return this.get("name");
		},

		getDescription: function() {
			return this.get("description");
		},

		// As master detail domain

		getDetailClassId: function() {
			var cardinality = this.get("cardinality");
			var classId = "";
			if (cardinality == "1:N") {
				classId = this.get("idClass2");
			} else if (cardinality == "N:1") {
				classId = this.get("idClass1");
			}

			return classId;
		},

		getDetailClassName: function() {
			var cardinality = this.get("cardinality");
			var className = "";
			if (cardinality == "1:N") {
				className = this.get("nameClass2");
			} else if (cardinality == "N:1") {
				className = this.get("nameClass1");
			}

			return className;
		},

		getMasterClassName: function() {
			var cardinality = this.get("cardinality");
			var className = "";
			if (cardinality == "1:N") {
				className = this.get("nameClass1");
			} else if (cardinality == "N:1") {
				className = this.get("nameClass2");
			}

			return className;
		},

		getDetailSide: function() {
			var c = this.get("cardinality");
			if (c == "1:N") {
				return "_2";
			} else if (c == "N:1") {
				return "_1";
			} else {
				return undefined;
			}
		}
	});

	Ext.define("CMDBuild.cache.CMReportModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "active", type: "boolean"},
			{name: "text", type: "string"},
			{name: "type", type: "string"},
			{name: "group", type: "string"}
		]
	});
	
	Ext.define("CMDBuild.cache.CMReporModelForGrid", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "id",type: 'string'},
			{name: "type", type: "string"},
			{name: "groups", type: "string"},
			{name: "query", type: "string"},
			{name: "description", type: "string"},
			{name: "title", type: "string"}
		]
	});

   	Ext.define("CMDBuild.cache.CMReferenceStoreModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "Id", type: 'int'},
			{name: "Description",type: 'string'}
		]
	});

	Ext.define("CMDBuild.model.CMFilterModel", {
		extend: "Ext.data.Model",
		fields: [{
			name: "id",
			type: "string"
		},{
			name: "name",
			type: "string"
		}, {
			name: "description",
			type: "string"
		}, {
			name: "configuration",
			type: "auto"
		}, {
			name: "entryType",
			type: "string"
		},{
			name: "template",
			type: "boolean"
		},

		/**
		 * To know if this filter is currently applied
		 */
		{
			name: "applied",
			type: "boolean",
			persist: false
		},

		/**
		 * to know if the filter is created client side,
		 * and is not sync with the server
		 */
		{
			name: "local",
			type: "boolean",
			persist: false
		}],

		/**
		 * Return a full copy of this filter
		 * 
		 * @override
		 * @returns {CMDBuild.model.CMFilterModel}
		 */
		copy: function() {
			var dolly = new CMDBuild.model.CMFilterModel();
			dolly.set("id", this.get("id"));
			dolly.setName(this.getName());
			dolly.setDescription(this.getDescription());
			dolly.setConfiguration(Ext.apply({}, this.getConfiguration()));
			dolly.setEntryType(this.getEntryType());
			dolly.setApplied(this.isApplied());
			dolly.setLocal(this.isLocal());
			dolly.setTemplate(this.isTemplate());

			dolly.commit();

			if (this.dirty) {
				dolly.setDirty();
			}

			return dolly;
		},

		// Getter and setter

		getName: function() {
			var name = this.get("name") || "";
			return name;
		},

		setName: function(name) {
			this.set("name", name);
		},

		getDescription: function() {
			var description = this.get("description") || "";
			return description;
		},

		setDescription: function(description) {
			this.set("description", description);
		},

		getConfiguration: function() {
			var filter = this.get("configuration") || {};
			return filter;
		},

		getConfigurationMergedWithRuntimeAttributes: function(_runtimeParameterFields) {
			var configuration = Ext.clone(this.get("configuration"));
			var runtimeParameterFields = _runtimeParameterFields || [];

			var indexOfLastRuntimeAttributeMerged = 0;
			configuration.attribute = mergeRuntimeParametersToConf( //
				configuration.attribute, //
				runtimeParameterFields, //
				indexOfLastRuntimeAttributeMerged //
			);

			return configuration;
		},

		setConfiguration: function(configuration) {
			this.set("configuration", configuration);
		},

		getAttributeConfiguration: function() {
			var c = this.getConfiguration();
			var attributeConf = c.attribute || {};

			return attributeConf;
		},

		setAttributeConfiguration: function(conf) {
			var configuration = this.getConfiguration();
			delete configuration.attribute;
			if (Ext.isObject(conf) && Ext.Object.getKeys(conf).length > 0) {
				configuration.attribute = conf;
				this.set("configuration", configuration);
			}
		},

		getRuntimeParameters: function() {
			var runtimeParameters = [];
			var attributeConf = this.getAttributeConfiguration();

			return addRuntimeParameterToList(attributeConf, runtimeParameters);
		},

		getCalculatedParameters: function() {
			var calculatedParameters = [];
			var attributeConf = this.getAttributeConfiguration();

			return addCalculatedParameterToList(attributeConf, calculatedParameters);
		},

		getRelationConfiguration: function() {
			var configuration = this.getConfiguration();
			var relationConfiguration = configuration.relation || [];

			return relationConfiguration;
		},

		setRelationConfiguration: function(conf) {
			var configuration = this.getConfiguration();
			delete configuration.relation;

			if (Ext.isArray(conf) && conf.length > 0) {
				configuration.relation = conf;
				this.set("configuration", configuration);
			}
		},

		getFunctionConfiguration: function() {
			var c = this.getConfiguration();
			var attributeConf = c.functions || [];
			return attributeConf;
		},

		setFunctionConfiguration: function(functions) {
			var configuration = this.getConfiguration();
			if (functions.length > 0) {
				configuration.functions = functions;
			}
			else {
				delete configuration.functions;
			}
			this.set("configuration", configuration);
		},

		getEntryType: function() {
			var entryType = this.get("entryType") || "";
			return entryType;
		},

		setEntryType: function(entryType) {
			this.set("entryType", entryType);
		},

		isTemplate: function() {
			var applied = this.get("template") || false;
			return applied;
		},

		setTemplate: function(applied) {
			this.set("template", applied);
		},

		isApplied: function() {
			var applied = this.get("applied") || false;
			return applied;
		},

		setApplied: function(applied) {
			this.set("applied", applied);
		},

		isLocal: function() {
			var local = this.get("local") || false;
			return local;
		},

		setLocal: function(local) {
			this.set("local", local);
		}
	});

	function addRuntimeParameterToList(attributeConf, runtimeParameters) {
		if (Ext.isObject(attributeConf.simple)) {
			var conf = attributeConf.simple;
			if (conf.parameterType == "runtime") {
				runtimeParameters.push(conf);
			}
		} else if (Ext.isArray(attributeConf.and) 
				|| Ext.isArray(attributeConf.or)) {

			var attributes = attributeConf.and || attributeConf.or;
			for (var i=0, l=attributes.length; i<l; ++i) {
				addRuntimeParameterToList(attributes[i], runtimeParameters);
			}
		}

		return runtimeParameters;
	}

	function addCalculatedParameterToList(attributeConf, calculatedParameters) {
		if (Ext.isObject(attributeConf.simple)) {
			var conf = attributeConf.simple;
			if (conf.parameterType == "calculated") {
				calculatedParameters.push(conf);
			}
		} else if (Ext.isArray(attributeConf.and) 
				|| Ext.isArray(attributeConf.or)) {

			var attributes = attributeConf.and || attributeConf.or;
			for (var i=0, l=attributes.length; i<l; ++i) {
				addCalculatedParameterToList(attributes[i], calculatedParameters);
			}
		}

		return calculatedParameters;
	}

	var calculatedValuesMapping = {};
	calculatedValuesMapping["@MY_USER"] = function() {
		return CMDBuild.Runtime.UserId;
	};

	calculatedValuesMapping["@MY_GROUP"] = function() {
		return CMDBuild.Runtime.DefaultGroupId;
	};

	function mergeRuntimeParametersToConf(attributeConfiguration, runtimeParameterFields, indexOfLastRuntimeAttributeMerged) {
		var attributeConf = Ext.clone(attributeConfiguration);
		if (!attributeConf) {
			return;
		}

		if (Ext.isObject(attributeConf.simple)) {
			var conf = attributeConf.simple;
			if (conf.parameterType == "runtime") {
				var field = runtimeParameterFields[indexOfLastRuntimeAttributeMerged++];
				delete conf.parameterType;

				var value = [field.getValue()];
				if (field._cmSecondField) {
					value.push(field._cmSecondField.getValue());
				}

				conf.value = value; 
			} else if (conf.parameterType == "calculated") {
				var value = conf.value[0];
				if (typeof calculatedValuesMapping[value] == "function") {
					conf.value = [calculatedValuesMapping[value]()];
				}
			}

		} else if (Ext.isArray(attributeConf.and) 
				|| Ext.isArray(attributeConf.or)) {

			var attributes = attributeConf.and || attributeConf.or;
			for (var i=0, l=attributes.length; i<l; ++i) {
				attributes[i] = mergeRuntimeParametersToConf(attributes[i], runtimeParameterFields, indexOfLastRuntimeAttributeMerged);
			}
		}

		return attributeConf;
	}

})();