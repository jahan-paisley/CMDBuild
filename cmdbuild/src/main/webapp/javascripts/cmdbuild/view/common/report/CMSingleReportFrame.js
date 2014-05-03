(function() {

	Ext.define("CMDBuild.view.common.report.CMSingleReportFrame", {
		extend: 'Ext.panel.Panel',
		title: 'Report',
	    flex: 1,
	    width: '100%',
	    height: '100%',
	    border: true,
	    frame: true,
	    showReport: function() {
	    	this.update('<iframe frameborder="0" style="width:100%; height:100%;" src="services/json/management/modreport/printreportfactory"></iframe>');
	    },
		initComponent : function() {
			var me = this;
			this.requestReport = function(reportParams) {
				Ext.Ajax.request({
					url: 'services/json/management/modreport/createreportfactory',
					params: reportParams,
					success: function(response) {
						var ret = Ext.JSON.decode(response.responseText);
						if(ret.filled) { // report with no parameters
							me.showReport();
						}
						else { // show form with launch parameters
							var paramWin = new CMDBuild.Management.ReportParamWin({
								attributeList: ret.attribute, 
								windowFrame: me
							});
							paramWin.show();
						}
					},
					scope: this
				});
			};
			this.tbar = createTBar(this);
			this.callParent(arguments);
		},
		setReportId: function(reportId) {
			this.reportId = reportId;
		}
	});
})();

function createTBar(me) {
	var pdfButton = new Ext.button.Button( {
    	iconCls : "pdf",
        text: CMDBuild.Translation.format_pdf,
        textAlign: 'left',
        width: '120px',
        border: false,
        handler: function() {
        	me.requestReport({
				id: me.reportId,
				type: "CUSTOM",
				extension: "pdf"
			});        
        }
	});
	var odtButton = new Ext.button.Button( {
    	iconCls : "odt",
        xtype: 'button',
        textAlign: 'left',
        text: CMDBuild.Translation.format_odt,
        width: '120px',
        handler: function() {
        	me.requestReport({
				id: me.reportId,
				type: "CUSTOM",
				extension: "odt"
			});        
        }
	});
	var rtfButton = new Ext.button.Button( {
    	iconCls : "rtf",
        xtype: 'button',
        textAlign: 'left',
        text: CMDBuild.Translation.format_rtf,
        width: '120px',
        handler: function() {
        	me.requestReport({
				id: me.reportId,
				type: "CUSTOM",
				extension: "rtf"
			});        
        }
	});
	var csvButton = new Ext.button.Button( {
    	iconCls : "csv",
        xtype: 'button',
        textAlign: 'left',
        text: CMDBuild.Translation.format_csv,
        width: '120px',
        handler: function() {
        	me.requestReport({
				id: me.reportId,
				type: "CUSTOM",
				extension: "csv"
			});        
        }
	});
	return [pdfButton, odtButton, rtfButton, csvButton];
}