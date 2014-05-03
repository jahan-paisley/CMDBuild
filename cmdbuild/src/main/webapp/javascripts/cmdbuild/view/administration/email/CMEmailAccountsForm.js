(function() {

	var tr = CMDBuild.Translation.administration.email.accounts; // Path to translation

	Ext.define('CMDBuild.view.administration.email.CMEmailAccountsForm', {
		extend: 'Ext.form.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		delegate: undefined,

		autoScroll: false,
		buttonAlign: 'center',
		layout: 'fit',
		split: true,
		frame: false,
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		bodyCls: 'cmgraypanel',

		initComponent: function() {
			// Buttons configuration
			this.removeButton = Ext.create('Ext.button.Button', {
				id: 'removeButton',
				iconCls: 'delete',
				text: tr.remove,
				scope: this,
				handler: function() {
					this.delegate.cmOn('onRemoveButtonClick');
				}
			});

			this.setDefaultButton = Ext.create('Ext.button.Button', {
				id: 'setDefaultButton',
				iconCls: 'ok',
				text: tr.setDefault,
				scope: this,
				handler: function() {
					this.delegate.cmOn('onSetDefaultButtonClick');
				}
			});

			this.cmTBar = [
				Ext.create('Ext.button.Button', {
					iconCls: 'modify',
					text: tr.modify,
					scope: this,
					handler: function() {
						this.delegate.cmOn('onModifyButtonClick');
					}
				}),
				this.removeButton,
				this.setDefaultButton
			];

			this.cmButtons = [
				Ext.create('CMDBuild.buttons.SaveButton', {
					scope: this,
					handler: function() {
						this.delegate.cmOn('onSaveButtonClick');
					}
				}),
				Ext.create('CMDBuild.buttons.AbortButton', {
					scope: this,
					handler: function() {
						this.delegate.cmOn('onAbortButtonClick');
					}
				})
			];
			// END: Buttons configuration

			// Page FieldSets configuration
			this.nameField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.NAME,
				itemId: CMDBuild.ServiceProxy.parameter.NAME,
				fieldLabel: CMDBuild.Translation.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				allowBlank: false
			});

			this.isDefaultField = Ext.create('Ext.form.field.Checkbox', {
				hidden: true,
				name: CMDBuild.ServiceProxy.parameter.IS_DEFAULT
			});

			this.emailAccount = Ext.create('Ext.form.FieldSet', {
				title: tr.account,

				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				items: [
					this.nameField,
					this.isDefaultField,
					{
						xtype: 'hiddenfield',
						name: CMDBuild.ServiceProxy.parameter.ID,
						labelWidth: CMDBuild.LABEL_WIDTH
					}
				]
			});

			this.credentials = Ext.create('Ext.form.FieldSet', {
				title: tr.credentials,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					xtype: 'textfield'
				},

				items: [
					{
						fieldLabel: tr.username,
						allowBlank: false,
						name: CMDBuild.ServiceProxy.parameter.USERNAME
					},
					{
						inputType: 'password',
						fieldLabel: tr.password,
						allowBlank: false,
						name: CMDBuild.ServiceProxy.parameter.PASSWORD
					}
				]
			});

			this.outgoing = Ext.create('Ext.form.FieldSet', {
				title: tr.outgoing,
				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					xtype: 'textfield'
				},

				items: [
					{
						fieldLabel: CMDBuild.Translation.address,
						allowBlank: false,
						name: CMDBuild.ServiceProxy.parameter.ADDRESS
					},
					{
						fieldLabel: tr.smtpServer,
						name: CMDBuild.ServiceProxy.parameter.SMTP_SERVER
					},
					{
						xtype: 'numberfield',
						fieldLabel: tr.smtpPort,
						allowBlank: true,
						minValue: 1,
						maxValue: 65535,
						name: CMDBuild.ServiceProxy.parameter.SMTP_PORT
					},
					{
						xtype: 'checkbox',
						fieldLabel: tr.enableSsl,
						name: CMDBuild.ServiceProxy.parameter.SMTP_SSL
					}
				]
			});

			this.incoming = Ext.create('Ext.form.FieldSet', {
				title: tr.incoming,

				items: [
					{
						xtype: 'container',
						padding: '0 0 5 0',
						cls: "x-panel-body-default-framed cmborderbottom",

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH,
							xtype: 'textfield'
						},

						items: [
							{
								fieldLabel: tr.imapServer,
								name: CMDBuild.ServiceProxy.parameter.IMAP_SERVER
							},
							{
								xtype: 'numberfield',
								fieldLabel: tr.imapPort,
								allowBlank: true,
								minValue: 1,
								maxValue: 65535,
								name: CMDBuild.ServiceProxy.parameter.IMAP_PORT
							},
							{
								xtype: 'checkbox',
								fieldLabel: tr.enableSsl,
								name: CMDBuild.ServiceProxy.parameter.IMAP_SSL
							}
						]
					},
					{
						xtype: 'container',
						padding: '5 0',

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						defaults: {
							labelWidth: CMDBuild.LABEL_WIDTH,
							xtype: 'textfield'
						},

						items: [
							{
								fieldLabel: tr.incomingFolder,
								name: CMDBuild.ServiceProxy.parameter.INCOMING_FOLDER
							},
							{
								fieldLabel: tr.processedFolder,
								name: CMDBuild.ServiceProxy.parameter.PROCESSED_FOLDER
							},
							{
								fieldLabel: tr.rejectedFolder,
								name: CMDBuild.ServiceProxy.parameter.REJECTED_FOLDER
							},
							{
								xtype: 'checkbox',
								fieldLabel: tr.enableMoveRejectedNotMatching,
								name: CMDBuild.ServiceProxy.parameter.ENABLE_MOVE_REJECTED_NOT_MATCHING
							}
						]
					}
				]
			});
			// END: Page FieldSets configuration

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.container.Container', {
				region: 'center',
				frame: false,
				border: false,

				layout: {
					type: 'hbox',
					align:'stretch'
				},

				defaults: {
					flex: 1,
					layout: {
						type: 'vbox',
						align: 'stretch'
					}
				},

				items: [
					{
						xtype: 'container',
						margins: '0 3 0 0',
						items: [this.emailAccount, this.credentials]
					},
					{
						xtype: 'container',
						margins: '0 0 0 3',
						items: [this.outgoing, this.incoming]
					}
				]
			});

			Ext.apply(this, {
				tbar: this.cmTBar,
				items: [this.wrapper],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
			this.disableCMButtons();
		},

		disableNameField: function() {
			this.nameField.setDisabled(true);
		},

		/**
		 * Disable setDefaultButton and removeButton, if selected account is default
		 */
		disableSetDefaultAndRemoveButton: function() {
			this.setDefaultButton.setDisabled(
				this.isDefaultField.getValue()
			);

			this.removeButton.setDisabled(
				this.isDefaultField.getValue()
			);
		}
	});

})();