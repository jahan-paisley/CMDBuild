(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	// Local model
	Ext.define('CMDBuild.model.CMModelClassLevel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.VIEW_NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.IS_MAIN, type: 'boolean' }
		]
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep4Delegate', {
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
				case 'onBeforeEdit':
					return this.onBeforeEdit(param.fieldName, param.rowData);

				case 'onStepEdit':
					return this.onStepEdit();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		getData: function() {
			var data = [];
			var mainClassName = null;

			if (this.view.gridSelectionModel.hasSelection())
				mainClassName = this.view.gridSelectionModel.getSelection()[0];

			this.view.gridSelectionModel.getStore().each(function(record) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME))
					&& !Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.VIEW_NAME))
				) {
					var buffer = [];

					buffer[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME);
					buffer[CMDBuild.ServiceProxy.parameter.VIEW_NAME] = record.get(CMDBuild.ServiceProxy.parameter.VIEW_NAME);
					buffer[CMDBuild.ServiceProxy.parameter.IS_MAIN] = false;

					// Check to setup isMain parameter
					if (
						mainClassName != null
						&& buffer[CMDBuild.ServiceProxy.parameter.CLASS_NAME] == mainClassName.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME)
						&& buffer[CMDBuild.ServiceProxy.parameter.VIEW_NAME] == mainClassName.get(CMDBuild.ServiceProxy.parameter.VIEW_NAME)
					) {
						buffer[CMDBuild.ServiceProxy.parameter.IS_MAIN] = true;
					}

					data.push(buffer);
				}
			});

			return data;
		},

		/**
		 * Function used from next step to get all selected class names
		 *
		 * @return (Array) selectedClassArray
		 */
		getSelectedClassArray: function() {
			var selectedClassArray = [];
			var gridData = this.getData();

			for (key in gridData) {
				if (selectedClassArray.indexOf(gridData[key][CMDBuild.ServiceProxy.parameter.CLASS_NAME]) == -1)
					selectedClassArray.push(gridData[key][CMDBuild.ServiceProxy.parameter.CLASS_NAME]);
			}

			return selectedClassArray;
		},

		/**
		 * Function used from next step to get all selected view names
		 *
		 * @return (Array) selectedViewArray
		 */
		getSelectedViewArray: function() {
			var selectedViewArray = [];
			var gridData = this.getData();

			for (key in gridData) {
				if (selectedViewArray.indexOf(gridData[key][CMDBuild.ServiceProxy.parameter.VIEW_NAME]) == -1)
					selectedViewArray.push(gridData[key][CMDBuild.ServiceProxy.parameter.VIEW_NAME]);
			}

			return selectedViewArray;
		},

		isEmptyMappingGrid: function() {
			var gridData = this.getData();

			if (Ext.isEmpty(gridData) || (gridData.length == 0))
				return true;

			return false;
		},

		/**
		 * Function to update row stores/editor on beforeEdit event
		 *
		 * @param (String) fieldName
		 * @param (Object) rowData
		 */
		onBeforeEdit: function(fieldName, rowData) {
			switch (fieldName) {
				case CMDBuild.ServiceProxy.parameter.CLASS_NAME: {
					if (typeof rowData[CMDBuild.ServiceProxy.parameter.VIEW_NAME] != 'undefined') {
						this.buildClassCombo(false);
					} else {
						var columnModel = this.view.classLevelMappingGrid.columns[1];
						var columnEditor = columnModel.getEditor();

						if (!columnEditor.disabled)
							columnModel.setEditor({
								xtype: 'combo',
								disabled: true
							});
					}
				} break;
			}
		},

		/**
		 * To setup class combo editor
		 *
		 * @param (String) viewName
		 */
		buildClassCombo: function(onStepEditExecute) {
			var me = this;
			var columnModel = this.view.classLevelMappingGrid.columns[1];

			if (typeof onStepEditExecute == 'undefined')
				var onStepEditExecute = true;

			columnModel.setEditor({
				xtype: 'combo',
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				forceSelection: true,
				editable: false,
				allowBlank: false,
				store: _CMCache.getClassesStore(),
				queryMode: 'local',

				listeners: {
					select: function(combo, records, eOpts) {
						me.cmOn('onStepEdit');
					}
				}
			});

			if (onStepEditExecute)
				this.onStepEdit();
		},

		/**
		 * Step validation (at least one class/view association and main view check)
		 */
		onStepEdit: function() {
			this.view.gridEditorPlugin.completeEdit();

			if (
				!this.isEmptyMappingGrid()
				&& this.view.gridSelectionModel.hasSelection()
			) {
				this.setDisabledButtonNext(false);
			} else {
				this.setDisabledButtonNext(true);
			}
		},

		setDisabledButtonNext: function(state) {
			this.parentDelegate.setDisabledButtonNext(state);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep4', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'connector',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep4Delegate', this);

			this.gridSelectionModel = Ext.create('Ext.selection.CheckboxModel', {
				mode: 'single',
				showHeaderCheckbox: false,
				headerText: 'tr.main',
				headerWidth: 50,
				dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_MAIN_NAME,
				checkOnly: true,
				selectByPosition: Ext.emptyFn, // FIX: to avoid checkbox selection on cellediting (workaround)

				listeners: {
					selectionchange: function(model, record, index, eOpts) {
						me.delegate.cmOn('onStepEdit');
					}
				}
			});

			this.gridEditorPlugin = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1,

				listeners: {
					beforeedit: function(editor, e, eOpts) {
						me.delegate.cmOn('onBeforeEdit', {
							fieldName: e.field,
							rowData: e.record.data
						});
					}
				}
			});

			this.classLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				title: 'tr.classLevelMapping',
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',

				selModel: this.gridSelectionModel,
				plugins: this.gridEditorPlugin,

				columns: [
					{
						header: 'tr.viewName',
						dataIndex: CMDBuild.ServiceProxy.parameter.VIEW_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
							forceSelection: true,
							editable: false,
							allowBlank: false,
							store: CMDBuild.core.proxy.CMProxyTasks.getViewStore(),

							listeners: {
								select: function(combo, records, eOpts) {
									me.delegate.buildClassCombo();
								}
							}
						},
						flex: 1
					},
					{
						header: 'tr.className',
						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
						editor: {
							xtype: 'combo',
							disabled: true
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
					model: 'CMDBuild.model.CMModelClassLevel',
					data: []
				}),

				tbar: [
					{
						text: CMDBuild.Translation.common.buttons.add,
						iconCls: 'add',
						handler: function() {
							me.classLevelMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelClassLevel'));
						}
					}
				]
			});

			Ext.apply(this, {
				items: [this.classLevelMappingGrid]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable next button only if grid haven't selected class
			 */
			show: function(view, eOpts) {
				var me = this;

				Ext.Function.createDelayed(function() { // HACK: to fix problem which fires show event before changeTab() function
					if (me.delegate.isEmptyMappingGrid())
						me.delegate.setDisabledButtonNext(true);
				}, 1)();
			}
		}
	});

})();