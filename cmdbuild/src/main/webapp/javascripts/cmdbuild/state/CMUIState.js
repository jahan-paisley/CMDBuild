(function() {
	Ext.define("CMDBuild.state.UIStateDelegate", {
		onFullScreenChangeToGridOnly: Ext.emptyFn,
		onFullScreenChangeToFormOnly: Ext.emptyFn,
		onFullScreenChangeToOff: Ext.emptyFn
	});

	Ext.define("CMDBuild.state.UIState", {

		FULLSCREEN_MODES: {
			grid: "grid",
			form: "form",
			off: "off"
		},

		constructor: function() {
			this.callParent(arguments);

			this.fullScreenMode = this.FULLSCREEN_MODES.off;
			this.delegates = [];
		},

		onlyGrid: function() {
			this.fullScreenMode = this.FULLSCREEN_MODES.grid;
			this.callForDelegates("onFullScreenChangeToGridOnly");
		},

		onlyForm: function() {
			this.fullScreenMode = this.FULLSCREEN_MODES.form;
			this.callForDelegates("onFullScreenChangeToFormOnly");
		},

		fullScreenOff: function() {
			this.fullScreenMode = this.FULLSCREEN_MODES.off;
			this.callForDelegates("onFullScreenChangeToOff");
		},

		onlyFormIfFullScreen: function() {
			if (this.fullScreenMode != this.FULLSCREEN_MODES.off) {
				this.onlyForm();
			}
		},

		onlyGridIfFullScreen: function() {
			if (this.fullScreenMode != this.FULLSCREEN_MODES.off) {
				this.onlyGrid();
			}
		},

		isFullscreenOff: function() {
			return this.fullScreenMode == this.FULLSCREEN_MODES.off;
		},

		isOnlyGrid: function() {
			return this.fullScreenMode == this.FULLSCREEN_MODES.grid;
		},

		isOnlyForm: function() {
			return this.fullScreenMode == this.FULLSCREEN_MODES.form;
		},

		addDelegate: function(d) {
			if (d) {
				this.delegates.push(d);
			}
		},

		callForDelegates: function(methodName, params) {
			for (var i=0; i<this.delegates.length; ++i) {
				var d = this.delegates[i];
				if (d && typeof d[methodName] == "function") {
					d[methodName].apply(d, params || []);
				}
			}
		}
	});

	_CMUIState = new CMDBuild.state.UIState();
})();