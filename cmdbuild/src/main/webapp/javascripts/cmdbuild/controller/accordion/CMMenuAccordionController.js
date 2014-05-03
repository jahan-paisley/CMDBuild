(function() {
	Ext.define("CMDBuild.controller.accordion.CMMenuAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",
		
		constructor: function(accordion) {
			this.store = accordion.store;
			this.callParent(arguments);
			
			_CMCache.on("cm_group_saved", updateStore, this);
		}
	});

	function updateStore(group) {
		this.accordion.onGroupAdded(group);
	}
})();