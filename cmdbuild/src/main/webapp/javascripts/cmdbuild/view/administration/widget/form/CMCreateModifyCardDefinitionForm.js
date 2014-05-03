(function() {

	Ext.define("CMDBuild.view.administration.widget.form.CMCreateModifyCardDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".CreateModifyCard"
		},

		initComponent: function() {
			this.callParent(arguments);
		},

		// override
		buildForm: function() {
			var me = this,
				tr = CMDBuild.Translation.administration.modClass.widgets[this.self.WIDGET_NAME];

			this.callParent(arguments);

			this.targetClass = new CMDBuild.field.ErasableCombo({
				fieldLabel : tr.fields.target,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'targetClass',
				valueField : 'name',
				displayField : 'description',
				editable : false,
				store : _CMCache.getClassesAndProcessesStore(),
				queryMode : 'local'
			});

			this.cqlText = new Ext.form.field.Text({
				fieldLabel : tr.fields.cql,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : 'idcardcqlselector'
			});

			this.readOnlyCheck = new Ext.form.field.Checkbox({
				name: "readonly",
				fieldLabel: tr.fields.readOnly,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			// defaultFields is inherited
			this.defaultFields.add(this.readOnlyCheck, this.targetClass, this.cqlText);
		},

		// override
		fillWithModel: function(model) {
			this.callParent(arguments);

			this.targetClass.setValue(model.get("targetClass"));
			this.cqlText.setValue(model.get("idcardcqlselector"));
			this.readOnlyCheck.setValue(model.get("readonly"));
		},

		// override
		getWidgetDefinition: function() {
			var me = this;

			return Ext.apply(me.callParent(arguments), {
				targetClass: me.targetClass.getValue(),
				idcardcqlselector: me.cqlText.getValue(),
				readonly: me.readOnlyCheck.getValue()
			});
		}
	});
})();