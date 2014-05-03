(function() {

	var dashboards = {},

		availableDataSources = {
			
		},

		availableDataSourcesStore = new Ext.data.SimpleStore({
			fields : ["name"],
			data : []
		}),

		events = {
			add: "cm-dashboard-added",
			remove: "cm-dashboard-removed",
			modify: "cm-dashboard-modify"
		};

	Ext.define("CMDBuild.cache.CMCacheDashboardFunctions", {
		statics: {
			DASHBOARD_EVENTS: events
		},

		DASHBOARD_EVENTS: events,

		/*
		 * dd is a map : {
		 * 	dashboardId: {dashboardDefinition},
		 * 	dashboardId: {dashboardDefinition},
		 *  ...
		 * } 
		 */
		addDashboards: function(dd) {
			for (var key in dd) {
				var definition = dd[key];
				definition.id = key;
				this.addDashboard(definition);
			}
		},

		addDashboard: function(d) {
			var model = CMDBuild.model.CMDashboard.build(d);
			if (model) {
				dashboards[d.id] = model;
				this.fireEvent(this.DASHBOARD_EVENTS.add, model);
			}
		},

		removeDashboardWithId: function(id) {
			var d = dashboards[id];
			if (d) {
				delete dashboards[id];
				this.fireEvent(this.DASHBOARD_EVENTS.remove, id);
			}
		},

		modifyDashboard: function(dashboard, id) {
			var d = dashboards[dashboard.id] || dashboards[id];
			if (d) {
				d.setName(dashboard.name);
				d.setDescription(dashboard.description);
				d.setGroups(dashboard.groups);
				this.fireEvent(this.DASHBOARD_EVENTS.modify, d);
			}
		},

		getDashboards: function() {
			return dashboards;
		},

		getDashboardById: function(id) {
			return dashboards[id] || null;
		},

		setAvailableDataSources: function(ds) {
			if (!ds) {
				ds = [];
			}

			var names = [];

			for (var i=0, l=ds.length, item; i<l; ++i) {
				item = ds[i];
				// fill a map to retrive quicly the in&output of
				// the datasources instead of store all the info
				// in the model of the store. This data could not
				// be changed from the UI
				availableDataSources[item.name] = {
					input: item.input || [],
					output: item.output || []
				};

				// to fill the combo
				names[i] = {
					name: item.name
				};
			}

			availableDataSourcesStore.loadData(names);
		},

		getAvailableDataSourcesStore: function() {
			return availableDataSourcesStore;
		},

		getDataSourceInput: function(dsName) {
			var ds = availableDataSources[dsName];
			if (ds) {
				return ds.input;
			} else {
				return [];
			}
		},

		getDataSourceOutput: function(dsName) {
			var ds = availableDataSources[dsName];
			if (ds) {
				return ds.output;
			} else {
				return [];
			}
		}
	});
})();