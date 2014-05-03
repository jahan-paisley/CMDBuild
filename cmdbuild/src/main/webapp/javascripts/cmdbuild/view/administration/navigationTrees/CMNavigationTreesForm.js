(function() {

	Ext.define("CMDBuild.view.administration.navigationTrees.CMNavigationTreesForm", {
		extend: "Ext.form.Panel",
		alias: "navigationtreesform",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		translation : CMDBuild.Translation.administration.modClass.domainProperties,

		initComponent : function() {

			this.modifyButton = new Ext.button.Button({
				iconCls : 'modify',
				text: CMDBuild.Translation.tree_modify, 
				scope: this,
				handler: function() {
					this.delegate.cmOn("onModifyButtonClick");
				}
			});

			this.deleteButton = new Ext.button.Button({
				iconCls : 'delete',
				text: CMDBuild.Translation.tree_remove, 
				scope: this,
				handler: function() {
					this.delegate.cmOn("onDeleteButtonClick");
				}
			});

			this.saveButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.save,
				scope: this,
				handler: function() {
					this.delegate.cmOn("onSaveButtonClick");
				}
			});

			this.abortButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.abort,
				scope: this,
				handler: function() {
					this.delegate.cmOn("onAbortButtonClick");
				}
			});

			this.cmTBar = [this.modifyButton, this.deleteButton];
			this.cmButtons = [this.saveButton, this.abortButton];

			this.class_store = _CMCache.getClassesAndProcessesStore();


			this.active = new Ext.ux.form.XCheckbox({
				fieldLabel: this.translation.is_active,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: "active",
				checked: true
			});


			this.treeName = new Ext.form.TextField({
				fieldLabel : this.translation.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : "name",
				allowBlank : false,
				vtype : 'alphanum',
				enableKeyEvents: true,
				cmImmutable: true
			});

			this.treeDescription = new Ext.form.CMTranslatableText({
				fieldLabel : CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : "description",
				allowBlank : false,
				translationsKeyType: "Tree", 
				translationsKeyField: "Description",
				vtype : 'cmdbcomment'
			});
			this.rootName = new CMDBuild.field.CMBaseCombo({
				fieldLabel: this.translation.class_target,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: "rootClassName",
				triggerAction: 'all',
				valueField: 'id',
				displayField: 'description',
				minChars: 0,
				allowBlank: false,
				store: this.class_store,
				queryMode: "local",
				cmImmutable: true
			});
			this.formPanel = new Ext.form.FormPanel( {
				region : "center",
				frame : true,
				border : true,
				autoScroll : true,
				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH
				},
				items : [this.active, this.treeName, this.treeDescription, this.rootName]
			});

			Ext.apply(this, {
				tbar: this.cmTBar,
				buttonAlign: "center",
				buttons: this.cmButtons,
				frame: false,
				border: false,
				layout: "border",
				cls: "x-panel-body-default-framed",
				bodyCls: 'cmgraypanel',
				items: [this.formPanel]
			});
			
			this.plugins = [new CMDBuild.FormPlugin()];
			this.callParent(arguments);

			this.rootName.on('change', function(nameField, newValue, oldValue) {
			}, this);
			
			this.disableModify();
		},

		onTreeSelected: function(tree) {
			this.treeDescription.translationsKeyName = tree.type;
			if (tree) {
                this.reset();
				this.loadForm(tree);
			}
		},

		onNew: function() {
			this.resetForm();
		},

		getData: function() {
			var val = this.rootName.getValue();
			var name = this.treeName.getValue();
			if (! (val && name)) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return {
					error: true
				};
			}
			var et = (val == parseInt(val)) ?
					_CMCache.getEntryTypeById(this.rootName.getValue()) :
					_CMCache.getEntryTypeByName(this.rootName.getValue());
			return {
				name : this.treeName.getValue(),
				active: this.active.getValue(),
				description: this.treeDescription.getValue(),
				rootName: et.get("name"),
				rootDescription: et.get("text")
			};
		},

		setDefaultValues: function() {
			this.active.setValue(true);
		},
		
		onAddButtonClick: function() {
			this.reset();
			this.enableModify(all = true);
			this.setDefaultValues();
			_CMCache.initAddingTranslations();
		},

		enableModify: function(all) {
			this.mixins.cmFormFunctions.enableModify.call(this, all);
		},
		
		resetForm: function() {
			this.treeName.setValue("");
			this.treeDescription.setValue("");
			this.rootName.setValue("");
		},
		
		loadForm: function(tree) {
			this.treeName.setValue(tree.type);
			this.rootName.setValue(tree.targetClassName);
			this.treeDescription.setValue(tree.targetClassDescription);
		}
	});
})();