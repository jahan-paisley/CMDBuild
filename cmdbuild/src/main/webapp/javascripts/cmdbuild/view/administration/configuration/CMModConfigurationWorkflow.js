(function() {
	var tr = CMDBuild.Translation.administration.setup.workflow;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationWorkflow", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		configFileName: 'workflow',
		
		constructor: function() {
			this.items = [ {
				xtype : 'fieldset',
				title : tr.general,
				autoHeight : true,
				defaultType : 'textfield',
				items : [ {
					fieldLabel : tr.enabled,
					xtype : 'xcheckbox',
					name : 'enabled'
				}, {
					fieldLabel : tr.endpoint,
					allowBlank : false,
					name : 'endpoint',
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				} ]
			}, {
				xtype : 'fieldset',
				title : tr.credential,
				autoHeight : true,
				defaultType : 'textfield',
				items : [ {
					fieldLabel : tr.user,
					allowBlank : false,
					name : 'user'
				}, {
					fieldLabel : tr.password,
					allowBlank : false,
					name : 'password',
					inputType : 'password'
				}, {
					fieldLabel : tr.engine,
					allowBlank : false,
					name : 'engine',
					disabled : true
				}, {
					fieldLabel : tr.scope,
					allowBlank : true,
					name : 'scope',
					disabled : true
				} ]
			} ]
			this.callParent(arguments);
		},
		
		afterSubmit: function() {
			var f = this.getForm();
			var en = f.findField("enabled");
			CMDBuild.Config.workflow.enabled = en.getValue();
			
			if (CMDBuild.Config.workflow.enabled) {
				_CMMainViewportController.enableAccordionByName("process");
			} else {
				_CMMainViewportController.disableAccordionByName("process");
			}
		}
	});
})();