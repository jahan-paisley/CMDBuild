(function() {

	var tr = CMDBuild.Translation.administration.email.templates; // Path to translation

	Ext.define('CMDBuild.view.administration.email.CMEmailTemplates', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		title: tr.title,
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			this.addButton = Ext.create('Ext.Button', {
				iconCls: 'add',
				text: tr.add,
				scope: this,
				handler: function() {
					this.delegate.cmOn('onAddButtonClick');
				}
			});

			this.grid = Ext.create('CMDBuild.view.administration.email.CMEmailTemplatesGrid', {
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.email.CMEmailTemplatesForm', {
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.ServiceProxy.parameter.TOOLBAR_TOP,
						items: [this.addButton]
					}
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();