/**
 * This class read the size in percentage in the configuration file and create a modal popup-window 
 * 
 * @class CMDBuild.PopupWindow
 * @extends Ext.Window
 */

Ext.define("CMDBuild.PopupWindow", {
	extend: "Ext.window.Window",
	modal: true,
	layout: 'fit',
	resizable: false,
	defaultSize: 0.80,

	initComponent: function() {
		if (!this.autoHeight) {
			var percentualHeight;
			var configHeight = CMDBuild.Config.cmdbuild.popuppercentageheight;
			if (configHeight) {
				percentualHeight = configHeight/100;
			} else {
				percentualHeight = this.defaultSize;
			}
			this.height = Ext.getBody().getHeight() * percentualHeight;
		}

		if (!this.autoWidth) {
			var percentualWidth;
			var configWidth = CMDBuild.Config.cmdbuild.popuppercentagewidth;
			if (configWidth) {
				percentualWidth = configWidth/100;
			} else {
				percentualWidth = this.defaultSize;
			}
			this.width = Ext.getBody().getWidth() * percentualWidth;
		} else {
			this.autoWidth = false;
			this.y = 10000;
		}

		this.callParent(arguments);
	}
});
