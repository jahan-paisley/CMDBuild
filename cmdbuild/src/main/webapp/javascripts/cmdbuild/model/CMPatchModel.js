(function() {
	Ext.define("CMPatchModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "name",  type: 'string'},
			{name: "description",  type: 'string'}
		]
	});
})();