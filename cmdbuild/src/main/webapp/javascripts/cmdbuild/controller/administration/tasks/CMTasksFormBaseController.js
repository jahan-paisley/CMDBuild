(function() {

	/**
	 * Base class to extends to create form controller implementation
	 */
	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormBaseController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: undefined,

		// Abstract
		cmOn: function() {
			throw 'CMTasksFormBaseController: cmOn() unimplemented method';
		},

		disableTypeField: function() {
			this.delegateStep[0].setDisabledTypeField(true);
		},

		onAbortButtonClick: function() {
			if (this.selectedId != null) {
				this.onRowSelected();
			} else {
				this.view.reset();
				this.view.disableModify();
				this.view.wizard.changeTab(0);
			}
		},

		onAddButtonClick: function(name, param, callBack) {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.parentDelegate.loadForm(param.type);
			this.view.reset();
			this.view.enableTabbedModify();
			this.disableTypeField();
			this.view.wizard.changeTab(0);
		},

		onCloneButtonClick: function() {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.resetIdField();
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.disableTypeField();
			this.view.wizard.changeTab(0);
		},

		onModifyButtonClick: function() {
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.disableTypeField();
			this.view.wizard.changeTab(0);
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

		// Abstract
		onRowSelected: function() {
			throw 'CMTasksFormBaseController: onRowSelected() unimplemented method';
		},

		// Abstract
		onSaveButtonClick: function() {
			throw 'CMTasksFormBaseController: onSaveButtonClick() unimplemented method';
		},

		removeItem: function() {
			if (this.selectedId == null) {
				// Nothing to remove
				return;
			}

			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.CMProxyTasks.remove({
				type: this.taskType,
				params: {
					id: this.selectedId
				},
				scope: this,
				success: this.success,
				callback: this.callback
			});
		},

		resetIdField: function() {
			this.delegateStep[0].setValueId();
		},

		/**
		 * @param (Boolean) state
		 */
		setDisabledButtonNext: function(state) {
			this.view.nextButton.setDisabled(state);
		},

		success: function(result, options, decodedResult) {
			var me = this;
			var store = this.parentDelegate.grid.store;

			store.load({
				callback: function() {
					me.view.reset();

					var rowIndex = this.find(
						CMDBuild.ServiceProxy.parameter.ID,
						(decodedResult.response) ? decodedResult.response : me.delegateStep[0].getValueId()
					);

					if (rowIndex < 0)
						rowIndex = 0;

					me.selectionModel.select(rowIndex, true);
				}
			});

			this.view.disableModify(true);
			this.view.wizard.changeTab(0);
		}
	});

})();