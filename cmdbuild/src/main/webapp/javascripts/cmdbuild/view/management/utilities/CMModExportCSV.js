Ext.define("CMDBuild.view.management.utilities.CMModExportCSV", {
	extend: "Ext.panel.Panel",
	cmName: 'exportcsv',
	layout: 'border',
	hideMode:  'offsets',
	frame: true,
	border: false,
	translation: CMDBuild.Translation.management.modutilities.csv,

	initComponent: function() {

		this.exportBtn = new CMDBuild.buttons.ExportButton({
			scope: this,
			formBind: true,
			handler: function(){
				this.form.getForm().submit();
			}
		});

		this.classList = new CMDBuild.field.CMBaseCombo({
			store: _CMCache.getClassesStore(),
			fieldLabel : this.translation.selectaclass,
			queryMode: 'local',
			name : _CMProxy.parameter.CLASS_NAME,
			hiddenName : _CMProxy.parameter.CLASS_NAME,
			valueField : 'name',
			displayField : 'description',
			allowBlank : false,
			editable: false
		});

		this.separator = new Ext.form.ComboBox({ 
			name: 'separator',
			fieldLabel: this.translation.separator,
			valueField: 'value',
			displayField: 'value',
			hiddenName: 'separator',
			store: new Ext.data.SimpleStore({
				fields: ['value'],
				data : [[';'],[','],['|']]
			}),
			width: 150,
			value: ";",
			queryMode: 'local',
			editable: false,
			allowBlank: false
		});

		this.form = new Ext.form.Panel({
			region: 'center',
			hideMode:  'offsets',
			frame: true,
			border: true,
			monitorValid: true,
			labelWidth: 200,
			method: 'POST',
			url : 'services/json/management/exportcsv/export',
			standardSubmit:true, // IE Fix (see exportBtn for more)
			items:	[
				this.classList,
				this.separator,
				this.exportBtn
			]
		});

		this.form.on('clientvalidation', function(form, valid){
			this.exportBtn.setDisabled(!valid)
		}, this);

		Ext.apply(this, {
			title: CMDBuild.Translation.management.modutilities.csv.title_export,
			items:[this.form]
		});

		this.callParent(arguments);
	}
});