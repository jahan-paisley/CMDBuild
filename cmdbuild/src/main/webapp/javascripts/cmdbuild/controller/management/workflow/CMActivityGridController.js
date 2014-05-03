(function() {
	var STATE_VALUE_COMPLETED = "closed.completed";

	Ext.define("CMDBuild.controller.management.workflow.CMActivityGridController", {
		extend: "CMDBuild.controller.management.common.CMCardGridController",

		mixins: {
			activityPanelControllerDelegate: "CMDBuild.controller.management.workflow.CMActivityPanelControllerDelegate"
		},

		constructor: function(view, supercontroller) {
			this.callParent(arguments);

			this.CMEVENTS.processClosed = "processTerminated";

			this.addEvents(this.CMEVENTS.processClosed);

			// from cmmodworkflow
			this.mon(this.view.statusCombo, "select", onStatusComboSelect, this);
			this.mon(this.view.addCardButton, "cmClick", this.onAddCardButtonClick, this);
			this.mon(this.view, "activityInstaceSelect", this.onActivityInfoSelect, this);

			
		},

		// override
		buildStateDelegate: function() {
			var sd = new CMDBuild.state.CMWorkflowStateDelegate();
			var me = this;

			sd.onProcessClassRefChange = function(entryType, danglingCard) {
				me.onEntryTypeSelected(entryType, danglingCard);
			};

			_CMWFState.addDelegate(sd);
		},

		// override
		getEntryType: function() {
			return _CMWFState.getProcessClassRef();
		},

		onAddCardButtonClick: function(p) {
			this.gridSM.deselectAll();

			_CMWFState.setProcessInstance(new CMDBuild.model.CMProcessInstance({
				classId: p.classId
			}));

			CMDBuild.LoadMask.get().show();

			CMDBuild.ServiceProxy.workflow.getstartactivitytemplate(p.classId, {
				scope: this,
				success: function success(response, request, decoded) {
					var activity = new CMDBuild.model.CMActivityInstance(decoded.response || {});
					_CMWFState.setActivityInstance(activity);
				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				},
				important: true
			});
		},

		/*
		 * The activityInfo has only the base info about the activity.
		 * Do a request to have the activity data and set it in the _CMWFState
		 */
		onActivityInfoSelect: function(activityInfoId) {
			var me = this;
			if (!activityInfoId ||
					// prevent the selection of the same activity
					(me.lastActivityInfoId && me.lastActivityInfoId == activityInfoId)) {

				return;
			} else {
				me.lastActivityInfoId = null;
			}

			updateViewSelection(activityInfoId, me);

			CMDBuild.ServiceProxy.workflow.getActivityInstance({
				classId: _CMWFState.getProcessInstance().getClassId(),
				cardId: _CMWFState.getProcessInstance().getId(),
				activityInstanceId: activityInfoId
			}, {
				success: function success(response, request, decoded) {
					var activity = new CMDBuild.model.CMActivityInstance(decoded.response || {});
					me.lastActivityInfoId = activityInfoId;
					_CMWFState.setActivityInstance(activity);
				}
			});
		},

		// override
		onCardSelected: function(sm, selection) {
			if (Ext.isArray(selection)) {
				if (selection.length > 0) {
					var pi = selection[0];
					var activities = pi.getActivityInfoList();
					this.lastActivityInfoId = null;

					var me = this;
					_CMWFState.setProcessInstance(pi, function() {
						if (activities.length > 0) {
							toggleRow(pi, me);
							if (activities.length == 1) {
								var ai = activities[0];
								if (ai && ai.id) {
									me.onActivityInfoSelect(ai.id);
								}
							}
						} else {
							_debug("A proces without activities", pi);
						}
					});
				}
			}
		},

		// override
		_onGetPositionSuccessForcingTheFilter: function(p, position, resText) {
			this.view.setStatus(resText.FlowStatus);
			this.callParent(arguments);
		},

		// override
		_onGetPositionFailureWithoutForcingTheFilter: function(response) {
			/**
			 * FIXME
			 * The FlowStatus is not returned
			 * if the card is not found...
			 */
//			var flowStatusOfSearchedCard = response.FlowStatus;
//			if (flowStatusOfSearchedCard == STATE_VALUE_COMPLETED) {
				this.view.skipNextSelectFirst();
				_CMWFState.setProcessInstance(new CMDBuild.model.CMProcessInstance());
				_CMUIState.onlyGridIfFullScreen();
//			} else {
//				this.callParent(arguments);
//			}
		},

		// override
		onEntryTypeSelected: function(entryType, danglingCard) {
			this.callParent(arguments);
			this.view.addCardButton.updateForEntry(entryType);
		},

		// activityPanelControllerDelegate
		onCardSaved: function(cardId) {
			this.openCard({
				Id: cardId,
				// use the id class of the grid to use the right filter
				// when look for the position
				IdClass: this.getEntryType().get("id")
			});
		}
	});

	function onStatusComboSelect() {
		this.view.updateStatusParamInStoreProxyConfiguration();
		this.view.loadPage(1);
	}

	function toggleRow(pi, me) {
		var p = me.view.plugins[0];
		if (p) {
			p.toggleRow(pi.index, forceExpand = true);
		}
	}

	function updateViewSelection(activityInfoId, me) {
		try {
			var activityRowEl = Ext.query('p[id='+activityInfoId+']', me.view.getEl().dom)[0];
			activityRowEl = new Ext.Element(activityRowEl);
			var p = me.view.plugins[0];

			p.selectSubRow(me.view, activityRowEl);
		} catch (e) {
			_debug("Can't select the activity " + activityInfoId);
		}
	}
})();