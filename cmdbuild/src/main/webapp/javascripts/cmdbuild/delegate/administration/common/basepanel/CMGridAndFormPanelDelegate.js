/**
 * Give a base implementation of the delegates
 * 	CMDBuild.delegate.administration.common.basepanel.CMFormDelegate
 * 	CMDBuild.delegate.administration.common.basepanel.CMGridDelegate
 * 
 * and add his own method that are called form a CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel
 */

Ext.define("CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate", {

	mixins: {
		formDelegate: "CMDBuild.delegate.administration.common.basepanel.CMFormDelegate",
		gridDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridDelegate"
	},

	constructor: function(view) {
		this.view = view;
		view.addDelegate(this);
		view.form.addDelegate(this);
		view.grid.addDelegate(this);
	},

	selectFirstRow: function() {
		if (this.view.grid) {
			var store = this.view.grid.getStore();
			var sm = this.view.grid.getSelectionModel();

			if (store && sm) {
				var count = store.getTotalCount();
				if (count>0) {
					sm.select(store.getAt(0));
				}
			}
		}
	},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel} panel
	 * called from the panel after a click on the add button
	 */
	onGridAndFormPanelAddButtonClick: function(panel) {
		var all = true;
		this.record = null;
		this.fieldManager.reset();
		panel.enableModify(all);
		panel.clearSelection();
		_CMCache.initAddingTranslations();
	},

	/**
	 * called after the save button click
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	onGridAndFormPanelSaveButtonClick: function(form) {},

	/**
	 * called after the confirmation of a remove
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	onGridAndFormPanelRemoveConfirmed: function(form) {},

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 * the form that call the function
	 * 
	 * @param {String} action
	 * a string that say if the button is clicked when configured
	 * to activate or deactivate something ["enable" | "disable"]
	 */
	onEnableDisableButtonClick: function(form, action) {},

	// as form delegate

	onFormModifyButtonClick: function(form) {
		this.view.enableModify();
		_CMCache.initModifyingTranslations();
	},

	onFormRemoveButtonClick: function(form) {
		var me = this;
		Ext.Msg.show({
			title: CMDBuild.Translation.attention,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					me.onGridAndFormPanelRemoveConfirmed(form);
					me.view.disableModify();
				}
			}
		});
	},

	onFormSaveButtonClick: function(form) {
		var form = this.view.form.getForm();
		if (form && form.isValid()) {
			this.view.disableModify();
			this.onGridAndFormPanelSaveButtonClick(form);
		} else {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
		}
	},

	onFormAbortButtonClick: function(form) {
		var enableCMTBar = false;
		if (this.record) {
			this.fieldManager.loadRecord(this.record);
			enableCMTBar = true;
		} else {
			this.fieldManager.reset();
		}

		this.view.disableModify(enableCMTBar);
	},

	// as grid delegate

	onCMGridSelect: function(grid, record) {
		this.record = record;
		var enableToolbar = !!record;
		this.view.disableModify(enableToolbar);
		if (this.fieldManager) {
			this.fieldManager.loadRecord(record);
		}
	}
});