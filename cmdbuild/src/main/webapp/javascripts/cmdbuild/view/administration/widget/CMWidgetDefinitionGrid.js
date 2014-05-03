(function() {
	var fields = CMDBuild.model.CMWidgetDefinitionModel._FIELDS;
	Ext.define("CMDBuild.view.administration.widget.CMWidgetDefinitionGrid", {
		extend: "Ext.grid.Panel",

		initComponent: function() {
			var tr = CMDBuild.Translation.administration.modClass.widgets;

			this.store = new Ext.data.Store({
				model: "CMDBuild.model.CMWidgetDefinitionModel",
				data: []
			});

			this.columns = [{
				header : tr.commonFields.type,
				dataIndex : fields.type,
				flex: 1,
				renderer: function(value) {
					return tr[value].title;
				}
			},{
				header: tr.commonFields.buttonLabel,
				dataIndex: fields.label,
				flex: 2
			},
			new Ext.ux.CheckColumn({
				header : tr.commonFields.active,
				dataIndex : fields.active,
				width: 90,
				cmReadOnly: true
			})];

			this.callParent(arguments);
		},

		count: function() {
			return this.store.count();
		},

		addRecord: function(record, selectAfter) {
			this.removeRecordWithId(record.get("id"));
			var addedRec = this.store.add(record);
			if (selectAfter) {
				this.getSelectionModel().select(addedRec);
			}
		},

		removeRecordWithId: function(recordId) {
			var record = this.store.getById(recordId);
			if (record != null) {
				this.store.remove(record);
			}
		},

		removeAllRecords: function() {
			this.store.removeAll();
		},

		clearSelection: function() {
			this.getSelectionModel().deselectAll();
		}
	});
})();