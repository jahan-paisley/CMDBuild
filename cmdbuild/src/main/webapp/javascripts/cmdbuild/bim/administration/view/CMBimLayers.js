(function() {
	var SEPARATOR = "@79@";
	var attributesStore;
	var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
		clicksToEdit: 1
	});
	Ext.define("AttributesCombo", {
		extend: 'Ext.form.ComboBox',
		queryMode: 'local',
		displayField: 'attributeDescription',
		valueField: 'classNameattributeName'
	});

	Ext.define("CMDBuild.bim.administration.view.CMBimLayers.LookupStore", {
		extend: "Ext.data.Store",
		fields: ['classNameattributeName', 'attributeDescription'],
		data: []
	});
	
	Ext.define("CMDBuild.bim.administration.view.CMBimLayersDelegate", {
		/**
		 * Called after a click on
		 * a check column
		 * 
		 * @param {CMDBuild.bim.administration.view.CMBimLayers} grid the grid
		 * that call this method
		 * @param {CMDBuild.bim.data.CMBimLayerModel} record the record
		 * that holds the data of the clicked row
		 * @param {String} dataIndex the name of the clicked column
		 * @param {boolean or String} the new value of the column
		 */
		onDataColumnChange: function(grid, record, dataIndex, value) {},
	});

	Ext.define("CMDBuild.bim.administration.view.CMBimLayers", {
		extend: "Ext.panel.Panel",

		delegate: new CMDBuild.bim.administration.view.CMBimLayersDelegate(),

		initComponent: function() {
			var me = this;
			attributesStore = Ext.create("CMDBuild.bim.administration.view.CMBimLayers.LookupStore", {});
			this.attributesCombo = new AttributesCombo({
				store: attributesStore
			});
			this.attributesCombo.on('blur', function() {
				/* Solve a problem on change page
				 * If the combo is active when the user change page
				 * on returning on this page the combo is around on the screen
				 * the cellEditing.completeEdit() don't work
				 */
				me.attributesCombo.width = 0;
			});
			this.attributesCombo.on('change', function(combo, value, oldValue) {
				cellEditing.completeEdit();
				me.onAttributesComboChange(value);
			});
			this.store = CMDBuild.bim.proxy.layerStore();
			this.grid = Ext.create('Ext.grid.Panel', {
				title: CMDBuild.Translation.bim + " " + CMDBuild.Translation.layers,
				region: 'center',
				store: this.store,
				columns: [
					{
						flex: 1,
						text: CMDBuild.Translation.class,
						dataIndex: 'description'
					},
					checkColumn(me, CMDBuild.Translation.active, "active"),
					checkColumn(me, CMDBuild.Translation.root, "root"),
					checkColumn(me, CMDBuild.Translation.export, "export"),
					checkColumn(me, CMDBuild.Translation.container, "container"),
					{ 
						text: CMDBuild.Translation.reference_to_root, 
						dataIndex: 'rootreference', 
						flex: 1,
						editor: this.attributesCombo, 
						renderer: this.renderClass
					}
				],
				selModel: {
					selType: 'cellmodel'
				},
				plugins: [cellEditing]
			});
			this.grid.on('beforeedit', function(editor, e, eOpts) {
				/* currentGridIdx is valid ONLY during the editing of the combo
				 * and is saved here because the onchange of the combo have not this value
				 */
				me.currentGridIdx = e.rowIdx;
				var record = this.store.getAt(e.rowIdx);
				var className = record.get("className");
				if (e.field == "rootreference") {
					attributesStore.clearFilter(true);
					attributesStore.filterBy(function(row) {
						var valueCombo = row.get("classNameattributeName");
						var classNameCombo = valueCombo.split(SEPARATOR);
						return className === classNameCombo[0];
					});
				}
			});
			this.layout = 'border';
			this.items = [this.grid];
			this.callParent(arguments);
		},

		load: function() {
			this.grid.getSelectionModel().deselectAll();
			this.store.removeAll();
			/*
			 * The callback prepare the store for the lookup
			 * the lookup must have all possible values that are filtered
			 * by the class name during the editing of the single row.
			 * Only at the last callBack the grid store is loaded. In such way
			 * the values of the combos are initialized
			 */
			CMDBuild.bim.proxy.getAllLayers({
			    scope: this,
			    callback: function(a, b, response) {
			    	var me = this;
			    	var records =  Ext.JSON.decode(response.responseText).bimLayer;
					CMDBuild.bim.proxy.rootClassName({
						success: function(operation, config, response) {
							var references = [];
							me.loadAttributes(response.root, records, records, references);
						},
						callback: Ext.emptyFn
					});
			    }
			});
		},

		loadGridStore: function(records) {
			Ext.suspendLayouts();
			for (var i = 0; i < records.length; i++) {
				var model = {
					className: records[i].className,
					description: records[i].description,
					active: records[i].active,
					root: records[i].root,
					export: records[i].export,
					container: records[i].container,
					rootreference: (! records[i].rootreference) ? "" : records[i].className + SEPARATOR + records[i].rootreference,
				};
				this.store.add(model);
			}
			Ext.resumeLayouts();
			
		},

		loadAttributes: function(root, allRecords, records, references) {
			var me = this;
			if (records.length == 0) {
				attributesStore.clearFilter(true);
				attributesStore.removeAll();
				for (var i = 0; i < references.length; i++) {
					var model = {
						classNameattributeName: references[i]["className"] + SEPARATOR + references[i]["attributeName"],
						attributeDescription: references[i]["attributeDescription"]
					};
					attributesStore.add(model);
				}
				this.loadGridStore(allRecords);
				return;
			}
			var cl = _CMCache.getEntryTypeByName(records[0].className);
			if (cl) {
				_CMCache.getAttributeList(cl.get("id"), function(attributes) {
					me.loadReferences(root, records[0].className, references, attributes);
					records = records.slice(1);
					me.loadAttributes(root, allRecords, records, references);
				});
			}
		},
		
		loadReferences: function(root, className, references, attributes) {
			for(var i = 0; i < attributes.length; i++) {
				var type = attributes[i].type;
				if (type == "REFERENCE") {
					if (attributes[i]["referencedClassName"] == root) {
						references.push({
							className: className,
							attributeName : attributes[i]["name"],
							attributeDescription: attributes[i]["description"]
						});
					}
				}
			}
		},

		renderClass: function(attributeName, cell, dataRow) {
			var record = attributesStore.findRecord("classNameattributeName", attributeName);
			return (record) ? record.get("attributeDescription") : "";
		},
		
		onCheckColumnChange: function(cell, recordIndex, checked) {
			var dataIndex = cell.dataIndex;
			var record = this.store.getAt(recordIndex);

			this.delegate.onDataColumnChange(this, record, dataIndex, checked);
		},
		onAttributesComboChange: function(value) {
			var recordIndex = this.currentGridIdx;
			var record = this.store.getAt(recordIndex);
			var nameAttribute = "";
			if (value) {
				var ar = value.split(SEPARATOR);
				if (ar.length >= 2) {
					nameAttribute = ar[1];
				}
			}
			this.delegate.onDataColumnChange(this, record, 'rootreference', nameAttribute);
		}
	});

	function checkColumn(me, header, dataIndex) {
		return {
			align: "center",
			dataIndex: dataIndex,
			fixed: true,
			text: header,
			width: 70,
			xtype: 'checkcolumn',
			listeners: {
				checkchange: function(cell, recordIndex, checked) {
					me.onCheckColumnChange(cell, recordIndex, checked);
				}
			}
		};
	}

})();