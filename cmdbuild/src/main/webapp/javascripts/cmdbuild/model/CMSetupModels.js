(function() {
	Ext.define("TranslationModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "name", type: 'string'},
			{name: "value",  type: 'string'}
		]
	});
	
	Ext.define("CMTableForComboModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "name", type: 'string'},
			{name: "id",  type: 'string'},
			{name: "description",  type: 'string'}
		]
	});
})();