Ext.define("CMDBuild.field.CMToggleButtonToShowReferenceAttributes", {
	extend: "Ext.button.Button",
	subFields: [], // passed on instantiation
	enableToggle: true,
	iconCls: "down",
	cls: "clearButtonBGandBorder",

	initComponent: function() {
		for (var i=0, f=null; i<this.subFields.length; ++i) {
			f=this.subFields[i];
			if (f) {
				f.hide();
			}
		}
	},

	listeners: {
		toggle: function(b, pressed) {
			if (pressed) {
				b.setIconCls("up-hover");
			} else {
				b.setIconCls("down-hover");
			}

			for (var i=0, f=null; i<b.subFields.length; ++i) {
				f=b.subFields[i];
				if (f) {
					f.setVisible(pressed);
				}
			}
		},

		mouseover: function(b) {
			if (b.iconCls == "down") {
				b.setIconCls("down-hover");
			} else {
				b.setIconCls("up-hover");
			}
		},

		mouseout: function(b) {
			if (b.iconCls == "down-hover") {
				b.setIconCls("down");
			} else {
				b.setIconCls("up");
			}
		}
	}
});