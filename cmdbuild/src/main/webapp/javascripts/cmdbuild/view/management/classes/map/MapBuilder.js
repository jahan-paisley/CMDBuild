CMDBuild.Management.MapBuilder = (function() {

	var bounds = new OpenLayers.Bounds(-20037508.34, -20037508.34, 20037508.34, 20037508.34),
		projection = new OpenLayers.Projection("EPSG:900913"),
		displayProjection = new OpenLayers.Projection("EPSG:4326");

	function buildMap(divId) {
		var options = {
			projection: projection,
			displayProjection: displayProjection,
			units: "m",
			numZoomLevels: 25,
			maxResolution: 156543.0339,
			maxExtent: bounds,
			div: divId,
			initBaseLayers: initBaseLayers,
			// Set starting size object
			// to avoid null pointer exception
			// on Firefox
			size: new OpenLayers.Size(0,0)
		};

		var map = new CMDBuild.Management.CMMap(options);
		map.cmBaseLayers = [];

		// map.addControl(new OpenLayers.Control.LayerSwitcher());
		map.addControl(new OpenLayers.Control.ScaleLine());

		map.addControl(new CMDBuild.Management.CMZoomAndMousePositionControl({
			zoomLabel : CMDBuild.Translation.management.modcard.gis.zoom,
			positionLabel : CMDBuild.Translation.management.modcard.gis.position
		}));

		addFakeLayer(map);

		return map;
	};

	function initBaseLayers() {
		var DEFAULT_MIN_ZOOM = 0,
			DEFAULT_MAX_ZOOM = 18,
			gisConfig = CMDBuild.Config.gis,
			map = this;

		// add OSM if configured
		if (gisConfig.osm && gisConfig.osm == "on") {
			var osm = new OpenLayers.Layer.OSM("Open Street Map", null, {
				numZoomLevels: 25,
				cmdb_minZoom: gisConfig.osm_minzoom || DEFAULT_MIN_ZOOM,
				cmdb_maxZoom: gisConfig.osm_maxzoom || DEFAULT_MAX_ZOOM,

				isInZoomRange: function(zoom) {
					var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
					return (zoom >= this.cmdb_minZoom && zoom <= max);
				},

				setVisibilityByZoom: function(zoom) {
					this.setVisibility(this.isInZoomRange(zoom));
				}
			});

			map.addLayers([osm]);
			map.cmBaseLayers.push(osm);
			map.setBaseLayer(osm);
		}

		// add GOOGLE if configured
		if (gisConfig.google && gisConfig.google == "on") {
			var googleLayer = new OpenLayers.Layer.Google(
				"Google",
				{
					sphericalMercator: true
				}
			);

			googleLayer.cmdb_minZoom = gisConfig.google_minzoom || DEFAULT_MIN_ZOOM;
			googleLayer.cmdb_maxZoom = gisConfig.google_maxzoom || DEFAULT_MAX_ZOOM;
			googleLayer.setVisibilityByZoom = function(zoom) {
				var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
				var isInRange = (zoom >= this.cmdb_minZoom && zoom <= max);

				this.setVisibility(isInRange);
			};

			map.addLayers([googleLayer]);
			map.setBaseLayer(googleLayer);
		}

		// add YAHOO if configured
		if (gisConfig.yahoo && gisConfig.yahoo == "on") {
			var yahooLayer = new OpenLayers.Layer.Yahoo(
				"Yahoo",
				{
					sphericalMercator: true
				}
			);
			yahooLayer.cmdb_minZoom = gisConfig.yahoo_minzoom || DEFAULT_MIN_ZOOM;
			yahooLayer.cmdb_maxZoom = gisConfig.yahoo_maxzoom || DEFAULT_MAX_ZOOM;
			yahooLayer.setVisibilityByZoom = function(zoom) {
				var max = this.cmdb_maxZoom <= DEFAULT_MAX_ZOOM ? this.cmdb_maxZoom : DEFAULT_MAX_ZOOM;
				var isInRange = (zoom >= this.cmdb_minZoom && zoom <= max);

				this.setVisibility(isInRange);
			};

			map.addLayers([yahooLayer]);
			map.setBaseLayer(yahooLayer);
		}

	};

	function addFakeLayer(map) {
		// add a fake base layer to set as base layer
		// when the real base layers are out of range.
		// Without this, the continue to ask the tails 
		var fakeBaseLayer = new OpenLayers.Layer.Vector("", {
			displayInLayerSwitcher: false,
			isBaseLayer: true
		});

		map.cmFakeBaseLayer = fakeBaseLayer;
		map.addLayers([fakeBaseLayer]);
	}

	return {
		buildMap: buildMap
	};
})();