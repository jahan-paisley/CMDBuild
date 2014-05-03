Ext.define("CMDBuild.Administration.ModIcons", {
	extend: "Ext.panel.Panel",

	cmName:"gis-icons",	
	translation: CMDBuild.Translation.administration.modcartography.icons,
	buttonsTr: CMDBuild.Translation.common.buttons,

	initComponent : function() {
		this.buildUIButtons();
		
		this.iconsGrid = new Ext.grid.GridPanel({
			title: this.translation.title,
			region: 'center',
			frame: false,
			border: true,
			bodyCls: 'cm-grid-autoheight',
			store: CMDBuild.ServiceProxy.Icons.getIconStore(),
			tbar: [this.addButton],
			sm: new Ext.selection.RowModel(),
			columns: [{	
				header: '&nbsp', 
				width: 50, 
				rowsfixed: true, 
				sortable: false,
				renderer: this.renderIcon, 
				align: 'center', 
				dataIndex: 'path',
				menuDisabled: true,
				hideable: false
			},{
				header : this.translation.description,
				hideable: true,
				hidden: false,
				dataIndex : 'description',
				flex: 1
			}]
		});
		this.iconsGrid.getSelectionModel().on("select", this.onRowSelect , this);

		this.description = new Ext.form.CMTranslatableText( {
			fieldLabel : this.translation.description,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'description',
			allowBlank : false,
			vtype : 'cmdbcomment',
			translationsKeyType: "GisIcon", 
			translationsKeyField: "Description"
		});
		this.uploadForm = new Ext.form.FormPanel({
			monitorValid: true,
			fileUpload: true,
			plugins: [new CMDBuild.CallbackPlugin()],
			region: 'south',
			height: "40%",
			split: true,
			frame: false,
			border: false,
			cls: "x-panel-body-default-framed cmbordertop",
			bodyCls: 'cmgraypanel',
			layout: "border",
			tbar: [this.modifyButton, this.removeButton],
			items: [{
				xtype: "panel",
				region: "center",
				frame: true,
				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH
				},
				items: [{
					xtype:'hidden',
					name: 'name'
				},{
					xtype: 'filefield',
					allowBlank: true,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					fieldLabel: this.translation.file,
					name: 'file'
				}, this.description
				]
			}],
			buttonAlign: 'center',
			buttons: [this.saveButton,this.abortButton]
		});

		this.frame = false;
		this.border = true;
		this.layout = 'border';
		this.items = [this.iconsGrid, this.uploadForm];

		this.callParent(arguments);

		this.on('show', function() {
			var sm = this.iconsGrid.getSelectionModel();
			var store = this.iconsGrid.getStore();

			if (sm && !sm.hasSelection()
					&& store
					&& store.data.length > 0) {

				sm.select(0);
			}

			this.uploadForm.setFieldsDisabled();
		}, this);

		this.iconsGrid.getStore().on('load', function(store, records, options) {
			if (records.length == 0) {
				this.modifyButton.disable();
				this.removeButton.disable();
			}
		}, this);
	},

	//private
	renderIcon: function(value, cell, record) {
		var path = record.data.path + "?" + Math.floor(Math.random()*100); //to force the reload
		return "<img src=\"" + path + "\" alt=\"" + record.data.name + "\" class=\"icon-grid-image\"/>";
	},

	//private
	buildUIButtons: function() {
		this.addButton = new Ext.Button({
			text: this.buttonsTr.add,
			iconCls: 'add',
			scope: this,
			handler: this.onAddClick
		});

		this.saveButton = new Ext.Button({
			text: this.buttonsTr.save,
			scope: this,
			disabled: true,
			formBind: true,
			handler: this.onSave
		});
		
		this.abortButton = new Ext.Button({
			text: this.buttonsTr.abort,
			scope: this,
			disabled: true,
			handler: this.onAbort
		});
		
		this.modifyButton = new Ext.Button({
	    	text: this.buttonsTr.modify,
	    	iconCls: 'modify',
	    	scope: this,
	    	disabled: true,
	    	handler: this.onModify
	    });
		
		this.removeButton = new Ext.Button({
	    	text: this.buttonsTr.remove,
	    	iconCls: 'delete',
	    	scope: this,
	    	disabled: true,
	    	handler: this.onRemove
	    });
		
  	},
  	
  	//private
  	onRowSelect: function(sm, record, index) {
  		this.disableModify();
  		this.modifyButton.enable();
  		this.removeButton.enable();
  		this.uploadForm.getForm().reset();
  		this.uploadForm.getForm().loadRecord(record);
		Ext.apply(this.description, {
			translationsKeyName: record.get("name")
		});
  	},
  	
  	//private
  	onAddClick: function() {
		this.iconsGrid.getSelectionModel().clearSelections();
		this.uploadForm.getForm().reset();
		this.enableModify();
		this.uploadForm.saveStatus = "add";
		_CMCache.initAddingTranslations();
	},
  	
  	//private	
  	onAbort: function() {
  		this.disableModify();
  		this.iconsGrid.getSelectionModel().clearSelections();
  		this.uploadForm.getForm().reset();
  	},
  	
  	//private  	
  	onModify: function() {
  		this.enableModify();
  		var descriptionField = this.uploadForm.getForm().findField("description");
  		if (descriptionField && descriptionField[0]) {
  			descriptionField[0].disable();
  		}
  		this.uploadForm.saveStatus = "modify";
		_CMCache.initModifyingTranslations();
 	},
  	
  	//private  	
  	disableModify: function() {
  		this.addButton.enable();
  		this.modifyButton.disable();
  		this.removeButton.disable();
  		this.saveButton.disable();
  		this.abortButton.disable();
  		this.uploadForm.setFieldsDisabled();
  	},

  	//private
  	enableModify: function() {
  		this.addButton.disable();
  		this.modifyButton.disable();
  		this.removeButton.disable();
  		this.saveButton.enable();
  		this.abortButton.enable();
  		this.uploadForm.setFieldsEnabled(true);
  	},
  	
  	//private  	
  	onSave: function() {
  		var form = this.uploadForm.getForm();
  		//the save status is set only when click to add
  		//button or modify button. It's used to choose the url of
  		//the save request

		if (form.isValid()) {
			CMDBuild.LoadMask.get().show();
			var config = {
				scope: this,
				success: function(form, action) {
					var description = form.getValues().description;
					this.iconsGrid.store.load({
					    scope: this,
					    callback: function(records, operation, success) {
					        // the operation object
					        // contains all of the details of the load operation
					        console.log(records);
					        var r = this.iconsGrid.store.findRecord("description", description);
					        _CMCache.flushTranslationsToSave(r.get("name"));
					    }
					});
				},
				failure: this.requestFailure,
				callback: this.requestCallback
			};

			if (this.uploadForm.saveStatus == "add") {
				CMDBuild.ServiceProxy.Icons.upload(form, config);
			} else {
				CMDBuild.ServiceProxy.Icons.update(form, config);
			}
		}
  	},

  	//private  	
  	onRemove: function() {
  		var title = this.translation.alert.title;
		var msg = this.translation.alert.msg;
  		var doRequest = function(btn) {
  			if (btn != "yes") {
  				return
  			}
	  		var selectedRow = this.iconsGrid.getSelectionModel().getSelection();
	  		if (selectedRow && selectedRow.length > 0) {
	  			var selectedData = selectedRow[0];
	  			CMDBuild.LoadMask.get().show();
	  			CMDBuild.ServiceProxy.Icons.remove({
					scope : this,
					important: true,
					params : {
						"name": selectedData.get("name")
					},
					success: function(form, action) {
						this.iconsGrid.store.load();
					},
					failure: this.requestFailure,
					callback: this.requestCallback
		  	 	});
	  		}
  		};

  		Ext.Msg.confirm(title, msg, doRequest, this);
  	},

  	//private
  	requestFailure: this.onAbort,

  	//private  	
  	requestCallback: function() {
  		CMDBuild.LoadMask.get().hide();
		this.disableModify();
  	}
});