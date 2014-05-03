(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	// Local model
	Ext.define('CMDBuild.model.CMModelAttributeLevel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.VIEW_NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.IS_KEY, type: 'boolean' }
		]
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep5Delegate', {
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
			var isKeySelection = null;

			if (this.view.gridSelectionModel.hasSelection())
				isKeySelection = this.view.gridSelectionModel.getSelection();

			this.view.attributeLevelMappingGrid.getStore().each(function(record) {
				if (
					!Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME))
					&& !Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE))
					&& !Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.VIEW_NAME))
					&& !Ext.isEmpty(record.get(CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE))
				) {
					var buffer = [];

					buffer[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = record.get(CMDBuild.ServiceProxy.parameter.CLASS_NAME);
					buffer[CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE] = record.get(CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE);
					buffer[CMDBuild.ServiceProxy.parameter.VIEW_NAME] = record.get(CMDBuild.ServiceProxy.parameter.VIEW_NAME);
					buffer[CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE] = record.get(CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE);
					buffer[CMDBuild.ServiceProxy.parameter.IS_KEY] = false;

					// Check to setup isKey parameter
					for (key in isKeySelection) {
						if (
							buffer[CMDBuild.ServiceProxy.parameter.CLASS_NAME] == isKeySelection[key].get(CMDBuild.ServiceProxy.parameter.CLASS_NAME)
							&& buffer[CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE] == isKeySelection[key].get(CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE)
							&& buffer[CMDBuild.ServiceProxy.parameter.VIEW_NAME] == isKeySelection[key].get(CMDBuild.ServiceProxy.parameter.VIEW_NAME)
							&& buffer[CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE] == isKeySelection[key].get(CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE)
						) {
							buffer[CMDBuild.ServiceProxy.parameter.IS_KEY] = true;
							break;
						}
					}

					data.push(buffer);
				}
			});

			return data;
		},

		isEmptyMappingGrid: function() {
			if (CMDBuild.Utils.isEmpty(this.getData()))
				return true;

			return false;
		},

		/**
		 * To setup class attribute combo store
		 *
		 * @param (String) className
		 */
		buildClassAttributesCombo: function(className, onStepEditExecute) {
			if (!Ext.isEmpty(className)) {
				var me = this;
				var columnModel = this.view.attributeLevelMappingGrid.columns[3];
				var classId = _CMCache.getEntryTypeByName(className).get(CMDBuild.ServiceProxy.parameter.ID);
				var attributesListStore = [];

				if (typeof onStepEditExecute == 'undefined')
					var onStepEditExecute = true;

				for (var key in _CMCache.getClasses()) {
					if (key == classId)
						attributesListStore.push(this.view.classesAttributesMap[key]);
				}

				columnModel.setEditor({
					xtype: 'combo',
					displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					forceSelection: true,
					editable: false,
					allowBlank: false,

					store: Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
						data: attributesListStore[0]
					}),

					listeners: {
						select: function(combo, records, eOpts) {
							me.cmOn('onStepEdit');
						}
					}
				});

				if (onStepEditExecute)
					this.onStepEdit();
			}
		},

		/**
		 * To setup view attribute combo editor
		 *
		 * @param (String) viewName
		 */
		buildViewAttributesCombo: function(viewName, onStepEditExecute) {
			if (!Ext.isEmpty(viewName)) {
				var me = this;
				var columnModel = this.view.attributeLevelMappingGrid.columns[1];
				var attributesListStore = [
					{ 'name': 'Function1', 'description': 'Function 1' },
					{ 'name': 'Function2', 'description': 'Function 2' },
					{ 'name': 'Function3', 'description': 'Function 3' }
				];

				if (typeof onStepEditExecute == 'undefined')
					var onStepEditExecute = true;

// TODO: to finish implementation when stores will be ready
//				for (var key in _CMCache.getClasses()) {
//					if (key == classId)
//						attributesListStore.push(this.view.classesAttributesMap[key]);
//				}

				columnModel.setEditor({
					xtype: 'combo',
					displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					forceSelection: true,
					editable: false,
					allowBlank: false,

					store: Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
						data: attributesListStore
					}),

					listeners: {
						select: function(combo, records, eOpts) {
							me.cmOn('onStepEdit');
						}
					}
				});

				if (onStepEditExecute)
					this.onStepEdit();
			}
		},

		getClassStore: function() {
			return _CMCache.getClassesStore();
		},

		getViewStore: function() {
			return CMDBuild.core.proxy.CMProxyTasks.getViewStore();
		},

		/**
		 * Function to update rows stores/editors on beforeEdit event
		 *
		 * @param (String) fieldName
		 * @param (Object) rowData
		 */
		onBeforeEdit: function(fieldName, rowData) {
			switch (fieldName) {
				case CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE: {
					if (
						(typeof rowData[CMDBuild.ServiceProxy.parameter.CLASS_NAME] != 'undefined')
						&& !Ext.isEmpty(rowData[CMDBuild.ServiceProxy.parameter.CLASS_NAME])
					) {
						this.buildClassAttributesCombo(rowData[CMDBuild.ServiceProxy.parameter.CLASS_NAME], false);
					} else {
						var columnModel = this.view.attributeLevelMappingGrid.columns[3];
						var columnEditor = columnModel.getEditor();

						if (!columnEditor.disabled)
							columnModel.setEditor({
								xtype: 'combo',
								disabled: true
							});
					}
				} break;

				case CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE: {
					if (
						(typeof rowData[CMDBuild.ServiceProxy.parameter.VIEW_NAME] != 'undefined')
						&& !Ext.isEmpty(rowData[CMDBuild.ServiceProxy.parameter.VIEW_NAME])
					) {
						this.buildViewAttributesCombo(rowData[CMDBuild.ServiceProxy.parameter.VIEW_NAME], false);
					} else {
						var columnModel = this.view.attributeLevelMappingGrid.columns[1];
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
		 * Step validation (at least one class/view association and main view check)
		 */
		onStepEdit: function() {
			this.view.gridEditorPlugin.completeEdit();

			if (!this.isEmptyMappingGrid()) {
				this.setDisabledButtonNext(false);
			} else {
				this.setDisabledButtonNext(true);
			}
		},

		setDisabledButtonNext: function(state) {
			this.parentDelegate.setDisabledButtonNext(state);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep5', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'connector',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.classesAttributesMap = _CMCache.getAllAttributesList();
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep5Delegate', this);

			this.gridSelectionModel = Ext.create('Ext.selection.CheckboxModel', {
				mode: 'multi',
				showHeaderCheckbox: false,
				headerText: 'tr.isKey',
				headerWidth: 50,
				dataIndex: CMDBuild.ServiceProxy.parameter.IS_KEY,
				checkOnly: true,
				selectByPosition: Ext.emptyFn, // FIX: to avoid checkbox selection on cellediting (workaround)
				injectCheckbox: 4,

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

			this.attributeLevelMappingGrid = Ext.create('Ext.grid.Panel', {
				layuout: 'fit',
				title: 'tr.attributeLevelMapping',
				considerAsFieldToDisable: true,
				margin: '0 0 5 0',

				selModel: this.gridSelectionModel,
				plugins: this.gridEditorPlugin,

				columns: [
					{
						header: 'tr.view',
						dataIndex: CMDBuild.ServiceProxy.parameter.VIEW_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
							forceSelection: true,
							editable: false,
							allowBlank: false,
							store: me.delegate.getViewStore(),

							listeners: {
								select: function(combo, records, eOpts) {
									me.delegate.buildViewAttributesCombo(records[0].get(CMDBuild.ServiceProxy.parameter.VALUE));
								}
							}
						},
						flex: 1
					},
					{
						header: 'tr.viewAttribute',
						dataIndex: CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE,
						editor: {
							xtype: 'combo',
							disabled: true
						},
						flex: 1
					},
					{
						header: 'tr.className',
						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
						editor: {
							xtype: 'combo',
							displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
							valueField: CMDBuild.ServiceProxy.parameter.NAME,
							forceSelection: true,
							editable: false,
							allowBlank: false,
							store: me.delegate.getClassStore(),
							queryMode: 'local',

							listeners: {
								select: function(combo, records, eOpts) {
									me.delegate.buildClassAttributesCombo(records[0].get(CMDBuild.ServiceProxy.parameter.NAME));
								}
							}
						},
						flex: 1
					},
					{
						header: 'tr.classAttribute',
						dataIndex: CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE,
						editor: {
							xtype: 'combo',
							disabled: true
						},
						flex: 1
					},
// TODO: Future implementation
//					{
//						header: 'tr.function',
//						dataIndex: 'CMDBuild.ServiceProxy.parameter.FUNCTION',
//						editor: {
//							xtype: 'combo',
//							valueField: CMDBuild.ServiceProxy.parameter.VALUE,
//							displayField: CMDBuild.ServiceProxy.parameter.NAME,
//							forceSelection: true,
//							editable: false,
//							allowBlank: false,
//							store: CMDBuild.core.proxy.CMProxyTasks.getFunctionStore()
//						},
//						flex: 1
//					},
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
									me.attributeLevelMappingGrid.store.remove(record);
								}
							}
						]
					}
				],

				store: Ext.create('Ext.data.Store', {
					model: 'CMDBuild.model.CMModelAttributeLevel',
					data: []
				}),

				tbar: [
					{
						text: CMDBuild.Translation.common.buttons.add,
						iconCls: 'add',
						handler: function() {
							me.attributeLevelMappingGrid.store.insert(0, Ext.create('CMDBuild.model.CMModelAttributeLevel'));
						}
					}
				]
			});

			Ext.apply(this, {
				items: [this.attributeLevelMappingGrid]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable next button only if grid haven't selected class
			 */
			show: function(view, eOpts) {
				var me = this;

				Ext.Function.createDelayed(function() { // HACK: to fix problem witch fires show event before changeTab() function
					if (me.delegate.isEmptyMappingGrid())
						me.delegate.setDisabledButtonNext(true);
				}, 1)();
			}
		}
	});

})();