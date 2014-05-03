(function() {
	var tr = CMDBuild.Translation.administration.setup.server;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationServer", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		configFileName: 'server',
		
		constructor: function() {
			this.clearCacheButton = new Ext.button.Button({
				text : tr.clear_cache
			});
		
			this.clearProcesses = new Ext.button.Button({
				text : tr.servicesync
			});

			this.unlockAllCards = new Ext.button.Button({
				text : CMDBuild.Translation.unlock_all_cards
			});

			this.items = [{
				xtype : 'fieldset',
				title : tr.cache_management,
				items : [this.clearCacheButton],
				padding: "5"
			}, {
				xtype : 'fieldset',
				title : tr.servicesync,
				layout : 'column',
				items : [this.clearProcesses],
				padding: "5"
			}, {
				xtype : 'fieldset',
				title : CMDBuild.Translation.lock_cards_in_edit,
				layout : 'column',
				items : [this.unlockAllCards],
				padding: "5"
			}
		];

			this.callParent(arguments);
		},

		buildButtons: function() {
			this.callParent(arguments);
			this.buttons = [];
		}
	});
})();