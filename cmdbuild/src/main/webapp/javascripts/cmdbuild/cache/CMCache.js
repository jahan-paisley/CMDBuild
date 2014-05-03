(function() {
	var constants = CMDBuild.Constants;
	var dashboardClassesProcessStore = null;

	Ext.define("CMDBuild.cache.CMCache", {
		extend: "Ext.util.Observable",

		mixins: {
			lookup: "CMDBUild.cache.CMCacheLookupFunctions",
			entryType: "CMDBUild.cache.CMCacheClassFunctions",
			groups: "CMDBUild.cache.CMCacheGroupsFunctions",
			domains: "CMDBUild.cache.CMCacheDomainFunctions",
			reports: "CMDBUild.cache.CMCacheReportFunctions",
			dashboards: "CMDBuild.cache.CMCacheDashboardFunctions",
			attachmentCategories: "CMDBUild.cache.CMCacheAttachmentCategoryFunctions",
			gis: "CMDBUild.cache.CMCacheGisFunctions",
			filters: "CMDBuild.cache.CMCacheFilterFunctions",
			translations: "CMDBUild.cache.CMCacheTranslationsFunctions",
			navigationTrees: "CMDBUild.cache.CMCacheNavigationTreesFunctions"
		},

		constructor: function() {
			this._lookupTypes={};

			this.toString = function() {
				return "CMCache";
			};

			this.callParent(arguments);
			this.mapOfAttributes = {};
			this.mapOfReferenceStore = {};
		},

		/**
		 * Loads all classes attributes
		 *
		 * @return (Array) mapOfAttributes
		 */
		getAllAttributesList: function() {
			for (key in _CMCache.getClasses()) {
				_CMCache.getAttributeList(key, function(attributes) {
					return;
				});
			}

			return this.mapOfAttributes;
		},

		getAttributeList: function(idClass, callback) {
			if (this.mapOfAttributes[idClass]) {
				var attributes = this.mapOfAttributes[idClass];
				callback(attributes);
			} else {
				this.loadAttributes(idClass, callback);
			}
		},

		loadAttributes: function(classId, callback) {
			var me = this;
			var parameterNames = CMDBuild.ServiceProxy.parameter;
			var params = {};
			params[parameterNames.ACTIVE] = true;
			params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(classId);

			function success(response, options, result) {
				var attributes = result.attributes;
				var visibleAttributes = [];
				for (var i=0, l=attributes.length; i<l; ++i) {
					var attribute = attributes[i];
					if (attribute.fieldmode != "hidden") {
						visibleAttributes.push(attribute);
					}
				}

				visibleAttributes.sort(function(a,b){return a.index - b.index;});

				me.mapOfAttributes[classId] = visibleAttributes;
				if (callback) {
					callback(visibleAttributes);
				}
			}

			CMDBuild.ServiceProxy.attributes.read({
				params: params,
				success: success
			});
		},

		getReferenceStore: function(reference) {
			var key = reference.referencedClassName || reference.referencedIdClass;
			var fieldFilter = false;
			var oneTimeStore = null;

			if (reference.filter || reference.oneTime) {
				//build a non cached store with the filter active
				oneTimeStore = this.buildReferenceStore(reference);
				//set the fieldFilter to false and save the current value
				//of the fieldFilter to allow the building of a full store
				fieldFilter = reference.filter;
				reference.filter = false;
			}

			//build a not filtered store and cache it
			if (!this.mapOfReferenceStore[key]) {
				this.mapOfReferenceStore[key] = this.buildReferenceStore(reference);
			}

			//restore the fieldFilter
			if (fieldFilter) {
				reference.filter = fieldFilter;
			}

			return oneTimeStore || this.mapOfReferenceStore[key];
		},

		getReferenceStoreById: function(id) {
			return this.mapOfReferenceStore[id];
		},

		//private
		buildReferenceStore: function(reference) {
			var baseParams = this.buildParamsForReferenceRequest(reference),
				isOneTime = baseParams.CQL ? true : false,
				maxCards = parseInt(CMDBuild.Config.cmdbuild.referencecombolimit);

			var s = new Ext.data.JsonStore({
				model : "CMDBuild.cache.CMReferenceStoreModel",
				isOneTime: isOneTime,
				baseParams: baseParams, //retro-compatibility,
				pageSize: maxCards,
				proxy: {
					type: 'ajax',
					url: 'services/json/management/modcard/getcardlistshort',
					reader: {
						type: 'json',
						root: 'rows',
						totalProperty: 'results'
					},
					extraParams: baseParams
				},
				sortInfo: {
					field: 'Description',
					direction: 'ASC'
				},
				autoLoad : !isOneTime
			});

			return s;
		},

		//private
		buildParamsForReferenceRequest: function(reference) {
			var idClass = reference.idClass || reference.referencedIdClass;
			var className = reference.referencedClassName
				|| _CMCache.getEntryTypeNameById(idClass);

			var baseParams = {
				className: className
			};

			if (reference.filter) {
				baseParams.filter = Ext.encode({
					CQL: reference.filter
				});
			} else {
				baseParams.NoFilter = true;
			}

			return baseParams;
		},

		getForeignKeyStore: function(foreignKye) {
			var maxCards = parseInt(CMDBuild.Config.cmdbuild.referencecombolimit),
				baseParams = {
					limit: maxCards,
					className: foreignKye.fkDestination,
					NoFilter: true
				};

			var s = new Ext.data.JsonStore({
				model : "CMDBuild.cache.CMReferenceStoreModel",
				baseParams: baseParams, //retro-compatibility
				proxy: {
					type: 'ajax',
					url: 'services/json/management/modcard/getcardlistshort',
					reader: {
						type: 'json',
						root: 'rows'
					},
					extraParams: baseParams
				},
				sortInfo: {
					field: 'Description',
					direction: 'ASC'
				},
				autoLoad : true
			});

			return s;
		},

		isDescendant: function(subclassId, superclassId) {
			function isDescendant(tree, superclassId, subclassId) {
				if (!tree) { // don't know if this is needed
					return false;
				}

				var ids = {};
				tree.cascade(function() { ids[this.id]=this; });
				var subClass = ids[subclassId];
				var superClass = ids[superclassId];
				return superClass && subClass && subClass.isAncestor(superClass);
			};
			return superclassId == subclassId
				|| isDescendant(this.getTree(CMDBuild.Constants.treeNames.classTree), superclassId, subclassId)
				|| isDescendant(this.getTree(CMDBuild.Constants.treeNames.processTree), superclassId, subclassId);
		},

		onClassContentChanged: function(idClass) {
			reloadRelferenceStore(this.mapOfReferenceStore, idClass);
		},

		getTableGroup: getTableGroup,

		getClassesAndProcessesAndDahboardsStore: function() {
			if (dashboardClassesProcessStore == null) {
				var classesAndProcessStore = this.getClassesAndProcessesStore();
				var me = this;

				dashboardClassesProcessStore = new Ext.data.Store({
					model: "CMTableForComboModel",
					cmFill: function() {
						var dashboards = readDashboardsForComboStore(me);
						var classesAndProcesses = classesAndProcessStore.data.items;

						this.removeAll();
						this.add(dashboards);
						this.add(classesAndProcesses);
					},
					sorters: [{
						property : 'description',
						direction : 'ASC'
					}]
				});

				classesAndProcessStore.cmFill = Ext.Function.createSequence(classesAndProcessStore.cmFill, function() {
					dashboardClassesProcessStore.removeAll();
					dashboardClassesProcessStore.add(classesAndProcessStore.data.items);
					dashboardClassesProcessStore.add(readDashboardsForComboStore(me));
				});

				this.on(this.DASHBOARD_EVENTS.add, dashboardClassesProcessStore.cmFill, dashboardClassesProcessStore);
				this.on(this.DASHBOARD_EVENTS.remove, dashboardClassesProcessStore.cmFill, dashboardClassesProcessStore);
				this.on(this.DASHBOARD_EVENTS.modify, dashboardClassesProcessStore.cmFill, dashboardClassesProcessStore);
			}

			return dashboardClassesProcessStore;
		}
	});

	function readDashboardsForComboStore(me) {
		var dashboardsRaw = me.getDashboards();
		var dashboards = [];
		for (var d in dashboardsRaw) {
			d = dashboardsRaw[d];
			if (d) {
				dashboards.push({
					id: d.getId(),
					name: d.getName(),
					description: d.getDescription()
				});
			}
		}

		return dashboards;
	}

	function getTableGroup (table) {
		//the simple table are discriminate by the tableType
		var type;
		if (table.tableType && table.tableType != "standard") {
			type = table.tableType;
		} else {
			type = table.type;
		}

		if (constants.cachedTableType[type]) {
			return type;
		} else {
			throw new Error("Unsupported node type: "+type);
		}
	};

	function addAttributesToDomain(rawDomain, domain) {
		var rawAttributes = rawDomain.attributes;
		var attributeLibrary = domain.getAttributeLibrary();
		for (var i=0, l=rawAttributes.length; i<l; ++i) {

			var rawAttribute = rawAttributes[i];
			try {
				var attr = CMDBuild.core.model.CMAttributeModel.buildFromJson(rawAttribute);
				attributeLibrary.add(attr);
			} catch (e) {
				_debug(e);
			}
		}
	}

	function getFakeStore() {
		return {
			cmFill: function() {},
			cmFake: true
		};
	}

	function reloadRelferenceStore(stores, idClass) {
		var anchestors = _CMUtils.getAncestorsId(idClass);
		Ext.Array.each(anchestors, function(id) {
			var store = stores[id];
			if (store) {
				store.load();
			}
		});
	}

	CMDBuild.Cache = new CMDBuild.cache.CMCache();
	_CMCache = CMDBuild.Cache; //to uniform the variable names, maybe a day I'll can delete CMDBuild.Cache
})();