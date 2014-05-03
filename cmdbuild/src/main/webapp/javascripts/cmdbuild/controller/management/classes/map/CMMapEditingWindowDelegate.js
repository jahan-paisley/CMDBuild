(function(){
	Ext.define("CMDBuild.controller.management.classes.map.CMMapEditingWindowDelegate", {
		extend: "CMDBuild.view.management.map.CMMapEditingToolsWindowDelegate",

		constructor: function(master) {
			this.master = master;
			this.editingControls = {};
			this.currentEditLayer = null;
			this.callParent(arguments);
		},

		buildEditControls: function buildEditControls(layer) {
			if (layer.editLayer) {
				if (this.editingControls[layer.editLayer.name]) {
					return;
				}

				var geoAttribute = layer.geoAttribute,
					creation = buildCreationControl(geoAttribute.type, layer.editLayer),
					transform = buildTransformControl(layer.editLayer);

				this.editingControls[layer.editLayer.name] = {
					creation: creation,
					transform: transform
				};

				this.master.map.addControls([creation, transform]);
				this.master.mapPanel.addLayerToEditingWindow(layer);
			}
		},

		destroyEditControls: function destroyEditControls(layer) {
			if (layer.editLayer) {
				if (this.master.mapState.isAUsedGeoAttribute(layer.geoAttribute)) {
					return;
				}

				var name = layer.editLayer.name;
				for (var control in this.editingControls[name]) {
					this.master.map.removeControl(this.editingControls[name][control]);
					delete this.editingControls[name][control];
				}

				delete this.editingControls[name];
			}
		},

		addFeatureButtonHasBeenToggled: function onAddFeatureButtonToggle(toggled) {
			if (toggled) {
				activateControl(this, this.currentEditLayer.name, "creation");
				deactivateControl(this, this.currentEditLayer.name, "transform");
			} else {
				deactivateControl(this, this.currentEditLayer.name, "creation");
				activateControl(this, this.currentEditLayer.name, "transform");
			}
		},

		removeFeatureButtonHasBeenClicked: function onRemoveFeatureButtonClick() {
			if (this.currentEditLayer) {
				this.currentEditLayer.removeAllFeatures();
			}
		},

		geoAttributeMenuItemHasBeenClicked: function activateEditControls(editLayer) {
			this.deactivateEditControls();

			this.currentEditLayer = editLayer;
			this.activateTransformConrol(editLayer.name);

			var editFeature = editLayer.features[0];

			if (editFeature) {
				setTransformControlFeature(this, editLayer.name, editFeature);
				editLayer.drawFeature(editFeature, "select");
			}
		},

		activateTransformConrol: function(layerName) {
			activateControl(this, layerName, "transform");
		},

		deactivateEditControls: function deactivateEditControls() {
			var controls = this.editingControls;
			for (var layer in controls) {
				for (var control in controls[layer]) {
					controls[layer][control].deactivate();
				}
			}
		}
	});

	function buildTransformControl(layer) { 
		var c = new OpenLayers.Control.ModifyFeature(layer);
		c.mode = OpenLayers.Control.ModifyFeature.DRAG
		|= OpenLayers.Control.ModifyFeature.ROTATE
		|= OpenLayers.Control.ModifyFeature.RESIZE;
		return c;
	};

	function buildCreationControl(type, layer) {
		var controlBuilders = {
			POINT: function(layer) {
				return new OpenLayers.Control.DrawFeature(layer, OpenLayers.Handler.Point);
			},
			POLYGON: function(layer) {
				return new OpenLayers.Control.DrawFeature(layer, OpenLayers.Handler.Polygon);
			},
			LINESTRING: function(layer) {
				return new OpenLayers.Control.DrawFeature(layer, OpenLayers.Handler.Path);
			}
		};
		return controlBuilders[type](layer);
	};

	function setTransformControlFeature(me, layerId, feature) {
		if (feature) {
			var l = me.editingControls[layerId];
			if (l["transform"]) {
				l["transform"].selectFeature(feature);
			}
		}
	};

	function activateControl(me, layerId, controlName) {
		var l = me.editingControls[layerId];
		if (l[controlName]) {
			l[controlName].activate();
		}
	};

	function deactivateControl(me, layerId, controlName) {
		var l = me.editingControls[layerId];
		if (l[controlName]) {
			l[controlName].deactivate();
		}
	};
})();