(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	Ext.define('CMDBuild.controller.administration.email.CMEmailTemplatesController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		// Overwrite
		constructor: function(view) {
			this.callParent(arguments);

			// Handlers exchange
			this.grid = view.grid;
			this.form = view.form;
			this.view.delegate = this;
			this.grid.delegate = this;
			this.form.delegate = this;

			this.selectedName = null;
			this.selectionModel = this.grid.getSelectionModel();
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick();

				case 'onItemDoubleClick':
				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick();

				case 'onRowSelected':
					return this.onRowSelected();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (
						this.parentDelegate
						&& typeof this.parentDelegate == 'object'
					) {
						return this.parentDelegate.cmOn(name, param, callBack);
					}
				}
			}
		},

		onAbortButtonClick: function() {
			if (this.selectedName != null) {
				this.onRowSelected();
			} else {
				this.form.reset();
				this.form.disableModify();
			}
		},

		onAddButtonClick: function() {
			this.selectionModel.deselectAll();
			this.selectedName = null;
			this.form.reset();
			this.form.enableModify(true);
		},

		onModifyButtonClick: function() {
			this.form.disableCMTbar();
			this.form.enableCMButtons();
			this.form.enableModify(true);
			this.form.disableNameField();
		},

		onRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.administration.setup.remove,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes') {
						this.removeItem();
					}
				}
			});
		},

		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				var me = this;
				this.selectedName = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.NAME);

				// Selected user asynchronous store query
				this.selectedDataStore = CMDBuild.core.proxy.CMProxyEmailTemplates.get();
				this.selectedDataStore.load({
					params: {
						name: this.selectedName
					},
					callback: function() {
						me.form.loadRecord(this.getAt(0));
						me.form.disableModify(true);
					}
				});
			}
		},

		onSaveButtonClick: function() {
			var nonvalid = this.form.getNonValidFields();

			if (nonvalid.length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

			CMDBuild.LoadMask.get().show();
			var formData = this.form.getData(true);

			if (formData.id == null || formData.id == '') {
				CMDBuild.core.proxy.CMProxyEmailTemplates.create({
					params: formData,
					scope: this,
					success: this.success,
					callback: this.callback
				});
			} else {
				CMDBuild.core.proxy.CMProxyEmailTemplates.update({
					params: formData,
					scope: this,
					success: this.success,
					callback: this.callback
				});
			}
		},

		removeItem: function() {
			if (this.selectedName == null) {
				// Nothing to remove
				return;
			}

			var me = this;
			var store = this.grid.store;

			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.CMProxyEmailTemplates.remove({
				params: {
					name: this.selectedName
				},
				scope: this,
				success: function() {
					this.form.reset();

					store.load({
						callback: function() {
							me.selectionModel.select(0, true);
						}
					});
				},
				callback: this.callback()
			});
		},

		success: function(result, options, decodedResult) {
			var me = this;
			var store = this.grid.store;

			store.load({
				callback: function() {
					var rowIndex = this.find(
						CMDBuild.ServiceProxy.parameter.NAME,
						me.form.getForm().findField(CMDBuild.ServiceProxy.parameter.NAME).getValue()
					);

					me.selectionModel.select(rowIndex, true);
					me.form.disableModify(true);
				}
			});
		}
	});

})();