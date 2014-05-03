(function() {

	var ERROR_TEMPLATE = "<p class=\"{0}\">{1}</p>";

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate", {
		onCardSaved: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.management.workflow.CMActivityPanelController", {
		extend: "CMDBuild.controller.management.classes.CMCardPanelController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function(v, owner, widgetControllerManager, delegate) {
			this.callParent(arguments);

			// this flag is used to define if the user has click on the
			// save or advance button. The difference is that on save 
			// the widgets do nothing and the saved activity goes in display mode.
			// On advance, otherwise, the widgets do the react (save their state) and
			// the saved activity lies in edit mode, to continue the data entry.
			this.isAdvance = false;

			this.mon(this.view, this.view.CMEVENTS.advanceCardButtonClick, this.onAdvanceCardButtonClick, this);
			this.mon(this.view, this.view.CMEVENTS.editModeDidAcitvate, onEditMode, this);
			this.mon(this.view, this.view.CMEVENTS.checkEditability, onCheckEditability, this);
			this.mon(this.view, this.view.CMEVENTS.displayModeDidActivate, onDisplayMode, this);

			_CMWFState.addDelegate(this);
			this.setDelegate(delegate || new CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate());
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate");
			this.delegate = d;
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			var me = this;
			this.clearView();

			if (processInstance != null // is null on abort of a new process
					&& processInstance.isStateCompleted()) {

				me.loadFields(processInstance.getClassId(), function loadFieldsCb() {
					me.fillFormWithProcessInstanceData(processInstance);
				});
			} else {
				enableStopButtonIfUserCanUseIt(this, processInstance);
			}
		},

		// wfStateDelegate
		onActivityInstanceChange: function(activityInstance) {
			var me = this;
			var processInstance = _CMWFState.getProcessInstance();

			// reduce the layouts work while
			// fill the panel and build the widgets.
			// Resume it at the end
			// and force a layout update

			Ext.suspendLayouts();

			if (!activityInstance.nullObject 
					&& activityInstance.isNew()) {

				/*
				 * I could be in a tab different to the first one,
				 * but to edit a new card is necessary to have the editing form.
				 * So I force the view to go on the ActivityTab
				 * 
				 * Do it here instead of in the CMModWorkflowController
				 * because it must be done before all operation
				 * over the form for rendering issues
				 */
				this.superController.view.showActivityPanel();
			}

			me.view.updateInfo(activityInstance.getPerformerName(), activityInstance.getDescription());

			// at first update the widget because they could depends
			// to the form. The Template resolver starts when the form goes
			// in edit mode, so the widgets must be already ready to done them works

			var updateWidget = processInstance.isStateOpen() || activityInstance.isNew();
			if (updateWidget) {
				me.widgetControllerManager.buildControllers(activityInstance);
			} else {
				me.widgetControllerManager.buildControllers(null);
			}

			// resume the layouts here
			// because the form already suspend
			// the layouts automatically
			Ext.resumeLayouts();
			this.view.doLayout();

			// Load always the fields
			me.loadFields(processInstance.getClassId(), function loadFieldsCb() {
				// fill always the process to trigger the
				// template resolver of filtered references
				me.fillFormWithProcessInstanceData(processInstance);
				manageEditability(me, activityInstance, processInstance);
			});

		},

		// override
		onModifyCardClick: function() {
			this.isAdvance = false;
			var pi = _CMWFState.getProcessInstance();
			var ai = _CMWFState.getActivityInstance();

			if (pi && pi.isStateOpen()
					&& ai && ai.isWritable()) {

				this.view.editMode();
			}
		},

		// override
		onRemoveCardClick: function() {
			var me = this;
			Ext.Msg.confirm(
				CMDBuild.Translation.management.modworkflow.abort_card, // title
				CMDBuild.Translation.management.modworkflow.abort_card_confirm, // message
				confirmCB);

			function confirmCB(btn) {
				if (btn != 'yes') {
					return;
				} else {
					deleteActivity.call(me);
				}
			}
		},

		// override
		onSaveCardClick: function() {
			this.isAdvance = false;
			save.call(this);
		},

		// override
		onAbortCardClick: function() {
			this.isAdvance = false;
			var activityInstance = _CMWFState.getActivityInstance();

			if (activityInstance && activityInstance.isNew()) {
				this.onProcessInstanceChange();
			} else {
				this.onActivityInstanceChange(activityInstance);
			}

			_CMUIState.onlyGridIfFullScreen();
		},

		onAdvanceCardButtonClick: function() {
			this.isAdvance = true;
			this.widgetControllerManager.waitForBusyWidgets(save, this);
		},

		clearView: function() {
			this.view.clear();
		},

		changeClassUIConfigurationForGroup: function(disabledForGroupButtons) {
			this.view.form.modifyCardButton.disabledForGroup = disabledForGroupButtons.modify;
			this.view.form.cloneCardButton.disabledForGroup = disabledForGroupButtons.clone;
			this.view.form.deleteCardButton.disabledForGroup = disabledForGroupButtons.remove;
			if (this.view.form.modifyCardButton.disabledForGroup)
				this.view.form.modifyCardButton.disable();
			else
				this.view.form.modifyCardButton.enable();
			if (this.view.form.cloneCardButton.disabledForGroup)
				this.view.form.cloneCardButton.disable();
			else
				this.view.form.cloneCardButton.enable();
			if (this.view.form.deleteCardButton.disabledForGroup)
				this.view.form.deleteCardButton.disable();
			else
				this.view.form.deleteCardButton.enable();
		},
		
		// override
		loadFields: function(entryTypeId, cb) {
			var me = this;
			var activityInstance = _CMWFState.getActivityInstance();
			var processInstance = _CMWFState.getProcessInstance();
			var variables = [];

			if (activityInstance) {
				variables = activityInstance.getVariables();
			}

			function onAttributesLoaded(attributes) {

				if (activityInstance.isNew()
						|| processInstance.isStateOpen()) {

					attributes = CMDBuild.controller.common.WorkflowStaticsController.filterAttributesInStep(attributes, variables);
				} else {
					// if here, we have a closed process, so show
					// all the attributes
				}

				me.view.fillForm(attributes, editMode = false);

				if (cb) {
					cb();
				}
			}

			if (entryTypeId) {
				_CMCache.getAttributeList(entryTypeId, onAttributesLoaded);
			} else {
				onAttributesLoaded([]);
			}
		},

		fillFormWithProcessInstanceData: function(processInstance) {
			if (processInstance != null) {
				this.view.loadCard(processInstance.asDummyModel());
				this.view.displayModeForNotEditableCard();
			}
		},

		// override
		onShowGraphClick: function() {
			var pi = _CMWFState.getProcessInstance();
			var classId = pi.getClassId();
			var cardId = pi.getId();

			CMDBuild.Management.showGraphWindow(classId, cardId);
		},

		// TODO: Needs some refactoring
		// override
		doFormSubmit: Ext.emptyFn,
		onSaveSuccess: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn,
		buildCardModuleStateDelegate: Ext.emptyFn
	});

	function deleteActivity() {
		var me = this;
		var processInstance = _CMWFState.getProcessInstance();

		if (!processInstance
				|| processInstance.isNew()) {
			return;
		}

		me.clearView();

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.workflow.terminateActivity({
			params: {
				classId: processInstance.getClassId(),
				cardId: processInstance.getId()
			},
			success: success,
			failure: failure
		});

		function success(response) {
			CMDBuild.LoadMask.get().hide();
			me.fireEvent(me.CMEVENTS.cardRemoved);
		}

		function failure() {
			CMDBuild.LoadMask.get().hide();
			CMDBuild.Msg.error(
				CMDBuild.Translation.errors.error_message,
				CMDBuild.Translation.errors.generic_error,
				true);
		}
	}


	function onCheckEditability(cb) {
		var me = this;
		var pi = _CMWFState.getProcessInstance();

		// for a new process do nothing
		if (pi.isNew()) {
			cb();
			return;
		}

		var requestParams = {
			processInstanceId: pi.getId(),
			className: pi.get("className"),
			beginDate: pi.get("beginDateAsLong")
		}

		CMDBuild.ServiceProxy.workflow.isPorcessUpdated({
			params: requestParams,
			success: function(operation, requestConfiguration, decodedResponse) {
				var isUpdated = decodedResponse.response.updated;
				if (isUpdated) {
					cb();
				}
			}
		});
	}

	function onEditMode() {
		this.editMode = true;
		if (this.widgetControllerManager) {
			this.widgetControllerManager.onCardGoesInEdit();
		}
	}

	function onDisplayMode() {
		this.editMode = false;
	}

	function save() {
		var me = this,
			requestParams = {},
			pi = _CMWFState.getProcessInstance(),
			ai = _CMWFState.getActivityInstance(),
			valid;

		if (pi) {
			var formValues = this.view.getValues();
			// used server side to be sure to update
			// the last version of the process
			formValues.beginDate = pi.get("beginDateAsLong");

			requestParams = {
				classId: pi.getClassId(),
				attributes: Ext.JSON.encode(formValues),
				advance: me.isAdvance
			};

			if (pi.getId()) {
				requestParams.cardId = pi.getId();
			}

			if (ai && ai.getId) {
				requestParams.activityInstanceId = ai.getId();
			}

			// Business rule: Someone want the validation
			// only if advance and not if save only
			valid = requestParams.advance ? validate(me) : true;

			if (valid) {

				requestParams["ww"] = Ext.JSON.encode(this.widgetControllerManager.getData(me.advance));
				_debug("save the process with params", requestParams);

				CMDBuild.LoadMask.get().show();

				CMDBuild.ServiceProxy.workflow.saveActivity({
					params: requestParams,
					scope : this,
					clientValidation: this.isAdvance, //to force the save request
					callback: function(operation, success, response) {
						CMDBuild.LoadMask.get().hide();
					},
					success: function(operation, requestConfiguration, decodedResponse) {
						var savedCardId = decodedResponse.response.Id;
						this.view.displayMode();

						// to enable the editing for the
						// right processInstance
						if (me.isAdvance) {
							me.idToAdvance = savedCardId;
						} else {
							_CMUIState.onlyGridIfFullScreen();
						}

						this.delegate.onCardSaved(savedCardId);
					}
				});
			}
		} else {
			_debug("There are no processInstance to save");
		}
	}

	function validateForm(me) {
		var form = me.view.getForm();
		var invalidAttributes = CMDBuild.controller.common.CardStaticsController.getInvalidAttributeAsHTML(form);

		if (invalidAttributes != null) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidAttributes, false);

			return false;
		} else {
			return true;
		}
	}

	function validate(me) {
		var valid = validateForm(me),
			wrongWidgets = me.widgetControllerManager.getWrongWFAsHTML();

		if (wrongWidgets != null) {
			valid = false;
			var msg = Ext.String.format(ERROR_TEMPLATE
					, CMDBuild.Constants.css.error_msg
					, CMDBuild.Translation.errors.invalid_extended_attributes);
			CMDBuild.Msg.error(null, msg + wrongWidgets, popup = false);
		}

		return valid;
	}

	function manageEditability(me, activityInstance, processInstance) {

		if (activityInstance.isNew()) {
			me.view.editMode();
			return;
		}

		if (!processInstance.isStateOpen()
				|| activityInstance.isNullObject()
				|| !activityInstance.isWritable()) {

			me.view.displayModeForNotEditableCard();
			enableStopButtonIfUserCanUseIt(me, processInstance);
			return;
		}

		if (me.isAdvance
				&& processInstance.getId() == me.idToAdvance) {

			me.view.editMode();
			me.isAdvance = false;
			return;
		}

		me.view.displayMode(enableTbar = true);
		enableStopButtonIfUserCanUseIt(me, processInstance);
	}

	function enableStopButtonIfUserCanUseIt(me, processInstance) {
		me.view.disableStopButton();
		if (!processInstance) {
			return;
		}

		var processClassId = processInstance.getClassId();
		if (processClassId) {
			var processClass = _CMCache.getEntryTypeById(processClassId);
			if (processClass) {
				var theUserCanStopTheProcess = processClass.isUserStoppable() || CMDBuild.Runtime.IsAdministrator;
				var theProcessIsNotAlreadyTerminated = processInstance.isStateOpen() || processInstance.isStateSuspended();

				if (theUserCanStopTheProcess && theProcessIsNotAlreadyTerminated) {
					me.view.enableStopButton();
				}
			}
		}
	}

})();