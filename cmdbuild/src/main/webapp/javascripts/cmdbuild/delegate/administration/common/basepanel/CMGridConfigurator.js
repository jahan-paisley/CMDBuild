/**
 * @class CMDBuild.delegate.administration.common.basepanel.CMGridConfigurator
 * 
 * Object to delegate the grid configuration.
 * It must be able to:
 * 	get the store
 * 	get the column configuration
 */
Ext.define("CMDBuild.delegate.administration.common.basepanel.CMGridConfigurator", {
	/**
	 * @return {Ext.data.Store} store to use for the grid
	 */
	getStore: function() {},

	/**
	 * @return {Ext.grid.column.Column[]} columns to use for the grid
	 */
	getColumns: function() {}
});