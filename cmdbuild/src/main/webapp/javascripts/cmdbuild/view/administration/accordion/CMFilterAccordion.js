(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMFilterAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.search_filters,

		hideMode: "offsets",

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();
			root.removeAll();

			root.appendChild({
				text: CMDBuild.Translation.filters_for_groups,
				cmName: "groupfilter",
				leaf: true
			});
		}
	});

})();