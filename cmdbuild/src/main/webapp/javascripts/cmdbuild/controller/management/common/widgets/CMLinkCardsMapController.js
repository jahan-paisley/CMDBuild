(function() {
	var TRUE = "1";

	Ext.define("CMDBuild.controller.management.workflow.widgets.CMLinkCardsMapController", {
		mixins: {
			observable: "Ext.util.Observable"
		},
		/*
		 * conf is an object like
		 * {
				view: this.view.mapPanel, 
				ownerController: this,
				model: this.model,
				widgetConf: this.widgetConf
			}
		 * 
		 */
		constructor: function(conf) {
			Ext.apply(this, conf);

			this.classId = this.widgetConf.ClassId;

			var multipleSelect = this.NoSelect!=TRUE && !this.SingleSelect,
				referredEntryType = _CMCache.getEntryTypeById(this.classId);

			this.lastSelection = null;

			this.mon(this.view, "addlayer", onLayerAdded, this);
			this.mon(this.view, "removelayer",onLayerRemoved,this);

			this.mon(this.model, "select", this.selectByCardId, this);
			this.mon(this.model, "deselect", this.deselectByCardId, this);

			this.mon(this.view, "render", function() {
				this.map = this.view.getMap();

				this.selectControl = new CMDBuild.Management.CMSelectFeatureController([], {
					hover: false,
					toggle: true,
					clickout: false,
					multiple: multipleSelect,
					multipleKey: "shiftKey"
				});
	
				this.map.addControl(this.selectControl);
				this.selectControl.activate();
	
				this.popupControl = new CMDBuild.Management.PopupController();
				this.map.addControl(this.popupControl);
				this.popupControl.activate();

				if (referredEntryType) {
					this.map.update(referredEntryType, withEditLayer = false);
				}
			}, this, {single: true});
		},

		buildEditControls: function() {
			_debug("Build edit controls");
		},

		setSelectableLayers: function() {
			_debug("setSelectableLayers");
		},

		selectByCardId: function(cardId) {
			if (this.map) {
				var feature = getFeatureByMasterCard.call(this, cardId);

				if (feature != null) {
					this.selectControl.select(feature);
				}
			}
		},

		deselectByCardId: function(cardId) {
			if (this.map) {
				var feature = getFeatureByMasterCard.call(this, cardId);

				if (feature != null) {
					this.selectControl.unselect(feature);
				}
			}
		},

		getLastSelection: function() {
			return this.lastSelection;
		},

		centerMapOnSelection: function() {
			var me = this;

			if (this.model.hasSelection()) {
				var ss = this.model.getSelections(),
					s = ss[0];
	
				function onSuccess(resp, req, feature) {
					// the card could have no feature
					if (feature.properties) {
						me.map.centerOnGeometry(feature);
					}
				};
	
				CMDBuild.ServiceProxy.getFeature(this.classId, s, onSuccess);
			}
		}
	});

	function onLayerAdded(params) {
		var layer = params.layer,
		me = this;

		if (layer == null || !layer.CM_Layer) {
			return;
		}

		this.popupControl.addLayer(layer);
		this.selectControl.addLayer(layer);

		layer.events.on({
			"featureselected": onFeatureSelected,
			"featureunselected": onFeatureUnselected,
			"featureadded": onFeatureAdded,
			scope: me
		});
	}

	function onLayerRemoved(params) {
		var layer = params.layer,
		me = this;

		if (layer == null || !layer.CM_Layer) {
			return;
		}

		this.popupControl.addLayer(layer);
		this.selectControl.removeLayer(layer);

		layer.events.un({
			"featureselected": onFeatureSelected,
			"featureunselected": onFeatureUnselected,
			"featureadded": onFeatureAdded,
			scope: me
		});
	}

	function onFeatureSelected(params) {
		var cardId = params.feature.attributes.master_card;
		this.model.select(cardId);
		this.lastSelection = cardId;
	}

	function onFeatureUnselected(params) {
		var cardId = params.feature.attributes.master_card;
		this.model.deselect(cardId);
		if (this.lastSelection == cardId) {
			this.lastSelection = null;
		}
	}

	function onFeatureAdded(p) {
		var master_card = p.feature.attributes.master_card;
		if (master_card && this.model.isSelected(master_card)) {
			this.selectControl.select(p.feature);
			centerMapOnLoadedFeature.call(this, p.feature);
		}
	}

	function centerMapOnLoadedFeature(feature) {
		var center = feature.geometry.getCentroid();
		var lonLat = new OpenLayers.LonLat(center.x, center.y);
		this.map.setCenter(lonLat);
	}

	function centerMapOnCardId(cardId) {
		var me = this;

		function onSuccess(resp, req, feature) {
			// the card could have no feature
			if (feature.geometry) {
				me.map.centerOnGeometry(feature);
			}
		};

		CMDBuild.ServiceProxy.getFeature(this.classId, cardId, onSuccess);
	}

	function getFeatureByMasterCard(id) {
		return this.map.getFeatureByMasterCard(id);
	}

	function featureInLayerSelection(feature) {
		var layer = feature.layer;
		var selections = layer.selectedFeatures;
		
		for (var i=0, l=selections.length; i<l; ++i) {
			var f = selections[i];
			if (f.attributes.master_card == feature.attributes.master_card) {
				return true;
			}
		}
		return false;
	}
})();