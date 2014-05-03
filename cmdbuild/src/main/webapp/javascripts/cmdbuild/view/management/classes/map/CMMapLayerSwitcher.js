(function() {

	var EXTERNAL_LAYERS_FOLDER_NAME = "cm_external_layers_folder";
	var CMDBUILD_LAYERS_FOLDER_NAME = "cm_cmdbuild_layers_folder";
	var GEOSERVER_LAYERS_FOLDER_NAME = "cm_geoserver_layers_folder";

	Ext.define("CMDBuild.view.management.map.CMMapLayerSwitcherDelegate", {
		/**
		 * 
		 * @param {Ext.data.NodeInterface} node The Node of the tree that represents the layer
		 * @param {Boolean} checked The new check value
		 */
		onLayerCheckChange: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.map.CMMapLayerSwitcher", {
		extend: "Ext.tree.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.map.CMMapLayerSwitcherDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.rootVisible = false;
			this.useArrows = true;
			this.frame = false;
			this.border = false;
			this.bodyBorder = false;

			this.store = new Ext.create('Ext.data.TreeStore', {
				fields: [{
					name: "folderName", type: "string"
				}, {
					name: "text", type: "string"
				}],
				root : {
					expanded : true,
					children : [{
						text: CMDBuild.Translation.administration.modClass.tabs.geo_attributes,
						leaf: false,
						expanded: true,
						folderName: CMDBUILD_LAYERS_FOLDER_NAME,
						checked: true
					}, {
						text: CMDBuild.Translation.administration.modcartography.external_services.title,
						leaf: false,
						expanded: true,
						folderName: EXTERNAL_LAYERS_FOLDER_NAME,
						checked: true
					}]
				}
			});

			this.mon(this, "checkchange", notifyToDelegateTheCheckChange, this);

			this.callParent(arguments);
		},

		/**
		 * 
		 * @param {OpenLayers.Layer} layer Add a node to the tree that represent the given layer
		 */
		addLayerItem: function(layer) {
			if (layer.displayInLayerSwitcher) {
				var targetFolder = retrieveTargetFolder(layer, this.getRootNode());

				try {

					var child = targetFolder.appendChild({
						text: layer.name,
						leaf: true,
						checked: (layer.getVisibility()) ? true : false,
						iconCls: "cmdbuild-nodisplay"
					});

					// passing the layerId with the configuration
					// has no effect. There is no time to
					// investigate... do it in the ugly way ;)
					child.layerId = layer.id;
					child.layerIndex = layer.cmdb_index;
				} catch (e) {
					_debug("Fail to add layer", layer);
				}
			}
		},

		/**
		 * 
		 * @param {OpenLayers.Layer} layer Removes from the tree the layer that representes the given layer
		 */
		removeLayerItem: function(layer) {
			var node = this.getNodeByLayerId(layer.id);

			if (node) {
				node.remove(true);
			}
		},

		/**
		 * 
		 * @param {String} layerId The id to use to identify the node of the tree
		 * @param {Boolean} checked The value to set to the checked property
		 */
		setItemCheckByLayerId: function(layerId, checked) {
			var node = this.getNodeByLayerId(layerId);

			if (node) {
				node.set("checked", checked);
			}
		},

		/**
		 * 
		 * @param {String} layerId The id of the layer to use to retrieve the node
		 * @returns A Ext.data.NodeInterface or null
		 */
		getNodeByLayerId: function(layerId) {
			return this.getRootNode().findChildBy(function(child) {
				return child.layerId == layerId;
			}, null, true);
		},

		updateSorting: function() {
			var root = this.getRootNode();
			if (root && root.childNodes) {
				for (var i=0, l=root.childNodes.length; i<l; ++i) {
					var node = root.childNodes[i];
					if (node) {
						node.sort(function(a, b) {
							if (a.layerIndex == b.layerIndex) {
								return 0;
							} else if (a.layerIndex > b.layerIndex){
								return 1;
							} else {
								return -1;
							}
						}, true);
					}
				}
			}
		}
	});

	function notifyToDelegateTheCheckChange(node, checked) {
		if (node.isLeaf()) {
			this.callDelegates("onLayerCheckChange", [node, checked]);
		} else {
			notifyChackChangeForAllBranch(this, node, checked);
		}
	}

	function notifyChackChangeForAllBranch(me, node, checked) {
		if (!node) {
			return;
		}

		node.set("checked", checked);
		if (node.isLeaf()) {
			me.callDelegates("onLayerCheckChange", [node, checked]);
		} else {
			for (var i=0, l=node.childNodes.length; i<l; ++i) {
				var child = node.childNodes[i];
				notifyChackChangeForAllBranch(me, child, checked);
			}
		}
	}

	function retrieveTargetFolder(layer, root) {
		var targetFolder = null;

		if (layer.isBaseLayer) {
			targetFolder = folderNodeByName(root, EXTERNAL_LAYERS_FOLDER_NAME);
		} else if (layer.CM_geoserverLayer) {
			targetFolder = retrieveGeoserverFolder(root);
		} else {
			targetFolder = folderNodeByName(root, CMDBUILD_LAYERS_FOLDER_NAME);
		}

		return targetFolder;
	}

	function retrieveGeoserverFolder(root) {
		var extarnalServicesFolder = folderNodeByName(root, EXTERNAL_LAYERS_FOLDER_NAME);
		var geoserverFolder = folderNodeByName(extarnalServicesFolder, GEOSERVER_LAYERS_FOLDER_NAME);

		if (!geoserverFolder) {
			geoserverFolder = extarnalServicesFolder.appendChild({
				text: CMDBuild.Translation.administration.modcartography.geoserver.title,
				leaf: false,
				expanded: true,
				folderName: GEOSERVER_LAYERS_FOLDER_NAME,
				checked: true
			});
		}

		return geoserverFolder;
	}

	function folderNodeByName(root, folderName) {
		return root.findChildBy(function(child) {
			return child.get("folderName") == folderName;
		}, null, true);
	}
})();