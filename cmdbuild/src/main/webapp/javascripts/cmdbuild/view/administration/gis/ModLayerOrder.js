(function() {
	
	Ext.define("CMDBuild.Administration.ModLayerOrder", {
		extend: "CMDBuild.Administration.LayerGrid",

		cmName: "gis-layers-order",
		title: CMDBuild.Translation.administration.modcartography.layermanager.title,
		initComponent: function() {
			this.border = true;
			this.callParent(arguments);
		},

		enableDragDrop: true,

		// override
		beforeRowMove: function(node, data, dropRec, dropPosition) {
			this.fireEvent("cm-rowmove", {
				node: node,
				data: data,
				dropRec: dropRec,
				dropPosition: dropPosition
			});

			return true;
		}
	});
})();