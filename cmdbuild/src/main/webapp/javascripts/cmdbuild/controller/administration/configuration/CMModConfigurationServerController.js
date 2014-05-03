(function() {
	Ext.define("CMDBuild.controller.administration.configuration.CMModConfigurationServerController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		
		constructor: function(view) {
			this.callParent([view]);
			
			this.view.clearCacheButton.on("click", function() {
				CMDBuild.Ajax.request( {
					url : 'services/json/utils/clearcache',
					loadMask : true,
					success : CMDBuild.Msg.success
				});
			});

			this.view.clearProcesses.on("click", function() {
				CMDBuild.Ajax.request( {
					url : 'services/json/workflow/sync',
					loadMask : true,
					success : CMDBuild.Msg.success
				});
			});

			this.view.unlockAllCards.on("click", function() {
				_CMProxy.card.unlockAllCards({
					success : CMDBuild.Msg.success
				});
			});
		}
	});
})();