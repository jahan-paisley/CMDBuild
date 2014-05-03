(function() {
	Ext.define("CMDBuild.controller.administration.configuration.CMModConfigurationController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		constructor: function(view) {
			this.callParent([view]);

			this.view.saveButton.on("click", function() {
				CMDBuild.LoadMask.get().show();
				CMDBuild.ServiceProxy.configuration.save({
					scope: this,
					params: this.view.getValues(),
					callback: function() {
						CMDBuild.LoadMask.get().hide();
						//needed to mantein the consistenece beetween the information displayed and the
						//information in the config file
						this.readConfiguration();
					}
				}, name = this.view.configFileName);
			}, this);

			this.view.abortButton.on("click", function() {
				this.readConfiguration();
			}, this);
		},

		onViewOnFront: function() {
			if (this.view.isVisible()) {
				this.readConfiguration();
			}
			_CMCache.initModifyingTranslations();
			this.view.doLayout();
		},

		readConfiguration: function(){
			CMDBuild.ServiceProxy.configuration.read({
				scope: this,
				success: function(response){
					this.view.populateForm(Ext.JSON.decode(response.responseText));
					this.view.afterSubmit(Ext.JSON.decode(response.responseText).data);
				}
			}, name = this.view.configFileName);
		}
	});
})();