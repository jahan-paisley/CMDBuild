(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMNavigationTreeController", {
		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMNavigationTreeDefinitionForm.WIDGET_NAME
		},

		// override
		constructor: function() {
			this.callParent(arguments);
		},

		// override
		setDefaultValues: function() {
			this.callParent(arguments);
		}

	});
})();