(function() {

	Ext.define("CMDBuild.view.common.report.CMSingleReportPage", {
		extend: 'Ext.panel.Panel',
	    layout: {
	        type: 'hbox'
	    },
	    width: '100%',
	    border: false,
	    frame: true,
	    dockedItems: [{
	        xtype: 'toolbar',
	        dock: 'bottom',
	        ui: 'footer',
	        bodyCls: "x-panel-body-default-framed cmbordertop",
	        bodyStyle: {
	        	padding: "5px 5px 0 5px"
	        },
	        cls: "x-panel-body-default-framed",
		        layout: {
	            pack: 'center'
	        },	    	
	    	items: [
	            { 	xtype: 'button', 
	            	width: '80px', 
	            	text:  CMDBuild.Translation.common.buttons.save, 
	            	handler: function() {
	            	/*TODO*/
//	        	        var xhr = new XMLHttpRequest(); 
//	        	        var cmUrl = 'services/json/management/modreport/printreportfactory';
//	        	        xhr.open('GET', cmUrl, true); 
//	        	        xhr.responseType = "blob";
//	        	        xhr.onreadystatechange = function () { 
//	        	            if (xhr.readyState == 4) {
//	        	                var a = 4;//xhr.response);
//	        	            }
//	        	        };
//	        	        xhr.send(null);
	            	} 
	            }
	        ]
	    }],	    
		autoScroll: false,
		buttonAlign: 'center',
		reportId: undefined,
		controller: undefined,
	
		setReportId: function(reportId) {
			this.reportId = reportId;
		},
		setController: function(controller) {
			this.controller = controller;
		},
	
		onReportTypeSelected : function(report) {
		},
	
		requestReport: function(reportParams) {
			Ext.Ajax.request({
				url: 'services/json/management/modreport/createreportfactory',
				params: reportParams,
				success: function(response) {
					var ret = Ext.JSON.decode(response.responseText);
					if(ret.filled) { // report with no parameters
						var popup = window.open("services/json/management/modreport/printreportfactory", "Report", "height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable");
						if (!popup) {
							CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
						}
					}
					else { // show form with launch parameters
						var paramWin = new CMDBuild.Management.ReportParamWin({
							attributeList: ret.attribute
						});
						paramWin.show();
					}
				},
				scope: this
			});
		}
	
	});
})();


