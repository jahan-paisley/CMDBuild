(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMPingController", {
		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMPingDefinitionForm.WIDGET_NAME
		},

		// override
		setDefaultValues: function() {
			this.callParent(arguments);
			this.view.count.setValue(CMDBuild.view.administration.widget.form.CMPingDefinitionForm.DEFAULT_COUNT_VALUE);
		}
	});
})();