(function() {
	CMDBuild.GeoUtils = {
		readGeoJSON: function(geoJson) {
			var parser = new OpenLayers.Format.GeoJSON();
			return parser.parseGeometry(geoJson);
		}
	};
})();