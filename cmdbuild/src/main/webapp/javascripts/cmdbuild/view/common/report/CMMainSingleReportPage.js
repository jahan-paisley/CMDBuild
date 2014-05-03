(function() {

	Ext.define("CMDBuild.view.common.report.CMMainSingleReportPage", {
		extend: 'Ext.container.Container',
		title: 'Report',
	    layout: {
	        type: 'vbox'
	    },
	    width: '100%',
	    height: '100%',
		initComponent : function() {
			this.buttonsView = new CMDBuild.view.common.report.CMSingleReportPage({
				mainWindow: this
			});
			this.showView = new CMDBuild.view.common.report.CMSingleReportFrame({
				mainWindow: this
			});
			this.items = [this.showView];//, this.buttonsView];/*TODO*/
			this.callParent(arguments);
		},
		requestReport: function(params) {
			this.showView.requestReport(params);
		},
		setReportId: function(id) {
			this.showView.setReportId(id);
		}
	});

})();