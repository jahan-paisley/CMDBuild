Ext.define("CMDBuild.view.administration.report.CMReportForm", {
	extend: "Ext.panel.Panel",

	mixins : {
		cmFormFunction: "CMDBUild.view.common.CMFormFunctions"
	},
	
	translation: CMDBuild.Translation.administration.modreport.importJRFormStep1,
	
	initComponent:function() {
	
		this.cmTBar = [
			this.modifyButton = new Ext.button.Button({
				iconCls : 'modify',
				text : CMDBuild.Translation.administration.modreport.modify
			}),

			this.deleteButton = new Ext.button.Button({
				iconCls : 'delete',
				text : CMDBuild.Translation.administration.modreport.remove
			})
		];

		this.cmButtons = [
			this.saveButton = new Ext.button.Button({
				text : CMDBuild.Translation.common.buttons.save,
				scope : this
			}),
	
			this.abortButton = new Ext.button.Button({
				text : CMDBuild.Translation.common.buttons.abort,
				scope : this
			})
		];

		var LABEL_WIDTH = 240;
		var FIELD_WIDTH = LABEL_WIDTH + 260;
		var STEP2_OFFSET = 100;
		
		this.step1 = new CMDBuild.view.administration.report.CMReportFormStep1({
			frame: true,
			border: false,
			flex: 1,
			fieldDefaults: {
				labelWidth: LABEL_WIDTH,
				width: FIELD_WIDTH
			}
		});

		this.step2 = new CMDBuild.view.administration.report.CMReportFormStep2({
			frame: false,
			border: false,
			flex: 1,
			fieldDefaults: {
				labelWidth: STEP2_OFFSET + LABEL_WIDTH,
				width: STEP2_OFFSET + FIELD_WIDTH
			}
		});

		Ext.apply(this, {
			frame: false,
			border: false,
 			cls: "x-panel-body-default-framed cmbordertop",
			bodyCls: 'cmgraypanel',
			tbar : this.cmTBar,
			layout: "card",
			activeItem:0,
			items : [ this.step1, this.step2],
			buttonAlign: "center",
			buttons : this.cmButtons
		});

		this.callParent(arguments);
		this.disableModify();
	},
	
	showStep1: function() {
		this.layout.setActiveItem(this.step1.id);
	},
	
	showStep2: function() {
		this.layout.setActiveItem(this.step2.id);
	},
	
	onReportTypeSelected: function() {},
	
	onReportSelected: function(report) {
		this.disableModify(enableCMTBar = true);
		this.step1.onReportSelected(report);
		this.step2.removeAll();
	},
	
	reset: function() {
		this.step1.reset();
		this.step2.reset();
		this.showStep1();
	}
});