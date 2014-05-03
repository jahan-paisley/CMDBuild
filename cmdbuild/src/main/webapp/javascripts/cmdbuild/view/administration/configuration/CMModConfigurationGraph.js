(function() {
	var tr = CMDBuild.Translation.administration.setup.graph;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationGraph", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		configFileName: 'graph',
		
		constructor: function() {
			this.items = [{
				xtype: 'xcheckbox',
				name: 'enabled',
				fieldLabel: CMDBuild.Translation.administration.setup.graph.enabled
			}, {
				xtype : 'numberfield',
				fieldLabel : tr.baseLevel,
				allowBlank : false,
				minValue : 1,
				maxValue : 5,
				name : 'baseLevel'
			}, {
				xtype : 'numberfield',
				fieldLabel : tr.extensionMaximumLevel,
				allowBlank : false,
				minValue : 1,
				maxValue : 5,
				name : 'extensionMaximumLevel'
			}, {
				xtype : 'numberfield',
				fieldLabel : tr.clusteringThreshold,
				allowBlank : false,
				minValue : 2,
				maxValue : 20,
				name : 'clusteringThreshold'
			} ]
			this.callParent(arguments);
		}
	});
})();