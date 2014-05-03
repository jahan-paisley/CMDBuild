(function() {
	var tr = CMDBuild.Translation.administration.modWorkflow;

	Ext.require('CMDBuild.core.proxy.CMProxyWorkflow');

	Ext.define('CMDBuild.view.administration.workflow.CMProcess', {
		extend: 'Ext.panel.Panel',

		cmName:'process',

		constructor: function() {

			this.addClassButton = Ext.create('Ext.button.Button', {
				iconCls: 'add',
				text: tr.add_process
			});

			this.printSchema = Ext.create('CMDBuild.PrintMenuButton', {
				text: CMDBuild.Translation.administration.modClass.print_schema,
				formatList: ['pdf', 'odt']
			});

			this.processForm = Ext.create('CMDBuild.view.administration.workflow.CMProcessForm', {
				title: tr.tabs.properties
			});

			this.attributesPanel = Ext.create('CMDBuild.view.administration.workflow.CMProcessAttributes', {
				title: tr.tabs.attributes,
				border: false,
				disabled: true
			});

			this.domainGrid = Ext.create('CMDBuild.Administration.DomainGrid', {
				title: tr.tabs.domains,
				border: false,
				disabled: true
			});

			this.cronPanel = Ext.create('CMDBuild.view.administration.workflow.CMProcessTasks', {
				title: CMDBuild.Translation.administration.tasks.title,
				border: false,
				disabled: true
			});

			this.tabPanel = Ext.create('Ext.tab.Panel', {
				frame: false,
				border: false,
				activeTab: 0,

				items: [
					this.processForm,
					this.attributesPanel,
					this.domainGrid,
					this.emailTemplatePanel,
					this.cronPanel
				]
			});

			Ext.apply(this, {
				tbar: [this.addClassButton, this.printSchema],
				title: tr.title,
				basetitle: tr.title + ' - ',
				layout: 'fit',
				items: [this.tabPanel],
				frame: false,
				border: true
			});

			this.callParent(arguments);
		},

		onAddClassButtonClick: function() {
			this.tabPanel.setActiveTab(0);
		},

		onClassDeleted: function() {
			this.attributesPanel.disable();
		},

		onProcessSelected: Ext.emptyFn
	});
})();