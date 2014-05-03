(function() {

	Ext.define("CMDBuild.view.management.CMMiniCardGridModel", {
		extend: "Ext.data.Model",
		fields: [{
			name: "Id", type: "int"
		}, {
			name: "IdClass", type: "int"
		}, {
			name: "ClassName", type: "string"
		}, {
			name: "Code", type: "string"
		}, {
			name: "Description", type: "string"
		}, {
			name: "Details", type: "auto"
		}, {
			name: "Attributes", tyoe: "auto"
		}],

		getDetails: function() {
			return this.get("Details") || [];
		},

		getAttributes: function() {
			return this.get("Attributes") || [];
		}
	});

	Ext.define("CMDBuild.data.CMMiniCardGridBaseDataSource", {
		constructor: function() {
			this.store = new Ext.data.Store ({
				pageSize: _CMUtils.grid.getPageSize(),
				model: 'CMDBuild.view.management.CMMiniCardGridModel',
				autoLoad: false,
				remoteSort: true,
				proxy: {
					type: "ajax",
					url: 'services/json/management/modcard/getcardlistshort',
					reader: {
						root: "rows",
						type: "json",
						totalProperty: "results",
						idProperty: "Id"
					},
					extraParams: {
						attributes: Ext.encode(["Id", "IdClass", "Code", "Description"])
					}
				},
				sorters: [{
					property: 'Code',
					direction: 'ASC'
				}, {
					property: 'Description',
					direction: 'ASC'
				}]
			});

			return this.callParent(arguments);
		},

		getStore: function() {
			return this.store;
		},

		getLastEntryTypeIdLoaded: function() {
			if (this.store.proxy.extraParams) {
				return this.store.proxy.extraParams.IdClass;
			}

			return null;
		},

		loadStoreForEntryTypeId: function(entryTypeId, cb) {
			this.store.proxy.setExtraParam("className", _CMCache.getEntryTypeNameById(entryTypeId));
			this.store.load({
				callback: cb
			});
		},

		loadPageForCard: function(card, cb) {
			// TODO
		}
	});
})();