Ext.define("CMDBuild.bim.management.view.CMBimPlayerLayersDelegate", {
	/*
	 * @param {CMDBuild.bim.management.view.CMBimPlayerLayers} bimLayerPanel the
	 * layers panel that call the method @param {String} ifcLayerName the name
	 * of the layer for which the check is changed @param {Boolean} checked the
	 * current value of the check
	 */
	onLayerCheckDidChange: function(bimLayerPanel, ifcLayerName, checked) {
	}
});

Ext.define("CMDBuild.bim.management.view.CMBimPlayerLayers", {
	extend: "Ext.grid.Panel",

	initComponent: function() {
		this.store = Ext.create('Ext.data.Store', {
			fields: ['id', 'checked', 'description'],
			data: []
		});

		var me = this;

		this.columns = [
				{
					xtype: "checkcolumn",
					dataIndex: 'checked',
					fixed: true,
					header: '&nbsp',
					hideable: false,
					listeners: {
						scope: this,
						checkchange: function(column, rowIndex, checked) {
							var record = me.store.getAt(rowIndex);
							if (record) {
								me.delegate.onLayerCheckDidChange(me, record
										.get("id"), checked);
							}
						}
					},
					menuDisabled: true,
					sortable: false,
					width: 30
				}, {
					flex: 1,
					text: 'Name',
					dataIndex: 'description'
				}];

		this.callParent(arguments);
	},

	loadLayers: function(data) {
		this.store.loadData(data);
	},

	selectLayer: function(layerName) {
		var recordIndex = this.store.find("id", layerName);
		if (recordIndex >= 0) {
			var record = this.store.getAt(recordIndex);
			record.set("checked", true);
			record.commit();
		}
	}

});