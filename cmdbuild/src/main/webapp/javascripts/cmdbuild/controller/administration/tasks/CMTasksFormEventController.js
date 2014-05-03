(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormEventController", {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		parentDelegate: undefined,
		delegateStep: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'event',

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onClassSelected':
					this.onClassSelected(param.className);

				case 'onCloneButtonClick':
					return this.onCloneButtonClick();

				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick();

				case 'onRowSelected':
					return this.onRowSelected();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param (String) type
		 */
		// overwrite
		onAddButtonClick: function(name, param, callBack) {
			this.callParent(arguments);

			switch (param.type) {
				case 'event_asynchronous':
					return this.delegateStep[3].setDisabledAttributesGrid(true);

				case 'event_synchronous':
					return this.delegateStep[2].setDisabledAttributesGrid(true);

				default:
					throw 'CMTasksFormEventController error: task type not recognized';
			}
		},

		/**
		 * @param (String) className
		 */
		onClassSelected: function(className) {
			this.setDisabledButtonNext(false);
			this.delegateStep[1].className = className;
		},

		// overwrite
		onModifyButtonClick: function() {
			this.callParent(arguments);

			_debug('onModifyButtonClick to implement');
		},

		// overwrite
		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.ID);

				// Selected task asynchronous store query
				this.selectedDataStore = CMDBuild.core.proxy.CMProxyTasks.get(this.taskType);
				this.selectedDataStore.load({
					scope: this,
					params: {
						id: this.selectedId
					},
					callback: function(records, operation, success) {
						if (!Ext.isEmpty(records)) {
							var record = records[0];

							// TODO: to check if response has phase data or not to extends taskType value

							this.parentDelegate.loadForm(this.taskType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find good fix, so i must set all fields manually

//							// Set step1 [0] datas
//							me.delegateStep[0].setValueActive(record.get(CMDBuild.ServiceProxy.parameter.ACTIVE));
//							me.delegateStep[0].setValueAttributesGrid(record.get(CMDBuild.ServiceProxy.parameter.ATTRIBUTES));
//							me.delegateStep[0].setValueDescription(record.get(CMDBuild.ServiceProxy.parameter.DESCRIPTION));
//							me.delegateStep[0].setValueId(record.get(CMDBuild.ServiceProxy.parameter.ID));
//							me.delegateStep[0].setValueWorkflowCombo(record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME));
//
//							// Set step2 [1] datas
//							me.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));
//							me.delegateStep[1].setValueBase(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));

							this.view.disableModify(true);
						}
					}
				});

				this.view.wizard.changeTab(0);
			}
		},

		// overwrite
		onSaveButtonClick: function() {
			var nonvalid = this.view.getNonValidFields();

			if (nonvalid.length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

//			CMDBuild.LoadMask.get().show();
			var attributesGridValues = this.delegateStep[2].getValueAttributeGrid();
			var filterData = this.delegateStep[1].getDataFilters();
			var formData = this.view.getData(true);
			var submitDatas = {};

			// Form validating by type
				switch (formData[CMDBuild.ServiceProxy.parameter.TYPE]) {
					case 'event_asynchronous': {

						// Cron field validation
						if (!this.delegateStep[1].getCronDelegate().validate(this.parentDelegate.form.wizard))
							return;

						submitDatas[CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue(
							formData[CMDBuild.ServiceProxy.parameter.CRON_INPUT_TYPE]
						);
					} break;

					case 'event_synchronous': {
						submitDatas[CMDBuild.ServiceProxy.parameter.PHASE] = formData[CMDBuild.ServiceProxy.parameter.PHASE];
						submitDatas[CMDBuild.ServiceProxy.parameter.GROUPS] = Ext.encode(this.delegateStep[0].getValueGroups());
					} break;

					default:
						throw 'CMTasksFormEventController error: task type not recognized';
				}

			// Form submit values formatting
				if (!CMDBuild.Utils.isEmpty(attributesGridValues))
					submitDatas[CMDBuild.ServiceProxy.parameter.ATTRIBUTES] = Ext.encode(attributesGridValues);

			// Data filtering to submit only right values
			submitDatas[CMDBuild.ServiceProxy.parameter.ACTIVE] = formData[CMDBuild.ServiceProxy.parameter.ACTIVE];
			submitDatas[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = formData[CMDBuild.ServiceProxy.parameter.CLASS_NAME];
			submitDatas[CMDBuild.ServiceProxy.parameter.DESCRIPTION] = formData[CMDBuild.ServiceProxy.parameter.DESCRIPTION];
			submitDatas[CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE] = formData[CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE];
			submitDatas[CMDBuild.ServiceProxy.parameter.ID] = formData[CMDBuild.ServiceProxy.parameter.ID];
			submitDatas[CMDBuild.ServiceProxy.parameter.TYPE] = formData[CMDBuild.ServiceProxy.parameter.TYPE];
			submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME] = formData[CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME];

_debug(filterData);
_debug(formData);
_debug(submitDatas);

//			if (Ext.isEmpty(formData[CMDBuild.ServiceProxy.parameter.ID])) {
//				CMDBuild.core.proxy.CMProxyTasks.create({
//					type: this.taskType,
//					params: submitDatas,
//					scope: this,
//					success: this.success,
//					callback: this.callback
//				});
//			} else {
//				CMDBuild.core.proxy.CMProxyTasks.update({
//					type: this.taskType,
//					params: submitDatas,
//					scope: this,
//					success: this.success,
//					callback: this.callback
//				});
//			}

			_debug('onSaveButtonClick to implement');
		}
	});

})();