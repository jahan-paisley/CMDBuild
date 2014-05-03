(function() {
	Ext.define("CMDBuild.controller.management.classes.CMMapLayerSwitcherController", {

		mixins: {
			mapDelegate: "CMDBuild.view.management.map.CMMapPanelDelegate"
		},

		constructor: function(layerSwitcher, map) {
			this.layerSwitcerView = layerSwitcher;
			var layers = map.layers || [];

			// Add the current layers
			for (var i=0, l=null; i<layers.length; ++i) {
				l = layers[i];
				this.onLayerAdded(null, {
					layer: l
				});
			}
		},

		/* As mapDelegate *******************/

		onLayerAdded: function(map, params) {
			var layer = getLayerFromMapParams(params);

			if (layer != null) {
				this.layerSwitcerView.addLayerItem(layer);
				layer.events.register("visibilitychanged", this, visibilityChanged);
			}
		},

		onLayerRemoved: function(map, params) {
			var layer = getLayerFromMapParams(params);
			if (layer != null) {
				layer.events.unregister("visibilitychanged", this, visibilityChanged);
				this.layerSwitcerView.removeLayerItem(layer);
			}
		},

		onLayerChanged: function(mapPanel, params) {
			if (params.property == "order") {
				this.layerSwitcerView.updateSorting();
			}
		},

		onMapPanelVisibilityChanged: Ext.emptyFn
	});

	function visibilityChanged(param) {
		var layer = param.object;
		this.layerSwitcerView.setItemCheckByLayerId(layer.id, layer.getVisibility());
	}

	function getLayerFromMapParams(params) {
		if (!params
				|| !params.layer
				|| !(params.layer.CMDBuildLayer || params.layer.CM_geoserverLayer)) {

			return null;
		} else {
			return params.layer;
		}
	}
})();