(function() {

	Ext.define('CMDBuild.model.CMModelEmailTemplates.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.SUBJECT, type: 'string' }
		]

	});

	Ext.define('CMDBuild.model.CMModelEmailTemplates.singleTemplate', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.TO, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.CC, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.BCC, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.SUBJECT, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.BODY, type: 'string' }
		]
	});

})();
