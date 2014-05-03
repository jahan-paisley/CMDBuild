(function() {
	var tr = CMDBuild.Translation.configure.step3;

	Ext.define("CMDBuild.setup.Step3",{
		extend: "Ext.panel.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		constructor: function() {
			this.title = CMDBuild.Translation.configure.title;

			this.adminUser = new Ext.form.TextField({
				name: 'admin_user',
				fieldLabel: tr.admin_user,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false,
				disabled: true
			});

			this.adminPassword = new Ext.form.TextField({
				name: 'admin_password',
				inputType: 'password', 
				fieldLabel: tr.admin_password,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false,
				disabled: true
			});

			this.confirmAdminPassword = new Ext.form.TextField({
				inputType : 'password',
				fieldLabel : tr.confirm_password,
				labelWidth: CMDBuild.LABEL_WIDTH,
				vtype : 'password',
				initialPassField : this.adminPassword.getId(),
				allowBlank : false,
				disabled : true
			});

			this.callParent(arguments);
		},
		
		initComponent: function() {
			Ext.apply(this, {
				frame: true,
				items: [
					this.adminUser,
					this.adminPassword,
					this.confirmAdminPassword
				]
			});
			
			this.callParent(arguments);

			this.on('show', this.enableFields);
			this.on('hide', this.disableFields);
		}
	});

})();