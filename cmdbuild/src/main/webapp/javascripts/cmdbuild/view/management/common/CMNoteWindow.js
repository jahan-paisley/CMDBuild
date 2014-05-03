(function() {
Ext.define("CMDBuild.view.management.common.CMNoteWindow", {
	extend: "CMDBuild.PopupWindow",
	withButtons: false,

	initComponent: function() {
		var me = this;
		this.note = new CMDBuild.view.management.classes.CMCardNotesPanel({
			withButtons: this.withButtons,
			getExtraButtons: function() {
				return new Ext.Button({
					text: CMDBuild.Translation.common.buttons.close,
					handler: function() {
						me.destroy();
					}
				});
			}
		});

		this.CMEVENTS = this.note.CMEVENTS;

		if (this.withButtons) {
			this.relayEvents(this.note, [this.note.CMEVENTS.saveNoteButtonClick, this.note.CMEVENTS.cancelNoteButtonClick]);
		}

		Ext.apply(this, {
			items: [this.note],
			buttonAlign: "center"
		});

		this.callParent(arguments);
	},

	getForm: function() {
		return this.note.getForm();
	},

	reset: function() {
		this.note.reset();
	},

	loadCard: function(card) {
		this.note.loadCard(card);
	},

	disableModify: function(enableModifyButton) {
		this.note.disableModify(enableModifyButton);
	},

	syncForms: function() {
		return this.note.syncForms();
	},

	updateWritePrivileges: function(priv) {
		return this.note.updateWritePrivileges(priv);
	}
});

})();