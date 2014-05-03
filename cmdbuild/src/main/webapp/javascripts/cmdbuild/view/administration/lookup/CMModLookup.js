Ext.define("CMDBuild.Administration.ModLookup", {
	extend: "Ext.panel.Panel",
	cmName: 'lookuptype',
	translation: CMDBuild.Translation.administration.modLookup,
	initComponent: function() {

		this.addLookupTypeButton = new Ext.button.Button({
			iconCls:'add',
			text: this.translation.add_lookuptype
		});

		this.lookupTypeForm = new CMDBuild.view.administration.lookup.CMLookupTypeForm();
		this.lookupGrid = new CMDBuild.view.administration.lookup.CMLookupGrid({region: "center"});
		this.lookupForm = new CMDBuild.view.administration.lookup.CMLookupForm({
			height : '50%',
			region : 'south',
			split : true
		});

		this.tabPanel = new Ext.tab.Panel({
			activeTab : 0,
			frame : false,
			border : false,
			margin: "1 0 0 0",
			items : [ {
				title : this.translation.tabs.properties,
				layout : 'fit',
				frame : false,
				border : false,
				items : [ this.lookupTypeForm ],
				padding : "1 0 0 0"
			}, {
				title : this.translation.tabs.lookuplist,
				layout : 'border',
				border : false,
				frame : false,
				items : [ this.lookupGrid, this.lookupForm ]
			}]
		});

		Ext.apply(this, {
			hideMode: 'offsets', // fix a render bug of combobox
			title: this.translation.title,
			basetitle: this.translation.title+ ' - ',
			layout: 'fit',
			id: this.id + '_panel',
			tbar: [this.addLookupTypeButton],
			items: [this.tabPanel],
			frame: false,
			border: true
		});


		this.callParent(arguments);
	},

	onSelectLookupType: function(eventParams) {
		if (eventParams) {
			this.addLookupTypeButton.enable();
			this.enableLookupTab();
		}
	},
	
	activateLookupTypeForm: function() {
		this.tabPanel.setActiveTab(this.tabPanel.items.get(0));
	},
	
	disableLookupTab: function() {
		this.tabPanel.items.get(1).disable();
	},
	
	enableLookupTab: function() {
		this.tabPanel.items.get(1).enable();
	},
	
	onAbortNewLtype: function() {
		this.addLookupTypeButton.enable();
	}
});