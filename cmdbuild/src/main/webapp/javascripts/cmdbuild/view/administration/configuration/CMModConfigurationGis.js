(function() {
	var tr = CMDBuild.Translation.administration.setup.gis;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationGis", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		alias: "widget.configuregis",
		configFileName: 'gis',
		
		constructor: function() {
			this.items = [{
				xtype: 'xcheckbox',
				name: 'enabled',
				fieldLabel: tr.enable
			},{
				xtype: 'numberfield',
				name:'center.lat',
				decimalPrecision: 6,
				fieldLabel: tr.center_lat
			},{
				xtype: 'numberfield',
				name:'center.lon',
				decimalPrecision: 6,
				fieldLabel: tr.center_lon
			},{
				xtype: 'numberfield',
				name:'initialZoomLevel',
				fieldLabel: tr.initial_zoom,
				minValue : 0,
				maxValue : 25
			}]

			this.callParent(arguments);
		},
		
		afterSubmit: function(conf) {
			CMDBuild.Config.gis = Ext.apply(CMDBuild.Config.gis, conf);
			CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);
			
			if (CMDBuild.Config.gis.enabled) {
				_CMMainViewportController.enableAccordionByName("gis");
			} else {
				_CMMainViewportController.disableAccordionByName("gis");
			}
		}
	});
})();