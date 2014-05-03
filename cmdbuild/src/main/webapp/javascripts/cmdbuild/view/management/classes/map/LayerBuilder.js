(function() {
	var WMS_IMAGE_FORMAT = 'image/png';
	var GOESERVER_SERVICE_TYPE = "wms";
	var GEOSERVER = "_Geoserver";
	var DEFAULT_MIN_ZOOM = 0;
	var DEFAULT_MAX_ZOOM = 25;

	function AbstractLayer() {};

	AbstractLayer.prototype = {
		CMDBuildLayer : true,
		CM_Layer : true,
		activateStrategies : Ext.emptyFn,
		destroyStrategies : Ext.emptyFn,
		selectFeatureByMasterCard : Ext.emptyFn,
		selectFeature : Ext.emptyFn,
		getFeatureByMasterCard : Ext.emptyFn,
		clearSelection : Ext.emptyFn,
		getEditedGeometry : Ext.emptyFn,
		refreshFeatures : Ext.emptyFn
	};

	CMDBuild.Management.CMMap.LayerBuilder = {
		/*
		 * buildLayer configuration object:
    	 * {
    	 * 	classId: integer,
    	 * 	geoAttribute: a cached attribute of the class referred from classId
    	 * 	withEditLayer: boolean to say if we want a editLayer or not 
    	 * }
    	 */
		buildLayer: function(config, map) {
			var classId = config.classId,
				geoAttribute = config.geoAttribute,
				withEditLayer = config.withEditLayer,
				editLayer = null,
				layer = null;

			if (!geoAttribute) {
				return null;
			}

			if (isItMineOrOfMyAncestors(geoAttribute, classId)
					&& withEditLayer) {
				// add the edit layer only for the layer
				// defined for the current class or for an ancestor
				editLayer = buildEditLayer(geoAttribute, map);
			}

			if (geoAttribute.masterTableName != GEOSERVER) {
				layer = buildCmdbLayer(geoAttribute, classId, editLayer, map);
			} else {
				layer = buildGeoserverLayer(geoAttribute);
			}

			return layer;
		}
	};

	function buildCmdbLayer(geoAttribute, classId, editLayer, map) {
		var layerDescription = geoAttribute.description;

		/*
		 * At first, we want prefix the class name only for
		 * the layer that does not belong to the actual class
		 * Now we want to prefix it to all. To remember this
		 * decision, comment the condition to skip the
		 * owned layers 
		 */

		// if (!editLayer) {

		var masterClass = _CMCache.getEntryTypeByName(geoAttribute.masterTableName);
		if (masterClass) {
			layerDescription = masterClass.get("text") + " - " + layerDescription;
		}

		// }

		var layer = new CMDBuild.Management.CMMap.MapLayer(layerDescription, {
			targetClassName: getClassNameForRequest(geoAttribute, classId),
			geoAttribute: geoAttribute,
			editLayer: editLayer,
			eventListeners: {
				/*
				 * Select a feature if, when added to the map, refers
				 * to the current card
				 * 
				 * 
				 * p.feature, the OpenLayers.Feature added
				 * p.object, the layer that fires the event
				 * p.type, the event type
				 * p.element, the HTML element of the layer
				 */
				featureadded: function(p) {

					var feature = p.feature;
					if (!feature) {
						return;
					}
					map.featureWasAdded(feature);
				}
			}
		});

		return Ext.applyIf(layer, new AbstractLayer());
	};

	function buildEditLayer(geoAttribute, map) {
		// the edit layer is used to manage the single insert.
		// ask to the map if has already an editLayer for this geoAttribute,
		// if not, build a new layer for it.
		var name = CMDBuild.state.GeoAttributeState.getKey(geoAttribute)+'-Edit';

		var editLayer = map.getLayerByName(name);

		if (!editLayer) {
			editLayer = new OpenLayers.Layer.Vector(name, {
				projection: new OpenLayers.Projection("EPSG:900913"),
				displayInLayerSwitcher: false,
				// cmdb stuff
				geoAttribute: geoAttribute,
				CM_EditLayer: true,
				CM_Layer: true
			});
		}

		return editLayer;
	};

	function buildGeoserverLayer(geoAttribute) {
		var geoserver_ws = CMDBuild.Config.gis.geoserver_workspace,
			geoserver_url = CMDBuild.Config.gis.geoserver_url;

		var layer = new OpenLayers.Layer.WMS(geoAttribute.description,
			geoserver_url + "/" + GOESERVER_SERVICE_TYPE, {
				layers : geoserver_ws + ":" + geoAttribute.geoServerName,
				format : WMS_IMAGE_FORMAT,
				transparent : true
			}, {
				singleTile : true,
				ratio : 1
			});

		layer.cmdb_minZoom = geoAttribute.minZoom || DEFAULT_MIN_ZOOM;
		layer.cmdb_maxZoom = geoAttribute.maxZoom || DEFAULT_MAX_ZOOM;
		layer.cmdb_index = geoAttribute.index;

		layer.geoAttribute = geoAttribute;
		layer.editLayer = undefined;

		layer = Ext.applyIf(layer, new AbstractLayer());

		layer.CMDBuildLayer = false;
		layer.CM_Layer = false;
		layer.CM_geoserverLayer = true;
		return layer;
	};

	// say if an attribute belong to the passed table
	// or to an ancestor of him
	function isItMineOrOfMyAncestors(attr, tableId) {
		var table = _CMCache.getEntryTypeById(tableId);

		while (table) {
			if (attr.masterTableName == table.get("name")) {
				return table;
			} else {
				var parentId = table.get("parent");
				if (parentId) {
					table = _CMCache.getEntryTypeById(parentId);
				} else {
					table = null;
				}
			}
		}

		return false;
	};

	function getClassNameForRequest(geoAttribute, tableId) {
		var table = isItMineOrOfMyAncestors(geoAttribute, tableId);
		if (table) {
			return table.get("name");
		} else {
			return geoAttribute.masterTableName;
		}
	};

})();