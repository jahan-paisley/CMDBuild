Ext.define("CMDBuild.view.management.common.CMCardWindow", {
	extend: "CMDBuild.PopupWindow",

	cmEditMode: false, // if true, after the attributes load go in edit mode
	withButtons: false, // true to use the buttons build by the CMCardPanel

	initComponent:function() {

		this.cardPanel = new CMDBuild.view.management.classes.CMCardPanel({
			withButtons: this.withButtons,
			withToolBar: this.withToolBar,
			allowNoteFiled: true,
			border: false,
			frame: false,
		});

		var ee = this.cardPanel.CMEVENTS;
		this.CMEVENTS = {
			saveCardButtonClick: ee.saveCardButtonClick,
			abortButtonClick: ee.abortButtonClick,
			formFilled: ee.formFilled,
			widgetButtonClick: ee.widgetButtonClick,
			editModeDidAcitvate: ee.editModeDidAcitvate,
			displayModeDidActivate: ee.displayModeDidActivate
		};

		this.relayEvents(this.cardPanel, [
			ee.saveCardButtonClick,
			ee.abortButtonClick,
			ee.formFilled,
			ee.widgetButtonClick,
			ee.editModeDidAcitvate,
			ee.displayModeDidActivate
		]);

		this.addEvents(ee.saveCardButtonClick);
		this.addEvents(ee.abortButtonClick);
		this.addEvents(ee.formFilled);
		this.addEvents(ee.widgetButtonClick);
		this.addEvents(ee.editModeDidAcitvate);
		this.addEvents(ee.displayModeDidActivate);

		if (this.classId) {
			var privileges = _CMUtils.getClassPrivileges(this.classId);
			this.cardPanel.writePrivilege = privileges.write;
		}

		if (!this.withButtons) {
			this.closeButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.close,
				handler : this.close,
				scope : this
			});
			this.buttons = [this.closeButton];
		}

		this.items = [this.cardPanel];
		this.buttonAlign = "center";
		this.border = false;
		this.frame = false;
		this.callParent(arguments);

		_CMUtils.forwardMethods(this, this.cardPanel, ["displayMode",
				"editMode", "fillForm", "loadCard", "reset", "getForm",
				"getWidgetButtonsPanel", "isInEditing"]);
	}
});