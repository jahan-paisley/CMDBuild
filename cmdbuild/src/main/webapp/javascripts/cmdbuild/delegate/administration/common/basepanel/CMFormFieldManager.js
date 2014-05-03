/**
 * @class CMDBuild.view.administration.common.CMFormFiledBuilder
 * 
 * It must be able to
 * generate the fields
 * set the values of the fields from a given Ext.data.Model
 * get the field values as a key/value map
 */

Ext.define("CMDBuild.delegate.administration.common.basepanel.CMFormFiledsManager", {

	/**
	 * @return {array} an array of Ext.component to use as form items
	 */
	build: function() {},

	/**
	 * 
	 * @param {Ext.data.Model} record
	 * the record to use to fill the field values
	 */
	loadRecord: function(record) {},

	/**
	 * @return {object} values
	 * a key/value map with the values of the fields
	 */
	getValues: function() {},

	/**
	 * clean up all the fields
	 */
	reset: function() {}
});
