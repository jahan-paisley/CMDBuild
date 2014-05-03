(function() {

	var tr = CMDBuild.Translation.administration.email.accounts; // Path to translation

	Ext.define('CMDBuild.view.administration.email.CMEmailAccountsGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {
			this.gridColumns = [
				{
					text: tr.isDefault,
					dataIndex: CMDBuild.ServiceProxy.parameter.IS_DEFAULT,
					align: 'center',
					width: 50,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					scope: this,
					renderer: this.defaultGridColumnRenderer
				},
				{
					text: CMDBuild.Translation.name,
					dataIndex: CMDBuild.ServiceProxy.parameter.NAME,
					flex: 1
				},
				{
					text: tr.address,
					dataIndex: CMDBuild.ServiceProxy.parameter.ADDRESS,
					flex: 1
				}
			];

			this.gridStore = CMDBuild.core.proxy.CMProxyEmailAccounts.getStore();

			Ext.apply(this, {
				columns: this.gridColumns,
				store: this.gridStore
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected');
			},

			/**
			 * Event to load store on view display and first row selection as CMDbuild standard
			 */
			viewready: function() {
				this.store.load({
					scope: this,
					callback: function() {
						if (!this.getSelectionModel().hasSelection())
							this.getSelectionModel().select(0, true);
					}
				});
			}
		},

		/**
		 * isDefault renderer to add icon in grid
		 *
		 * @param (Object) value
		 */
		defaultGridColumnRenderer: function(value) {
			if(typeof value == 'boolean') {
				if(value) {
					value = '<img src="images/icons/tick.png" alt="Is Default" />';
				} else {
					value = null;
				}
			}

			return value;
		}
	});

})();