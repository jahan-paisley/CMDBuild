(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController", {
		extend: "Ext.Base",

		mixins: {
			observable: "Ext.util.Observable"
		},

		constructor: function(config) {
			this.callParent(arguments);

			Ext.apply(this, config);

			if (typeof this.self.WIDGET_NAME == "undefined") {
				throw "You have to set the static WIDGET_NAME property to the controller implementation";
			} else {
				this.WIDGET_NAME = this.self.WIDGET_NAME;
			}

			if (typeof this.view == "undefined") {
				throw "You have to pass a view when create an instance of " + this.$className;
			}
		},

		fillFormWithModel: function(model) {
			if (typeof model == "object" 
				&& model.$className == "CMDBuild.model.CMWidgetDefinitionModel") {
			
				this.view.fillWithModel(model);
			}
		},

		setDefaultValues: function() {
			this.view.active.setValue(true);
		},

		afterEnableEditing: Ext.emptyFn,
		disableNonFieldElements: Ext.emptyFn,
		enableNonFieldElements: Ext.emptyFn
	});
})();