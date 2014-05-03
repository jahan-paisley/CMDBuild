(function() {
	var LOOKUP_FIELDS = CMDBuild.ServiceProxy.LOOKUP_FIELDS;

	Ext.define("CMLookupTypeForCombo", {
		extend: 'Ext.data.Model',
		fields: [ {
			name: "type",
			type: 'string'
		} ]
	});
	
	Ext.define("CMLookupTypeForParentStoreCombo", {
		extend: 'Ext.data.Model',
		fields: [
			{name: LOOKUP_FIELDS.ParentDescription, type: "string"},
			{name: LOOKUP_FIELDS.ParentId, type: "int"}
		]
	});
	
	Ext.define("CMLookupForGrid", {
		extend: 'Ext.data.Model',
		fields: [
			{name: LOOKUP_FIELDS.Code, type: 'string'},
			{name: LOOKUP_FIELDS.Description, type: 'string'},
			{name: LOOKUP_FIELDS.Index, type: 'int'},
			{name: LOOKUP_FIELDS.ParentId, type: 'int'},
			{name: LOOKUP_FIELDS.ParentDescription, type: 'string'},
			{name: LOOKUP_FIELDS.Active, type: 'string'},
			{name: LOOKUP_FIELDS.Id, type: 'string'},
			{name: LOOKUP_FIELDS.Notes, type: 'string'}
		]
	});

    Ext.define("CMLookupFieldStoreModel", {
        extend: "Ext.data.Model",
        fields: [
			{name: LOOKUP_FIELDS.Id, type: 'int'},
			{name: LOOKUP_FIELDS.Description, type: "string"},
			{name: LOOKUP_FIELDS.ParentId, type: "int"},
			{name: LOOKUP_FIELDS.Index, type: "int"}
        ]
    });
})();