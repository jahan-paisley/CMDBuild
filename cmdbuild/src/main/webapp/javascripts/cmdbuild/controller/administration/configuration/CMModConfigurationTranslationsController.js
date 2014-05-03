(function() {
	Ext.define("CMDBuild.controller.administration.configuration.CMModConfigurationTranslationsController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function(view) {
			this.callParent([view]);

			this.view.saveButton.on("click", function() {
				CMDBuild.LoadMask.get().show();
				var params = this.view.getValues();
				for (var i = 0; i < this.view.languages.length; i++) {
					if (! params[this.view.languages[i]]) {
						params[this.view.languages[i]] = "off";
					}
				}
				CMDBuild.ServiceProxy.translations.saveActiveTranslations({
					scope: this,
					params: params,
					callback: function() {
						CMDBuild.LoadMask.get().hide();
						//needed to mantein the consistenece beetween the information displayed and the 
						//information in the config file
						this.readConfiguration();
						_CMCache.resetMultiLanguages();
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
			this.view.doLayout();
		},

		readConfiguration: function(){
			CMDBuild.ServiceProxy.translations.readActiveTranslations({
				scope: this,
				success: function(response){
					this.view.populateForm(Ext.JSON.decode(response.responseText));
					this.view.afterSubmit(Ext.JSON.decode(response.responseText).data);
				}
			}, name = this.view.configFileName);
		}
	});
})();