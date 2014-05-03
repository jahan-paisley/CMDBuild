(function() {

	Ext.define("CMDBuild.Administration.LayerVisibilityGrid", {
		extend: "CMDBuild.Administration.LayerGrid",
		currentClass: undefined,

		cmCheckColumnReadOnly: false,

		initComponent: function() {
			this.callParent(arguments);
			var me = this;

			this.mon(this, "activate", function() {
				CMDBuild.LoadMask.get().show();
				me.store.load({
					callback: function(records, operation, success) {
						selectVisibleLayers.call(me, me.currentClassId);
						CMDBuild.LoadMask.get().hide();
					}
				});
			}, null);
		},

		onClassSelected: function(s) {
			this.currentClassId = s.id || 0;
			selectVisibleLayers.call(this, this.currentClassId);
		},

		/**
		 * @override
		 */
		onVisibilityChecked: function(cell, recordIndex, checked) {
			var record = this.store.getAt(recordIndex);
			var et = _CMCache.getEntryTypeById(this.currentClassId);

			CMDBuild.LoadMask.get().show();
			CMDBuild.ServiceProxy.saveLayerVisibility({
				params: {
					tableName: et.get("name"),
					layerFullName: record.getFullName(),
					visible: checked
				},
				success: function() {
					_CMCache.onGeoAttributeVisibilityChanged();
					record.setVisibilityForTableName(et.get("name"), checked);
				},
				failure: function() {
					record.set(column.dataIndex, !checked);
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
					record.commit();
				}
			});
		}
	});

	function selectVisibleLayers(tableId) {
		Ext.suspendLayouts();
		var et = _CMCache.getEntryTypeById(tableId);
		var s = this.store;
		var columnDataIndex = this.getVisibilityColDataIndex();

		s.each(function(record) {
			record.set(columnDataIndex, record.isVisibleForEntryType(et));
			record.commit();
		});
		Ext.resumeLayouts();
	};
})();