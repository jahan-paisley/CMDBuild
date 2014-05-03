Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardLayoutPanelController", {

	alias: ["controller.cmdashboardlayoutconf"],

	mixins: {
		columnController: "CMDBuild.controller.common.CMDashboardColumnController", // the order is important
		viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardLayoutPanelDelegate"
	},

	constructor: function(view) {
		this.view = view;
		this.view.setDelegate(this);
		this.dashboard = null;
		this.proxy = CMDBuild.ServiceProxy.Dashboard;
	},

	// called by the super-controller

	dashboardWasSelected: function(dashboard) {
		var me = this;

		me.dashboard = dashboard;
		me.view.enable();

		if (me.view.isTheActiveTab()) {
			me.view.configureForDashboard(me.dashboard);
		} else {
			me.view.mon(me.view, "activate", function() {
				me.view.configureForDashboard(me.dashboard);
			}, me, {
				single: true
			});
		}
	},

	prepareForAdd: function() {
		this.view.clearAll();
		this.view.disable();
	},

	// view delegate

	onAddColumnClick: function() {
		var actualColumnCount = this.view.countColumns();
		var factor = 1/(actualColumnCount + 1);

		this.view.addColumn({
			charts: [],
			width: factor
		});
	},

	onRemoveColumnClick: function() {
		this.view.removeEmptyColumns();
	},

	onColumnWidthSliderChange: function() {
		this.view.syncColumnWidthToSliderThumbs();
	},

	onSaveButtonClick: function() {
		this.proxy.modifyColumns(this.dashboard.getId(), this.view.getColumnsConfiguration());
	},

	onAbortButtonClick: function() {
		this.dashboardWasSelected(this.dashboard);
	}
});