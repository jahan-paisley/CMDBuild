(function() {

	/**
	 * @class CMDBuild.WidgetBuilders.LookupAttribute
	 * @extends CMDBuild.WidgetBuilders.ComboAttribute
	 */
	Ext.ns("CMDBuild.WidgetBuilders");
	CMDBuild.WidgetBuilders.LookupAttribute = function() {};
	CMDBuild.extend(CMDBuild.WidgetBuilders.LookupAttribute, CMDBuild.WidgetBuilders.ComboAttribute);

	/**
	 * @override
	 * @param attribute
	 * @return CMDBuild.Management.LookupCombo
	 */
	CMDBuild.WidgetBuilders.LookupAttribute.prototype.buildAttributeField = function(attribute) {
		return CMDBuild.Management.LookupCombo.build(attribute);
	};

	/**
	 * @override
	 */
	CMDBuild.WidgetBuilders.LookupAttribute.prototype.markAsRequired = function(field, attribute) {
		//do nothing because the LookupField class manage this function
		return field;
	};

	/**
	 * @override
	 */
	CMDBuild.WidgetBuilders.LookupAttribute.prototype.buildCellEditor = function(attribute) {
		var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, readOnly = false);

		if (field.isMultiLevel) {
			var fake_field = buildFakeField(attribute);
			fake_field.on("focus", function() {
				onFakeFieldFocus.call(this, attribute);
			}, fake_field);

			return fake_field;
		} else {
			return field;
		}
	};

	/**
	 * @override
	 */

	CMDBuild.WidgetBuilders.LookupAttribute.prototype.genericBuildFieldsetForFilter = function(fieldId, fields, query, originalFieldName) {

		var field = fields[0];

		if (field instanceof CMDBuild.field.MultiLevelLookupPanel) {
			var removeFieldButton = new Ext.button.Button({
				iconCls : 'delete',
				border : false,
				padding : "3 0 0 3"
			});

			var orPanel = new Ext.Panel({
				columnWidth: 0.2,
				html : 'or',
				border : false,
				bodyCls : "x-panel-body-default-framed"
			});

			field.columnWidth = .5;
			field.items.each(function(f) {
				f.padding = 0;
			});

			var fieldset = new Ext.panel.Panel({
				frame : false,
				border : false,
				bodyCls : "x-panel-body-default-framed",
				removeButton : removeFieldButton,
				fieldsetCategory : originalFieldName,
				queryCombo : query,
				hideMode : 'offsets',

				layout : {
					type : 'hbox',
					pack : 'start',
					align : 'top'
				},

				defaults: {
					margins:'0 5 0 0'
				},

				items : [removeFieldButton, query, field, orPanel],

				getAttributeField : function() {
					return fields[0];
				},

				getQueryCombo : function() {
					return query;
				},

				getOrPanel : function() {
					return orPanel;
				}
			});

			return fieldset;

		} else {
			return CMDBuild.WidgetBuilders.BaseAttribute.prototype.genericBuildFieldsetForFilter.apply(this, arguments);
		}
	}


	function buildFakeField(attribute) {
		return new CMDBuild.field.ErasableCombo({
			labelAlign: "right",
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: attribute.fieldLabel || attribute.name,
			labelSeparator: ":",
			name: attribute.name,
			hiddenName: attribute.name,
			store: new Ext.data.Store({
				fields: ["Id", "Description"],
				data: []
			}),
			queryMode: 'local',
			triggerAction: "all",
			valueField: 'Id',
			displayField: 'Description',
			allowBlank: true,
			CMAttribute: attribute
		});
	}

	function onFakeFieldFocus(attribute) {
		var me = this,
			pos = this.getPosition();

		if (this.editingWindow) {
			if (this.editingWindow.pos[0] == pos[0] && this.editingWindow.pos[1] == pos[1]) {
				return;
			} else {
				this.editingWindow.destroy();
				delete this.editingWindow;
			}
		}

		var fieldForTheWindow = buildFieldForTheWindow(attribute);

		this.editingWindow = new Ext.window.Window({
			pos: pos,
			x: pos[0],
			y: pos[1],
			width: me.getWidth(),
			draggable: false,
			closable: false,
			items: [fieldForTheWindow],
			buttonAlign: "center",
			buttons: [{
				xtype: "button",
				text: CMDBuild.Translation.common.buttons.confirm,
				handler: function() {
					var value = fieldForTheWindow.getValue(),
						rawValue = fieldForTheWindow.getRawValue(),
						recordIndex = me.store.find(me.valueField, value),
						notInStore = recordIndex == -1,
						record;

					if (notInStore) {
						var data = {};
						data[me.displayField] = rawValue;
						data[me.valueField] = value;
						record = me.store.add(data)[0];
					} else {
						record = me.store.getAt(recordIndex);
					}

					me.setValue(value);
					me.fireEvent("select", me, record);

					me.editingWindow.destroy();
					delete me.editingWindow;
				}
			},{
				xtype: "button",
				text: CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					me.editingWindow.destroy();
					delete me.editingWindow;
				}
			}]
		}).show();
	}

	function buildFieldForTheWindow(attribute) {
		var fieldForTheWindow = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, readOnly = false),
			fields = fieldForTheWindow.items.items;

		for (var i=0, l=fields.length; i<l; ++i) {
			var item = fields[i];
			item.hideLabel = true;
			item.padding = "0 0 0 0";
		}

		return fieldForTheWindow;
	}
})();
