(function() {

Ext.define("CMDBuild.view.common.report.CMReportGrid", {
	extend: "Ext.grid.Panel",

	translation : CMDBuild.Translation.management.modreport.reportForm,
	filtering : false,
	reportType : '',
	exportMode: false,

	layout : {
		type : 'fit',
		reserveScrollbar : true // There will be a gap even when there's no scrollbar
	},

	initComponent : function() {
		this.columns = [{
			header : "Id",
			dataIndex : "id",
			hidden:true
		},{
			header : "Query",
			dataIndex : "query",
			hidden:true
		},{
			header : this.translation.name,
			sortable : true,
			dataIndex : "title",
			flex: 1
		},{
			header : this.translation.description,
			sortable : true,
			dataIndex : "description",
			flex: 1
		},{
			header : this.translation.report,
			sortable : false,
			dataIndex : "type",
			width: this.exportMode ? 60 : 110,
			fixed: true,
			tdCls: "grid-button",
			renderer: Ext.Function.bind(loadReportIcons, this),
			menuDisabled: true,
			hideable: false
		}];

		this.store = _CMCache.getReportGridStore();

		this.pagingBar = new Ext.toolbar.Paging({
			store: this.store,
			displayInfo: true,
			displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
			emptyMsg: CMDBuild.Translation.common.display_topic_none
		});

		this.bbar = this.pagingBar;

		this.callParent(arguments);

		this.on('beforeitemclick', cellclickHandler);
	},

	onReportTypeSelected : function(report) {
		this.load();
	},

	load: function() {
		this.getStore().load();
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
	},

	clearSelections: function() {
		this.getSelectionModel().clearSelections();
	},

	// Block the report panel to front if selected from the navigation menu
	beforeBringToFront : function(selection) {
		if (selection) {
			var r = selection.raw || selection.data;
			return !(r && r.objid);
		}
	}

});

function loadReportIcons(reportType,x,store) {
	if(reportType=='CUSTOM') {
		var html ='<div class="cmcenter">';
		if (this.exportMode) {
			html += '<img qtip="Sql" style="cursor:pointer" class="sql" src="images/icons/ico_sql.png"/>&nbsp;&nbsp;';
			html += '<img qtip="Zip" style="cursor:pointer" class="zip" src="images/icons/ico_zip.png"/>&nbsp;&nbsp;';
		} else {
			html += '<img qtip="Adobe Pdf" style="cursor:pointer" class="pdf" src="images/icons/ico_pdf.png"/>&nbsp;&nbsp;';
			html += '<img qtip="OpenOffice Odt" style="cursor:pointer" class="odt" src="images/icons/ico_odt.png"/>&nbsp;&nbsp;';
			html += '<img qtip="Rich Text Format" style="cursor:pointer" class="rtf" src="images/icons/ico_rtf.png"/>&nbsp;&nbsp;';
			html += '<img qtip="Csv" style="cursor:pointer" class="csv" src="images/icons/ico_csv.png"/>&nbsp;&nbsp;';
		};
		html += "</div>;"
		return html;
	} else {
		//openoffice
	}
}

function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
	var reportExtension = event.target.className;
	var selectedRow = grid.getStore().getAt(rowIndex).json;
	if (reportExtension == 'pdf'
		|| reportExtension == 'csv'
			|| reportExtension == 'odt'
				|| reportExtension == 'zip'
					|| reportExtension == 'rtf') {

		this.requestReport({
				id: model.get("id"),
				type: model.get("type"),
				extension: reportExtension
			});

	} else if (reportExtension == 'sql') {
		var win = new CMDBuild.PopupWindow({
			title: 'Sql',
			items: [{
				xtype: 'panel',
				autoScroll: true,
				html: '<pre style="padding:5px; font-size: 1.2em">' + model.get("query") + '</pre>'
			}],
			buttons: [{
				text: CMDBuild.Translation.common.buttons.close,
				handler: function() {
					win.destroy();
				}
			}]
		}).show();
	}
}
})();