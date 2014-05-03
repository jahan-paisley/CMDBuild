Ext.define("CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMGridConfigurator",

	/**
	 * @return a Ext.data.Store to use for the grid
	 */
	getStore: function() {
		
	},

	/**
	 * @return an array of Ext.grid.column.Column to use for the grid
	 */
	getColumns: function() {
		return [{
			header: CMDBuild.Translation.administration.modClass.attributeProperties.name,
			dataIndex: _CMProxy.parameter.NAME,
			flex: 1
		}, {
			header: CMDBuild.Translation.administration.modClass.attributeProperties.description,
			dataIndex: _CMProxy.parameter.DESCRIPTION,
			flex: 1
		}];
	}
});
