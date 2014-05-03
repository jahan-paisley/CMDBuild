Ext.define("CMDBuild.view.administration.widget.CMWidgetDefinitionForm", {
	extend: "Ext.form.Panel",

	mixins: {
		formFunctions: "CMDBUild.view.common.CMFormFunctions"
	},

	initComponent: function() {
		var me = this;
		this.modifyButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.modify,
			iconCls: "modify",
			handler: function() {
				me.fireEvent("cm-modify");
			}
		});

		this.abortButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.remove,
			iconCls: "delete",
			handler: function() {
				me.fireEvent("cm-remove");
			}
		});

		this.tbar = this.cmTBar = [this.modifyButton, this.abortButton];

		this.callParent(arguments);

		this.disableCMTbar();
	},

	reset: function() {
		this.removeAll();
	},

	// override
	enableModify: function(all) {
		this.mixins.formFunctions.enableModify.call(this, all);
		this.items.each(function(item) {
			if (item.enableNonFieldElements) {
				item.enableNonFieldElements();
			}
		});
	},

	// override
	disableModify: function(enableCMTBar) {
		this.mixins.formFunctions.disableModify.call(this, enableCMTBar);
		this.items.each(function(item) {
			if (item.disableNonFieldElements) {
				item.disableNonFieldElements();
			}
		});
	}
});