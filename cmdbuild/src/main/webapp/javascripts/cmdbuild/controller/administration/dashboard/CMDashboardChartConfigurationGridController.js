(function() {

	function pkg(s) {
		return "CMDBuild.controller.administration.dashboard."+s;
	}

	Ext.define(pkg("CMDashboardChartConfigurationGridControllerDelegate"), {
		chartWasSelected: Ext.emptyFn
	});

	Ext.define(pkg("CMDashboardChartConfigurationGridController"), {

		statics: {
			cmcreate: function(view) {
				return new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationGridController(view, view.getStore())
			}
		},

		mixins: {
			viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationGridDelegate"
		},

		constructor : function(view, store) {
			this.callParent(arguments);
			this.view = view;
			this.store = store;

			this.view.setDelegate(this);
		},

		clearSelection: function() {
			this.view.clearSelection();
		},

		initComponent : function() {
			this.callParent(arguments);
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, pkg("CMDashboardChartConfigurationGridControllerDelegate"));
			this.delegate = d;
		},

		loadCharts: function(charts, idToSelect) {
			this.store.loadData(charts);
			if (idToSelect) {
				var me = this;
				this.store.each(function(r) {
					if (r.getId() == idToSelect) {
						me.view.selectRecord(r);
						return false;
					}
				})
			}
		},

		onChartSelect: function(chart) {
			this.delegate.chartWasSelected(chart);
		}
	});

})();
