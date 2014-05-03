(function() {

	Ext.define("CMDBuild.view.management.CMMiniCardGridDelegate", {
		constructor: function(config) {
			Ext.apply(this, config);
			return this.callParent(arguments);
		},
		/**
		 * @param {CMDBuild.view.management.CMMiniCardGridDelegat} grid This grid
		 */
		miniCardGridDidActivate: Ext.empfyFn,

		/**
		 * @param {CMDBuild.view.management.CMMiniCardGridDelegat} grid This grid
		 * @param {object} card An object with Id e IdClass for the card
		 */
		miniCardGridWantOpenCard: Ext.emptyFn,

		/**
		 * @param {CMDBuild.view.management.CMMiniCardGridDelegat} grid This grid
		 * @param {Ext.data.Model} record the recrod that was selected
		 */
		miniCardGridItemSelected: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.CMMiniCardGrid", {
		extend: "Ext.grid.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
					"CMDBuild.view.management.CMMiniCardGridDelegate");

			this.callParent(arguments);
		},

		// configuration
		withPagingBar: true,
		denySelection: true,

		initComponent: function() {
			var me = this;

			ensureDataSource(me);
			addPagingBar(me);

			this.columns = this.columns || [];

			this.columns = this.columns.concat([{
				text: CMDBuild.Translation.management.modcard.relation_columns.code,
				dataIndex: 'Code',
				flex: 1,
				sortable: true
			}, {
				text: CMDBuild.Translation.management.modcard.relation_columns.description,
				dataIndex: 'Description',
				flex: 2,
				sortable: true
			}, {
				width : 40,
				menuDisabled : true,
				xtype : 'actioncolumn',
				tooltip : CMDBuild.Translation.management.modcard.open_relation,
				align : 'center',
				icon : 'images/icons/bullet_go.png',
				handler : function(grid, rowIndex, colIndex, actionItem,
						event, record, row) {
					askToOpenCardForRecord(me, record);
				},
				isDisabled : function(view, rowIdx, colIdx, item, record) {
					return false;
				}
			}]);


			this.mon(this, "activate", function() {
				this.callDelegates("miniCardGridDidActivate", this);
			}, this);

			this.mon(this, "beforeselect", function(selectionModel, record) {
				if (this.denySelection) {
					return false;
				} else {
					this.callDelegates("miniCardGridItemSelected", [me, record]);
					return true;
				}
			}, this);

			this.mon(this, "itemdblclick", function(gridView, record) {
				askToOpenCardForRecord(me, record);
			}, this);

			this.callParent(arguments);
		},

		/**
		 * 
		 * @param {Ext.data.Model} row The row to select
		 */
		selectRecordSilently: function(row) {
			if (!row) {
				return;
			}

			try {
				var sm = this.getSelectionModel();
				if (sm) {
					sm.suspendEvents();
					sm.select(row);
					sm.resumeEvents();
				}
			} catch (e) {
				_debug("ERROR selecting the CMMiniCardGrid", e);
			}
		},

		selectCardSilently: function(card) {
			deselectSilently(this);

			if (!card) {
				return;
			}

			var record = this.store.findRecord("Id", card.get("Id"));
			if (record) {
				this.selectRecordSilently(record);
			}
		},

		getDataSource: function() {
			return this.dataSource;
		}
	});

	function ensureDataSource(me) {
		if (!me.dataSource) {
			me.dataSource = new CMDBuild.data.CMMiniCardGridBaseDataSource();
		}

		me.store = me.dataSource.getStore();
	}

	function addPagingBar(me) {
		if (me.withPagingBar) {
			var filter = new CMDBuild.field.GridSearchField({grid: me});
			var menu = new Ext.menu.Menu({
				items: [filter]
			});

			me.bbar = new Ext.toolbar.Paging({
				store: me.store,
				items: [{
					iconCls: 'search',
					menu: menu
				}]
			});
		}
	}

	function askToOpenCardForRecord(me, record) {
		// to highlight the relative row
		me.selectRecordSilently(record);

		me.callDelegates( "miniCardGridWantOpenCard", [me,{
			Id : record .get("Id"),
			IdClass : record .get("IdClass")
		}]);
	}

	function deselectSilently(me) {
		try {
			var sm = me.getSelectionModel();
			if (sm) {
				var suppressEvent = true;
				sm.deselectAll(suppressEvent);
			}
		} catch (e) {
			_debug("ERROR deselecting the mini card grid", e);
		}
	}
})();