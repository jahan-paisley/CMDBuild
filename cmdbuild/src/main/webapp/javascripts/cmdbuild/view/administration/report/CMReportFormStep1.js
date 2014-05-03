(function() {

	var tr = CMDBuild.Translation.administration.modreport.importJRFormStep1;

	Ext.define("CMDBuild.view.administration.report.CMReportFormStep1", {
		extend : "Ext.form.Panel",

		mixins : {
			cmFormFunction: "CMDBUild.view.common.CMFormFunctions"
		},

		encoding : 'multipart/form-data',
		fileUpload : true,
		defaultType : 'textfield',
		plugins : [ new CMDBuild.CallbackPlugin() ],
		autoScroll: true,

		initComponent : function() {

			this.fileField = new Ext.form.field.File({
				fieldLabel : tr.master_report_jrxml,
				allowBlank : false,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : 'jrxml'
			});

			this.name = new Ext.form.field.Text({
				fieldLabel : tr.name,
				allowBlank : false,
				name : 'name',
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				cmImmutable: true
			});

			this.description = new Ext.form.CMTranslatableTextArea({
				fieldLabel : tr.description,
				allowBlank : false,
				name : 'description',
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				translationsKeyType: "Report", 
				translationsKeyField: "Description",
				maxLength : 100,
			});

			this.items = [
				this.name,
				this.description
			];

			this.callParent(arguments);

			this.disableFields();

			this.on("render", function() {

				this.groups = new CMDBuild.view.common.field.CMGroupSelectionList({
					height: 300,
					valueField : 'name',
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					considerAsFieldToDisable: true
				});

				this.add(this.groups);
				this.add(this.fileField);

			}, this, {single: true});
		},

		onReportSelected: function(report) {
			this.reset();
			this.name.setValue(report.get("title"));
			this.description.setValue(report.get("description"));
			setValueToMultiselect(this.groups, report.get("groups"));
			this.description.translationsKeyName = report.get("title");
		}
	});

	function setValueToMultiselect(m, stringValue) {
		var v = stringValue.split(",");
		m.setValue(v);
	}
})();