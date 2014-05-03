(function() {
	var tr = CMDBuild.Translation.administration.modcartography.geoserver;
	
	Ext.define("CMDBuild.view.administration.gis.CMModGeoServer", {
		extend: "Ext.panel.Panel",
		
		cmName: "gis-geoserver",
		firstShow: true,
		initComponent : function() {
			this.addLayerButton = new Ext.button.Button({
				iconCls: 'add',
				text: tr.add_layer
			});

			this.layersGrid = new CMDBuild.Administration.GeoServerLayerGrid({
				region: "center",
				enableDragDrop: true,
				frame: false,
				border: false,
				tbar: [this.addLayerButton]
			});

			this.form = new CMDBuild.Administration.GeoServerForm({
				height: "50%",
				autoScroll: false,
				frame: false,
				border: false,
				region: "south",
				split: true
			});

			Ext.apply(this, {
				title: tr.title,
				layout: "border",
				items: [this.layersGrid, this.form],
				border: true
			});

			this.callParent(arguments);
		}
	});
})();