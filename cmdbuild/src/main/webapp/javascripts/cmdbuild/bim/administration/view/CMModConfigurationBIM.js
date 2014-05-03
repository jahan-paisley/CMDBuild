(function() {

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationBIM", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: CMDBuild.Translation.bim,
		configFileName: 'bim',

		constructor: function() {
			this.items = [{
				xtype: 'xcheckbox',
				name: 'enabled',
				fieldLabel: CMDBuild.Translation.enabled
			}, {
				xtype : 'textfield',
				fieldLabel : CMDBuild.Translation.url,
				name : 'url'
			}, {
				xtype : 'textfield',
				fieldLabel : CMDBuild.Translation.username,
				name : 'username'
			}, {
				xtype : 'textfield',
				fieldLabel : CMDBuild.Translation.password,
				inputType : 'password',
				name : 'password'
			} ];

			this.callParent(arguments);
		},

		afterSubmit: function() {
			var f = this.getForm();
			var en = f.findField("enabled");
			CMDBuild.Config.workflow.enabled = en.getValue();

			if (CMDBuild.Config.workflow.enabled) {
				_CMMainViewportController.enableAccordionByName("bim");
			} else {
				_CMMainViewportController.disableAccordionByName("bim");
			}
		}
	});

})();