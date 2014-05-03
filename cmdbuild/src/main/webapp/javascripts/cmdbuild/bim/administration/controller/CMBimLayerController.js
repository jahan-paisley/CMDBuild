Ext.define("CMDBuild.controller.administration.filter.CMBimLayerController", {
	extend: "CMDBuild.controller.CMBasePanelController",

	constructor: function(view) {
		this.callParent(arguments);

		this.view.delegate = this;
	},

	onViewOnFront: function(group) {
		this.view.load();
	},

	/* CMBimLayersDelegate */

	onDataColumnChange: function(grid, record, dataIndex, value) {
		var me = this;
		var className = record.get("className");

		CMDBuild.LoadMask.instance.show();
		CMDBuild.bim.proxy.saveLayer({
			params: {
				className: className,
				attribute: dataIndex,
				value: value
			},

			success: function() {
				me.view.load();
			},

			failure: function() {
				me.view.load();
			},

			callback: function() {
				CMDBuild.LoadMask.instance.hide();
			}

		});
	}
});