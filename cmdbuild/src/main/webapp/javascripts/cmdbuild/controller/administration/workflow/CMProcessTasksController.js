(function() {

	Ext.define('CMDBuild.controller.administration.workflow.CMProcessTasksController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		currentProcess: undefined,
		currentProcessTaskId: undefined,
		grid: undefined,
		selectionModel: undefined,
		view: undefined,

		// Overwrite
		constructor: function(view) {

			// Handlers exchange
			this.view = view;
			this.grid = view.grid;
			this.view.delegate = this;
			this.grid.delegate = this;

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
				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onItemDoubleClick':
					return this.onItemDoubleClick(name, param);

				case 'onModifyButtonClick':
					return this.onModifyButtonClick(name, param);

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick(name, param);

				case 'onRowSelected':
					return this.onRowSelected();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onAddButtonClick: function(name, param, callBack) {
			var me = this;

			this.targetAccordion.expand();

			Ext.Function.createDelayed(function() {
				me.targetAccordion.selectNodeById(param.type);

				Ext.Function.createDelayed(function() {
					me.targetController.cmOn(name, param, callBack);
					me.targetController.form.delegate.delegateStep[0].setValueWorkflowCombo(me.currentProcess.get(CMDBuild.ServiceProxy.parameter.NAME));
					me.targetController.form.delegate.onModifyButtonClick();
				}, 100)();
			}, 500)();
		},

		onItemDoubleClick: function(name, param) {
			var me = this;

			this.targetAccordion.expand();

			Ext.Function.createDelayed(function() {
				me.targetAccordion.selectNodeById(param.type);

				me.targetController.grid.getStore().load({
					callback: function() {
						var selectionIndex = me.targetController.grid.getStore().find(CMDBuild.ServiceProxy.parameter.ID, param.id);

						if (selectionIndex > 0) {
							me.targetController.grid.getSelectionModel().select(
								selectionIndex,
								true
							);
						} else {
							CMDBuild.Msg.error(
								CMDBuild.Translation.common.failure,
								Ext.String.format('Cannot find taks with id ' + param.id + ' in store')
							);

							me.targetController.form.delegate.selectedId = null;
							me.targetController.form.disableModify();
						}
					}
				});
			}, 500)();
		},

		onModifyButtonClick: function(name, param) {
			var me = this;

			if (this.currentProcessTaskId) {
				param.id = this.currentProcessTaskId;

				this.onItemDoubleClick(null, param);

				Ext.Function.createDelayed(function() {
					if (me.targetController.form.delegate.selectedId != null)
						me.targetController.form.delegate.onModifyButtonClick();
				}, 1000)();
			}
		},

		onProcessSelected: function(processId, process) {
			this.currentProcess = process;

			if (!process || process.get('superclass')) {
				this.view.disable();
			} else {
				this.view.enable();

				this.grid.reconfigure(CMDBuild.core.proxy.CMProxyTasks.getStoreByWorkflow());
				this.grid.store.load({
					params: {
						workflowClassName: process.get(CMDBuild.ServiceProxy.parameter.NAME)
					}
				});
			}
		},

		onRemoveButtonClick: function(name, param) {
			var me = this;

			if (this.currentProcessTaskId) {
				param.id = this.currentProcessTaskId;

				this.targetAccordion.expand();

				Ext.Function.createDelayed(function() {
					me.targetAccordion.selectNodeById(param.type);

					Ext.Function.createDelayed(function() {
						if (me.targetController.form.delegate.selectedId != null)
							me.targetController.form.delegate.onRemoveButtonClick();
					}, 1000)();
				}, 500)();
			}
		},

		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.currentProcessTaskId = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.ID);
				this.view.enableCMTbar();

				// This declaration positioned in constructor doesn't works for targetAccordion
				this.targetAccordion = _CMMainViewportController.findAccordionByCMName('tasks');
				this.targetController = _CMMainViewportController.panelControllers['tasks'];
			}
		}
	});

})();