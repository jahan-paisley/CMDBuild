Ext.define('CMDBuild.bim.data.CMBimLayerModel', {
	extend: 'Ext.data.Model',
	fields: [{
		name: 'className',
		type: 'string'
	}, {
		name: 'description',
		type: 'string'
	}, {
		name: 'active',
		type: 'boolean'
	}, {
		name: 'root',
		type: 'boolean'
	}, {
		name: 'export',
		type: 'boolean'
	}, {
		name: 'container',
		type: 'boolean'
	}, {
		name: 'rootreference',
		type: 'string'
	}]
});