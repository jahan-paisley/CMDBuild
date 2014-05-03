(function() {
	var DEFAULT_MIN_ZOOM = 0;
	var DEFAULT_MAX_ZOOM = 25;

	/**
	 * @class GeoAttributeState
	 */
	CMDBuild.state.GeoAttributeState = function(values, zoom) {
		if (typeof values == "undefined") {
			return null;
		}

		/**
		 * @memberOf GeoAttributeState
		 * @private
		 * @type Object
		 * an object with the geoAttribute serialization
		 */
		var _values = values || {};

		this.getValues = function() {
			return _values;
		};

		/**
		 * @memberOf GeoAttributeState
		 * @type Boolean
		 * if the current zoom of the
		 * map fit with the configured zoom range
		 * 
		 */
		var _zoomValid = CMDBuild.state.GeoAttributeState.isZoomValid(values, zoom);

		this.isZoomValid = function() {
			return _zoomValid;
		};

		this.setZoomValid = function(valid) {
			_zoomValid = valid;
		};

		/**
		 * @memberOf GeoAttributeState
		 * @private
		 * @type Boolean
		 * the visibility of the relative layer, set via UI
		 */
		var _userVisible = true;

		this.isUserVisible = function() {
			return _userVisible;
		};

		this.setUserVisible = function(visible) {
			_userVisible = visible;
		};

		/**
		 * @memberOf GeoAttributeState
		 * @private
		 * @type Boolean
		 */
		var _used = false;

		this.isUsed = function() {
			return _used;
		};

		this.setUsed = function(used) {
			_used = used;
		};

		/**
		 * @memberOf GeoAttributeState
		 * @type String
		 * Return a string that identify the attribute instance wide
		 */
		this.getKey = function() {
			return CMDBuild.state.GeoAttributeState.getKey(_values);
		};
	};

	CMDBuild.state.GeoAttributeState.getKey = function getKey(values) {
		var prefix = values.masterTableName || "Geoserver";
		return  prefix + "-" + values.name;
	};

	CMDBuild.state.GeoAttributeState.isZoomValid = function isZoomValid(values, zoom) {
		var min = values.minZoom || DEFAULT_MIN_ZOOM;
		var max = values.maxZoom || DEFAULT_MAX_ZOOM;
		return (zoom >= min && zoom <= max);
	};

	CMDBuild.state.CMGeoAttributeCatalog = function() {
		var _geoAttributes = {};

		/**
		 * 
		 * @param {Object} values
		 * @param {int} zoom Used to init the visibility of the GeoAttributeState if this is not already created
		 * @returns
		 */
		this.getGeoAttributeWithValues = function getGeoAttributeWithValues(values, zoom) {
			if (!values) {
				return;
			}

			var ga = new CMDBuild.state.GeoAttributeState(values, zoom);

			if (typeof _geoAttributes[ga.getKey()] == "undefined") {
				_geoAttributes[ga.getKey()] = ga;
				return ga;
			} else {
				return _geoAttributes[ga.getKey()];
			}
		};

		this.reset = function reset(delegate) {
			for (var key in _geoAttributes) {
				var ga = _geoAttributes[key];
				if (ga.isUsed()) {
					ga.setUsed(false);
					if (delegate) {
						delegate.geoAttributeUsageChanged(ga);
					}
				}
			}
		};

		this.getUsedGeoAttributes = function getUsedGeoAttributes() {
			var used = [];
			for (var key in _geoAttributes) {
				var ga = _geoAttributes[key];
				if (ga.isUsed()) {
					used.push(ga);
				}
			}

			return used;
		};

		this.isAUsedGeoAttribute = function isGeoAttributeUsed(values) {
			var used = false;
			var key = CMDBuild.state.GeoAttributeState.getKey(values);
			var ga = _geoAttributes[key];
			if (ga) {
				used = ga.isUsed();
			}

			return used;
		};

		this.isGeoAttributeVisibleToUser = function isGeoAttributeVisibleToUser(values) {
			var ga = this.getGeoAttributeWithValues(values);
			return ga.isUserVisible();
		};
	};

	CMDBuild.state.CMFeatureVisibilityCatalog = function() {
		var _classes = {};

		this.setVisibilityForCard = function(className, cardId, visibility) {
			var storedFeaturesInfo = getStoredInfoForClassName(className);
			storedFeaturesInfo[cardId] = visibility;
		};

		this.isFeatureVisible = function(className, cardId) {
			var storedFeaturesInfo = getStoredInfoForClassName(className);
			var visible = null;
			if (storedFeaturesInfo) {
				if (typeof storedFeaturesInfo[cardId] != "undefined") {
					visible = storedFeaturesInfo[cardId];
				}
			}

			return visible;
		};

		function getStoredInfoForClassName(className) {
			if (typeof _classes[className] == "undefined") {
				_classes[className] = {};
			}

			return _classes[className];
		}
	};

	/** @class CMMapStateDelegate */
	CMDBuild.state.CMMapStateDelegate = function() {
		this.geoAttributeUsageChanged = function(geoAttribute){};
		this.geoAttributeZoomValidityChanged = function(geoAttribute){};
	};

	/** @class CMMapState */
	CMDBuild.state.CMMapState = function(delegate) {
		/**
		 * @memberOf CMMapState
		 * @private
		 */
		this.geoAttributeCatalog = new CMDBuild.state.CMGeoAttributeCatalog();

		this.featureVisibilityCatalog = new CMDBuild.state.CMFeatureVisibilityCatalog();

		this.geoServerLayersVisibility = {};

		/**
		 * @memberOf CMMapState
		 * @private
		 */
		this.delegate = delegate;

		/**
		 * @memberOf CMMapState
		 * @param {CMDBuild.Management.CMMap.MapLayer}
		 * layer the layer to add
		 */
		this.addLayer = function(layer, zoom) {
			var geoAttribute = this.geoAttributeCatalog.getGeoAttributeWithValues(layer.geoAttribute, zoom);
			if (!geoAttribute) {
				return;
			}

			layer.setVisibility(geoAttribute.isUserVisible());
		};

		/**
		 * @memberOf CMMapState
		 * @param {CMDBuild.Management.CMMap.MapLayer} layer The layer to update
		 * @param {int} zoom the current map zoom level 
		 */
		this.updateLayerVisibility = function(layer, zoom) {
			var geoAttribute = this.geoAttributeCatalog.getGeoAttributeWithValues(layer.geoAttribute, zoom);
			if (!geoAttribute) {
				return;
			}

			geoAttribute.setUserVisible(layer.getVisibility());
		};

		/**
		 * @memberOf CMMapState
		 * @param {array of object} geoAttributes The attributes to activate
		 */
		this.update = function(geoAttributes, zoom) {
			this.geoAttributeCatalog.reset(this.delegate);
			for (var i=0, l=geoAttributes.length; i<l; ++i) {
				var ga = this.geoAttributeCatalog.getGeoAttributeWithValues(geoAttributes[i], zoom);
				if (ga) {
					ga.setUsed(true);
					if (this.delegate) {
						this.delegate.geoAttributeUsageChanged(ga);
					}
				}
			}
		};

		this.updateForZoom = function(zoom) {
			var usedGeoAttributes = this.geoAttributeCatalog.getUsedGeoAttributes();
			for (var i=0, l=usedGeoAttributes.length; i<l; ++i) {
				var ga = usedGeoAttributes[i];
				var isNowZoomValid = CMDBuild.state.GeoAttributeState.isZoomValid(ga.getValues(), zoom);
				if (ga.isZoomValid() !== isNowZoomValid) {
					ga.setZoomValid(isNowZoomValid);
					if (this.delegate) {
						this.delegate.geoAttributeZoomValidityChanged(ga);
					}
				}
			}
		};

		this.isAUsedGeoAttribute = function isAUsedGeoAttribute(values) {
			return this.geoAttributeCatalog.isAUsedGeoAttribute(values);
		};

		this.isGeoAttributeVisibleToUser = function isGeoAttributeVisibleToUser(values) {
			var visible = this.geoAttributeCatalog.isGeoAttributeVisibleToUser(values);
			_debug("Layer visibile ?", values, visible);
			return visible;
		};

		this.setFeatureVisisbility = function(className, cardId, visible) {
			this.featureVisibilityCatalog.setVisibilityForCard(className, cardId, visible);
			if (this.delegate) {
				this.delegate.featureVisibilityChanged(className, cardId, visible);
			}
		};

		this.isFeatureVisible = function(className, cardId) {
			return this.featureVisibilityCatalog.isFeatureVisible(className, cardId);
		};

		this.setGeoServerLayerVisibility = function(layerName, visible) {
			this.geoServerLayersVisibility[layerName] = visible;
		};

		this.isGeoServerLayerVisible = function(layerName) {
			return this.geoServerLayersVisibility[layerName];
		}
	};

})();