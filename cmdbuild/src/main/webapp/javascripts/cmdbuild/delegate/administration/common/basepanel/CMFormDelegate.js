/**
 * @class CMDBuild.delegate.administration.common.basepanel.CMFormDelegate
 * 
 * Responds to the events fired from the Form
 */
Ext.define("CMDBuild.delegate.administration.common.basepanel.CMFormDelegate", {
	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 */
	onFormModifyButtonClick: function(form) {},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 */
	onFormRemoveButtonClick: function(form) {},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 */
	onFormSaveButtonClick: function(form) {},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 */
	onFormAbortButtonClick: function(form) {},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 * 
	 * @param {String} action
	 * a string that say if the button is clicked when configured
	 * to activate or deactivate something ["disable" | "enable"]
	 */
	onEnableDisableButtonClick: function(form, action) {}

});
