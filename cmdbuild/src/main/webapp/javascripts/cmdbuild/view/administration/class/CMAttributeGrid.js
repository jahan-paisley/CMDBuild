(function() {
	var _p = _CMProxy.parameter;
	var ATTR = {
		INDEX: _p.INDEX,
		NAME: _p.NAME,
		DESCRIPTION: _p.DESCRIPTION,
		TYPE: _p.TYPE,
		IS_BASEDSP: _p.DISPLAY_IN_GRID,
		IS_UNIQUE: _p.UNIQUE,
		IS_NOT_NULL: _p.NOT_NULL,
		IS_INHERITED: "inherited",
		IS_ACTIVE: _p.ACTIVE,
		FIELD_MODE: _p.FIELD_MODE,
		GROUP: _p.GROUP,
		ABSOLUTE_CLASS_ORDER: "absoluteClassOrder",
		CLASS_ORDER_SIGN: "classOrderSign",
		EDITOR_TYPE: _p.EDITOR_TYPE
	};

	var REQUEST = {
		ROOT: "attributes"
	};

	var ATTR_TO_SKIP = "Notes";

	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;

Ext.define("CMDBuild.view.administration.classes.CMAttributeGrid", {
	extend: "Ext.grid.Panel",

	statics: {
		ATTRIBUTES: ATTR
	},

	alias: "attributegrid",

	remoteSort: false,
	includeInherited: true,
	eventtype : 'class', 

	hideNotNull: false, // for processes
	
	hideMode: "offsets",

	constructor:function() {

		this.addAttributeButton = new Ext.button.Button( {
			iconCls : 'add',
			text : translation.add_attribute
		});

		this.orderButton = new Ext.button.Button({	
			iconCls : 'order',
			text : translation.set_sorting_criteria
		});

		this.inheriteFlag = new Ext.form.Checkbox({
			boxLabel : CMDBuild.Translation.administration.modClass.include_inherited,
			boxLabelCls: "cmtoolbaritem",
			checked : true,
			scope : this,
			handler : function(obj, checked) {
				this.setIncludeInheritedAndFilter(includeInherited = checked);
			}
		});

		this.buildStore();
		this.buildColumnConf();
		this.buildTBar();

		this.callParent(arguments);
	},
	
	initComponent: function() {
		Ext.apply(this, {
			viewConfig: {
				loadMask: false,
				plugins : {
					ptype : 'gridviewdragdrop',
					dragGroup : 'dd',
					dropGroup : 'dd'
				},
				listeners : {
					scope: this,
					beforedrop: function() {
						// it is not allowed to reorder the attribute if
						// there are also the inherited attrs
						return this.inheriteFlag.checked;
					},
					drop : function(node, data, dropRec, dropPosition) {
						this.fireEvent("cm_attribute_moved", arguments);
					}
				}
			}
		});
		
		this.callParent(arguments);
		
		this.getStore().on('load', function(store, records, opt) {
			this.filterInheritedAndNotes();
		}, this);
	},

	// private
	buildColumnConf: function() {
		this.columns = [ {
			hideable : false,
			hidden : true,
			dataIndex : ATTR.INDEX,
			flex: 1
		}, {
			header : translation.name,
			dataIndex : ATTR.NAME,
			flex: 1
		}, {
			header : translation.description,
			dataIndex : ATTR.DESCRIPTION,
			flex: 1
		}, {
			header : translation.type,
			dataIndex : ATTR.TYPE,
			flex: 1
		},{
			header : translation.editorType.label,
			dataIndex : ATTR.EDITOR_TYPE,
			flex: 1,
			hidden : true
		},
		new Ext.ux.CheckColumn( {
			header : translation.isbasedsp,
			dataIndex : ATTR.IS_BASEDSP,
			cmReadOnly: true
		}),
		new Ext.ux.CheckColumn( {
			header : translation.isunique,
			dataIndex : ATTR.IS_UNIQUE,
			cmReadOnly: true
		}),
		new Ext.ux.CheckColumn( {
			header : translation.isnotnull,
			dataIndex : ATTR.IS_NOT_NULL,
			cmReadOnly: true
		}),
		new Ext.ux.CheckColumn( {
			header : translation.inherited,
			hidden : true,
			dataIndex : ATTR.IS_INHERITED,
			cmReadOnly: true
		}), 
		new Ext.ux.CheckColumn( {
			header : translation.isactive,
			dataIndex : ATTR.IS_ACTIVE,
			cmReadOnly: true
		}), {
			header : translation.field_visibility,
			dataIndex : ATTR.FIELD_MODE,
			renderer : renderEditingMode,
			flex: 1
		}, {
			header : translation.group,
			dataIndex : ATTR.GROUP,
			hidden : true,
			flex: 1
		}];
	},

	buildStore: function() {
		this.store = new Ext.data.Store({
			fields: [
				ATTR.INDEX, ATTR.NAME, ATTR.DESCRIPTION, ATTR.TYPE, ATTR.IS_UNIQUE,
				ATTR.IS_BASEDSP, ATTR.IS_NOT_NULL, ATTR.IS_INHERITED, ATTR.FIELD_MODE,
				ATTR.IS_ACTIVE, ATTR.GROUP, ATTR.ABSOLUTE_CLASS_ORDER, ATTR.CLASS_ORDER_SIGN,
				ATTR.EDITOR_TYPE
			],
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modclass/getattributelist',
				reader : {
					type : 'json',
					root : REQUEST.ROOT
				}
			},
			sorters : [ {
				property : ATTR.INDEX,
				direction : "ASC"
			}]
		});
	},
	
	buildTBar: function() {
		this.tbar = [this.addAttributeButton, this.orderButton, '->', this.inheriteFlag ];
	},
	
	onClassSelected: function(idClass) {
		this.refreshStore(idClass, idAttributeToSelectAfter = null);
	},

	refreshStore: function(idClass, indexAttributeToSelectAfter) {
		var parameterNames = CMDBuild.ServiceProxy.parameter;
		var params = {};
		params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(idClass);

		this.store.load({
			params: params,
			scope: this,
			callback: function(records, opt, success) {
				this.filterInheritedAndNotes();
                if (this.rendered) {
                    this.selectRecordAtIndexOrTheFirst(indexAttributeToSelectAfter);
                }
            }
		});
	},

	setIncludeInheritedAndFilter: function(includeInherited) {
		this.includeInherited = includeInherited;
		this.filterInheritedAndNotes();
	},

	filterInheritedAndNotes: function() {
		var inh = this.includeInherited;
		this.getStore().filterBy(function(record) {
			return (record.get(ATTR.NAME) != ATTR_TO_SKIP) && (inh || !record.get(ATTR.IS_INHERITED));
		});
	},

	selectFirstRow: function() {
		var _this = this;
		Ext.Function.defer(function() {
			if (_this.store.getCount() > 0 && _this.isVisible()) {
				var sm = _this.getSelectionModel();
				if (! sm.hasSelection()) {
					sm.select(0);
				}
			}
		}, 200);
	},
	
    selectRecordAtIndexOrTheFirst: function (indexAttributeToSelectAfter) {
        if (indexAttributeToSelectAfter) {
            var r = this.store.findRecord(ATTR.INDEX, indexAttributeToSelectAfter);
            if (r) {
                this.getSelectionModel().select(r);
            }
        } else {
            try {
                if (this.store.count() != 0) {
                    this.getSelectionModel().select(0);
                }
            } catch (e) {
                // fail if the grid is not rendered	
            }
        }
    },

	onAddAttributeClick: function() {
		this.getSelectionModel().deselectAll();
	}
});

function renderEditingMode(val) {
	return translation["field_" + val];
}

})();