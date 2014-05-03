(function() {

	var classes = {},
		processes = {},
		classMapIdAndName = {},
		superclassesStore = getFakeStore(),
		superProcessStore = getFakeStore(),
		classStore = getFakeStore(),
		classesAndProcessesStore = getFakeStore();

	
	Ext.define("CMDBUild.cache.CMCacheClassFunctions", {

		getClasses: function() {
			return classes;
		},

		getProcesses: function() {
			return processes;
		},

		getEntryTypes: function() {
			var tmp = Ext.apply({}, classes);
			return Ext.apply(tmp, processes);
		},

		addClasses: function(etypes) {
			for (var i=0, l=etypes.length; i<l; ++i) {
				var et = etypes[i];
				if (et.type == "class") {
					this.addClass(etypes[i]);
				} else if(et.type == "processclass") {
					this.addProcess(etypes[i]);
				}
			}

			callCmFillForStores();
		},

		addClass: function(et) {
			var newEt = Ext.create("CMDBuild.cache.CMEntryTypeModel", et);
			classes[et.id] = newEt;
			
			return newEt;
		},

		addProcess: function(et) {
			var newEt = Ext.create("CMDBuild.cache.CMEntryTypeModel", et);
			processes[et.id] = newEt;
			
			return newEt;
		},

		addWidgetToEntryTypes: function(data, onlyActive) {
			var entryTypes = this.getEntryTypes();
			for (var id in entryTypes) {
				var et = entryTypes[id];
				var widgets = data[et.get("name")];

				if (widgets) {
					if (onlyActive) {
						var toAdd = [];
						for (var i=0, l=widgets.length; i<l; ++i) {
							var w = widgets[i];
							if (w.active) {
								toAdd.push(w);
							}
						}
						et.setWidgets(toAdd);
					} else {
						et.setWidgets(widgets);
					}
				}
			}
		},

		getClassById: function(id) {
			return classes[id];
		},

		getProcessById: function(id) {
			return processes[id];
		},

		getEntryTypeById: function(id) {
			var c = this.getClassById(id),
				p = this.getProcessById(id);
			
			if (c) {
				return c;
			} else if (p) {
				return p;
			} else {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
						Ext.String.format(CMDBuild.Translation.errors.reasons.CLASS_NOTFOUND, id));
			}
		},

		isEntryTypeById: function(id) {
			var c = this.getClassById(id),
				p = this.getProcessById(id);
			
			return (c || p);
		},

		isEntryTypeByName: function(name) {
			var entryTypes = this.getEntryTypes();
			for (var id in entryTypes) {
				var e = entryTypes[id];
				if (name == e.get("name")) {
					return true;
				}
			}
			return false;
		},

		getEntryTypeByName: function(name) {
			var entryTypes = this.getEntryTypes();
			for (var id in entryTypes) {
				var e = entryTypes[id];
				if (name == e.get("name")) {
					return e;
				}
			}

			CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
					Ext.String.format(CMDBuild.Translation.errors.reasons.CLASS_NOTFOUND, name));

			return null;
		},

		getEntryTypeNameById: function(id) {
			if (typeof classMapIdAndName[id] == "undefined") {
				var et = this.getEntryTypeById(id);
				if (et) {
					classMapIdAndName[id] = et.get("name");
				} else {
					classMapIdAndName[id] = "";
				}
			}

			return classMapIdAndName[id];
		},

		getSuperclassesAsStore: function() {
			if (superclassesStore.cmFake) {
				superclassesStore = buildSuperclassesStore();
				superclassesStore.cmFill();
			}

			return superclassesStore;
		},
		
		getSuperProcessAsStore: function() {
			if (superProcessStore.cmFake) {
				superProcessStore = buildSuperProcessStore();
				superProcessStore.cmFill();
			}

			return superProcessStore;
		},
		
		getClassesStore: function() {
			if (classStore.cmFake) {
				classStore = buildClassesStore();
				classStore.cmFill();
			}

			return classStore;
		},

		getClassesAndProcessesStore: function() {
			if (classesAndProcessesStore.cmFake) {
				classesAndProcessesStore = buildClassesAndProcessesStore();
				classesAndProcessesStore.cmFill();
			}

			return classesAndProcessesStore;
		},

		getClassRootId: function() {
			return getTableIdFromSetByName(classes, "Class");
		},

		getActivityRootId: function() {
			return getTableIdFromSetByName(processes, "Activity");
		},

		onClassSaved: function(_class) {
			var c = this.addClass(_class);
			callCmFillForStores();
			this.fireEvent("cm_class_saved", c);

			return c;
		},
		
		onProcessSaved: function(process) {
			var p = this.addProcess(process);
			callCmFillForStores();
			this.fireEvent("cm_process_saved", p);

			return p;
		},

		onClassDeleted: function(idClass) {
			classes[idClass] = undefined;
			delete classes[idClass];
			callCmFillForStores();
			
			this.fireEvent("cm_class_deleted", idClass);
		},

		onProcessDeleted: function(idClass) {
			processes[idClass] = undefined;
			delete processes[idClass];
			callCmFillForStores();
			
			this.fireEvent("cm_process_deleted", idClass);
		},

		onWidgetSaved: function(idClass, widget) {
			var et = this.getEntryTypeById(idClass);
			et.removeWidgetById(widget.id);
			et.addWidget(widget);
		},

		onWidgetDeleted: function(idClass, id) {
			var et = this.getEntryTypeById(idClass);
			et.removeWidgetById(id);
		}
	});

	function buildSuperclassesStore() {
		var store = new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in classes) {
					var table = classes[i];
					if (table.data.superclass) {
						data.push({
							id: table.data.id,
							description: table.data.text
						});
					}
				}
				this.add(data);
				this.sort('description', 'ASC');
			},
			sorters: [{
				property : 'description',
				direction : 'ASC'
			}]
		});
		return store;
	}

	function buildClassesStore() {
		return new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in classes) {
					var table = classes[i];
					if (table.data.name != "Class") {
						data.push({
							id: table.data.id,
							description: table.data.text,
							name: table.getName()
						});
					}
				}
				this.add(data);
				this.sort('description', 'ASC');
			},
			sorters: [{
				property : 'description',
				direction : 'ASC'
			}]
		});
	}
	
	function buildClassesAndProcessesStore() {
		return new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				
				function addToData(t) {
					if (t.data.tableType != "simpletable"
						&& t.data.name != "Class" 
						&& t.data.name != "Activity") {

						data.push({
							id: t.data.id,
							name: t.data.name,
							description: t.data.text
						});
					}
				}

				for (var i in classes) {
					addToData(classes[i]);
				}
				for (var i in processes) {
					addToData(processes[i]);
				}

				this.add(data);
				this.sort('description', 'ASC');
			},
			sorters: [{
				property : 'description',
				direction : 'ASC'
			}]
		});
	}
	
	function buildSuperProcessStore() {
		return new Ext.data.Store({
			model: "CMTableForComboModel",
			cmFill: function() {
				this.removeAll();
				var data = [];
				for (var i in processes) {
					var table = processes[i];
					if (table.data.superclass) {
						data.push({
							id: table.data.id,
							description: table.data.text
						});
					}
				}
				this.add(data);
				this.sort('description', 'ASC');
			},
			sorters: [{
				property : 'description',
				direction : 'ASC'
			}]
		});
	}
	
	// returns a null object (pattern) to avoid checks on onClassSaved
	function getFakeStore() {
		return {
			cmFill: function() {},
			cmFake: true
		};
	}
	
	function buildGeoAttributesStoreForClass(classId) {
		try {
			var et = _CMCache.getEntryTypeById(classId);

			return new Ext.data.Store({
				model: "GISLayerModel",
				autoLoad : false,
				data: et.getMyGeoAttrs(),
				sorters : [ {
					property : 'index',
					direction : "ASC"
				}],
				cmFill: function() {
					this.removeAll();
					this.add(et.getMyGeoAttrs());
					this.sort('description', 'ASC');
				}
			});

		} catch (e) {
			_debug("I can not build a geoAttribute store for classId with id " + classId);
		}
	}
	
	function getTableIdFromSetByName(set, name) {
		for (var t in set) {
			t = set[t]; 
			if (t.get("name") == name) {
				return t.get("id");
			}
		}
	}
	
	function callCmFillForStores() {
		classStore.cmFill();
		superProcessStore.cmFill();
		superclassesStore.cmFill();
		classesAndProcessesStore.cmFill();
	}
})();