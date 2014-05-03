(function() {
	var DEFAULT_MIN_ZOOM = 0;
	var DEFAULT_MAX_ZOOM = 25;
	var DEFAULT_POINT_DISTANCE_TOLLERANCE = 8; // the default radius of a point
/**
 * @class CMDBuild.Management.CMDBuildMap.MapLayer
 */

CMDBuild.PatchedBBOX = OpenLayers.Class(OpenLayers.Strategy.BBOX, {
	/*
	 * This method is called after a request,
	 * there are problems when remove the layer from the map...
	 * it seems that the strategy does not realize that the
	 * layer is now null
	 * 
	 * (...and I deactivate the strategy before remove the layer...)
	 */
	merge: function(resp) {
		if (this.layer) {
			return OpenLayers.Strategy.BBOX.prototype.merge.apply(this, arguments);
		}
	}
});

CMDBuild.Management.CMMap.MapLayer = OpenLayers.Class(OpenLayers.Layer.Vector, {

	initialize: function(name, options) {
		// Set the google projection
		this.projection = new OpenLayers.Projection("EPSG:900913"),

		// CMDBuild stuff
		this.editLayer = undefined,
		this.geoAttribute = undefined,
		this.cmdb_minZoom = options.geoAttribute.minZoom || DEFAULT_MIN_ZOOM;
		this.cmdb_maxZoom = options.geoAttribute.maxZoom || DEFAULT_MAX_ZOOM;
		this.cmdb_index = options.geoAttribute.index;

		this.hiddenFeature = {};
		this._defaultStyleConfiguration = Ext.decode(options.geoAttribute.style);
		this.styleMap = new OpenLayers.StyleMap({
			"default": this._defaultStyleConfiguration,
			"select": new OpenLayers.Style(OpenLayers.Feature.Vector.style["default"]),
			"temporary": Ext.decode(options.geoAttribute.style)
		});

		this.protocol = new OpenLayers.Protocol.HTTP({
			url: 'services/json/gis/getgeocardlist',
			params: {
				className: options.targetClassName,
				attribute: options.geoAttribute.name
			},
			format: new OpenLayers.Format.GeoJSON()
		});

		this.strategies = [
			new CMDBuild.PatchedBBOX({
				autoActivate: true
			}),
			new OpenLayers.Strategy.Refresh({
				autoActivate: true
			})
		];

		OpenLayers.Layer.Vector.prototype.initialize.apply(this, arguments);
	},

	activateStrategies: function(activate) {
		for (var i=0, strategy=null; i < this.strategies.length; ++i) {
			strategy = this.strategies[i];

			if (activate) {
				strategy.activate();
				if (typeof strategy.refresh == "function") {
					strategy.refresh();
				}
			} else {
				strategy.deactivate();
			}
		}
	},

	refreshStrategies: function() {
		for (var i=0, strategy=null; i<this.strategies.length; ++i) {
			strategy = this.strategies[i];
			if (strategy.refresh) {
				strategy.force = true;
				strategy.refresh();
				strategy.force = false;
			}
		}
	},

	destroyStrategies: function() {
		for (var i=0, strategy=null; i<this.strategies.length; ++i) {
			strategy = this.strategies[i];
			if (strategy) {
				strategy.destroy();
			}
		}
	},

	selectFeatureByMasterCard: function(masterCardId) {
		var f = this.getFeatureByMasterCard(masterCardId);
		this.selectFeature(f);
	},

	selectFeature: function(f) {
		if (f) {
			if (this.editLayer) {
				this.lastSelection = f.clone();
				this.removeFeatures([f]);

				this.editLayer.removeAllFeatures();
				this.editLayer.addFeatures(f.clone());
			}
		}
	},

	clearSelection: function() {
		if (this.lastSelection) {
			// restore the feature that was selected
			this.addFeatures( [this.lastSelection.clone()]);
			this.lastSelection = undefined;
		}

		if (this.editLayer) {
			this.editLayer.removeAllFeatures();
		}
	},

	getFeatureByMasterCard: function(masterCardId) {
		var features = this.features;
		for (var i=0, l = features.length; i < l; ++i) {
			var f = features[i];
			if (f.attributes.master_card == masterCardId) {
				return f;
			}
		}
		return null;
	},

	getFeaturesInLonLat: function(lonlat) {
		var features = this.features;
		var out = [];

		if (lonlat) {
			var point = new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat);
			for (var i=0, f=null, g=null; i < features.length; ++i) {
				f = features[i];
				g = f.geometry;
				if (g.CLASS_NAME == "OpenLayers.Geometry.Point") {

					var distance = g.distanceTo(point)/this.getResolution();
					var tollerance = this._defaultStyleConfiguration.pointRadius || DEFAULT_POINT_DISTANCE_TOLLERANCE;
					if (distance < tollerance) {
						out.push(f.clone());
					}
				} else if (typeof g.intersects == "function"
					&& g.intersects(point)) {

					out.push(f.clone());
				}
			}
		}

		return out;
	},

	// the feature to hide could be passed when the
	// layer load the feature remotely
	hideFeatureWithCardId: function(masterCardId, feature) {
		var f = feature || this.getFeatureByMasterCard(masterCardId);
		if (f) {
			this.hiddenFeature[masterCardId] = f.clone();
			this.removeFeatures([f]);
		}
	},

	showFeatureWithCardId: function(masterCardId) {
		var f = this.hiddenFeature[masterCardId];
		if (f) {
			this.addFeatures([f.clone()]);

			delete this.hiddenFeature[masterCardId];
		}
	},

	getEditedGeometry: function() {
		try {
			var f = this.editLayer.features;
			return f[0].geometry;
		} catch (Error) {
			return null;
		}
	}
});

})();