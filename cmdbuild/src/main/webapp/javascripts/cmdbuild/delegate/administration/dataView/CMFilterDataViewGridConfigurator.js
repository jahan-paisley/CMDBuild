Ext.define("CMDBuild.delegate.administration.common.dataview.CMFilterDataViewGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {
		if (this.store == null) {
			this.store = _CMProxy.dataView.filter.store();
		}

		return this.store;
	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		var columns = this.callParent(arguments);
		columns.push({
			dataIndex: _CMProxy.parameter.SOURCE_CLASS_NAME,
			header: CMDBuild.Translation.targetClass,
			flex: 1
		});

		return columns;
	}
});