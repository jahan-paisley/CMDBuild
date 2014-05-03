(function() {
	
	var tr_attributes = CMDBuild.Translation.administration.modClass.attributeProperties;

	var columns = [{
		header: tr_attributes.type,
		sortable: true,
		dataIndex: 'type',
		flex: 1
	},{
		header: tr_attributes.name,
		sortable: true,
		dataIndex: 'name',
		flex: 1
	},{
		header: tr_attributes.description,
		sortable: true,
		dataIndex: 'description',
		flex: 1
	}];

	Ext.define("CMDBuild.view.administration.classes.CMGeoAttributesGrid", {
		extend: "CMDBuild.view.administration.classes.CMAttributeGrid",

		initComponent: function() {
			this.callParent(arguments);

			this.on("render", function() {
					if (this.danglingStore) {
						this.reconfigure(this.danglingStore, this.columns);
					}
				}, this, {
					single: true
				});
		},

		// override
		buildColumnConf: function() {
			this.columns = columns;
		},

		// override
		buildStore: function() {
			this.store = new Ext.data.SimpleStore( {
				model: "GISLayerModel"
			});
		},

		//override
		buildTBar: function() {
			this.tbar = [this.addAttributeButton];
		},

		// override
		refreshStore: function(idClass, nameOfAttributeToSelect) {
			var et = _CMCache.getEntryTypeById(idClass);
			var me = this;

			_CMCache.getLayersForEntryTypeName(et.get("name"), function(layers) {
				me.store.loadData(layers);
				me.selectAttributeByName(nameOfAttributeToSelect);
			});
		},

		selectAttributeByName: function(geoAttributeName) {
			var sm = this.getSelectionModel();
			if (geoAttributeName) {
				var r = this.store.findRecord("name", geoAttributeName);
				if (r) {
					sm.select(r);
				}
			} else if (this.store.count() != 0) {
				sm.select(0);
			}
		}

	});
})();