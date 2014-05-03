(function() {
	Ext.define("CMDBuild.view.management.common.widgets.CMPing", {
		extend: "Ext.panel.Panel",
		frame: false,
		border: false,

		initComponent: function() {
			this.autoScroll = true;
			this.callParent(arguments);
		},

		statics : {
			WIDGET_NAME: ".Ping"
		},

		showPingResult: function(r) {
			this.removeAll();
			this.add({
				html: "<pre>" + r || ""+ "</pre>",
				bodyCls: "cm-pre",
				frame: false,
				border: false
			});
		}
	});
})();