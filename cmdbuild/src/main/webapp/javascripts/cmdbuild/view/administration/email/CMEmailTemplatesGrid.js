(function() {

	Ext.define('CMDBuild.view.administration.email.CMEmailTemplatesGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {

			this.gridColumns = [
				{
					dataIndex: CMDBuild.ServiceProxy.parameter.NAME,
					text: CMDBuild.Translation.name,
					flex: 1
				},
				{
					dataIndex: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					header: CMDBuild.Translation.description_,
					flex: 3
				},
				{
					dataIndex: CMDBuild.ServiceProxy.parameter.SUBJECT,
					header: CMDBuild.Translation.subject,
					flex: 2
				}
			];

			this.gridStore = CMDBuild.core.proxy.CMProxyEmailTemplates.getStore();

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
		}
	});

})();