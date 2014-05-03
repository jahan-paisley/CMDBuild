Ext.define("CMDBuild.view.management.common.widgets.CMManageEmail", {
	extend: "Ext.panel.Panel",

	loaded: false,

	statics: {
		WIDGET_NAME: ".ManageEmail"
	},

	constructor: function(c) {
		this.widgetConf = c.widget;
		this.activity = c.activity.raw || c.activity.data;

		this.callParent([this.widgetConf]); // to apply the conf to the panel
	},

	initComponent : function() {
		this.emailGrid = new CMDBuild.view.management.common.widgets.CMEmailGrid({
			autoScroll : true,
			processId : this.activity.Id,
			readWrite : !this.widgetConf.ReadOnly,
			frame: false,
			border: false
		});

		_CMUtils.forwardMethods(this, this.emailGrid, [
			"addTemplateToStore",
			"addToStoreIfNotInIt",
			"hasDraftEmails",
			"removeTemplatesFromStore",
			"getDraftEmails",
			"getNewEmails",
			"removeRecord",
			"setDelegate"
		]);

		this.frame = false;
		this.border = false;
		this.items = [this.emailGrid];
		this.cls = "x-panel-body-default-framed";

		this.callParent(arguments);
	},

	getOutgoing: function(modifiedOnly) {
		var allOutgoing = modifiedOnly ? false : true;
		var outgoingEmails = [];
		var emails = this.emailGrid.getStore().getRange();
		for (var i=0, n=emails.length; i<n; ++i) {
			var currentEmail = emails[i];
			if (allOutgoing || !currentEmail.get("Id") || currentEmail.dirty) {
				outgoingEmails.push(currentEmail.data);
			}
		}
		return outgoingEmails;
	},

	getDeletedEmails: function() {
		return this.emailGrid.deletedEmails;
	}
});
