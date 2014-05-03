(function() {

	var tr = CMDBuild.Translation.administration.modcartography;

	Ext.define("CMDBuild.view.administration.accordion.CMGISAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: tr.title,
		cmName: "gis",
		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},
		updateStore: function() {
			var root = this.store.getRootNode();
			root.removeAll();
			root.appendChild([{
				text: tr.icons.title,
				leaf: true,
				cmName: "gis-icons"
			}, {
				text: tr.external_services.title,
				leaf: true,
				cmName: "gis-external-services"
			}, {
				text: tr.layermanager.title,
				leaf: true,
				cmName: "gis-layers-order"
			}, {
				text: tr.geoserver.title,
				leaf: true,
				cmName: "gis-geoserver"
			}, {
				text: tr.navigationTree.title,
				leaf: true,
				cmName: "gis-filter-configuration"
			}]);
		}
	});

})();