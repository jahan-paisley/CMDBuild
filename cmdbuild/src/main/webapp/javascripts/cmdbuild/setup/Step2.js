(function() {
	var tr = CMDBuild.Translation.configure.step2;

	Ext.define("CMDBuild.setup.Step2",{
		extend: "Ext.panel.Panel",
		constructor: function() {
			this.title = CMDBuild.Translation.configure.title;
			this.dbType = new Ext.form.ComboBox( { 
				name: 'db_type',
				fieldLabel: tr.db_create_type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				valueField: 'name',
				displayField: 'value',
				store: new Ext.data.SimpleStore({
					fields: ['name', 'value'],
					data : [
				        ['empty', tr.db_empty],
				        ['demo', tr.db_demo],
				        ['existing', tr.db_existing]
					]
				}),
				queryMode: 'local',
				editable: false,
				value: 'empty'
			});
			
			this.sharkSchema = new Ext.ux.form.XCheckbox({
				name: 'shark_schema',
				fieldLabel: tr.create_shark,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.dbTypeAndNameFieldSet = new Ext.form.FieldSet({
				title: tr.db_create,
				items: [
					this.dbType,
					{
						xtype: "textfield",
						name: 'db_name',
						fieldLabel: tr.db_create_name,
						labelWidth: CMDBuild.LABEL_WIDTH,
						allowBlank: false
					},
					this.sharkSchema
				]
			});
			
			this.host = new Ext.form.TextField({
				name: 'host',
				allowBlank: false,
				fieldLabel: tr.db_host,
				labelWidth: CMDBuild.LABEL_WIDTH,
				listeners: this.validationFieldListeners
			});
			
			this.port = new Ext.form.TextField({
				name: 'port',
				allowBlank: false,
				fieldLabel: tr.db_port,
				labelWidth: CMDBuild.LABEL_WIDTH,
				listeners: this.validationFieldListeners
			});
			
			this.user = new Ext.form.TextField({
				name: 'user',
				allowBlank: false,
				fieldLabel: tr.db_superUser,
				labelWidth: CMDBuild.LABEL_WIDTH,
				listeners: this.validationFieldListeners
			});

			this.password = new Ext.form.TextField({
				name: 'password',
				id: 'password',
				allowBlank: true,
				inputType:'password', 
				fieldLabel: tr.db_password,
				labelWidth: CMDBuild.LABEL_WIDTH,
				listeners: this.validationFieldListeners
			});

			this.connectionButton = new Ext.Button({
				text: tr.db_test_connection,
				disabled: false
			});

			this.dbConnectionFieldset = new Ext.form.FieldSet({
				title: tr.db_connection + ' (PostgreSQL ' + CMDBuild.Config.cmdbuild.jdbcDriverVersion +')',
				collapsed: false,
				items: [
					this.host,
					this.port,
					this.user,
					this.password,
					this.connectionButton
				]
			});

			this.userType = new Ext.form.ComboBox( { 
				name: 'user_type',
				fieldLabel: tr.user_type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				valueField: 'name',
				displayField: 'value',
				store: new Ext.data.SimpleStore({
					fields: ['name', 'value'],
					data : [
						['superuser', tr.db_superUser],
						['limuser', tr.limited_user],
						['new_limuser', tr.new_limited_user]
					]
				}),
				queryMode: 'local',
				editable: false,
				value: 'superuser'
			});

			this.limUser = new Ext.form.TextField({
				name: 'lim_user',
				fieldLabel: tr.db_user,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false,
				disabled: true
			});

			this.limPassword = new Ext.form.TextField({
				name: 'lim_password',
				inputType:'password', 
				fieldLabel: tr.db_password,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false,
				disabled: true
			});

			this.confirmLimPassword = new Ext.form.TextField({
				fieldLabel: tr.db_confirm_password,
				labelWidth: CMDBuild.LABEL_WIDTH,
				inputType:'password',
				vtype: 'password',
				initialPassField: this.limPassword.getId(),
				disabled: true
			});

			this.userFieldSet = new Ext.form.FieldSet({
				title: tr.db_user_create,
				defaultType: 'textfield',
				items: [
					this.userType,
					this.limUser,
					this.limPassword,
					this.confirmLimPassword
				]
			});

			this.callParent(arguments);
		},
		
		initComponent: function() {
			this.autoScroll = true;
			this.items = [this.dbTypeAndNameFieldSet, this.dbConnectionFieldset, this.userFieldSet];
			this.frame = true;

			this.callParent(arguments);

			this.userType.on('select', this.onUsertypeSelect, this);
		},
		
		onUsertypeSelect: function(combo, record, index){
			this.usertypeEnableFields(record[0].get("name"));
		},
		
		usertypeEnableFields: function(userType) {
			this.usertypeDisableFields();
			this.userType.enable();
			if (userType == "new_limuser") {
				this.limPassword.enable();
				this.confirmLimPassword.enable();
				this.limUser.enable();
			}	
			if (userType == "limuser") {
				this.limPassword.enable();
				this.limUser.enable();
			}
		},
		
		usertypeDisableFields: function(){
			this.limUser.disable();
			this.limPassword.disable();
			this.confirmLimPassword.disable();
			this.userType.disable();
		},
		
		usertypeEnableFields: function(userType){
			this.usertypeDisableFields();
			this.userType.enable();
			if (userType == "new_limuser") {
				this.limPassword.enable();
				this.confirmLimPassword.enable();
				this.limUser.enable();
			}	
			if (userType == "limuser") {
				this.limPassword.enable();
				this.limUser.enable();
			}
		},
		
		onDbTypeSelect: function(name) {
		   if (name == "existing") {
			   this.sharkSchema.disable();
			   this.sharkSchema.setValue(false);
			   this.usertypeDisableFields();
			   this.userFieldSet.hide();
		   } else {
			   this.sharkSchema.enable();
			   this.usertypeEnableFields(this.userType.getValue());
			   this.userFieldSet.show();
		   }
		}

	});
})();