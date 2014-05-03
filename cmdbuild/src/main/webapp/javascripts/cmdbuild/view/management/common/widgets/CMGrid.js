(function() {
	var OBJECT_VALUES = "__objectValues__";
	
	Ext.define("CMDBuild.view.management.common.widgets.CMGrid", {
		extend: "Ext.panel.Panel",
		autoScroll: true,	
		statics: {
			WIDGET_NAME: ".Grid"
		},

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;
			this.grid = new CMDBuild.view.management.widgets.grid.CMGridPanel();
			this.items = [this.grid];
			this.addButton = addButton(this);
			this.tbar = [this.addButton];
			this.callParent(arguments);
		},

		getData: function() {
			var data = [];
			var store = this.grid.getStore();
			for (var i = 0; i < store.getCount(); i++) {
				var item = store.getAt(i);
				item = Ext.encode(item.data);
				data.push(item);
			}
			return data;
		},

		deleteRow: function(rowIndex) {
			var store = this.grid.getStore();
			store.removeAt(rowIndex);
		},

		newRow: function() {
			var store = this.grid.getStore();
			store.add(this.getStoreForFields(this.columns.fields));
		},

		addRendererToHeader: function(h) {
			h.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
				value = value || record.get(h.dataIndex);
				if (typeof value == "undefined" 
					|| value == null) {

					return "";
				}
				if (h.field.store) {
					var comboRecord = h.field.store.findRecord("Id", value); 
					value = (comboRecord) ?	comboRecord.get("Description") : "";
				}
				return value;
			};
		},

		setColumnsForClass: function() {
			this.columns = this.buildColumnsForAttributes(this.cardAttributes);
			var s = this.getStoreForFields(this.columns.fields);
			addGraphicIcons(this);
			this.suspendLayouts();
			this.grid.reconfigure(s, this.columns.headers);
			this.resumeLayouts(true);
		},

		getStoreForFields: function(fields) {
			var s = this.buildStore(fields);
			return s;
		},

		buildStore: function(fields) {
			fields.push({name: "Id", type: "int"});
			fields.push({name: "IdClass", type: "int"});
			return new Ext.data.Store({
				fields: fields,
				data: []
			});
		},

		buildColumnsForAttributes: function(classAttributes) {
			this.classAttributes = classAttributes;
			var headers = [];
			var fields = [];
			var classId = this.delegate.getCurrentClass().get("id");
			if (_CMUtils.isSuperclass(classId)) {
				headers.push(this.buildClassColumn());
			}

			for (var i=0; i<classAttributes.length; i++) {
				var attribute = classAttributes[i];
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);
				var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(attribute);
				editor.hideLabel = true;

				if (header) {
					header.field = editor;
					this.addRendererToHeader(header);
					headers.push(header);

					fields.push(header.dataIndex);
				} 
				else if (attribute.name == "Description") {
					// FIXME Always add Description, even if hidden, for the reference popup
					fields.push("Description");
				}
			}
			return {
				headers: headers,
				fields: fields
			};
		}
	});

	Ext.define('CMDBuild.model.widget.GridRowModel', {
		extend: 'Ext.data.Model',
		fields: []
	});

	Ext.define("CMDBuild.view.management.widgets.grid.CMGridPanel", {
		extend: "Ext.grid.Panel",
        selModel: {
            selType: 'cellmodel'
        },
        border: false,
		constructor: function() {
			this.cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit: 1
			});
			this.callParent(arguments);
		},
		initComponent: function() {

			this.columns = [];
			this.plugins = [this.cellEditing];

			this.callParent(arguments);
		}
		
	});

	function addButton(me) {
		return Ext.create("Ext.button.Button", {
			iconCls: 'add',
			text: CMDBuild.Translation.row_add, 
			disabled: false,
			handler: function() {
				me.delegate.cmOn("onAdd");
			}
			
		});
	}
	function addGraphicIcons(me) {
		var actionModify =         {
	            xtype:'actioncolumn',
	            width:30,
	            items: [{
	    			iconCls: 'modify',
	                tooltip: CMDBuild.Translation.row_edit, 
	                handler: function(grid, rowIndex, colIndex) {
	                    var record = grid.getStore().getAt(rowIndex);
	                    me.delegate.cmOn("onEdit", {
	                    	record: record
	                    });
	                }
	            }]
	        };
		var actionDelete =         {
	            xtype:'actioncolumn',
	            width:30,
	            items: [{
	    			iconCls: 'delete',
	                tooltip: CMDBuild.Translation.row_delete, 
	                handler: function(grid, rowIndex, colIndex) {
	                    me.delegate.cmOn("onDelete", {
	                    	rowIndex: rowIndex
	                    });
	                }
	            }]
	        };

    	me.columns.headers.push(actionModify, actionDelete);
    }
	function isADate(v) {
		return (v && v.constructor && v.constructor.name == "Date");
	}
	
	/* -----------------------------------------------------------------
	 * EDIT
	 */
	Ext.define("CMDBuild.view.management.common.widgets.CMGridEdit", {
		extend: "CMDBuild.PopupWindow",
		defaultSizeW: 0.90,
		defaultSizeH: 0.80,

		initComponent: function() {
			var me = this;
			this.closeButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.close,
				handler : function() {
					me.delegate.cmOn("onEditClosed");
				},
				scope : this
			});
			this.buttons = [this.closeButton];

			this.buttonAlign = "center";
			this.buttons = [this.closeButton];
			this.form = Ext.create("Ext.form.Panel", {
				autoScroll: true,
				bodyCls: "x-panel-body-default-framed cmbordertop",
				bodyStyle: {
					padding: "5px 5px 0 5px"
				},
				cls: "x-panel-body-default-framed",
				items: buildFormFields(this.record, this.cardAttributes)
			});
			this.items = [this.form];
			this.callParent(arguments);
			this.height = this.height * this.defaultSizeH;
			this.width = this.width * this.defaultSizeW;
		},

		saveData: function() {
			saveData(this.record, this.form.getValues());
		}
		
	});
	function buildFormFields(record, attributes) {
		var items = [];
		for (var i = 0; i < attributes.length; i++) {
			var attribute = attributes[i];
			var attributesMap = CMDBuild.Management.FieldManager.getAttributesMap();
			var item = attributesMap[attribute.type].buildField(attribute, false, false);
			items.push(item);
			var value = record.get(attribute.name);
			item.setValue(value);
		}
		return items;
	}
	function saveData(record, values) {
		for(var property in values){
		    record.set(property, values[property]);
		}
	}
})();