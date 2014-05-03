(function() {

	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.view.management.classes.CMCardTabPanel", {
		extend: "Ext.tab.Panel",

		constructor: function() {

			var tabs = CMDBuild.model.CMUIConfigurationModel.cardTabs;
			var disabledTabs = _CMUIConfiguration.getDisabledCardTabs();

			this.cardPanel = new CMDBuild.view.management.classes.CMCardPanel({
				title: tr.tabs.card,
				border: false,
				withToolBar: true,
				withButtons: true
			});

			this.cardHistoryPanel = isEnabled(disabledTabs, tabs.history) ? new CMDBuild.view.management.classes.CMCardHistoryTab({
				title: tr.tabs.history,
				border: false,
				disabled: true
			}) : null;

			this.cardNotesPanel = isEnabled(disabledTabs, tabs.notes) ? new CMDBuild.view.management.classes.CMCardNotesPanel({
				title: tr.tabs.notes,
				disabled: true
			}) : null;

			this.relationsPanel = isEnabled(disabledTabs, tabs.relations) ? new CMDBuild.view.management.classes.CMCardRelationsPanel({
				title: tr.tabs.relations,
				border: false,
				disabled: true
			}) : null;

			this.mdPanel = isEnabled(disabledTabs, tabs.details) ? new CMDBuild.view.management.classes.masterDetails.CMCardMasterDetail({
				title: tr.tabs.detail,
				disabled: true
			}) : null;

			this.attachmentPanel = isEnabled(disabledTabs, tabs.attachments) ? new CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel({
				title: tr.tabs.attachments,
				disabled: true
			}): null;

			this.callParent(arguments);

			this.cardPanel.displayMode();
		},

		initComponent: function() {
			this.frame = false;
			this.items = [
				this.cardPanel,
				this.mdPanel,
				this.cardNotesPanel,
				this.relationsPanel,
				this.cardHistoryPanel,
				this.attachmentPanel
			];

			this.callParent(arguments);
		},

		reset: function(idClass) {
			this.activateFirstTab();
			this.items.each(function(item) {
				if (item.reset) {
					item.reset();
				}
				if (item.onClassSelected) {
					item.onClassSelected(idClass);
				}
			});
		},

		activateRelationTab: function() {
			this.setActiveTab(this.relationsPanel);
		},

		getCardPanel: function() {
			return this.cardPanel;
		},

		getMDPanel: function() {
			return this.mdPanel;
		},

		getHistoryPanel: function() {
			return this.cardHistoryPanel;
		},

		getRelationsPanel: function() {
			return this.relationsPanel;
		},

		getNotePanel: function() { // TODO: substitute with getNotesPanel
			return this.cardNotesPanel;
		},

		// CMTabbedWidgetDelegate

		getAttachmentsPanel: function() {
			return this.attachmentPanel;
		},

		getNotesPanel: function() {
			return this.cardNotesPanel;
		},

		showWidget: function (w) {
			return false; // not implemented yet
		},

		activateFirstTab: function() {
			this.setActiveTab(this.cardPanel);
		}
	});

	function isEnabled(disabledTabs, name) {
		return !Ext.Array.contains(disabledTabs, name);
	}
})();