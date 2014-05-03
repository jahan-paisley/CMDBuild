(function() {
	Ext.define("CMDBuild.controller.accordion.CMLookupAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",
		
		constructor: function(accordion) {
			this.store = accordion.store;
			this.callParent(arguments);
			
			_CMCache.on("cm_new_lookuptype", updateStore, this);
			_CMCache.on("cm_modified_lookuptype", updateStore, this);
		}
	});

	function updateStore(lType) {
		this.accordion.updateStore();
		this.accordion.selectNodeById(lType.id);
	}
})();