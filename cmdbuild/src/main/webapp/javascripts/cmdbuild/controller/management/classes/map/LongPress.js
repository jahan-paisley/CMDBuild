OpenLayers.Handler.LongPress = OpenLayers.Class(OpenLayers.Handler.Click, {

	longPressTime: 600, // milliseconds

	// add the time-stamp to the info
	getEventInfo : function(evt) {
		var out = OpenLayers.Handler.Click.prototype.getEventInfo.apply(this, arguments);
		out.timeStamp = new Date().getTime();

		return out;
	},

	mousedown: function(evt) {

		var me = this;
		me._longPress = true;

		window.setTimeout(
			function() {
				if (!me.down 
						|| !me._longPress
						|| !me.passesTolerance(evt)) {
					return;
				}

				me.callback('longpress', [evt]);
			},
			me.longPressTime
		);

		return OpenLayers.Handler.Click.prototype.mousedown.apply(this, arguments);
	},

	mouseup: function(evt) {
		this._longPress = false;
		return OpenLayers.Handler.Click.prototype.mouseup.apply(this, arguments);
	}
});

OpenLayers.Control.LongPress = OpenLayers.Class(OpenLayers.Control, {
	initialize : function(options) {
		this.handlerOptions = OpenLayers.Util.extend({},
				this.defaultHandlerOptions);

		OpenLayers.Control.prototype.initialize.apply(this, arguments);

		this.handler = new OpenLayers.Handler.LongPress(this, {
			'longpress' : this.onLongPress
		}, this.handlerOptions);
	},

	// implement it on creation
	onLongPress: function(e) {}
});