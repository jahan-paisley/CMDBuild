(function() {

	var LOOKUP_FIELDS = {
		Id: 'Id',
		Code: 'Code',
		Description: 'Description',
		ParentId: 'ParentId',
		Index: 'Number',
		Type: 'Type',
		ParentDescription: 'ParentDescription',
		Active: 'Active',
		Notes: 'Notes'
	};

	CMDBuild.ServiceProxy.LOOKUP_FIELDS = LOOKUP_FIELDS;

	CMDBuild.ServiceProxy.lookup = {
		readAllTypes: function(p) {
			p.method = 'GET';
			p.url = "services/json/schema/modlookup/tree";
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		getLookupFieldStore: function(type) {
			var s = Ext.create("Ext.data.Store", {
				model: "CMLookupFieldStoreModel",
				proxy: {
					type: 'ajax',
					url : 'services/json/schema/modlookup/getlookuplist',
					reader: {
						type: 'json',
						root: 'rows'
					},
					extraParams : {
						type : type,
						active : true,
						"short" : true
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names not handled correctly
				},
				sorters : [ {
					property : LOOKUP_FIELDS.Index,
					direction : "ASC"
				}],
				autoLoad : true,

				// Disable paging
				defaultPageSize: 0,
				pageSize: 0
			});

			return s;
		},

		getLookupGridStore: function() {
			return new Ext.data.Store({
				model : "CMLookupForGrid",
				autoLoad : false,
				proxy : {
					type : 'ajax',
					url : 'services/json/schema/modlookup/getlookuplist',
					reader : {
						type : 'json',
						root : 'rows'
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names not handled correctly
				},
				sorters : [ {
					property : 'Number',
					direction : "ASC"
				}]
			});
		},

		setLookupDisabled: function(p, disable) {
			var url = 'services/json/schema/modlookup/enablelookup';
			if (disable) {
				url = 'services/json/schema/modlookup/disablelookup';
			}

			p.method = 'POST';
			p.url = url;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		saveLookup: function(p) {
			p.method = 'POST';
			p.url = "services/json/schema/modlookup/savelookup";

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		saveLookupType: function(p) {
			p.method = 'POST';
			p.url = "services/json/schema/modlookup/savelookuptype";

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();