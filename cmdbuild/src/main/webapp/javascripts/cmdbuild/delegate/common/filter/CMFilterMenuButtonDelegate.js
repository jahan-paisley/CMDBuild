Ext.define("CMDBuild.delegate.common.filter.CMFilterMenuButtonDelegate", {
	/**
	 * Called by the CMFilterMenuButton when click
	 * to the clear button
	 * 
	 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
	 * the button that calls the delegate
	 */
	onFilterMenuButtonClearActionClick: Ext.emptyFn,

	/**
	 * Called by the CMFilterMenuButton when click
	 * to the new button
	 * 
	 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
	 * the button that calls the delegate
	 */
	onFilterMenuButtonNewActionClick: Ext.emptyFn,

	/**
	 * Called by the CMFilterMenuButton when click
	 * to on the apply icon on a row of the picker
	 * 
	 * @param {object} filter, the filter to apply
	 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
	 * the button that calls the delegate
	 */
	onFilterMenuButtonApplyActionClick: Ext.emptyFn,

	/**
	 * Called by the CMFilterMenuButton when click
	 * to on the modify icon on a row of the picker
	 * 
	 * @param {object} filter, the filter to modify
	 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
	 * the button that calls the delegate
	 */
	onFilterMenuButtonModifyActionClick: Ext.emptyFn,

	/**
	 * Called by the CMFilterMenuButton when click
	 * to on the save icon on a row of the picker
	 * 
	 * @param {object} filter, the filter to save
	 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
	 * the button that calls the delegate
	 */
	onFilterMenuButtonSaveActionClick: Ext.emptyFn,

	/**
	 * Called by the CMFilterMenuButton when click
	 * to on the modify icon on a row of the picker
	 * 
	 * @param {object} filter, the filter to modify
	 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
	 * the button that calls the delegate
	 */
	onFilterMenuButtonCloneActionClick: Ext.emptyFn,

	/**
	 * Called by the CMFilterMenuButton when click
	 * to on the remove icon on a row of the picker
	 * 
	 * @param {object} filter, the filter to remove
	 * @param {CMDBuild.view.management.common.filter.CMFilterMenuButton} button
	 * the button that calls the delegate
	 */
	onFilterMenuButtonRemoveActionClick: Ext.emptyFn,
});

Ext.define("CMDBuild.delegate.common.filter.CMRuntimeParameterWindowDelegate", {
	/**
	 * Called by the CMRuntimeParameter when click
	 * to on the save button
	 * 
	 * @param {CMDBuild.view.management.common.filter.CMRuntimeParameterWindow} the
	 * window that calls this method
	 * @param {object} filter, the filter used to configure the CMRuntimeParameterWindow
	 */
	onRuntimeParameterWindowSaveButtonClick: function(runtimeParameterWindow, filter) {}
});