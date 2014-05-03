(function() {
	/**
	 * @class CMDBuild.Management.CMDBuildMap
	 */
	CMDBuild.Management.CMMap = OpenLayers.Class(OpenLayers.Map, {

		getEditableLayers: function() {
			var layers = this.layers;
			var editLayers = [];
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer.editLayer) {
					editLayers.push(layer.editLayer);
				}
			}
			return editLayers;
		},

		activateStrategies: function(acvitate) {
			var layers = this.layers;
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];

				if (typeof layer.activateStrategies == "function") {
					layer.activateStrategies(acvitate);
				}
			}
		},

		refreshStrategies: function() {
			var layers = this.layers;
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];
				if (typeof layer.refreshStrategies == "function") {
					layer.refreshStrategies();
				}
			}
		},

		centerOnGeometry: function(geometry) {
			// The geometry function getCentroid()
			// give us some problems...
			// So, take the first point of the geometry (a point return an array with
			// only itself) to center the map.
			try {
				var geom = CMDBuild.GeoUtils.readGeoJSON(geometry.geometry);
				var aPoint = a = geom.getVertices()[0];
				var center = new OpenLayers.LonLat(aPoint.x, aPoint.y);
				this.setCenter(center, this.getZoom());
//				this.activateStrategies(true);
			} catch (Error) {
				_debug("Map: centerOnGeometry - Error");
				/*
				 * if the server doesn't return a feature
				 * the readGeoJSON methods throw an error.Rightly!!
				 */
			}
		},

		getFeatureByMasterCard: function(id) {
			var layers = this.layers;
			for (var i=0, l=this.layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer) {
					var feature = layer.getFeatureByMasterCard(id);
					if (feature) {
						return feature;
					}
				}
			}
			return null;
		},

		getFeaturesInLonLat: function(lonlat) {
			var layers = this.layers;
			var features = [];
			for (var i=0, l=this.layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer 
						&& typeof layer.getFeaturesInLonLat == "function") {

					features = features.concat(layer.getFeaturesInLonLat(lonlat));
				}
			}

			return features;
		},

		clearSelection: function() {
			var layers = this.layers;
			for (var i=0, l=this.layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer 
						&& typeof clearSelection == "function") {

					layer.clearSelection();
				}
			}
		},

		getLayerByName: function(name) {
			var l = this.getLayersByName(name);
			if (l.length > 0) {
				return l[0];
			} else {
				return null;
			}
		},

		getLayersByTargetClassName: function(targetClassName) {
			var layers = [];
			for (var i=0, layer=null; i<this.layers.length; ++i) {
				layer = this.layers[i];

				if (layer.geoAttribute 
						// TODO or an ancestor?
						&& layer.geoAttribute.masterTableName == targetClassName) {

					layers.push(layer);
				}
			}

			return layers;
		},

		getGeoServerLayerByName: function(layerName) {
			for (var i=0, layer=null; i<this.layers.length; ++i) {
				layer = this.layers[i];
				if (!layer.CM_geoserverLayer) {
					continue;
				}

				if (layer.geoAttribute.name == layerName) {
					return layer;
				}
			}
			return null;
		},

		getEditedGeometries: function() {
			var mapOfFeatures = {};
			var layers = this.layers;
			for (var i=0, l=layers.length; i<l; ++i) {
				var layer = layers[i];
				if (layer.editLayer) {
					var geo = layer.getEditedGeometry();
					if (geo != null) {
						mapOfFeatures[layer.geoAttribute.name] = geo.toString();
					} else {
						mapOfFeatures[layer.geoAttribute.name] = "";
					}
				}
			}
			return mapOfFeatures;
		},

		getCmdbLayers: function() {
			var out = [];
			for (var i=0, l=this.layers.length; i<l; ++i) {
				var layer = this.layers[i];
				if (layer 
						&& layer.geoAttribute
						&& !layer.CM_EditLayer
						&& !layer.CM_geoserverLayer) {

					out.push(layer);
				}
			}

			return out;
		},

		// called by the layers when a feature is added

		featureWasAdded: function(feature) {
			if (this.delegate) {
				this.delegate.featureWasAdded(feature);
			}
		}
	});
})();