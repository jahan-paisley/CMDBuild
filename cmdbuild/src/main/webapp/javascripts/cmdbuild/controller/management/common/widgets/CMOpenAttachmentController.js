(function() {
	var TRUE = "true";

	Ext.define("CMDBuild.controller.management.workflow.CMActivityAttachmentsController", {

		extend : "CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function() {
			this.callParent(arguments);
			_CMWFState.addDelegate(this);
		},

		// override
		// we want the attachments in readOnly mode, so set the privilege
		// to can only read. Then if there is the OpenAttachement extend attribute
		// it'll enable the editing

		// new business rule: read a configuration parameter to enable the editing
		// of attachments of closed activities (and then without damn openAttachment widget)
		updateViewPrivilegesForEntryType: function(et) {
			var priv = false;
			var pi = _CMWFState.getProcessInstance();

			if (CMDBuild.Config.workflow.add_attachment_on_closed_activities == TRUE
					&& pi 
					&& pi.isStateCompleted()) {

				priv = true;
			}

			this.view.updateWritePrivileges(priv);
		},

		// override
		// It is not possible add an attachment at the first step of the process
		onAddAttachmentButtonClick: function() {
			var pi = _CMWFState.getProcessInstance();
			if (pi && pi.isNew()) {
				new CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
						CMDBuild.Translation.management.modworkflow.extattrs.attachments.must_save_to_add,
						popup = false);

			} else {
				this.callParent(arguments);
			}
		},

		// override
		updateView: function() {
			var pi = _CMWFState.getProcessInstance();
			var processClass = _CMCache.getEntryTypeById(pi.getClassId());

			this.callParent([processClass]);
			this.view.hideBackButton();
		},

		// override
		getCard: function() {
			return _CMWFState.getProcessInstance() || null;
		},

		// override
		getCardId: function() {
			var pi = this.getCard();
			if (pi) {
				return pi.getId();
			}
		},

		// override
		getClassId: function() {
			var pi = _CMWFState.getProcessInstance();
			if (pi) {
				return pi.getClassId();
			}
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			this._loaded = false;
			if (processInstance.isNew() || 
					this.theModuleIsDisabled()) {

				this.view.disable();
			} else {
				this.updateView();
			}
		},

		// override
		buildCardModuleStateDelegate: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.management.common.widgets.CMOpenAttachmentController", {

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: ".OpenAttachment"
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.mon(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		destroy: function() {
			this.mun(this.view.backToActivityButton, "click", this.onBackToActivityButtonClick, this);
		},

		activeView: function() {
			this.view.cmActivate();
		},

		onBackToActivityButtonClick: function() {
			try {
				this.view.hideBackButton();
				this.ownerController.activateFirstTab();
			} catch (e) {
				CMDBuild.log.error("Something went wrong displaying the Activity panel");
			}
		}
	});

	function isANewActivity(a) {
		return typeof a.get != "function" || a.get("Id") == -1;
	}
})();