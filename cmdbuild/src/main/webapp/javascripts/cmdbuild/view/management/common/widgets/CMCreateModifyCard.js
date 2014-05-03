(function() {

	Ext.define("CMDBuild.view.management.common.widgets.CMCreateModifyCard", {

		extend: "CMDBuild.view.management.common.CMFormWithWidgetButtons",

		statics: {
			WIDGET_NAME: ".CreateModifyCard"
		},

		withButtons: false,

		initComponent: function() {
			this.addCardButton = new CMDBuild.AddCardMenuButton({
				classId: undefined
			});

			Ext.apply(this, {
				tbar: [this.addCardButton],
				border: false,
				frame: false,
				padding: "0 0 5px 0",
				cls: "x-panel-body-default-framed"
			});

			this.callParent(arguments);
		},

		// buttons that the owner panel add to itself
		getExtraButtons: function() {
			var me = this;
			return [new Ext.Button( {
				text : CMDBuild.Translation.common.buttons.save,
				name : 'saveButton',
				handler: function() {
					me.fireEvent(me.CMEVENTS.saveCardButtonClick);
				}
			})];
		},

		initWidget: function(entryType, isEditable) {
			if (entryType.isSuperClass()) {
				this.addCardButton.updateForEntry(entryType);
				this.addCardButton.setDisabled(!isEditable);
			} else {
				this.addCardButton.disable();
			}
		}
	});
})();