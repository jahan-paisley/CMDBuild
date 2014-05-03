(function() {

	var col_tr = CMDBuild.Translation.management.modcard.history_columns;

	Ext.define("CMDBuild.view.management.workflow.CMActivityHistoryTab", {
		extend: "CMDBuild.view.management.classes.CMCardHistoryTab",

		getGridColumns: function() {
			return this.callParent(arguments).concat([
				{header: col_tr.activity_name, width: 40, sortable: false, dataIndex: "Code", flex:1},
				{header: col_tr.performer, sortable: false, dataIndex: "Executor", flex:1}
			]);
		},

		isFullVersion: function() {
			return !_CMUIConfiguration.isSimpleHistoryModeForProcess();
		},

		getStoreFields: function() {
			return this.callParent(arguments).concat([
				"Code",
				"Executor"
			]);
		}
	});

})();
