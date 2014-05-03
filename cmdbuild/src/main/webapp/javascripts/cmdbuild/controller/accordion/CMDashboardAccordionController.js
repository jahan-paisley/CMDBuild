(function() {
	Ext.define("CMDBuild.controller.accordion.CMDashboardAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",

		mixins: {
			viewDelegate: "CMDBuild.view.administration.accordion.CMDashboardAccordionDelegate"
		},

		constructor: function(accordion) {
			this.callParent(arguments);

			this.accordion.setDelegate(this);

			var events = CMDBuild.cache.CMCacheDashboardFunctions.DASHBOARD_EVENTS;

			_CMCache.on(events.add, updateStoreToSelectNode, this);
			_CMCache.on(events.modify, updateStoreToSelectNode, this);
			_CMCache.on(events.remove, updateStore, this);
		},

		// view delegate
		onChartDropped: function(chartId, dashboardId) {
			var sm = this.accordion.getSelectionModel();
			var currentSelection = sm.getSelection();
			if (currentSelection 
					&& Ext.isArray(currentSelection)
					&& currentSelection.length > 0) {

				currentSelection = currentSelection[0];
			}

			if (currentSelection 
					&& typeof currentSelection.getId == "function"
					&& currentSelection.getId() != dashboardId) {

				var me = this;
				CMDBuild.ServiceProxy.Dashboard.chart.move(currentSelection.getId(), dashboardId, chartId, function callback() {
					me.accordion.deselect();
					me.accordion.selectNodeById(currentSelection.getId());
				});
			}
		}
	});

	function updateStoreToSelectNode(dashboard) {
		this.updateStoreToSelectNodeWithId(dashboard.getId());
	}

	function updateStore() {
		this.accordion.updateStore();
	}

})();