(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.model.CMModelReference', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.VALUE, type: 'string' }
		]
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep6Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep6', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'connector',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep6Delegate', this);

			this.referenceMappingGrid = Ext.create('Ext.grid.Panel', {
				title: 'tr.referenceMapping',
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',

				columns: [
					{
						header: 'tr.viewName',
						dataIndex: CMDBuild.ServiceProxy.parameter.VIEW_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.NAME,
							valueField: CMDBuild.ServiceProxy.parameter.VALUE,
							forceSelection: true,
							editable: false,
							allowBlank: false,
							store: CMDBuild.core.proxy.CMProxyTasks.getViewStore()
						},
						flex: 1
					},
					{
						header: 'tr.className',
						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
						editor: {
							xtype: 'combo',
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
							displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
							forceSelection: true,
							editable: false,
							allowBlank: false,
							store: _CMCache.getClassesAndProcessesAndDahboardsStore(),
							queryMode: 'local'
						},
						flex: 1
					},
					{
						xtype: 'actioncolumn',
						width: 30,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true,

						items: [
							{
								icon: 'images/icons/cross.png',
								tooltip: CMDBuild.Translation.administration.modClass.attributeProperties.meta.remove,
								handler: function(grid, rowIndex, colIndex, node, e, record, rowNode) {
									me.classLevelMappingGrid.store.remove(record);
								}
							}
						]
					}
				],

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.CMModelReference',
					data: []
				}),

				plugins: Ext.create('Ext.grid.plugin.CellEditing', {
					clicksToEdit: 1
				})
//				,
//
//				tbar: [
//					{
//						text: CMDBuild.Translation.common.buttons.add,
//						iconCls: 'add',
//						handler: function() {
//							me.referenceMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelReference'));
//						}
//					}
//				]
			});

			Ext.apply(this, {
				items: [this.referenceMappingGrid]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * To populate grid with selected classes if empty
			 */
			show: function(view, eOpts) {
_debug('on show step 6');
			}
		},
	});

})();