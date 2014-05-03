(function() {

	Ext.define("CMDBuild.view.administration.common.CMDynamicKeyValueGrid", {
		extend: "CMDBuild.view.administration.common.CMKeyValueGrid",

		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				tbar: [{
					text: CMDBuild.Translation.common.buttons.add,
					iconCls: 'add',
					handler: function() {
						var m = new me.store.model();

						if (m) {
							me.store.insert(0, m);
							me.cellEditing.startEditByPosition({ row: 0, column: 0 });
						}
					}
				}]
			})

			this.callParent(arguments);
			this.mon(this, 'beforeitemclick', onCellClick, this);
		},

		// override
		getColumnsConf: function() {
			var c = this.callParent(arguments);

			c.push({
				width: 30,
				fixed: true,
				sortable: false,
				renderer: function renderDeleteActions() {
					return '<img style="cursor:pointer" title="' +
					CMDBuild.Translation.administration.modClass.attributeProperties.meta.remove +
					'" class="action-meta-delete" src="images/icons/cross.png"/>&nbsp;';
				},
				align: 'center',
				dataIndex: 'delete',
				menuDisabled: true,
				hideable: false
			});

			return c;
		}
	});

	function onCellClick(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className;

		if (className == "action-meta-delete")
			this.store.remove(model);
	}

})();