Ext.define("CMDBuild.controller.administration.dataview.CMSqlDataViewController", {
	extend: "CMDBuild.controller.CMBasePanelController",

	mixins: {
		gridFormPanelDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate"
	},

	constructor: function(view) {
		this.mixins.gridFormPanelDelegate.constructor.call(this, view);
		this.fieldManager = null;
		this.gridConfigurator = null;
		this.record = null;

		this.callParent(arguments);
	},

	onViewOnFront: function(selection) {
		if (this.fieldManager == null) {
			this.fieldManager = new CMDBuild.delegate.administration.common.dataview.CMSqlDataViewFormFieldsManager();
			this.view.buildFields(this.fieldManager);
			this.view.disableModify();
		}

		if (this.gridConfigurator == null) {
			this.gridConfigurator = new CMDBuild.delegate.administration.common.dataview.CMSqlDataViewGridConfigurator();
			this.view.configureGrid(this.gridConfigurator);
			this.gridConfigurator.getStore().load();
		}
	},

	// as gridFormPanelDelegate

	/**
	 * called after the save button click
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	// override
	onGridAndFormPanelSaveButtonClick: function(form) {
		var me = this;
		var values = this.fieldManager.getValues();
		var request = {
			params: values,
			success: function() {
				_CMCache.flushTranslationsToSave(values["name"]);
				me.gridConfigurator.getStore().load();
			}
		};

		if (this.record == null) {
			_CMProxy.dataView.sql.create(request);
		} else {
			request.params.id = me.record.getId();
			_CMProxy.dataView.sql.update(request);
		}
	},

	/**
	 * called after the confirmation of a remove
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	// override
	onGridAndFormPanelRemoveConfirmed: function(form) {
		var me = this;

		_CMProxy.dataView.sql.remove({
			params: {
				id: me.record.getId()
			},
			success: function() {
				me.gridConfigurator.getStore().load();
			}
		});
	}

});