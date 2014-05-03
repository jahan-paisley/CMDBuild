(function() {

	Ext.define("CMDBuild.controller.management.dataView.CMModCardController", {
		extend: "CMDBuild.controller.management.common.CMModController",

		mixins: {
			cmModSqlDataViewDelegate: "CMDBuild.view.management.dataView.CMModSQLDataViewDelegate"
		},

		constructor: function() {
			this.callParent(arguments);
			this.view.addDelegate(this);
		},

		onViewOnFront: function(node) {
			if (node) {
				var me = this;
				var sourceFunction = node.get("sourceFunction");
				var outputConfiguration = _CMCache.getDataSourceOutput(sourceFunction);

				var store = getStore(outputConfiguration, sourceFunction); //
				this.view.configureGrid( //
						store,
						getColumns(outputConfiguration)
					);

				this.view.updateTitleForEntry(node);
				store.load({
					callback: function (records, operation, success) {
						if (records 
								&& records.length > 0) {

							me.view.selectRecord(records[0]);
						}
					}
				});
			}
		},
	
		// as CMModSqlDataViewDelegte

		onModSQLDataViewGridSelected: function(panel, record) {
			this.view.showRecordData(record);
		}
	});

	function getStore(outputConfiguration, functionName) {
		return new Ext.data.Store({
			fields: outputConfiguration,
			pageSize: _CMUtils.grid.getPageSize(),
			remoteSort: true,
			autoLoad: false,
			proxy: {
				type: "ajax",
				url: "services/json/management/modcard/getsqlcardlist",
				reader: {
					root: "cards",
					type: "json",
					totalProperty: "results",
				},
				extraParams: {
					"function": functionName
				}
			}
		});
	}

	function getColumns(outputConfiguration) {
		var columns = [];

		for (var i=0, l=outputConfiguration.length; i<l; ++i) {
			var attribute = outputConfiguration[i];
			columns.push({
				header: attribute.name,
				dataIndex: attribute.name,
				flex: 1
			});
		}

		return columns;
	}
})();