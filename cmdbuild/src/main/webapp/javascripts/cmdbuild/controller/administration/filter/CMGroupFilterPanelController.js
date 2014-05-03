Ext.define("CMDBuild.controller.administration.filter.CMGroupFilterPanelController", {
	extend: "CMDBuild.controller.CMBasePanelController",

	mixins: {
		gridFormPanelDelegate: "CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate",
		specificFilterFormDelegate: "CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormDelegate"
	},

	constructor: function(view) {
		this.callParent(arguments);
		this.mixins.gridFormPanelDelegate.constructor.call(this, view);

		this.fieldManager = null;
		this.gridConfigurator = null;
		this.targetClassName = null;
		this.record = null;
	},

	onViewOnFront: function(group) {
		if (this.fieldManager == null) {
			this.fieldManager = new CMDBuild.delegate.administration.common.dataview.CMFiltersForGroupsFormFieldsManager();
			this.fieldManager.addDelegate(this);
			this.view.buildFields(this.fieldManager);
		}

		this.view.disableModify();

		if (this.gridConfigurator == null) {
			this.gridConfigurator = new CMDBuild.delegate.administration.common.group.CMFiltersForGroupGridConfigurator();
			this.view.configureGrid(this.gridConfigurator);
		}

		// TODO say to the store the group id to load for
		this.gridConfigurator.getStore().load();
	},

	// as gridFormPanelDelegate

	/**
	 * called after the save button click
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	// override
	onGridAndFormPanelSaveButtonClick: function(form) {
		var values = this.fieldManager.getValues();
		if (!values.filter) {
			CMDBuild.Msg.error(//
				CMDBuild.Translation.error, //
				CMDBuild.Translation.you_have_not_set_a_filter,
				false//
			);

			return;
		}

		var me = this;
		var action;
		var filterToSend = new CMDBuild.model.CMFilterModel(values);
		filterToSend.setConfiguration(values.filter.getConfiguration());
		filterToSend.setTemplate(true);

		if (this.record) {
			action = "update";
			filterToSend.setId(this.record.getId());
		} else {
			action = "create";
		}

		_CMProxy.Filter[action](filterToSend, {
			callback: function() {
				_CMCache.flushTranslationsToSave(values["name"]);
				me.gridConfigurator.getStore().load();
				me.fieldManager.reset();
			}
		});
	},

	/**
	 * called after the confirmation of a remove
	 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
	 */
	// override
	onGridAndFormPanelRemoveConfirmed: function(form) {
		var values = this.fieldManager.getValues();
		var me = this;
		if (values.filter) {
			_CMProxy.Filter.remove(values.filter, {
				callback: function() {
					me.gridConfigurator.getStore().load();
					me.fieldManager.reset();
				}
			});
		}
	},

	// as specificFilterFormDelegate

	/**
	 * 
	 * @param {CMDBuild.view.administration.common.CMFilterDataViewFormFiledsBuilder} builder
	 * the builder that call this method
	 * @param {string} className
	 * the name of the selected class
	 */
	onFilterDataViewFormBuilderClassSelected: function(builder, className) {
		if (className) {
			this.targetClassName = className;
			builder.setFilterChooserClassName(className);
		}
	}
});