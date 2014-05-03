(function() {

var tr = CMDBuild.Translation.administration.modsecurity.group;

Ext.define("CMDBuild.view.administration.group.CMGroupForm", {
	extend: "Ext.form.Panel",
	mixins: {
		cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
	},
	alias: "groupform",
  
	constructor: function() {

		this.enableGroupButton = new Ext.button.Button({
			iconCls: 'delete',
			text: tr.delete_group
		});
		
		this.modifyButton = new Ext.button.Button({
			iconCls: 'modify',
			text: tr.modify_group
		});
		
		this.saveButton = new CMDBuild.buttons.SaveButton();
		this.abortButton = new CMDBuild.buttons.AbortButton();

		this.groupName = new Ext.form.field.Text({
			fieldLabel : tr.group_name,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'name',
			allowBlank : false,
			vtype: 'alphanum',
			cmImmutable: true
		});

		this.groupDescription = new Ext.form.field.Text({
			fieldLabel : tr.group_description,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'description',
			allowBlank : false
		});

		this.groupType = new Ext.form.field.ComboBox({
			store: new Ext.data.Store({
				fields: ["value", "description"],
				data: [{
					value: CMDBuild.cache.CMGroupModel.type.NORMAL,
					description: tr.normal
				}, {
					value: CMDBuild.cache.CMGroupModel.type.ADMIN,
					description: tr.administrator
				}, {
					value: CMDBuild.cache.CMGroupModel.type.CLOUD_ADMIN,
					description: tr.limited_admin
				}]
			}),
			fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.type,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name: 'type',
			valueField: 'value',
			displayField: 'description',
			editable: false,
			queryMode: 'local'
		});

		this.groupEmail = new Ext.form.field.Text({
			vtype : 'emailOrBlank',
			fieldLabel : tr.email,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'email',
			allowBlank : true
		});

		this.activeCheck = new Ext.ux.form.XCheckbox({
			fieldLabel : tr.is_active,
			labelWidth: CMDBuild.LABEL_WIDTH,
			name : 'isActive',
			checked : true 
		});

		this.startingClass = new CMDBuild.field.ErasableCombo({
			fieldLabel : tr.starting_class,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : 'startingClass',
			valueField : 'id',
			displayField : 'description',
			editable: false,
			store : _CMCache.getClassesAndProcessesAndDahboardsStore(),
			queryMode: 'local'
		});

		this.cmTBar = [this.modifyButton, this.enableGroupButton ];
		this.cmButtons = [this.saveButton, this.abortButton];

		this.callParent(arguments);
	},

	initComponent: function() {
		this.tbar = this.cmTBar;
		this.items = [
			this.groupName,
			this.groupDescription,
			this.groupType,
			this.groupEmail,
			this.startingClass,
			this.activeCheck
		];

		this.buttonAlign = 'center';
		this.buttons = this.cmButtons;
		this.frame = false;
		this.border = false;
		this.cls = "x-panel-body-default-framed";
		this.bodyCls = "cmgraypanel";

		this.callParent(arguments);
		this.disableModify(enableTBar = false);
	},

	updateDisableEnableGroup : function() {
		if (this.activeCheck.getValue()) {
			this.enableGroupButton.setText(tr.delete_group);
			this.enableGroupButton.setIconCls('delete');
		} else {
			this.enableGroupButton.setText(tr.enable_group);
			this.enableGroupButton.setIconCls('ok');
		}
	},

	loadGroup: function(g) {
		this.reset();
		this.getForm().loadRecord(g);
		this.groupType.setValue(g.getType());
		this.updateDisableEnableGroup();
	},

	setDefaults: function() {
		this.groupType.setValue(CMDBuild.cache.CMGroupModel.type.NORMAL);
	}
});
})();