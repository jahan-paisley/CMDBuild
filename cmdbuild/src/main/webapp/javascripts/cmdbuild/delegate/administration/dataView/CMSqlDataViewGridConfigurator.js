Ext.define("CMDBuild.delegate.administration.common.dataview.CMSqlDataViewGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {
		if (this.store == null) {
			this.store = _CMProxy.dataView.sql.store();
		}

		return this.store;
	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		var columns = this.callParent(arguments);
		columns.push({
			header: CMDBuild.Translation.administration.modDashboard.charts.fields.dataSource,
			dataIndex: _CMProxy.parameter.SOURCE_FUNCTION,
			flex: 1
		});

		return columns;
	}
});