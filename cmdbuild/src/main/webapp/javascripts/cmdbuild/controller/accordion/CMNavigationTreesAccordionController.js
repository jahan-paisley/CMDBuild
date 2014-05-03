(function() {
	Ext.define("CMDBuild.controller.accordion.CMNavigationTreesAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",
		
		constructor: function(accordion) {
			this.store = accordion.store;
			this.callParent(arguments);

			_CMCache.on("cm_navigationTrees_saved", updateStore, this);
			_CMCache.on("cm_navigationTrees_deleted", onNavigationTreesDeleted, this);
		},

		expandForAdd: function() {
			this.accordion.expandSilently();
			_CMMainViewportController.bringTofrontPanelByCmName(this.accordion.cmName);
			_CMMainViewportController.panelControllers["navigationTrees"].onAddDomainButtonClick();
		},
		onUpdateCache: function() {
			this.accordionSM.deselectAll();
		}
	});

	function updateStore(domain) {
		this.accordion.updateStore();
		this.accordion.selectNodeById(domain.get("id"));
	}
	
	function onNavigationTreesDeleted(id) {
		this.accordion.removeNodeById(id);
		this.accordionSM.deselectAll();
	}
})();