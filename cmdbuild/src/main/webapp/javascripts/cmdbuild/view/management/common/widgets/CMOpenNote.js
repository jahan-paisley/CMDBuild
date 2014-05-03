(function() {
	Ext.define("CMDBuild.view.management.common.widgets.CMOpenNotes", {
		extend: "CMDBuild.view.management.classes.CMCardNotesPanel",

		initComponent: function() {
			this.callParent(arguments);
			this.backToActivityButton.hide();
		},

		configure: Ext.emptyFn,

		cmActivate: function() {
			this.enable();
			this.backToActivityButton.show();
			this.ownerCt.setActiveTab(this);
			this.enableModify();
		},

		buildButtons: function() {
			this.callParent(arguments);

			this.buttons = this.buttons || [];

			this.backToActivityButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.workflow.back,
				hidden: true
			});

			this.buttons.push(this.backToActivityButton);
		},

		hideBackButton: function() {
			this.backToActivityButton.hide();
		}
	});
})();