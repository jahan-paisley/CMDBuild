(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMBIMAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.bim,

		cmName: "bim",

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();
			root.removeAll();
			root.appendChild([{
				text: CMDBuild.Translation.projects,
				leaf: true,
				cmName: "bim-project"
			}, {
				text: CMDBuild.Translation.layers,
				leaf: true,
				cmName: "bim-layers"
			}]);
		}
	});

})();