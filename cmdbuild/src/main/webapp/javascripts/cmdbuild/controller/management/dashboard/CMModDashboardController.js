(function() {

	Ext.define("CMDBuild.controller.management.dashboard.CMModDashboardController", {

		extend: "CMDBuild.controller.CMBasePanelController",

		mixins: {
			columnController: "CMDBuild.controller.common.CMDashboardColumnController"
		},

		constructor: function() {
			this.callParent(arguments);
			if (this.view) {
				this.view.setDelegate(this);
			}

			this.dashboard = null;
		},

		onViewOnFront: function(selection) {
			if (selection && typeof selection.get == "function") {
				this.dashboard = _CMCache.getDashboardById(selection.get("id"));
				this.view.buildDashboardColumns(this.dashboard);
			}
		}
	});
})();