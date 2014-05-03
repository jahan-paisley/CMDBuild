(function() {

	var tr = CMDBuild.Translation.administration.email; // Path to translation

	Ext.define('CMDBuild.view.administration.accordion.CMAccordionEmail', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: tr.title,
		cmName: 'email',

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
		},

		updateStore: function() {
			var root = this.store.getRootNode();

			root.appendChild([
				{
					text: tr.accounts.title,
					leaf: true,
					cmName: 'emailAccounts'
				},
				{
					text: tr.templates.title,
					leaf: true,
					cmName: 'emailTemplates'
				}
			]);
		}
	});

})();