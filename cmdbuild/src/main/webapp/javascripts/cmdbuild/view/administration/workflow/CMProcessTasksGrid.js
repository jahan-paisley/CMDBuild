(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.workflow.CMProcessTasksGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			this.gridColumns = [
				{
					dataIndex: CMDBuild.ServiceProxy.parameter.ID,
					hidden: true
				},
				{
					text: CMDBuild.Translation.description_,
					dataIndex: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					flex: 2
				},
				{
					text: tr.active,
					width: 60,
					align: 'center',
					dataIndex: CMDBuild.ServiceProxy.parameter.ACTIVE,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					scope: this,
					renderer: function(value, metaData, record) {
						return this.activeGridColumnRenderer(value, metaData, record);
					}
				}
			];

			Ext.apply(this, {
				columns: this.gridColumns
			});

			this.callParent(arguments);
		},


		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick', {
					id: record.get(CMDBuild.ServiceProxy.parameter.ID),
					type: 'workflow'
				});
			},

			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected');
			}
		},

		/**
		 * @param {Object} value
		 * Used to render active database value to add icon
		 */
		activeGridColumnRenderer: function(value, metaData, record) {
			if(typeof value == 'boolean') {
				if(value) {
					value = '<img src="images/icons/accept.png" alt="' + tr.running + '" />';
				} else {
					value = '<img src="images/icons/cancel.png" alt="' + tr.stopped + '" />';
				}
			}

			return value;
		}
	});

})();