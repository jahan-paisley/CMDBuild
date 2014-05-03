(function() {
	Ext.define("CMDBuild.view.management.common.widgets.CMWidgetsWindow", {
		extend: "CMDBuild.PopupWindow",

		initComponent: function() {
			this.widgetsToAdd = {};
			this.widgetsContainer = new Ext.panel.Panel({
				layout: "card",
				activeItem: 0,
				hideMode: "offsets",
				border: false,
				frame: false,
				items: [{}]
			});

			var me = this;
			Ext.apply(this, {
				items: [this.widgetsContainer],
				buttonAlign: "center",
				buttons: [{
					text: CMDBuild.Translation.common.buttons.close,
					_cmNotRemoveMe: true, // flag to identiry the button when clean the buttons bar
					handler: function() {
						me.hide();
					}
				}]
			});

			this.callParent(arguments);
		},

		showWidget: function(widget, title) {
			this.setTitle(title);
			this.show();
			if (this.widgetsToAdd[widget.id]) {
				this.widgetsContainer.add(widget);
				delete this.widgetsToAdd[widget.id];
			}
			this.widgetsContainer.layout.setActiveItem(widget.id);
			this.removeExtraButtons();
			if (widget.getExtraButtons) {
				var extraButtons = widget.getExtraButtons();
				this.addExtraButtons(extraButtons);
			}
			if (widget.buttonLabel) {
				this.setTitle(widget.buttonLabel)
			}
		},

		addWidgt: function(w) {
			this.widgetsToAdd[w.id] = w;
		},

		destroy: function() {
			this.widgetsContainer.removeAll(autodestroy = true);
			delete this.widgetsToAdd;
			this.callParent(arguments);
		},

		addExtraButtons: function(extraButtons) {
			var bar = this.getButtonBar();
			if (bar) {
				bar.insert(0, extraButtons);
			}
		},

		removeExtraButtons: function() {
			var bar = this.getButtonBar();
			if (bar) {
				bar.items.each(function(i) {
					if (i._cmNotRemoveMe) {
						return;
					} else {
						bar.remove(i);
					}
				})
			}
		},

		// I have not found a clean solution to have the buttons bar,
		// generated in a panel with the "buttons" configuration object
		getButtonBar: function() {
			var docks = this.getDockedItems();
			for (var i=0, l=docks.length; i<l; ++i) {
				var d = docks[i];
				if (d.dock == "bottom") {
					return d;
				}
			}
			return null;
		},

		// if close the window the content is destroyed
		// so substitute close with hide for this window
		// (close is called when press the ESC key or clicking
		// the X top-right button of the window)
		close: function() {
			this.hide();
		}
	});
	Ext.define("CMDBuild.view.management.common.widgets.CMWidgetsWindowPopup", {
		extend: "CMDBuild.view.management.common.widgets.CMWidgetsWindow",
		defaultSizeW: 0.90,
		defaultSizeH: 0.80,

		initComponent: function() {
			this.callParent(arguments);
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;
		}
	});
})();