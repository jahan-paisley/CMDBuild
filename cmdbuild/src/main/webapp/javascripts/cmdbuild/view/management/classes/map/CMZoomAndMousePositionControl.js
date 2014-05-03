CMDBuild.Management.CMZoomAndMousePositionControl = OpenLayers.Class(OpenLayers.Control.MousePosition, {
	zoomLabel : "Zoom",
	positionLabel : "Position",

	redraw : function(evt) {

		var lonLat;

		if (evt == null) {
			this.reset();
			return;
		} else {
			if (evt.type == "mousemove") {
				if (this.lastXy == null
						|| Math.abs(evt.xy.x - this.lastXy.x) > this.granularity
						|| Math.abs(evt.xy.y - this.lastXy.y) > this.granularity) {
					this.lastXy = evt.xy;
					return;
				}
	
				lonLat = this.map.getLonLatFromPixel(evt.xy);
				if (!lonLat) {
					// map has not yet been properly initialized
					return;
				}
				if (this.displayProjection) {
					lonLat.transform(
							this.map.getProjectionObject(),
							this.displayProjection);
				}
				this.lastXy = evt.xy;
				this.lastLonLat = lonLat;
			}
		}

		if (lonLat || this.lastLonLat) {
			var newHtml = this.formatOutput(lonLat || this.lastLonLat);
	
			if (newHtml != this.element.innerHTML) {
				if (this.map && this.map.zoom) {
					this.element.innerHTML = this.zoomLabel + ": "
							+ this.map.zoom + " " + this.positionLabel + ": "
							+ newHtml;
				} else {
					this.element.innerHTML = newHtml;
				}
			}
		}
	},
	/**
	 * APIMethod: activate
	 */
	activate : function() {
		if (OpenLayers.Control.prototype.activate.apply(this, arguments)) {
			this.map.events.register('mousemove', this, this.redraw);
			this.map.events.register('mouseout', this, this.reset);
			this.map.events.register('zoomend', this, this.redraw);
			this.redraw();
			return true;
		} else {
			return false;
		}
	},

	/**
	 * APIMethod: deactivate
	 */
	deactivate : function() {
		if (OpenLayers.Control.prototype.deactivate.apply(this, arguments)) {
			this.map.events.unregister('mousemove', this, this.redraw);
			this.map.events.unregister('mouseout', this, this.reset);
			this.map.events.unregister('zoomend', this, this.redraw);
			this.element.innerHTML = "";
			return true;
		} else {
			return false;
		}
	}
});