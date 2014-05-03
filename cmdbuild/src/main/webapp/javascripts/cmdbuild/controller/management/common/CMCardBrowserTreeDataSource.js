(function() {
	var GEOSERVER = "GeoServer";

	Ext.define("CMDBuild.controller.management.classes.CMCardBrowserTreeDataSource", {
		GEOSERVER: GEOSERVER,
		constructor: function(cardBrowserTree, mapState) {
			this.cardBrowserTree = cardBrowserTree;
			this.cardBrowserTree.setDataSource(this);
			this.mapState = mapState;
			this.configuration = CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration;
			this.refresh();
			this.callParent(arguments);
		},

		refresh: function() {
			var me = this;
			me.cardBrowserTree.setRootNode({
				loading: true,
				text: CMDBuild.Translation.common.loading
			});

			// fill the first level of tree nodes
			// asking the cards according to the 
			// root of the configuration
			CMDBuild.ServiceProxy.gis.expandDomainTree({
				success: function successGetCardBasicInfoList(operation, options, response) {
					addGeoserverLayersToTree(response.root, me);
					me.cardBrowserTree.setRootNode(response.root);
				}
			});
		}
	});

	function addGeoserverLayersToTree(root, me) {
		var children = (root) ? root.children || [] : [];
		for (var i=0, l=children.length; i<l; ++i) {
			addGeoserverLayersToTree(children[i], me);
		}

		addGeoserverLayerIfConfigured(root, me);
	}

	function addGeoserverLayerIfConfigured(nodeConfiguration, me) {
		var mapping = me.configuration.geoServerLayersMapping;
		if (mapping) {
			var layerPerClass = mapping[nodeConfiguration.className];
			if (layerPerClass) {
				// TODO: More than one GeoServer layer per card
				var layerPerCard = layerPerClass[nodeConfiguration.cardId];
				if (layerPerCard) {
					nodeConfiguration.children = [{
						text: layerPerCard.description,
						cardId: layerPerCard.name,
						className: GEOSERVER,
						leaf: true,
						// the geoserver layer must be visible only
						// if is visible the binded card node
						checked: nodeConfiguration.checked
					}].concat(nodeConfiguration.children);
				}
			}
		}

		return nodeConfiguration;
	}
})();