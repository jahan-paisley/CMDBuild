(function() {

	Ext.require('CMDBuild.model.CMModelEmailAccounts');

	Ext.define('CMDBuild.core.proxy.CMProxyEmailAccounts', {
		statics: {
			create: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.accounts.post,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			get: function() {
				return Ext.create('Ext.data.JsonStore', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelEmailAccounts.singleAccount',
					proxy: {
						type: 'ajax',
						url: CMDBuild.core.proxy.CMProxyUrlIndex.email.accounts.get,
						reader: {
							type: 'json',
							root: 'response'
						}
					}
				});
			},

			getStore: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelEmailAccounts.grid',
					proxy: {
						type: 'ajax',
						url: CMDBuild.core.proxy.CMProxyUrlIndex.email.accounts.getStore,
						reader: {
							type: 'json',
							root: 'response.elements'
						}
					},
					sorters: [{
						property: CMDBuild.ServiceProxy.parameter.NAME,
						direction: 'ASC'
					}]
				});
			},

			remove: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.accounts.delete,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			setDefault: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.accounts.setDefault,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			update: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.email.accounts.put,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			}
		}
	});

})();