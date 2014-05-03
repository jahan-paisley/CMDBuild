(function() {

	Ext.define('CMDBuild.model.CMModelTasks.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.TYPE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'}
		]
	});

	// Model used from Processes -> Task Manager tab
	Ext.define('CMDBuild.model.CMModelTasks.grid.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.ATTACHMENTS_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.ATTACHMENTS_CATEGORY, type: 'int'},
			{ name: CMDBuild.ServiceProxy.parameter.ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.KEY_END, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.KEY_INIT, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.VALUE_END, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.VALUE_INIT, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

})();