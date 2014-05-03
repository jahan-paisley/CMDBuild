(function() {

	Ext.define("CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm", {
		extend: "Ext.panel.Panel",
		isWidgetDefinition: true,

		statics: {
			WIDGET_NAME: undefined
		},

		initComponent: function() {
			this.buildForm();

			this.callParent(arguments);

			if (this.self.WIDGET_NAME) {
				this.WIDGET_NAME = this.self.WIDGET_NAME;
			} else {
				throw "You must define a WIDGET_NAME in the CMBaseWidgetDefinitionForm subclass";
			}
		},

		// template method, must be implemented in subclasses
		buildForm: function() {
			var tr = CMDBuild.Translation.administration.modClass.widgets;

			this.buttonLabel = new Ext.form.CMTranslatableText({
				name: "label",
				fieldLabel: tr.commonFields.buttonLabel,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				labelWidth: CMDBuild.LABEL_WIDTH,
				translationsKeyType: "Widget", 
				translationsKeyField: "ButtonLabel",
				itemId: "ButtonLabel"
			});

			this.active = new Ext.form.field.Checkbox({
				name: "active",
				fieldLabel: tr.commonFields.active,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.alwaysenabled = new Ext.form.field.Checkbox({
				name: "alwaysenabled",
				fieldLabel: tr.commonFields.alwaysenabled,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.defaultFields = new Ext.panel.Panel({
				frame: true,
				border: true,
				items: [this.buttonLabel, this.active, this.alwaysenabled],
				flex: 1
			});

			Ext.apply(this, {
				items: [this.defaultFields]
			});
		},

		getWidgetDefinition: function() {
			throw "you must implement getWidgetDefinition";
		},

		fillWithModel: function(model) {
			this.buttonLabel.setValue(model.get("label"));
			this.active.setValue(model.get("active"));
			this.alwaysenabled.setValue(model.get("alwaysenabled"));
		},

		getWidgetDefinition: function() {
			var me = this;
			return {
				type: me.self.WIDGET_NAME,
				label: me.buttonLabel.getValue(),
				active: me.active.getValue(),
				alwaysenabled: me.alwaysenabled.getValue()
			};
		},

		disableNonFieldElements: Ext.emptyFn,
		enableNonFieldElements: Ext.emptyFn
	});

})();