(function() {

	Ext.define('CMDBuild.model.CMModelEmailAccounts.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ADDRESS, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.CMModelEmailAccounts.singleAccount', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ADDRESS, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ENABLE_MOVE_REJECTED_NOT_MATCHING, type: 'boolean' },
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.IMAP_PORT, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.IMAP_SERVER, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.IMAP_SSL, type: 'boolean' },
			{ name: CMDBuild.ServiceProxy.parameter.INCOMING_FOLDER, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.IS_DEFAULT, type: 'boolean' },
			{ name: CMDBuild.ServiceProxy.parameter.PASSWORD, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.PROCESSED_FOLDER, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.REJECTED_FOLDER, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.SMTP_PORT, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.SMTP_SERVER, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.SMTP_SSL, type: 'boolean' },
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.USERNAME, type: 'string' }
		]
	});

})();