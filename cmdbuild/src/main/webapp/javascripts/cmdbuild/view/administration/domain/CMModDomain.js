(function() {
	var translationDomainProp = CMDBuild.Translation.administration.modClass.domainProperties;
	var translationModClass = CMDBuild.Translation.administration.modClass;
	
	var baseTitle = translationModClass.tabs.domains;
	
	Ext.define("CMDBuild.view.administration.domain.CMModDomain", {
		extend: "Ext.panel.Panel",
		
		cmName:'domain',	
		translation: CMDBuild.Translation.administration.modClass,
		
		NAME: "CMModDomain",

		constructor: function() {
			this.addButton = new Ext.Button({
				iconCls: 'add',
				text: translationDomainProp.add_domain
			});

			this.domainForm = new CMDBuild.view.administration.domain.CMDomainForm({
				title: translationModClass.tabs.properties
			});

			this.domainAttributes = new CMDBuild.administration.domain.CMDomainAttribute({
				title: translationModClass.tabs.attributes
			});

			this.tabPanel = new Ext.TabPanel({
				region: "center",
				frame: false,
				border: false,
				items: [this.domainForm, this.domainAttributes],
				activeTab: 0
			});
			
			this.callParent(arguments);
		},

		initComponent : function() {
			this.layout = "border";
			this.title = baseTitle;
			this.tbar = [this.addButton];
			this.items = [this.tabPanel];
			this.frame = false;
			this.border = true;
			this.callParent(arguments);
		},

		selectPropertiesTab: function() {
			this.tabPanel.setActiveTab(this.domainForm);
		},

		onDomainDeleted: function() {
			this.domainAttributes.disable();
			this.domainForm.disableModify();
		},

		setTitleSuffix: function(domainDescription) {
			if (typeof domainDescription != "undefined") {
				this.setTitle(baseTitle + " - " + domainDescription);
			} else {
				this.setTitle(baseTitle);
			}
		} 
	});

})();