(function() {

	var tr = CMDBuild.Translation.administration.modsecurity.privilege;
	var parameter = _CMProxy.parameter;

	Ext.define("CMDBuild.view.administration.group.CMGroupPrivilegeGrid", {
		extend: "Ext.grid.Panel",
		alias: "privilegegrid",
		enableDragDrop: false,

		mixins: {
			cmFilterWindowDelegate: "CMDBuild.view.management.common.filter.CMFilterWindowDelegate",
			cmFilterChooserWindowDelegate: "CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate"
		},

		// configuration
		/**
		 * true to add a column to set
		 * the relative privilege to NONE
		 */
		withPermissionNone: true,

		/**
		 * true to add a column to set
		 * the relative privilege to READ
		 */
		withPermissionRead: true,

		/**
		 * true to add a column to set
		 * the relative privilege to WRITE
		 */
		withPermissionWrite: true,

		/**
		 * add a button to set the
		 * privileges filter
		 */
		withFilterEditor: false,

		/**
		 * the URL to call to notify
		 * the server of the click
		 */
		actionURL: undefined,

		// configuration

		initComponent: function() {
			this.recordToChange = null;

			this.columns = [{
				hideable: false,
				header: CMDBuild.Translation.description_,
				dataIndex: 'privilegedObjectDescription',
				flex: 1,
				sortable: true
			}];

			buildCheckColumn(this, 'none_privilege', this.withPermissionNone);
			buildCheckColumn(this, 'read_privilege', this.withPermissionRead);
			buildCheckColumn(this, 'write_privilege', this.withPermissionWrite);
			
			var setPrivilegeTranslation = CMDBuild.Translation.row_and_column_privileges;
			var removePrivilegeTranslation = CMDBuild.Translation.clear_row_and_colun_privilege;

			var me = this;
			if (this.withFilterEditor) {
				this.columns.push(iconButton(me, "privilege_filter", setPrivilegeTranslation, "", onSetPrivilegeFilterClick));
				this.columns.push(iconButton(me, "privilege_filter_remove", removePrivilegeTranslation, "", onRemovePrivilegeFilterClick));
			}
			this.columns.push(iconButton(me, "uiconfiguration", CMDBuild.Translation.ui_configuration_for_groups, "UI", onChangeClassUIConfiguration));

			this.viewConfig = {
				forceFit: true
			};

			this.plugins = [ // 
				Ext.create('Ext.grid.plugin.CellEditing', { //
					clicksToEdit: 1 //
				}) //
			];

			this.frame = false;
			this.border = false;

			this.callParent(arguments);

		},

		loadStoreForGroup: function(group) {
			this.currentGroup = group.get("id") || -1;
			this.loadStore();
		},

		loadStore: function() {
			if (this.currentGroup && this.currentGroup > 0) {
				this.getStore().load({
					params: {
						groupId: this.currentGroup
					}
				});
			}
		},

		clickPrivileges: function(cell, recordIndex, checked) {
			var me = this;
			this.recordToChange = this.store.getAt(recordIndex);
			if (me.actionURL) {
				CMDBuild.Ajax.request({
					url: me.actionURL,
					params: {
						privilege_mode: cell.dataIndex,
						groupId: me.recordToChange.getGroupId(),
						privilegedObjectId: me.recordToChange.getPrivilegedObjectId()
					},
					callback: function() {
						me.loadStore();
					}
				});
			}
		},
		// as CMGroupClassUIConfiguration delegate
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onSaveClassUIConfiguration':
					param.groupId = this.currentGroup;
					this.windowChangeClassUIConfiguration.destroy();
					saveClassUIConfiguration(param);
					break;
				case 'onAbortClassUIConfiguration':
					this.windowChangeClassUIConfiguration.destroy();
					break;
				default: {
					if (
						this.parentDelegate
						&& typeof this.parentDelegate === 'object'
					) {
						return this.parentDelegate.cmOn(name, param, callBack);
					}
				}
			}
			return undefined;
		},
		// as cmFilterWindowDelegate

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowApplyButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveButtonClick: function(filterWindow) {
			// BUSINNESS RULE: The user could not save the privileges if the filter
			// has some runtime parameter
			var filter = filterWindow.getFilter();
			var runtimeParameters = filter.getRuntimeParameters();
			var calculatedParameters = filter.getCalculatedParameters();

			if (runtimeParameters && runtimeParameters.length > 0) {
				CMDBuild.Msg.error(//
					CMDBuild.Translation.error, //
					CMDBuild.Translation.itIsNotAllowedFilterWithRuntimeParams, //
					false //
				);

				return;
			} else if (calculatedParameters && calculatedParameters.length > 0) {
				CMDBuild.Msg.error(//
					CMDBuild.Translation.error, //
					CMDBuild.Translation.itIsNotAllowedFilterWithCalculatedParams, //
					false //
				);

				return;
			}

			var params = {};
			params[parameter.PRIVILEGED_OBJ_ID] = filterWindow.group.getPrivilegedObjectId();
			params[parameter.GROUP_ID] = filterWindow.group.getGroupId();
			var attributesPrivileges = filterWindow.getAttributePrivileges();
			params[parameter.ATTRIBUTES] = Ext.encode(attributesPrivileges);
			params[parameter.FILTER] = Ext.encode(filter.getConfiguration());

			_CMProxy.group.setRowAndColumnPrivileges({
				params: params,
				success: function() {
					filterWindow.group.setPrivilegeFilter(params[parameter.FILTER]);
					filterWindow.group.setAttributePrivileges(attributesPrivileges);
					filterWindow.destroy();
				}
			});

		},

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveAndApplyButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowAbortButtonClick: function(filterWindow) {
			filterWindow.destroy();
		},

		// as filterChooserWindowDelegate

		/**
		 * @params {CMDBuild.view.common.field.CMFilterChooserWindow}
		 * filterWindow the window that call the delegate
		 * @params {Ext.data.Model} filter
		 * the selected record
		 */
		// override
		onCMFilterChooserWindowRecordSelect: function(filterWindow, filter) {
			filterWindow.setFilter(filter);
		}
	});

	function iconButton(me, icon, tooltip, header, callFunction) {
		return {
			header: header,
			fixed: true, 
			sortable: false, 
			align: 'center',
			tdCls: 'grid-button',
			menuDisabled: true,
			hideable: false,
            xtype:'actioncolumn',
            width:30,
            items: [{
    			icon: "images/icons/" + icon + ".png",
                tooltip: tooltip, 
                handler: function(grid, rowIndex, colIndex) {
                	var model = grid.getStore().getAt(rowIndex);
                	callFunction(me, model);
                }
            }]
		};	
	}
	// scope this
	function onSetPrivilegeFilterClick(me, model) {

		var className = model.get("privilegedObjectName");
		var entryType = _CMCache.getEntryTypeByName(className);
		var filterConfiguration = model.getPrivilegeFilter() || "{}";

		var filter = new CMDBuild.model.CMFilterModel({
			entryType: className,
			local: true,
			name: "",
			configuration: Ext.decode(filterConfiguration)
		});

//		var me = this;
		var parameterNames = CMDBuild.ServiceProxy.parameter;
		var params = {};
		params[parameterNames.ACTIVE] = false; // all the attributes
		params[parameterNames.CLASS_NAME] = entryType.getName();

		CMDBuild.ServiceProxy.attributes.read({
			params: params,
			success: function success(response, options, result) {
				var attributes = result.attributes;
	
				var filterWindow = new CMDBuild.view.administration.group.CMPrivilegeWindow({
					filter: filter,
					attributes: attributes,
					className: className,
					group: model
				});
	
				filterWindow.addDelegate(me);
				filterWindow.show();
			}
		});
	}

	// scope this
	function onRemovePrivilegeFilterClick(me, model) {
//		var me = this;
		Ext.Msg.show({
			title: CMDBuild.Translation.attention,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: me,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					var params = {};

					params[parameter.PRIVILEGED_OBJ_ID] = model.getPrivilegedObjectId();
					params[parameter.GROUP_ID] = model.getGroupId();

					_CMProxy.group.setRowAndColumnPrivileges({
						params: params,
						success: function() {
							me.loadStore();
						}
					});
				}
			}
		});

	}
	function onChangeClassUIConfiguration(me, model) {
		CMDBuild.ServiceProxy.group.loadClassUiConfiguration({
			params: {
				groupId: me.currentGroup,
				classId: model.get("privilegedObjectId")
			},
			success: function(operation, config, response) {
				var values = Ext.JSON.decode(response.response);
				me.windowChangeClassUIConfiguration = Ext.create('CMDBuild.view.administration.group.CMGroupClassUIConfiguration', {
					delegate: me,
					model: model,
					values: values
				});
				me.windowChangeClassUIConfiguration.show();
			}
		});
	}
	function saveClassUIConfiguration(param) {
		CMDBuild.ServiceProxy.group.saveClassUiConfiguration({
			params: param
		});
	}
	function buildCheckColumn(me, dataIndex, condition) {
		if (condition) {
			var checkColumn = new Ext.ux.CheckColumn({
				header: tr[dataIndex],
				align: "center",
				dataIndex: dataIndex,
				width: 70,
				fixed: true
			});
			me.columns.push(checkColumn);
			me.mon(checkColumn, "checkchange", me.clickPrivileges, me);
		}
	}

})();