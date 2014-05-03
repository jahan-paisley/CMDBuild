(function() {

var tr = CMDBuild.Translation.administration.modsecurity;

Ext.define("CMDBuild.view.administration.group.CMModGroup", {
	extend: "Ext.panel.Panel",
	cmName: 'group',

	initComponent: function() {
		this.addGroupButton = new Ext.button.Button( {
			iconCls : 'add',
			text : tr.group.add_group
		});

		this.groupForm = new CMDBuild.view.administration.group.CMGroupForm({
			title: tr.tabs.properties
		});

		this.classPrivilegesGrid = new CMDBuild.view.administration.group.CMGroupPrivilegeGrid({
			title: CMDBuild.Translation.administration.modClass.tree_title,
			store: _CMProxy.group.getClassPrivilegesGridStore(),
			actionURL: _CMProxy.url.privileges.classes.update,
			withFilterEditor: true,
			border: false
		});

		this.dataViewPrivilegesGrid = new CMDBuild.view.administration.group.CMGroupPrivilegeGrid({
			title: CMDBuild.Translation.views,
			store:_CMProxy.group.getDataViewPrivilegesGridStore(),
			actionURL: _CMProxy.url.privileges.dataView.update,
			withPermissionWrite: false,
			border: false
		});

		this.filterPrivilegesGrid = new CMDBuild.view.administration.group.CMGroupPrivilegeGrid({
			title: CMDBuild.Translation.search_filters,
			store:_CMProxy.group.getFilterPrivilegesGridStore(),
			actionURL: _CMProxy.url.privileges.filter.update,
			withPermissionWrite: false,
			border: false
		});

		this.privilegesPanel = new Ext.tab.Panel({
			title: tr.tabs.permissions,
			items: [
				this.classPrivilegesGrid,
				this.dataViewPrivilegesGrid,
				this.filterPrivilegesGrid
			]
		});

		this.userPerGroup = new CMDBuild.view.administration.group.CMGroupUsers({
			title: tr.users
		});

		this.uiConfigurationPanel = new CMDBuild.view.administration.group.CMGroupUIConfigurationPanel();

		this.tabPanel = new Ext.TabPanel({
			border : false,
			activeTab : 0,
			region: "center",
			items : [
				this.groupForm,
				this.privilegesPanel,
				this.userPerGroup,
				this.uiConfigurationPanel
			]
		});

		Ext.apply(this, {
			tbar:[this.addGroupButton],
			title : tr.group.title,
			basetitle : tr.group.title+ ' - ',
			items: [this.tabPanel],
			layout: "border",
			border: true
		});

		this.callParent(arguments);
	},

	onGroupSelected: function() {
		this.userPerGroup.disable();
		this.uiConfigurationPanel.disable();
	},

	onAddGroup: function() {
		this.tabPanel.setActiveTab(this.tabPanel.items.get(0));
		this.onGroupSelected();
	}
});

})();