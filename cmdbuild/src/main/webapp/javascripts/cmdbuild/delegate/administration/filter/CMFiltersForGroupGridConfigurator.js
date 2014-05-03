Ext.define("CMDBuild.delegate.administration.common.group.CMFiltersForGroupGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {
		if (this.store == null) {
			this.store = _CMProxy.Filter.newGroupStore();
		}

		return this.store;
	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		var columns = this.callParent(arguments);
		columns.push({
			dataIndex: _CMProxy.parameter.ENTRY_TYPE,
			header: CMDBuild.Translation.targetClass,
			flex: 1
		});

		return columns;
	}
});