(function() {
	var tr =  CMDBuild.Translation.management.modworkflow;

	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel", {
		extend: "CMDBuild.view.management.common.CMFormWithWidgetButtons",

		constructor: function() {
			this.callParent(arguments);

			this.CMEVENTS.advanceCardButtonClick = this.form.CMEVENTS.advanceCardButtonClick;
			this.addEvents(this.CMEVENTS.advanceCardButtonClick);
			this.relayEvents(this.form, [this.CMEVENTS.advanceCardButtonClick, this.CMEVENTS.checkEditability]);

			_CMUtils.forwardMethods(this, this.form, ["enableStopButton", "disableStopButton", "updateInfo"]);
		},

		buildForm: function() {
			return new CMDBuild.view.management.workflow.CMActivityPanel.Form({
				region: "center",
				cmOwner: this
			});
		},

		clear: function() {
			this.form.removeAll(destroy = true);
			this.form.updateInfo();
			this.widgets.removeAll(destroy = true);
			this.widgets.hide();
			this.displayMode();
		}
	});

	Ext.define("CMDBuild.view.management.workflow.CMActivityPanel.Form", {
		extend: "CMDBuild.view.management.classes.CMCardForm",
		constructor: function() {
			this.callParent(arguments);

			this.CMEVENTS.advanceCardButtonClick = "cm-advance";
			this.CMEVENTS.checkEditability = "cm-check-editability";

			this.addEvents(this.CMEVENTS.advanceCardButtonClick);
			this.addEvents(this.CMEVENTS.checkEditability);
		},

		// override
		buildTBar: function() {
			this.withToolBar = true;
			this.callParent(arguments);

			/*
			 * use buttons as label because the display fields
			 * have some strange behaviours, the time is short,
			 * this solution works, is enough
			 */
			var buttonAsLabelConf = {
				pressedCls: "",
				overCls:"",
				disabledCls: "",
				disable: function(){},
				style: {
					cursor: "auto",
					color: "#000000"
				}
			};
			this.activityPerformerName = new Ext.button.Button(buttonAsLabelConf);
			this.activityDescription = new Ext.button.Button(buttonAsLabelConf);

			this.modifyCardButton.setText(tr.modify_card);
			this.deleteCardButton.setText(tr.delete_card);

			this.cmTBar = [
				this.modifyCardButton,
				this.deleteCardButton,
				CMDBuild.Config.graph.enabled=="true" ? this.graphButton : '-',
				'->','-',
				this.activityPerformerName,
				'-',
				this.activityDescription,
				' '
			];
		},

		// override
		buildButtons: function() {
			this.withButtons = true;
			this.callParent(arguments);
			var me = this;
			this.advanceButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.workflow.advance,
				handler: function() {
					me.fireEvent(me.CMEVENTS.advanceCardButtonClick);
				}
			}),

			this.cmButtons = [this.saveButton, this.advanceButton, this.cancelButton];
		},

		// override
		/**
		 * The controller must ask to the
		 * server if the editing process
		 * is in its last version.
		 * 
		 * So here must defer the real behavior
		 * of this method after the asynchronous
		 * check.
		 * 
		 * To do that, pass a function to
		 * the controller
		 */
		editMode: function() {
			var me = this;
			function activityPanelEditMode() {
				CMDBuild.view.management.classes.CMCardForm.prototype.editMode.call(me, arguments);
			}

			me.fireEvent(me.CMEVENTS.checkEditability, activityPanelEditMode);
		},

		disableStopButton : function() {
			this.deleteCardButton.disable();
		},

		enableStopButton : function() {
			if (! this.deleteCardButton.disabledForGroup) {
				this.deleteCardButton.enable();
			}
		},

		updateInfo : function(performerName, activityDescription) {
			this.activityPerformerName.setText(performerName || "");
			this.activityDescription.setText(activityDescription || "");
		},

		canReconfigureTheForm: function() {
			return this.cmOwner.isTheActivePanel();
		}
	});

})();