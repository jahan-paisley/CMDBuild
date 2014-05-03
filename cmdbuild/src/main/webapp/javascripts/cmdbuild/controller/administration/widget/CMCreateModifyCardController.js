(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMCreateModifyCardController", {
		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMCreateModifyCardDefinitionForm.WIDGET_NAME
		},

		// override
		setDefaultValues: function() {
			this.callParent(arguments);
		}
	});
})();