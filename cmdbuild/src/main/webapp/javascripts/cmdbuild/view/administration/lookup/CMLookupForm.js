(function() {

	var LOOKUP_FIELDS = CMDBuild.ServiceProxy.LOOKUP_FIELDS;
	var tr = CMDBuild.Translation.administration.modLookup.lookupForm;

	Ext.define("CMDBuild.view.administration.lookup.CMLookupForm", {
		extend : "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		alias : "widget.lookupform",

		constructor : function() {

			this.modifyButton = new Ext.button.Button({
				iconCls: 'modify',
				text: tr.update_lookup,
				handler: function() {
					this.enableModify();
					_CMCache.initModifyingTranslations();
				},
				scope: this
			});

			this.disabelButton = new Ext.button.Button({
				iconCls : 'delete',
				text : tr.disable_lookup,
				handler : this.onDisableEnableAction,
				scope : this
			});

			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save,
				disabled: true
			})

	 		this.abortButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.abort,
				disabled: true,
				scope: this,
				handler: function() {
	 				this.disableModify()
	 			}
			})

			this.cmButtons = [this.saveButton, this.abortButton];
	 		this.cmTBar = [this.modifyButton, this.disabelButton];

			this.parentStore = new Ext.data.Store({
				model : "CMLookupTypeForParentStoreCombo",
				autoLoad : false,

				proxy : {
					url : 'services/json/schema/modlookup/getparentlist',
					type : "ajax",
					reader : {
						root : 'rows',
						type : "json"
					}
				},

				sorters : [ {
					property : LOOKUP_FIELDS.ParentDescription,
					direction : "ASC"
				} ]
			});

			this.activeCheck = new Ext.ux.form.XCheckbox({
				fieldLabel : tr.active,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : LOOKUP_FIELDS.Active,
				checked : true,
				disabled : true
			});

			this.layout = "border",
			this.description = new Ext.form.CMTranslatableText( {
				labelWidth: CMDBuild.LABEL_WIDTH,
				fieldLabel : tr.description,
				name : LOOKUP_FIELDS.Description,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank : false,
				disabled : true,
				translationsKeyType: "Lookup",
				translationsKeyField: "Description"
			});

			this.items = [{
				xtype: "panel",
				frame: true,
				border: false,
				padding: "5 5 5 5",
				region: "center",
				autoScroll: true,
				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH
				},
				items: [ {
					xtype : 'hidden',
					name : LOOKUP_FIELDS.Id
				}, {
					xtype : 'textfield',
					fieldLabel : tr.code,
					name : LOOKUP_FIELDS.Code,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					disabled : true
				},
					this.description,
				{
					xtype : 'combo',
					fieldLabel : tr.parentdescription,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					name : LOOKUP_FIELDS.ParentId,
					hiddenName : LOOKUP_FIELDS.ParentId,
					valueField : LOOKUP_FIELDS.ParentId,
					displayField : LOOKUP_FIELDS.ParentDescription,
					minChars : 0,
					disabled : true,
					store : this.parentStore,
					queryMode: "local",
					listConfig: {
						loadMask: false
					}
				}, {
					xtype : 'textarea',
					fieldLabel : tr.notes,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					name : LOOKUP_FIELDS.Notes,
					disabled : true
				}, this.activeCheck]
			}];

			Ext.apply(this, {
				tbar : this.cmTBar,
				defaultType : 'textfield',
				frame : false,
				border : false,
	 			cls: "x-panel-body-default-framed cmbordertop",
				bodyCls: 'cmgraypanel',
				buttonAlign : "center",
				buttons : this.cmButtons
			});

			this.callParent(arguments);
		},

		onSelectLookupType: function(lookupType) {
			if (lookupType) {
				this.type = lookupType;
			}

			this._reloadParentStore();
			this.getForm().reset();
			this.disableModify(enableCMTBar = false);
		},

		onSelectLookupGrid: function(selection) {
			this.getForm().loadRecord(selection);
			this.updateDisableEnableLookup();
			this.disableModify(enableCMTbar = true);
		},

		onAddLookupClick: function() {
			this.getForm().reset();
			this.enableModify();
		},

		_reloadParentStore : function() {
			if (this.type) {
				this.parentStore.load({
					params: {
						type: this.type
					}
				});
			} else {
				throw new Error('Reload Parent store with no type in LookupForm');
			}
		},

		updateDisableEnableLookup : function() {
			if (this.activeCheck.getValue()) {
				this.disabelButton.setText(tr.disable_lookup);
				this.disabelButton.setIconCls('delete');
			} else {
				this.disabelButton.setText(tr.enable_lookup);
				this.disabelButton.setIconCls('ok');
			}
		}
	});
})();
