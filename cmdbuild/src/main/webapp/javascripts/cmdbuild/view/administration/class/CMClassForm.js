(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;

	Ext.define("CMDBuild.view.administration.classes.CMClassForm", {
		extend : "Ext.panel.Panel",
		title : tr.title_add,
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		alias : "classform",
		defaultParent : "Class",
		layout : 'border',

		// config
		/**
		 * set to false to deny
		 * the building of save and
		 * abort buttons
		 */
		whithSaveAndCancelButtons: true,
		// config

		initComponent : function() {
			this.plugins = [new CMDBuild.FormPlugin()];
			this.border = false;
			this.frame = false;
			this.cls = "x-panel-body-default-framed";
			this.bodyCls = 'cmgraypanel';

			this.buildButtons();
			this.buildFormFields();
			this.buildItems();

			if (this.whithSaveAndCancelButtons) {
				this.buttonAlign = 'center';
				this.buttons = this.cmButtons;
			}

			this.tbar = this.cmTBar;

			this.callParent(arguments);

			this.typeCombo.on("select", onSelectType, this);
			this.className.on("change", function(fieldname, newValue, oldValue) {
				this.autoComplete(this.classDescription, newValue, oldValue);
			}, this);

			this.disableModify();
		},

		getForm : function() {
			return this.form.getForm();
		},

		onClassSelected : function(selection) {
			this.getForm().loadRecord(selection);
			Ext.apply(this.classDescription, {
				translationsKeyName: selection.get("name")
			});
			this.disableModify(enableCMTbar = true);
		},

		onAddClassButtonClick: function() {
			this.reset();
			this.inheriteCombo.store.cmFill();
			this.enableModify(all=true);
			this.setDefaults();
		},

		setDefaults: function() {
			this.isActive.setValue(true);
			this.typeCombo.setValue("standard");
			this.inheriteCombo.setValue(_CMCache.getClassRootId())
		},

		buildButtons: function() {
			this.deleteButton = new Ext.button.Button( {
				iconCls : 'delete',
				text : tr.remove_class
			}),

			this.modifyButton = new Ext.button.Button( {
				iconCls : 'modify',
				text : tr.modify_class,
				handler: function() {
					this.enableModify();
					_CMCache.initModifyingTranslations();
				},
				scope: this
			}),

			this.printClassButton = new CMDBuild.PrintMenuButton( {
				text : tr.print_class,
				formatList : [ 'pdf', 'odt' ]
			});

			if (this.whithSaveAndCancelButtons) {
				this.saveButton = new Ext.button.Button( {
					text : CMDBuild.Translation.common.buttons.save
				});
	
				this.abortButton = new Ext.button.Button( {
					text : CMDBuild.Translation.common.buttons.abort
				});

				this.cmButtons = [ this.saveButton, this.abortButton ];
			}

			this.cmTBar = [ this.modifyButton, this.deleteButton, this.printClassButton ];
		},

		// protected
		buildFormFields: function() {
			this.inheriteCombo = new Ext.form.ComboBox( {
				fieldLabel : tr.inherits,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : 'parent',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				cmImmutable : true,
				defaultParent : this.defaultParent,
				queryMode : "local",
				store : this.buildInheriteComboStore()
			});

			this.className = new Ext.form.field.Text( {
				fieldLabel : tr.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : 'name',
				allowBlank : false,
				vtype : 'alphanum',
				cmImmutable : true
			});

			this.classDescription = new Ext.form.CMTranslatableText( {
				fieldLabel : tr.description,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name : 'text',
				allowBlank : false,
				vtype : 'cmdbcomment',
				translationsKeyType: "Class", 
				translationsKeyField: "Description"
			});

			this.isSuperClass = new Ext.ux.form.XCheckbox( {
				fieldLabel : tr.superclass,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'superclass',
				cmImmutable : true
			});

			this.isActive = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.active,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'active'
			});

			var types = Ext.create('Ext.data.Store', {
				fields: ['value', 'name'],
				data : [
					{"value":"standard", "name":tr.standard},
					{"value":CMDBuild.Constants.cachedTableType.simpletable, "name":tr.simple}
				]
			});

			this.typeCombo = new Ext.form.field.ComboBox({
				fieldLabel : tr.type,
				labelWidth : CMDBuild.LABEL_WIDTH,
				width : CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
				name : 'tableType',
				hiddenName : 'tableType',
				valueField : 'value',
				displayField : 'name',
				editable : false,
				queryMode : "local",
				cmImmutable : true,
				store: types
			});

			this.typeCombo.setValue = Ext.Function.createInterceptor(this.typeCombo.setValue,
			onTypeComboSetValue, this);
		},

		// protected
		buildItems: function() {
			this.form = new Ext.form.FormPanel( {
				region: "center",
				frame: false,
				border: false,
				cls: "x-panel-body-default-framed",
				bodyCls: 'cmgraypanel',
				defaultType: 'textfield',
				monitorValid: true,
				autoScroll: true,
				items: this.getFormItems()
			});

			this.items = [this.form];
		},

		// protected
		getFormItems: function() {
			return [
				this.className,
				this.classDescription,
				this.typeCombo,
				this.inheriteCombo,
				this.isSuperClass,
				this.isActive
			]
		},

		buildInheriteComboStore: function() {
			return _CMCache.getSuperclassesAsStore();
		}
	});

	function onSelectType(field, selections) {
		var s = selections[0];
		if (s) {
			onTypeComboSetValue.call(this, s.get("value"));
		}
	}
			
	function onTypeComboSetValue(value) {
		if (value == "simpletable") {
			this.isSuperClass.hide();
			this.inheriteCombo.hide();
		} else {
			this.isSuperClass.show();
			this.inheriteCombo.show();
		}
	}
			
})();