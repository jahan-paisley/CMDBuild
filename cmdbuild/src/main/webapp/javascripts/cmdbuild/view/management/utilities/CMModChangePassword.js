(function() {

	Ext.define("CMDBuild.view.management.utilities.CMModChangePassword", {
		extend: "Ext.panel.Panel",
		cmName: 'changepassword',
		title : CMDBuild.Translation.management.modutilities.changepassword.title,

		constructor: function() {
			this.saveBtn = new Ext.button.Button({
				id:'confirmButton',
				text: CMDBuild.Translation.common.buttons.save,
				scope: this,
				handler: onSaveButtonClick
			});

			this.abortBtn = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.abort,
				scope: this,
				handler: function(){
					this.form.getForm().reset();
				}
			});

			this.form = new Ext.form.Panel({
				plugins: new CMDBuild.CallbackPlugin(),
				url: 'services/json/schema/modsecurity/changepassword',
				frame: true,
				border: false,
				region: "center",
				items: [{
					xtype: 'textfield',
					inputType:'password', 
					fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.oldpw,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: 'oldpassword',
					allowBlank: false
				},{
					xtype: 'textfield',
					inputType:'password', 
					fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.newpw,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: 'newpassword',
					id:'newpassword',
					allowBlank: false
				},{
					xtype: 'textfield',
					inputType:'password', 
					fieldLabel: CMDBuild.Translation.management.modutilities.changepassword.repnew,
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: 'confirmnewpassword',
					vtype: 'password',
					initialPassField: 'newpassword',
					allowBlank: false
				}]
			});

			Ext.apply(this, {
				frame: true,
				border: false,
				layout: "border",
				items:[this.form],
				buttonAlign: 'center',
				buttons: [this.saveBtn, this.abortBtn]
			});

			this.callParent(arguments);
		}
	});

	function onSaveButtonClick(){
		if (this.form.getForm().isValid()) {
			this.form.getForm().submit({
				scope: this,
				callback: function(){this.form.getForm().reset()},
				success: function(){
					CMDBuild.Msg.info("", CMDBuild.Translation.management.modutilities.changepassword.successmsgtext);
				}
			});
		} else {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
		}
	}

})();