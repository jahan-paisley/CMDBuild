(function() {

	Ext.define("CMDBuild.view.administration.widget.form.CMPingDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".Ping",
			DEFAULT_COUNT_VALUE: 3,
			COUNT_MIN: 1,
			COUNT_MAX: 10
		},

		initComponent: function() {
			this.callParent(arguments);
		},

		// override
		buildForm: function() {
			var tr = CMDBuild.Translation.administration.modClass.widgets;
			var me = this;

			this.callParent(arguments);

			this.templatesGrid = new CMDBuild.view.administration.common.CMDynamicKeyValueGrid({
				title: tr[me.self.WIDGET_NAME].fields.templates,
				margin: "0 0 0 3"
			});

			this.address = new Ext.form.field.Text({
				fieldLabel: tr[this.self.WIDGET_NAME].fields.address,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: "address"
			});

			this.count = new Ext.form.field.Number({
				fieldLabel: tr[this.self.WIDGET_NAME].fields.count,
				labelWidth: CMDBuild.LABEL_WIDTH,
				value: this.self.DEFAULT_COUNT_VALUE,
				maxValue: this.self.COUNT_MAX,
				minValue: this.self.COUNT_MIN
			});

			// defaultFields is inherited
			this.defaultFields.add(this.address, this.count);

			Ext.apply(this, {
				layout: {
					type: "hbox"
				},
				items: [this.defaultFields, this.templatesGrid]
			});
		},

		fillPresetWithData: function(data) {
			this.templatesGrid.fillWithData(data);
		},

		// override
		fillWithModel: function(model) {
			this.callParent(arguments);
			this.address.setValue(model.get("address"));
			this.count.setValue(model.get("count"));
			this.fillPresetWithData(model.get("templates"));
		},

		// override
		disableNonFieldElements: function() {
			this.templatesGrid.disable();
		},

		// override
		enableNonFieldElements: function() {
			this.templatesGrid.enable();
		},

		// override
		getWidgetDefinition: function() {
			var me = this;

			return Ext.apply(me.callParent(arguments), {
				address: me.address.getValue(),
				count: me.count.getValue(),
				templates: me.templatesGrid.getData()
			});
		}
	});
})();