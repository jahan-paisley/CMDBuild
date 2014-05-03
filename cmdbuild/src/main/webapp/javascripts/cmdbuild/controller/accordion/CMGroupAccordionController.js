(function() {
	Ext.define("CMDBuild.controller.accordion.CMGroupAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",
		
		constructor: function(accordion) {
			this.store = accordion.store;
			this.callParent(arguments);
			
			_CMCache.on("cm_group_saved", updateStore, this);
		}
	});

	function updateStore(group) {
		this.accordion.updateStore();
		this.accordion.deselect();
		this.accordion.selectNodeById(group.get("id"));
	}
})();