(function() {

	// Constants to identify the icons that the user
	// could click, and call the right callback
	var ACTION_CSS_CLASS = {
		saveFilter: "action-filter-save",
		modifyFilter: "action-filter-modify",
		removeFilter: "action-filter-remove",
		cloneFilter: "action-filter-clone"
	};

	var FILTER_BUTTON_LABEL = CMDBuild.Translation.management.findfilter.set_filter;
	var CLEAR_FILTER_BUTTON_LABEL = CMDBuild.Translation.management.findfilter.clear_filter;

	var TOOLTIP = {
		save: CMDBuild.Translation.common.buttons.save,
		modify: CMDBuild.Translation.common.buttons.modify,
		clone: CMDBuild.Translation.common.buttons.clone,
		remove: CMDBuild.Translation.common.buttons.remove
	};

	var ICONS_PATH = {
		save: "images/icons/disk.png",
		save_disabled: "images/icons/disk_disabled.png",
		modify: "images/icons/modify.png",
		clone: "images/icons/clone.png",
		remove: "images/icons/cross.png"
	};

	Ext.define("CMDBuild.view.management.common.filter.CMFilterMenuButton", {
		extend: "Ext.container.ButtonGroup",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
					"CMDBuild.delegate.common.filter.CMFilterMenuButtonDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.picker = null;

			var me = this;

			this.showListButton = new Ext.button.Button({
				text: FILTER_BUTTON_LABEL,
				iconCls: 'find',
				enableToggle: true,
				toggleHandler: function(button, state) {
					return showPicker(me, button, state);
				}
			});

			this.clearButton = new Ext.button.Button({
				text: CLEAR_FILTER_BUTTON_LABEL,
				iconCls: "clear_filter",
				disabled: true,
				handler: function() {
					me.callDelegates("onFilterMenuButtonClearActionClick", me);
				}
			});

			this.items = [this.showListButton, this.clearButton];

			this.callParent(arguments);

			this.mon(this, "move", function(button, x, y) {
				this.showListButton.toggle(false);
			}, this);
		},

		reconfigureForEntryType: function(entryType) {
			this.entryType = entryType;
			if (this.picker != null) {
				this.picker.destroy();
				this.picker = null;
			}
		},

		updatePickerPosition: function(position) {
			var box = position || this.getBox();
			if (this.picker != null) {
				this.picker.setPosition(box.x + 5, (box.y + box.height));
			}
		},

		setFilterButtonLabel: function(label) {
			var text = FILTER_BUTTON_LABEL;
			var tooltip = FILTER_BUTTON_LABEL;
			if (label) {
				text = Ext.String.ellipsis(label, 20);
				tooltip = label;
			}

			this.showListButton.setTooltip(tooltip);
			this.showListButton.setText(text);
		},

		deselectPicker: function() {
			this.picker.deselect();
		},

		load: function() {
			this.picker.load();
		},

		selectAppliedFilter: function() {
			this.picker.selectAppliedFilter();
		},

		enableClearFilterButton: function() {
			this.clearButton.enable();
		},

		disableClearFilterButton: function() {
			this.clearButton.disable();
		},

		getFilterStore: function() {
			var store = null;
			if (this.picker) {
				store = this.picker.getStore();
			}

			return store;
		}
	});

	var translation = CMDBuild.Translation.management.findfilter;
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	var CONDITION_TRANSLATION_MAP = {};
	CONDITION_TRANSLATION_MAP[operator.EQUAL] = translation.equals;
	CONDITION_TRANSLATION_MAP[operator.NOT_EQUAL] = translation.different;
	CONDITION_TRANSLATION_MAP[operator.NULL] = translation.nullo;
	CONDITION_TRANSLATION_MAP[operator.NOT_NULL] = translation.notnull;
	CONDITION_TRANSLATION_MAP[operator.GREATER_THAN] = translation.major;
	CONDITION_TRANSLATION_MAP[operator.LESS_THAN] = translation.minor;
	CONDITION_TRANSLATION_MAP[operator.BETWEEN] = translation.between;
	CONDITION_TRANSLATION_MAP[operator.CONTAIN] = translation.like;
	CONDITION_TRANSLATION_MAP[operator.NOT_CONTAIN] = translation.dontlike;
	CONDITION_TRANSLATION_MAP[operator.BEGIN] = translation.begin;
	CONDITION_TRANSLATION_MAP[operator.NOT_BEGIN] = translation.dontbegin;
	CONDITION_TRANSLATION_MAP[operator.END] = translation.end;
	CONDITION_TRANSLATION_MAP[operator.NOT_END] = translation.dontend;

	Ext.define("CMDBuild.view.management.common.filter.CMRuntimeParameterWindowField", {
		extend: "Ext.form.FieldContainer",

		// configuration
		valueField: null, 
		// configuration

		initComponent: function() {
			this.fieldLabel = (this.valueField.fieldLabel || "") + ' - ' + CONDITION_TRANSLATION_MAP[this.valueField._cmOperator];
			this.labelAlign = "top";
			this.labelSeparator = null;
			this.valueField.hideLabel = true;
			this.valueField.flex = 2;
			this.labelStyle = "font-weight: 100 !important; color: #000000 !important";
			this.layout = 'hbox';

			this.items = [this.valueField];

			if (this.valueField._cmOperator == operator.BETWEEN) {
				this.valueField2 = CMDBuild.Management.FieldManager.getFieldForAttr(this.valueField.CMAttribute);
				this.valueField2.hideLabel = true;
				this.valueField2.flex = 2;
				this.items.push({
					xtype: 'splitter'
				});

				// Put the second field as attribute of the
				// first, to be able to refer to it
				// when resolve the runtime parameters in the
				// filter model
				this.valueField._cmSecondField = this.valueField2;

				this.items.push(this.valueField2);
			}

			this.callParent(arguments);
		}
	});

	var TEXT_FIELD_HEIGHT = 80;
	var HTML_FIELD_HEIGHT = 200;
	var SIMPLE_FIELD_HEIGHT = 50;

	Ext.define("CMDBuild.view.management.common.filter.CMRuntimeParameterWindow", {

		extend: "Ext.window.Window",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
					"CMDBuild.delegate.common.filter.CMRuntimeParameterWindowDelegate");

			this.callParent(arguments);
		},

		// configuration
		runtimeAttributes: [],
		filter: undefined,
		// configuration

		initComponent: function() {
			this.height = calculateWindowHeight(this.runtimeAttributes);
			this.minHeight = 60;
			this.maxHeight = "80%";
			this.width = "50%";
			this.layout = "border";
			this.modal = true;

			this.items = [{
				region: "center",
				layout: {
					type: 'vbox',
					align: 'stretch',
					pack: 'center'
				},
				autoScroll: true,
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				},
				items: getRuntimeParameterWindowItems(this.runtimeAttributes),
				frame: false,
				border: false
			}];

			var me = this;

			this.buttonAlign = "center";
			this.buttons = [{
				text:  CMDBuild.Translation.management.findfilter.apply,
				handler: function() {
					me.callDelegates("onRuntimeParameterWindowSaveButtonClick", [me, me.filter]);
				}
			}, {
				text: CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					me.destroy();
				}
			}];

			this.callParent(arguments);
		}
	});

	function getRuntimeParameterWindowItems(fields) {
		var items = [];
		for (var i=0, l=fields.length; i<l; ++i) {
			var field = fields[i];
			items.push(new CMDBuild.view.management.common.filter.CMRuntimeParameterWindowField({
				valueField: field
			}));
		}

		return items;
	}

	function calculateWindowHeight(fields) {
		var height = 80;

		for (var i=0; i<fields.length; ++i) {
			var field = fields[i];
			var fieldClassName = Ext.getClassName(field);

			if (fieldClassName == "Ext.form.field.TextArea") {
				height += TEXT_FIELD_HEIGHT;
			} else if (fieldClassName == "CMDBuild.view.common.field.CMHtmlEditorField") {
				height += HTML_FIELD_HEIGHT;
			} else {
				height += SIMPLE_FIELD_HEIGHT;
			}
		}

		return height;
	}

	function showPicker(me, button, state) {
		if (me.picker == null) {
			me.picker = new CMDBuild.view.management.common.filter.CMFilterMenuButtonPicker({
				onStoreDidLoad: function() {
					_showPicker(me, state);
				},
				entryType: me.entryType,
				listeners: {
					beforeitemclick: function beforeitemclick(grid, model, htmlelement, rowIndex, event, opt) {
						var cssClassName = event.target.className;
						var callBacks = {};

						callBacks[ACTION_CSS_CLASS.saveFilter] = "onFilterMenuButtonSaveActionClick";
						callBacks[ACTION_CSS_CLASS.modifyFilter] = "onFilterMenuButtonModifyActionClick";
						callBacks[ACTION_CSS_CLASS.removeFilter] = "onFilterMenuButtonRemoveActionClick";
						callBacks[ACTION_CSS_CLASS.cloneFilter] = "onFilterMenuButtonCloneActionClick";

						if (typeof callBacks[cssClassName] == "string") {
							me.callDelegates(callBacks[cssClassName], [me, model.copy()]);
							me.deselectPicker();
						} else {
							// the row was selected
							me.callDelegates("onFilterMenuButtonApplyActionClick", [me, model.copy()]);
						}
					},

					addFilter: function() {
						me.callDelegates("onFilterMenuButtonNewActionClick", me);
					},

					show: function onFilterPickerShow(picker) {
						var windowSize = Ext.getBody().getViewSize();
						var pickerBox = picker.getBox();
						var buttonBox = me.getBox();

						if (windowSize && pickerBox && buttonBox) {
							if (pickerBox.bottom > windowSize.height) {
								// the bottom border of the picker is under
								// the bottom border of the window

								var delta = pickerBox.height + buttonBox.height;
								picker.setPosition(pickerBox.x, pickerBox.y - delta);
							}
						}
					}
				}
			});
		} else {
			_showPicker(me, state);
		}
	}

	function _showPicker(me, state) {
		if (state) {
			me.updatePickerPosition();
			if (me.picker.filtersCount()) {
				me.selectAppliedFilter();
				me.picker.show();
			} else {
				// If has no filters the user
				// can only add a new filter.
				me.callDelegates("onFilterMenuButtonNewActionClick", me);
				me.showListButton.toggle();
			}
		} else {
			me.picker.hide();
		}
	}

	/*
	 * This component is shown when toggle the main button of the CMFilterMenuButton
	 * Probably could be better to use a floating panel instead a window
	 */
	Ext.define("CMDBuild.view.management.common.filter.CMFilterMenuButtonPicker", {

		extend: "Ext.window.Window",

		// configuration
		entryType: null,
		onStoreDidLoad: Ext.emptyFn, // callBack to call after the store load
		// configuration

		baseCls: "", // To remove the blue rounded border of the ExtJS window style

		initComponent: function() {
			this.closable = false;
			this.draggable = false;
			this.resizable = false;
			this.frame = true;
			this.border = false;
			this.rowTemplate = '<p class="filterMenuButtonGrid-name" title="{1}">{0}</p>';

			this.cls = "filterMenuButtonGrid";

			var me = this;
			var store = _CMProxy.Filter.newUserStore();

			this.grid = new Ext.grid.Panel({
				width: 300,
				maxHeight: 200, // TODO calculate it
				autoScroll: true,
				hideHeaders: true,
				store: store,
				tbar: [{
					text: CMDBuild.Translation.management.findfilter.add,
					iconCls: "add",
					handler: function() {
						me.fireEvent("addFilter");
					}
				}],
				columns: [{
					renderer: function(value, metadata, record, rowIndex, colIndex, store, view) {
						var description = Ext.String.ellipsis(record.getDescription(), 50);
						var name = Ext.String.ellipsis(record.getName(), 45);
						if (record.dirty) {
							name += "*";
						}

						return Ext.String.format(me.rowTemplate, name, description);
					},
					flex: 1,
					menuDisabled: true,
					hideable: false
				}, {
					width: 100,
					fixed: true, 
					sortable: false, 
					renderer: function(value, metadata, record, rowIndex, colIndex, store, view) {
						if (record.isTemplate()) {
							return "";
						}

						var template = '<img style="cursor:pointer" title="{0}" class="{1}" src="{2}"/>';
						var saveIcon = record.dirty ? ICONS_PATH.save : ICONS_PATH.save_disabled;
						return Ext.String.format(template, TOOLTIP.save, ACTION_CSS_CLASS.saveFilter, saveIcon) +
							Ext.String.format(template, TOOLTIP.modify, ACTION_CSS_CLASS.modifyFilter, ICONS_PATH.modify) +
							Ext.String.format(template, TOOLTIP.clone, ACTION_CSS_CLASS.cloneFilter, ICONS_PATH.clone) +
							Ext.String.format(template, TOOLTIP.remove, ACTION_CSS_CLASS.removeFilter, ICONS_PATH.remove);
					},
					align: 'center',
					menuDisabled: true,
					hideable: false
				}]
			});

			this.relayEvents(this.grid, ['beforeitemclick', 'select']);
			this.items = [this.grid];

			this.callParent(arguments);

			this.load();
		},

		filtersCount: function() {
			return this.grid.getStore().count();
		},

		deselect: function() {
			this.grid.getSelectionModel().deselectAll();
		},

		selectAppliedFilter: function() {
			this.deselect();
			var appliedFilter = this.grid.getStore().findRecord("applied", true);
			if (appliedFilter != null) {
				this.grid.getSelectionModel().select(appliedFilter);
			}
		},

		getStore: function() {
			return this.grid.getStore();
		},

		load: function() {
			var me = this;
			var params = {};
			params[_CMProxy.parameter.CLASS_NAME] = me.entryType.getName();
			me.getStore().load({
				callback: me.onStoreDidLoad,
				params: params
			});
		}
	});

})();