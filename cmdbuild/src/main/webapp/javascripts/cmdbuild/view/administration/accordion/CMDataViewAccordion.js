(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMDataViewAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.views,

		hideMode: "offsets",

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();
			root.removeAll();

			var children = [{
				text: CMDBuild.Translation.filterView,
				cmName: "filterdataview",
				leaf: true
			}, {
				text: CMDBuild.Translation.sqlView,
				cmName: "sqldataview",
				leaf: true
			}];

			root.appendChild(children);
		}
	});

})();