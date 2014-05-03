Ext.define("CMDBuild.view.management.common.CMTabPanel", {
	extend: "Ext.tab.Panel",
	plain: true,

	initComponent: function() {
		this.tabPosition = CMDBuild.Config.cmdbuild.card_tab_position || "top",
		this.callParent(arguments);
		if (this.items.getCount() == 1) {
			this.getTabBar().hide();
		}
	},

	activateFirst: function() {
		this.setActiveTab(0);
	},
	//http://www.sencha.com/forum/showthread.php?261407-4.2.0-HTML-editor-SetValue-does-not-work-when-component-is-not-rendered	
	//This function for fixing the above bug
	//To delete when upgrade at extjs 4.2.1
	showAll: function() {
		var activeTab = this.getActiveTab();
		for (var i = 0; i < this.items.length; i++) {
			this.setActiveTab(i);
		}
		if (this.items.length > 0 && activeTab)
			this.setActiveTab(activeTab);
	},
	editMode: function() {
		this.items.each(function(item) {
			if (typeof item.editMode == "function") {
				item.editMode();
			}
		});
	},

	displayMode: function() {
		this.items.each(function(item) {
			if (typeof item.displayMode == "function") {
				item.displayMode();
			}
		});
	},

	ensureEditPanel: function() {
		this.items.each(function(item) {
			if (typeof item.ensureEditPanel == "function") {
				item.ensureEditPanel();
			}
		});
	}
});