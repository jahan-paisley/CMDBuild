Ext.define("CMDBuild.view.management.common.CMAttachmentsWindow", {
	extend: "CMDBuild.PopupWindow",

	initComponent: function() {
		this.grid = new CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel({
			border: false
		});

		var closeButton = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.close,
			name: 'saveButton',
			formBind: true,
			handler: function() {
				this.destroy();
			},
			scope: this
		});

		Ext.apply(this, {
			items : [ this.grid ],
			buttons : [ closeButton ],
			buttonAlign : "center"
		});

		this.callParent(arguments);
	}
});