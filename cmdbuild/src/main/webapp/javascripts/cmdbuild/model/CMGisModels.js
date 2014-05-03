(function() {
	Ext.define('IconsModel', {
		extend: 'Ext.data.Model',
		fields: [
			{name: 'name', type: 'string'},
			{name: 'description', type: 'string'},
			{name: 'path', type: 'string'}
		]
	});

	Ext.define('GISLayerModel', {
		extend: 'Ext.data.Model',
		fields: [
			{name: 'maxZoom', type: 'string'},
			{name: 'minZoom', type: 'string'},
			{name: 'style', type: 'string'},
			{name: 'description', type: 'string'},
			{name: 'index', type: 'number'},
			{name: 'name', type: 'string'},
			{name: 'fullName', type: 'string'},
			{name: 'type', type: 'string'},
			{name: "masterTableId", type: "string"},
			{name: "masterTableName", type: "string"},
			{name: "visibility", type: "auto"},
			{name: "cardBinding", type: "auto"},
			{name: "geoServerName", type: "string"}
		],

		getName: function() {
			return this.get("name");
		},

		getFullName: function() {
			return this.get("fullName");
		},

		getMasterTableName: function() {
			return this.get("masterTableName");
		},

		getNameForGeoServer: function() {
			return this.get("geoServerName");
		},

		getVisibility: function() {
			return this.get("visibility") || [];
		},

		getCardBinding: function() {
			return this.get("cardBinding");
		},

		setVisibilityForTableName: function(tableName, visibility) {
			var currentVisibility = this.getVisibility();
			var alreadyVisible = Ext.Array.contains(currentVisibility, tableName);
			if (visibility) {
				if (!alreadyVisible) {
					currentVisibility.push(tableName);
				}
			} else {
				if (alreadyVisible) {
					Ext.Array.remove(currentVisibility, tableName);
				}
			}

			_debug("Current visibility", currentVisibility);
			this.set("visibility", currentVisibility);
		},

		isVisibleForEntryType: function(et) {
			return Ext.Array.contains(this.getVisibility(), et.get("name"));
		}
	});
})();