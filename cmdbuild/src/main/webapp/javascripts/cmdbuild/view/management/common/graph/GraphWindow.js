Ext.define("CMDBuild.Management.GraphWindow", {
	extend: "CMDBuild.PopupWindow",
	resizable: true,

	initComponent: function() {
		var graphParams = {};
		Ext.apply(graphParams, CMDBuild.Config.graph, {
			classid: this.classId,
			objid: this.cardId
		});

		Ext.apply(this, {
			title: CMDBuild.Translation.management.graph.title,
			layout: "border", 
			items: {
				xtype: 'flash',
				url: 'flash/graph.swf',
				flashVars: graphParams,
				region: "center"
			}
		});

		this.callParent(arguments);
	}
});

CMDBuild.Management.showGraphWindow = function(classId, cardId) {
	new CMDBuild.Management.GraphWindow({
		classId: classId,
		cardId: cardId
	}).show();
};