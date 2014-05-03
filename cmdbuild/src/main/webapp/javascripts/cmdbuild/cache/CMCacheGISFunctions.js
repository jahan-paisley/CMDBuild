(function() {
	var layers = undefined;
	var layersStore = undefined;

	Ext.define("CMDBUild.cache.CMCacheGisFunctions", {
		getAllLayers: function(cb) {
			if (typeof layers == "undefined") {
				CMDBuild.ServiceProxy.gis.getAllLayers({
					success: function (operation, request, decoded) {
						layers = decoded.layers;
						cb(layers);
					}
				});
			} else {
				cb(layers);
			}
		},

		getLayersStore: function() {
			var me = this;
			if (typeof layersStore == "undefined") {
				layersStore =  new Ext.data.Store({
					model: "GISLayerModel",
					sorters: {
						property: 'index',
						direction: 'ASC'
					}
				});

				layersStore.load = function(conf) {
					me.getAllLayers(function(layers) {
						layersStore.loadData(layers);
						if (typeof conf != "undefined"
								&& typeof conf.callback == "function") {
							conf.callback(layers);
						}
					});
				};
			}

			return layersStore;
		},

		getLayersForEntryTypeName: function(entryTypeName, cb) {
			this.getAllLayers(function(layers) {
				var out = [];
				for (var i=0, l=layers.length; i<l; ++i) {
					var layer = layers[i];
					if (layer.masterTableName == entryTypeName) {
						out.push(layer);
					}
				}

				cb(out);
			});
		},

		getVisibleLayersForEntryTypeName: function(entryTypeName, cb) {
			this.getAllLayers(function(layers) {
				var out = [];
				for (var i=0, l=layers.length; i<l; ++i) {
					var layer = layers[i];
					if (Ext.Array.contains(layer.visibility, entryTypeName)) {
						out.push(layer);
					}
				}

				cb(out);
			});
		},

		onGeoAttributeSaved: function() {
			layers = undefined;
			if (typeof layersStore != "undefined") {
				layersStore.load();
			} 
		},

		onGeoAttributeDeleted: function(entryTypeName, attributeName) {
			layers = undefined;
			if (typeof layersStore != "undefined") {
				var layerIndex = findLayerIndexInStore(entryTypeName, attributeName);
				if (layerIndex >= 0) {
					layersStore.removeAt(layerIndex);
				}
			} 
		},

		onGeoAttributeVisibilityChanged: function() {
			layers = undefined;
		}
	});

	function findLayerIndexInStore(masterTableName, attributeName) {
		return layersStore.findBy(function(record) {
			return (record.getName() == attributeName && record.getMasterTableName() == masterTableName);
		});
	};

	function findLayerInStore(masterTableName, layerName) {
		var layer = null;
		if (typeof layersStore == "undefined") {
			return layer;
		}

		var layerIndex = findLayerIndexInStore(masterTableName, layerName);
		if (layerIndex >= 0) {
			layer = layersStore.getAt(layerIndex);
		}

		return layer;
	}

})();