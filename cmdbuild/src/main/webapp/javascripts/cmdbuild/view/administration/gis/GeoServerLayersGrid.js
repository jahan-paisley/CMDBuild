(function() {
	var tr = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_geo = CMDBuild.Translation.administration.modClass.geo_attributes;

	Ext.define("CMDBuild.Administration.GeoServerLayerGrid", {
		extend: "Ext.grid.Panel",

		region: 'center',
		frame: false,
		border: false,
		loadMask: true,

		initComponent: function() {
			this.sm = new Ext.selection.RowModel();
			this.store = CMDBuild.ServiceProxy.geoServer.getGeoServerLayerStore();
			this.columns = [{
				header: tr.name,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "name",
				flex: 1
			},{
				header: tr.description,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "description",
				flex: 1
			},{
				header: tr.type,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "type",
				flex: 1
			},{
				header: tr_geo.min_zoom,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "minZoom",
				flex: 1
			},{
				header: tr_geo.max_zoom,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "maxZoom",
				flex: 1
			}];

			this.callParent(arguments);
		},

		clearSelection: function() {
			this.getSelectionModel().deselectAll();
		},

		onModShow: function(firstLoad) {
			this.store.load();
		},

		selectFirstIfUnselected: function() {
			var sm = this.getSelectionModel();
			if (!sm.hasSelection()) {
				this.selectFirst();
			}
		},

		selectFirst: function(attempts) {
			var _attempts = attempts || 10;
			var me = this;
			if (this.store.isLoading()) {
				Ext.Function.createDelayed(me.selectFirst, 500, me, [--_attempts])();
				return;
			}

			if (this.store.count() != 0) {
				try {
					var sm = this.getSelectionModel();
					sm.select(0);
				} catch (e) { }
			}
		},

		loadStoreAndSelectLayerWithName: function(name) {
			var me = this;
			this.store.load({
				callback: function(records, operation, success) {
					var toSelect = me.store.find("name", name);
					if (toSelect >= 0) {
						me.getSelectionModel().select(toSelect);
					} else {
						me.selectFirst();
					}
				}
			});
		}
	});

})();