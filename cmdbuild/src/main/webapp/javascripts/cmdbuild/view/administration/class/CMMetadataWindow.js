(function() {
	var STATUS = {
		NEW: "NEW",
		DELETED: "DELETED",
		MODIFIED: "MODIFIED",
		NOT_MODIFIED: "NOT_MODIFIED"
	};

	Ext.define("CMDBuild.view.administration.classes.CMMetadataModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "key",type: 'string'},
			{name: "value",type: 'string'},
			{name: "status",type: 'string'}
		]
	});

	Ext.define("CMDBuild.view.administration.classes.CMMetadataWindow", {
		extend: "CMDBuild.PopupWindow",

		data: {}, //set on instantiation,
		dirtyFlag: false,

		translation: CMDBuild.Translation.administration.modClass.attributeProperties.meta,
		initComponent: function() {
			this.deletedMeta = [];
			this.saveBtn = new CMDBuild.buttons.SaveButton();

			this.abortBtn = new CMDBuild.buttons.AbortButton({
				handler: this.onAbort,
				scope: this
			});

			this.cellEditing = Ext.create( 'Ext.grid.plugin.CellEditing', {
				clicksToEdit : 1
			});

			this.grid = new Ext.grid.Panel({
				border: false,
				frame: false,
				selModel: {
					selType: 'cellmodel'
				},
				columns: [{
						header: this.translation.key,
						dataIndex: 'key',
						flex: 1,
						field: {
							allowBlank: false
						}
					},{
						header: this.translation.value,
						dataIndex: 'value',
						flex: 1,
						field: {
							allowBlank: false
						}
					},{
						header: '&nbsp', 
						width: 40, 
						fixed: true, 
						sortable: false, 
						renderer: this.renderDeleteActions, 
						align: 'center', 
						tdCls: 'grid-button', 
						dataIndex: 'delete',
						menuDisabled: true,
						hideable: false
					}
				],
				store: new Ext.data.SimpleStore({
					model: "CMDBuild.view.administration.classes.CMMetadataModel",
					data: []
				}),
				tbar: [{
					text: this.translation.add,
					handler: this.addMetadata,
					scope: this,
					iconCls: 'add'
				}],
				plugins: [this.cellEditing]
			});

			Ext.apply(this, {
				title: this.translation.title,
				items: [this.grid],
				buttonAlign: 'center',
				buttons: [this.saveBtn, this.abortBtn]
			});
	
			this.callParent(arguments);

			this.mon(this.grid, 'beforeitemclick', this.onCellClick, this);

			this.initGrid();
		},

		onCellClick: function(grid, model, htmlelement, rowIndex, event, opt) {
			var className = event.target.className; 

			if (className == "action-meta-delete") {
				if (model.data.status != STATUS.NEW) {
					model.data.status = STATUS.DELETED;
					this.deletedMeta[model.data.key] = model.data;
				}

				this.grid.store.remove(model);
			}
		},
	
		renderDeleteActions: function() {
			return '<img style="cursor:pointer" title="' +
			CMDBuild.Translation.administration.modClass.attributeProperties.meta.remove + 
			'" class="action-meta-delete" src="images/icons/cross.png"/>&nbsp;';
		},
	
		addMetadata: function() {
			var r = new CMDBuild.view.administration.classes.CMMetadataModel({
				key: this.translation.key,
				value: this.translation.value,
				status: STATUS.NEW
			});
			this.grid.store.insert(0, r);
			this.cellEditing.startEditByPosition({row: 0, column: 0});
		},

		getMetaAsMap: function() {
			var me = this,
				meta = {};

//			this.grid.store.add(this.deletedMeta);

			this.grid.store.each(function(r) {
				var k = r.data.key,
					nsk = me.ns + k,
					v = r.data.value,
					s = r.data.status,
					// if this is not the first time that I open the
					// old value is an object {value: xxx, status: xxx}
					oldValue = me.data[nsk];

				if (oldValue) {
					// this value could be modified
					var _oldValue;
					if (typeof oldValue.value != "undefined") {
						_oldValue = oldValue.value;
					} else {
						_oldValue = oldValue;
					}

					s = _oldValue == v ? STATUS.NOT_MODIFIED : STATUS.MODIFIED;
				} else {
					s = STATUS.NEW
				}

				meta[nsk] = {
					value: v,
					status: s
				};
			}, this);

			addDeletedToOuput(me, meta);

			CMDBuild.log.debug('getMetaAsMap', meta);
			return meta;
		},

		onAbort: function() {
			this.destroy();
		},

		initGrid: function() {
			var meta = CMDBuild.Utils.Metadata.extractMetaByNS(this.data, this.ns),
				r;

			this.deletedMeta = {};

			for (var key in  meta) {
				var value = meta[key];

				// if value is an object it is not the first time that the window is open
				// so the status is already set, else set it to not modified
				if (typeof value == "object") {
					value.key = key;
					r = new CMDBuild.view.administration.classes.CMMetadataModel(value);
				} else {
					r = new CMDBuild.view.administration.classes.CMMetadataModel({
						key: key,
						value: value,
						status: "NOT_MODIFIED"
					});
				}

				if (r.data.status == "DELETED") {
					this.deletedMeta[r.data.key] = r.data;
				} else {
					this.grid.store.add(r);
				}
			}
		}
	});

	function addDeletedToOuput(me, meta) {
		var data = CMDBuild.Utils.Metadata.extractMetaByNS(me.data, me.ns);

		for (var r in data) {
			if (me.grid.store.find("key", r) == -1) {
				if (typeof me.deletedMeta[r] == "object") {
					continue;
				} else {
					me.deletedMeta[r] = {
						status: STATUS.DELETED,
						value: ""
					};
				}
			}
		}

		for (var key in me.deletedMeta) {
			var data = me.deletedMeta[key];
			meta[me.ns + key] = {
				value: data.value,
				status: data.status
			};
		}
	}

})();