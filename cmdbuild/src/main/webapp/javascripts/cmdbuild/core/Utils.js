(function() {

	CMDBuild.Utils = (function() {
		var idCounter = 0;

		return {
			mergeCardsData: function(cardData1, cardData2) {
				var out = {};

				for (var prop in cardData1)
					out[prop] = cardData1[prop];

				for (var prop in cardData2) {
					if (out[prop]) {
						if (typeof out[prop] == "object") {
							out[prop] = CMDBuild.Utils.mergeCardsData(cardData1[prop], cardData2[prop]);
						} else {
							continue;
						}
					} else {
						out[prop] = cardData2[prop];
					}
				}

				return out;
			},

			/*
			 * Used to trace a change in the type of the selection parameter between two minor ExtJS releases
			 */
			getFirstSelection: function(selection) {
				if (Ext.isArray(selection)) {
					return selection[0];
				} else {
					return selection;
				}
			},

			nextId: function() {
				return ++idCounter;
			},

			Metadata: {
				extractMetaByNS: function(meta, ns) {
					var xaVars = {};

					for (var metaItem in meta) {
						if (metaItem.indexOf(ns)==0) {
							var tmplName = metaItem.substr(ns.length);

							xaVars[tmplName] = meta[metaItem];
						}
					};

					return xaVars;
				}
			},

			Format: {
				htmlEntityEncode : function(value) {
					return !value ? value : String(value).replace(/&/g, "&amp;");
				}
			},

			lockCard: {
				isEnabled: function() {
					var enabled = CMDBuild.Config.cmdbuild.lockcardenabled;

					return _CMUtils.evalBoolean(enabled);
				}
			},

			evalBoolean: function(v) {
				if (typeof v == "string") {
					return v === "true";
				} else {
					return !!v; //return the boolean value of the object
				}
			},

			// FIXME: Should be getEntryTypePrivileges
			getClassPrivileges: function(classId) {
				var entryType = _CMCache.getEntryTypeById(classId);

				return _CMUtils.getEntryTypePrivileges(entryType);
			},

			getClassPrivilegesByName: function(className) {
				var entryType = _CMCache.getEntryTypeByName(className || "");

				return _CMUtils.getEntryTypePrivileges(entryType);
			},

			getEntryTypePrivileges: function(et) {
				var privileges = {
					write: false,
					create: false
				};

				if (et) {
					privileges = {
						write: et.get("priv_write"),
						create: et.isProcess() ? et.isStartable() : et.get("priv_create")
					};
				}

				return privileges;
			},

			getEntryTypePrivilegesByCard: function(card) {
				var privileges = {
					write: false,
					create: false
				};

				if (card) {
					var entryTypeId = card.get("IdClass");
					var entryType = _CMCache.getEntryTypeById(entryTypeId);

					privileges = _CMUtils.getEntryTypePrivileges(entryType);
				}

				return privileges;
			},

			/**
			 * @param (Object) obj
			 * @return (Boolean)
			 */
			isEmpty: function(obj) {
				if (obj == null)
					return true;

				if (obj.length > 0)
					return false;

				if (obj.length === 0)
					return true;

				for (var key in obj) {
					if (hasOwnProperty.call(obj, key))
						return false;
				}

				return true;
			},

			isSimpleTable: function(id) {
				var table = _CMCache.getEntryTypeById(id);

				if (table) {
					return table.data.tableType == CMDBuild.Constants.cachedTableType.simpletable;
				} else {
					return false;
				}
			},

			isProcess: function(id) {
				return (!!_CMCache.getProcessById(id));
			},

			groupAttributes: function(attributes, allowNoteFiled) {
				var groups = {};
				var fieldsWithoutGroup = [];

				for (var i = 0; i < attributes.length; i++) {
					var attribute = attributes[i];

					if (!attribute)
						continue;

					if (!allowNoteFiled && attribute.name == "Notes") {
						continue;
					} else {
						var attrGroup = attribute.group;
						if (attrGroup) {
							if (!groups[attrGroup])
								groups[attrGroup] = [];

							groups[attrGroup].push(attribute);
						} else {
							fieldsWithoutGroup.push(attribute);
						}
					}
				}

				if (fieldsWithoutGroup.length > 0)
					groups[CMDBuild.Translation.management.modcard.other_fields] = fieldsWithoutGroup;

				return groups;
			},

			/**
			 * for each element call the passed fn,
			 * with scope the element
			 **/
			foreach: function(array, fn, params) {
				if (array) {
					for (var i = 0, l = array.length; i < l; ++i) {
						var element = array[i];
						fn.call(element,params);
					}
				}
			},

			/**
			 *
			 * @param {array} array an array in which search something
			 * @param {function} fn a function that is called one time for each
			 * element in the array. The function must return true if the
			 * item is the searched
			 *
			 * @returns an object of the array if the passed function return true, or null
			 */
			arraySearchByFunction: function(array, fn) {
				if (!Ext.isArray(array) || !Ext.isFunction(fn))
					return null;

				for (var i = 0, l = array.length; i < l; ++i) {
					var el = array[i];

					if (fn(el))
						return el;
				}

				return null;
			},

			isSuperclass: function(idClass) {
				var c =  _CMCache.getEntryTypeById(idClass);

				if (c) {
					return c.get("superclass");
				} else {
					// TODO maybe is not the right thing to do...
					return false;
				}
			},

			getAncestorsId: function(entryTypeId) {
				var et = null;
				var out = [];

				if (Ext.getClassName(entryTypeId) == "CMDBuild.cache.CMEntryTypeModel") {
					et = entryTypeId;
				} else {
					et = _CMCache.getEntryTypeById(entryTypeId);
				}

				if (et) {
					out.push(et.get("id"));

					while (et.get("parent") != "") {
						et = _CMCache.getEntryTypeById(et.get("parent"));
						out.push(et.get("id"));
					}
				}

				return out;
			},

			getDescendantsById: function(entryTypeId) {
				var children = this.getChildrenById(entryTypeId);
				var et = _CMCache.getEntryTypeById(entryTypeId);
				var out = [et];

				for (var i = 0; i < children.length; ++i) {
					var c = children[i];
					var leaves = this.getDescendantsById(c.get("id"));

					out = out.concat(leaves);
				}

				return out;
			},

			forwardMethods: function (wrapper, target, methods) {
				if (!Ext.isArray(methods))
					methods = [methods];

				for (var i = 0, l = methods.length; i < l; ++i) {
					var m = methods[i];

					if (typeof m == "string" && typeof target[m] == "function") {
						var fn = function() {
							return target[arguments.callee.$name].apply(target, arguments);
						};

						fn.$name = m;
						wrapper[m] = fn;
					}
				}
			},

			getChildrenById: function(entryTypeId) {
				var ett = _CMCache.getEntryTypes();
				var out = [];

				for (var et in ett) {
					et = ett[et];

					if (et.get("parent") == entryTypeId)
						out.push(et);
				}

				return out;
			},

			grid: {
				getPageSize: function getPageSize() {
					var pageSize;

					try {
						pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
					} catch (e) {
						pageSize = 20;
					}

					return pageSize;
				},

				getPageNumber: function getPageNumber(cardPosition) {
					var pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
					var pageNumber = 1;

					if (cardPosition == 0)
						return pageNumber;

					if (cardPosition)
						pageNumber = parseInt(cardPosition) / pageSize;

					return pageNumber + 1;
				}
			},

			PollingFunction: function(conf) {
				var DEFAULT_DELAY = 500;
				var DEFAULT_MAX_TIMES = 60;

				this.success =  conf.success || Ext.emptyFn;
				this.failure = conf.failure || Ext.emptyFn;
				this.checkFn = conf.checkFn || function() { return true; };
				this.cbScope = conf.cbScope || this;
				this.delay = conf.delay || DEFAULT_DELAY;
				this.maxTimes = conf.maxTimes || DEFAULT_MAX_TIMES;
				this.checkFnScope = conf.checkFnScope || this.cbScope;

				this.run = function() {
					if (this.maxTimes == DEFAULT_MAX_TIMES)
						CMDBuild.LoadMask.get().show();

					if (this.maxTimes > 0) {
						if (this.checkFn.call(this.checkFnScope)) {
							_debug("End polling with success");
							CMDBuild.LoadMask.get().hide();
							this.success.call(this.cbScope);
						} else {
							this.maxTimes--;
							Ext.Function.defer(this.run, this.delay, this);
						}
					} else {
						_debug("End polling with failure");
						CMDBuild.LoadMask.get().hide();
						this.failure.call();
					}
				};
			}
		};
	})();

	_CMUtils = CMDBuild.Utils;

	Ext.define("CMDBuild.Utils.CMRequestBarrier", {
		constructor: function(cb) {
			var me = this;

			this.dangling = 1;

			this.cb = function () {
				me.dangling--;

				if (me.dangling == 0)
					cb();
			};
		},

		getCallback: function() {
			this.dangling++;
			return this.cb;
		},

		start: function() {
			this.cb();
		}
	});

	CMDBuild.extend = function(subClass, superClass) {
		var ob = function() {};

		ob.prototype = superClass.prototype;
		subClass.prototype = new ob();
		subClass.prototype.constructor = subClass;
		subClass.superclass = superClass.prototype;

		if(superClass.prototype.constructor == Object.prototype.constructor)
			superClass.prototype.constructor = superClass;
	};

	CMDBuild.isMixedWith = function(obj, mixinName) {
		var m = obj.mixins || {};

		for (var key in m) {
			var mixinObj = m[key];

			if (Ext.getClassName(mixinObj) == mixinName)
				return true;
		}

		return false;
	};

	CMDBuild.instanceOf = function(obj, className) {
		while (obj) {
			if (Ext.getClassName(obj) == className)
				return true;

			obj = obj.superclass;
		}

		return false;
	};

	CMDBuild.checkInterface = function(obj, interfaceName) {
		return CMDBuild.isMixedWith(obj, interfaceName) || CMDBuild.instanceOf(obj, interfaceName);
	};

	CMDBuild.validateInterface = function(obj, interfaceName) {
		CMDBuild.IS_NOT_CONFORM_TO_INTERFACE = "The object {0} must implement the interface: {1}";

		if (!CMDBuild.checkInterface(obj, interfaceName))
			throw Ext.String.format(CMDBuild.IS_NOT_CONFORM_TO_INTERFACE, obj.toString(), interfaceName);
	};

})();