(function() {
	var buttonTr = CMDBuild.Translation.common.buttons;

	Ext.define("CMDBuild.view.administration.configuration.CMBaseModConfiguration", {
		extend: "Ext.form.Panel",

		constructor: function() {

			this.buildButtons();
			this.frame = true;
			this.overflowY = "auto";
			this.fieldDefaults = {
				labelAlign: 'left',
				labelWidth: CMDBuild.CFG_LABEL_WIDTH,
				width: CMDBuild.CFG_MEDIUM_FIELD_WIDTH
			};

			this.callParent(arguments);
		},

		getValues: function() {
			return this.getForm().getValues();
		},

		populateForm: function(configurationOptions) {
			this.valuesFromServer = configurationOptions.data;
			this.getForm().setValues(this.valuesFromServer);
		},

		buildButtons: function() {
			this.saveButton = new Ext.Button({
				text: buttonTr.save
			});

			this.abortButton = new Ext.Button({
				text: buttonTr.abort
			});

			this.buttonAlign = "center";
			this.buttons = [this.saveButton, this.abortButton];
		},

		/**
		 * Template method called in the
		 * callbak function of the form submit
		 **/
		afterSubmit: function() {
			_debug("before submit of the templateModSetuo");
		}
	});

})();