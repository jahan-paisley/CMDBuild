(function() {

	var reports = {};
	var gridStore = null;
	var comboStore = null;

	Ext.define("CMDBUild.cache.CMCacheReportFunctions", {
		addReports: function(rr) {
			for (var i=0, l=rr.length; i<l; ++i) {
				this.addReport(rr[i]);
			}
		},

		addReport: function(r) {
			var report = Ext.create("CMDBuild.cache.CMReportModel", r);
			reports[r.id] = report;

			return report;
		},

		reloadReportStores: function() {
			if (comboStore) {
				comboStore.load();
			}
		},

		getReports: function() {
			return reports;
		},

		getReportById: function(id) {
			return reports[id] || null;
		},

		getReportGridStore: function() {
			if (gridStore == null) {
				gridStore = new Ext.data.Store({
					model: "CMDBuild.cache.CMReporModelForGrid",
					pageSize: getPageSize(),
					proxy: {
						type: "ajax",
						url: 'services/json/management/modreport/getreportsbytype',
						reader: {
							type: "json",
							root: "rows",
							totalProperty: 'results'
						},
						extraParams: {
							type: "custom"
						}
					},
					autoLoad: false
				});
			}

			return gridStore;
		},

		getReportComboStore: function() {
			if (comboStore == null) {
				comboStore = new Ext.data.Store({
					model: "CMDBuild.model.CMReportAsComboItem",
					proxy: {
						type: "ajax",
						url: 'services/json/management/modreport/getreportsbytype',
						reader: {
							type: "json",
							root: "rows",
							totalProperty: 'results'
						},
						extraParams: {
							type: "custom", 
							limit: 1000
						}
					},
					autoLoad: true
				})
			}

			return comboStore;
		}
	});

	function getPageSize() {
		var pageSize;
		try {
			pageSize = parseInt(CMDBuild.Config.cmdbuild.rowlimit);
		} catch (e) {
			pageSize = 20;
		}

		return pageSize;
	}
})();