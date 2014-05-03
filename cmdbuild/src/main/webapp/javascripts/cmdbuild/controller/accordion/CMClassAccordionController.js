(function() {
	Ext.define("CMDBuild.controller.accordion.CMClassAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",
		
		constructor: function(accordion) {
			this.store = accordion.store;
			this.callParent(arguments);
			
			_CMCache.on("cm_class_saved", updateStore, this);
			_CMCache.on("cm_class_deleted", updateStore, this);
		}
	});

	function updateStore(c) {
		this.accordion.updateStore(c);
		if (c && c.get) {
			this.accordion.selectNodeById(c.get("id"));
		}
	}
})();