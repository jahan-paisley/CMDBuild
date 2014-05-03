Ext.define("CMDBuild.LoginWindowClass", {
	extend: "Ext.window.Window",
	ajaxOptions: [],

	initComponent: function() {

		var me = this;

		var enterKeyListener = {
				'specialkey': function(field, event) {
					if(event.getKey() == event.ENTER) {
						me.doLogin(field, event);
					}
				}
			};

		Ext.apply(this, {
			title: CMDBuild.Translation.login.relogin_title,
			width: 300,
			height: 155,
			layout: 'fit',
			bodyStyle: {
				padding: "5px 5px 10px 5px"
			},
			items: [this.form = new Ext.form.FormPanel({
				url: 'services/json/login/login',
				frame: false,
				bodyCls: "x-panel-body-default-framed",
				cls: "x-panel-body-default-framed",
				border: false,
				bodyStyle: {
					padding: "5px"
				},
				trackResetOnLoad: true,
				items: [this.messageCmp = new Ext.form.field.Display({
					value: CMDBuild.Translation.login.relogin_message,
					style: {
						padding: "0px 0px 5px 0px"
					}
				}),
				this.usernameField = new Ext.form.Hidden({
					name: 'username',
					value: CMDBuild.Runtime.Username
				}),
				this.passwordField = new Ext.form.Field({
					name: 'password',
					inputType : 'password',
					fieldLabel : CMDBuild.Translation.login.password,
					allowBlank : false,
					hidden: !CMDBuild.Runtime.AllowsPasswordLogin,
					listeners: enterKeyListener
				}),
				{
					name: 'role',
					xtype: 'hidden',
					value:  CMDBuild.Runtime.DefaultGroupName
				}],
				buttonAlign: 'center',
				buttons: [{
					text : CMDBuild.Translation.login.login,
					formBind : true,
					handler: this.doLogin,
					scope: this,
					hidden: !CMDBuild.Runtime.AllowsPasswordLogin
				},{
					text : CMDBuild.Translation.login.change_user,
					handler : this.reloadPage,
					hidden: !CMDBuild.Runtime.AllowsPasswordLogin
				},{
					text : Ext.MessageBox.buttonText.ok,
					handler : this.reloadPage,
					hidden: CMDBuild.Runtime.AllowsPasswordLogin
				}]
			})]
		});
		this.callParent(arguments);
	},

	addAjaxOptions: function(requestOption) {
		this.ajaxOptions.push(requestOption);
	},

	setAuthFieldsEnabled: function(enabled) {
		this.usernameField.setDisabled(!enabled);
		this.passwordField.setDisabled(!enabled);
		if (!enabled) {
			this.passwordField.setValue("******");
		}
	},

	doLogin: function() {
		CMDBuild.LoadMask.get().show();
		this.hide();
		this.form.getForm().submit({
			important: true,
			success: function() {
				this.passwordField.reset();
				if (this.refreshOnLogin) {
					window.location.reload();
				} else {
					CMDBuild.LoadMask.get().hide();
					for (var requestOption; requestOption=this.ajaxOptions.pop();) {
						CMDBuild.Ajax.request(requestOption);
					}
				}
			},
			failure: function(form, action) {
				this.showWithoutBringingToFront();
				CMDBuild.LoadMask.get().hide();
			},
			scope: this
		});
	},

	reloadPage: function() {
		window.location = ".";
	},

	/*
	 * Hack to let the error messages appear on top of it.
	 * Ext.Component.toFrontOnShow does not work because
	 * Ext.Component.afterShow does not check it and calls
	 * toFront anyway!
	 */
	showWithoutBringingToFront: function() {
		var oldToFront = this.toFront;
		this.toFront = Ext.emptyFn;
		this.show();
		this.toFront = oldToFront;
	}
});

CMDBuild.LoginWindow = new CMDBuild.LoginWindowClass({
	modal:true,
	refreshOnLogin: true
});