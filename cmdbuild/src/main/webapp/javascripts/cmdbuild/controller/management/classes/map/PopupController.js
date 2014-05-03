(function() {
	CMDBuild.Management.PopupController = OpenLayers.Class(CMDBuild.Management.CMSelectFeatureController, {
		initialize: function(layers, options) {
			layers = layers || [];
			options = options || {};
			
			this.hover = true;
			this.highlightOnly = true;
			this.renderIntent = "temporary";
			this.overFeature = onFeatureOver;
			
			// to not show the popup after the delay
			// if the mouse is not on the feature
			this.outFeature = function(f) {
				f.CM_over = false;
			};

			CMDBuild.Management.CMSelectFeatureController.prototype.initialize.apply(this, [layers, options]);
		}
	});

	function onFeatureOver(f) {
		f.CM_over = true;

		function showInfoBaloon(f) {
			if (!f.CM_over || f.CM_busy) {
				return;
			}

			if (f.CM_card) {
				buildPopUp(f);
			} else {
				f.CM_busy = true;
				CMDBuild.ServiceProxy.card.get({
					params: {
						Id: f.attributes.master_card,
						IdClass: f.attributes.master_class
					},
					scope: this,
					success: function(response, options, decoded) {
						f.CM_busy = false;
						f.CM_card = decoded.card;
						f.CM_card_attributes = decoded.attributes;
						buildPopUp(f);
					}
				});
			}
		}

		// defer the call to deny a pop-up explosion ;) 
		Ext.Function.createDelayed(showInfoBaloon, 1000, this, arguments)();
		return true;
	}

	function buildPopUp(f) {
		var g = f.geometry;

		if (g) {
			var centeroid = g.getCentroid();

			if (f.layer) {
				var popup = new OpenLayers.Popup.FramedCloud(
						"cloud_"+f.id, 
						new OpenLayers.LonLat(centeroid.x, centeroid.y), // TODO detect mouse position
						null,
						buildPopupContent(f),
						null,
						closeButton = true
					);
				popup.minSize = new OpenLayers.Size(50, 200);
				popup.panMapIfOutOfView = false;
				f.layer.map.addPopup(popup, exlusive=true);
			}
		}
	}

	function buildPopupContent(f) {
		var card = f.CM_card,
			attributes = f.CM_card_attributes,
			htmlTemplate = "<div class=\"map_cloud_content\">" +
				"<p class=\"map_cloud_title\">{0}</p>" +
				"{1}</div>",
			itemTemplate = "<p class=\"map_cloud_item\"><strong>{0}:</strong>"
			+ "<span>{1}</span></p>",
			items = "";

		for (var i=0, l=attributes.length; i<l; ++i) {
			var at = attributes[i];
			if (at.isbasedsp) {
				var attrValue = card[at.name] || "-";
				if (typeof attrValue == "object") {
					attrValue = attrValue.description;
				}
				items += Ext.String.format(itemTemplate, at.description, attrValue);
			}
		}

		return Ext.String.format(htmlTemplate, card.IdClass_value, items);
	}
})();