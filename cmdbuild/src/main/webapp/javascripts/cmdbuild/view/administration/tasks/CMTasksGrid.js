(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.tasks.CMTasksGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {
			this.gridColumns = [
				{
					dataIndex: CMDBuild.ServiceProxy.parameter.ID,
					hidden: true
				},
				{
					text: tr.type,
					dataIndex: CMDBuild.ServiceProxy.parameter.TYPE,
					flex: 1,
					scope: this,
					renderer: function(value, metaData, record) {
						return this.typeGridColumnRenderer(value, metaData, record);
					},
				},
				{
					text: CMDBuild.Translation.description_,
					dataIndex: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					flex: 4
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
				},
				{
					xtype: 'actioncolumn',
					align: 'center',
					width: 25,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							icon: 'images/icons/control_play.png',
							tooltip: tr.startLabel,
							scope: this,
							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.delegate.cmOn('onStartButtonClick', record);
							},
							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return record.get(CMDBuild.ServiceProxy.parameter.ACTIVE);
							}
						}
					]
				},
				{
					xtype: 'actioncolumn',
					align: 'center',
					width: 25,
					sortable: false,
					hideable: false,
					menuDisabled: true,
					fixed: true,
					items: [
						{
							icon: 'images/icons/control_stop.png',
							tooltip: tr.stopLabel,
							scope: this,
							handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
								this.delegate.cmOn('onStopButtonClick', record);
							},
							isDisabled: function(grid, rowIndex, colIndex, item, record) {
								return !record.get(CMDBuild.ServiceProxy.parameter.ACTIVE);
							}
						}
					]
				}
			];

//			// TODO: maybe for a future implementation
//			this.pagingBar = Ext.create('Ext.toolbar.Paging', {
//				store: this.store,
//				displayInfo: true,
//				displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
//				emptyMsg: CMDBuild.Translation.common.display_topic_none
//			});

			Ext.apply(this, {
//				bbar: this.pagingBar,
				columns: this.gridColumns
			});

			this.callParent(arguments);
		},


		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick');
			},

			select: function(model, record, index, eOpts) {
				this.delegate.cmOn('onRowSelected');
			}
		},

		/**
		 * Used to render active value to add icon in grid
		 */
		activeGridColumnRenderer: function(value, metaData, record) {
			if (typeof value == 'boolean') {
				if (value) {
					value = '<img src="images/icons/accept.png" alt="' + tr.running + '" />';
				} else {
					value = '<img src="images/icons/cancel.png" alt="' + tr.stopped + '" />';
				}
			}

			return value;
		},

		/**
		 * Rendering task type translating with local language data
		 */
		typeGridColumnRenderer: function(value, metaData, record) {
			if (typeof value == 'string') {
				if (this.delegate.correctTaskTypeCheck(value)) {
					var splittedType = value.split('_');
					value = '';

					for (var i = 0; i < splittedType.length; i++) {
						if (i == 0) {
							value += eval('CMDBuild.Translation.administration.tasks.tasksTypes.' + splittedType[i]);
						} else {
							value += ' ' + eval('CMDBuild.Translation.administration.tasks.tasksTypes.' + splittedType[0] + 'Types.' + splittedType[i]);
						}
					}
				}
			}

			return value;
		}
	});

})();