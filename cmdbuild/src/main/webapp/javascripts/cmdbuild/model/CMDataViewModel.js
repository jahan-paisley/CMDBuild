Ext.define('CMDBuild.model.CMDataViewModel', {
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
		name: 'filter',
		type: 'auto'
	}, {
		name: 'sourceClassName',
		type: 'string'
	}, {
		name: 'sourceFunction',
		type: 'auto'
	}, {
		name: 'type',
		type: 'string'
	}]
});