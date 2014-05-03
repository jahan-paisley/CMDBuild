(function() {
	
	Ext.define("CMDBuild.controller.administration.lookup.CMModLookupController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);

			this.lookupTypeFormController = new CMDBuild.controller.administration.lookup.CMLookupTypeFormController(this.view.lookupTypeForm);
			this.lookupGridController = new CMDBuild.controller.administration.lookup.CMLookupGridController(this.view.lookupGrid);
			this.lookupFormController = new CMDBuild.controller.administration.lookup.CMLookupFormController(this.view.lookupForm);

			this.lookupGridController.bindSubController(this.lookupFormController);
			this.lookupFormController.bindSubController(this.lookupGridController);

			this.view.addLookupTypeButton.on("click", function() {
				this.lookupTypeFormController.onAddLookupTypeClick();
				this.lookupGridController.onAddLookupTypeClick();
				this.lookupFormController.onAddLookupTypeClick();
				_CMMainViewportController.deselectAccordionByName("lookuptype");
				this.view.activateLookupTypeForm();
				this.view.disableLookupTab();
			}, this);
		},

		onViewOnFront: function(lookupType) {
			if (lookupType) {
				this.view.onSelectLookupType(lookupType.data);
				
				this.lookupTypeFormController.onSelectLookupType(lookupType.data);
				this.lookupGridController.onSelectLookupType(lookupType.data);
				this.lookupFormController.onSelectLookupType(lookupType.data.id);
			}
		}
	});

})();