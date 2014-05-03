(function() {
	var tr = CMDBuild.Translation.administration.modClass.attributeProperties,
		tr_geo = CMDBuild.Translation.administration.modClass.geo_attributes;

	Ext.define("CMDBuild.Administration.LayerGrid", {
		extend: "Ext.grid.Panel",
		region: 'center',
		frame: false,
		border: false,

		store: _CMCache.getLayersStore(),

		// custom stuff
		withCheckToHideLayer: false,
		cmCheckColumnReadOnly: true,

		sm: new Ext.selection.RowModel(),
		initComponent: function() {
			this.columns = [{
				header: tr.description,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "description",
				flex: 1
			},{
				header: tr_geo.master,
				hideable: true,
				hidden: false,
				sortable: false,
				dataIndex: "masterTableName",
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

			if (this.withCheckToHideLayer) {
				this.columns.push(buildCheckColumn.call(this));
			}

			this.viewConfig = {
				loadMask: false,
				plugins : {
					ptype : 'gridviewdragdrop',
					dragGroup : 'layersGridDDGroup',
					dropGroup : 'layersGridDDGroup'
				},
				listeners : {
					scope: this,
					beforedrop: Ext.emptyFn,
					drop : this.beforeRowMove
				}
			};
			this.callParent(arguments);
		},
		
		/**
		 * template method for the subclasses
		 */
		beforeRowMove: function(node, data, dropRec, dropPosition) {
			return true;
		},

		/**
		 * template method for the subclasses
		 */
		onVisibilityChecked: function(cell, recordIndex, checked) {}
	});

	function buildCheckColumn() {
		var column = new Ext.ux.CheckColumn( {
			header: tr_geo.visibility,
			sortable: false,
			dataIndex: "isvisible",
			cmReadOnly: this.cmCheckColumnReadOnly
		});

		this.mon(column, "checkchange", this.onVisibilityChecked, this);

		this.getVisibilityColDataIndex = function() {
			return column.dataIndex;
		};

		return column;
	};
})();