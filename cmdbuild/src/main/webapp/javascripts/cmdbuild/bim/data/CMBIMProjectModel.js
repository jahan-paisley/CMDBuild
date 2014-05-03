Ext.define('CMDBuild.bim.data.CMBIMProjectModel', {
	extend: 'Ext.data.Model',
	fields: [{
		name: 'id',
		type: 'string'
	}, {
		name: 'name',
		type: 'string'
	}, {
		name: 'description',
		type: 'string'
	}, {
		name: 'lastCheckin',
		type: 'auto'
	}, {
		name: 'active',
		type: 'boolean'
	}, {
		name: 'cardBinding',
		type: 'int'
	}]
});