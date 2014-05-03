(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep7Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep7', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'connector',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep7Delegate', this);

			Ext.apply(this, {
				items: [
				]
			});

			this.callParent(arguments);
		}
	});

})();