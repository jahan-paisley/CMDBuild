(function() {

	Ext.define("CMDBuild.controller.administration.dataview.CMFilerDataViewController", {
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
			this.className = null;
			this.record = null;
		},

		onViewOnFront: function(selection) {
			if (this.fieldManager == null) {
				this.fieldManager = new CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormFieldsManager();
				this.fieldManager.addDelegate(this);
				this.view.buildFields(this.fieldManager);
				this.view.disableModify();
			}
	
			if (this.gridConfigurator == null) {
				this.gridConfigurator = new CMDBuild.delegate.administration.common.dataview.CMFilterDataViewGridConfigurator();
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

			if (!values.filter) {
				CMDBuild.Msg.error(//
					CMDBuild.Translation.error, //
					CMDBuild.Translation.you_have_not_set_a_filter,
					false//
				);

				return;
			} else {
				// BUSINNESS RULE: The user could not save a view if the filter
				// has some runtime parameter
				var fakeFilter = new CMDBuild.model.CMFilterModel({configuration: Ext.decode(values.filter)});
				var runtimeAttributes = fakeFilter.getRuntimeParameters();

				if (runtimeAttributes && runtimeAttributes.length > 0) {
					CMDBuild.Msg.error(//
						CMDBuild.Translation.error, //
						CMDBuild.Translation.itIsNotAllowedFilterWithRuntimeParams, //
						false//
					);

					return;
				}
			}

			var request = {
				params: values,
				success: function() {
					_CMCache.flushTranslationsToSave(values["name"]);
					me.gridConfigurator.getStore().load();
				}
			};

			if (this.record == null) {
				_CMProxy.dataView.filter.create(request);
			} else {
				request.params.id = me.record.getId();
				_CMProxy.dataView.filter.update(request);
			}
		},

		/**
		 * called after the confirmation of a remove
		 * @param {CMDBuild.view.administration.common.basepanel.CMForm} form
		 */
		// override
		onGridAndFormPanelRemoveConfirmed: function(form) {
			var me = this;

			_CMProxy.dataView.filter.remove({
				params: {
					id: me.record.getId()
				},
				success: function() {
					me.gridConfigurator.getStore().load();
				}
			});

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
				this.className = className;
				builder.setFilterChooserClassName(className);
			}
		}
	});
})();