(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskEmail;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep3Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onCheckedAttachmentsFieldset':
					return this.onCheckedAttachmentsFieldset();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		getValueAttachmentsFieldsetCheckbox: function() {
			return this.view.attachmentsFieldset.checkboxCmp.getValue();
		},

		/**
		 * Read CMDBuild's alfresco configuration from server and set Combobox store
		 */
		onCheckedAttachmentsFieldset: function() {
			var me = this;

			if (this.view.attachmentsCombo.store.getCount() == 0) {
				CMDBuild.ServiceProxy.configuration.read({
					success: function(response) {
						var decodedJson = Ext.JSON.decode(response.responseText);

						me.view.attachmentsCombo.bindStore(
							CMDBuild.ServiceProxy.lookup.getLookupFieldStore(decodedJson.data['category.lookup'])
						);
					}
				}, name = 'dms');
			}
		},

		setValueAttachmentsCombo: function(value) {
			if (!Ext.isEmpty(value)) {
				// HACK to avoid forceSelection timing problem witch don't permits to set combobox value
				this.view.attachmentsCombo.forceSelection = false;
				this.view.attachmentsCombo.setValue(value);
				this.view.attachmentsCombo.forceSelection = true;
			}
		},

		setValueAttachmentsFieldsetCheckbox: function(value) {
			if (value) {
				this.view.attachmentsFieldset.expand();
				this.onCheckedAttachmentsFieldset();
			} else {
				this.view.attachmentsFieldset.collapse();
			}
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep3', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep3Delegate', this);

			// BodyParsing configuration
				this.bodyParsingFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.bodyParsing,
					checkboxToggle: true,
					checkboxName: 'CMDBuild.ServiceProxy.parameter.BODY_PARSING_ACTIVE',
					collapsed: true,
					layout: {
						type: 'vbox',
						align: 'stretch'
					},
					items: [
						{
							xtype: 'container',
							layout: 'hbox',

							defaults: {
								labelWidth: CMDBuild.LABEL_WIDTH,
								xtype: 'textfield'
							},

							items: [
								{
									fieldLabel: tr.keyInit,
									name: CMDBuild.ServiceProxy.parameter.KEY_INIT,
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								},
								{
									fieldLabel: tr.keyEnd,
									name: CMDBuild.ServiceProxy.parameter.KEY_END,
									margin: '0 0 0 20',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								}
							]
						},
						{
							xtype: 'container',
							layout: 'hbox',
							margin: '10 0',

							defaults: {
								labelWidth: CMDBuild.LABEL_WIDTH,
								xtype: 'textfield'
							},

							items: [
								{
									fieldLabel: tr.valueInit,
									name: CMDBuild.ServiceProxy.parameter.VALUE_INIT,
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								},
								{
									fieldLabel: tr.valueEnd,
									name: CMDBuild.ServiceProxy.parameter.VALUE_END,
									margin: '0 0 0 20',
									width: CMDBuild.ADM_BIG_FIELD_WIDTH
								}
							]
						}
					]
				});
			// END: BodyParsing configuration

			// SendMail configuration
				this.emailTemplateCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					fieldLabel: CMDBuild.Translation.administration.tasks.template,
					labelWidth: CMDBuild.LABEL_WIDTH,
					itemId: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					store: CMDBuild.core.proxy.CMProxyEmailTemplates.getStore(),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.NAME,
					forceSelection: true,
					editable: false,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.sendMailFieldset = Ext.create('Ext.form.FieldSet', {
					title: CMDBuild.Translation.administration.tasks.sendMail,
					checkboxToggle: true,
					checkboxName: 'CMDBuild.ServiceProxy.parameter.SEND_MAIL_ACTIVE',
					collapsed: true,
					layout: {
						type: 'vbox'
					},
					items: [this.emailTemplateCombo]
				});
			// END: SendMail configuration

			// Alfresco configuration
				this.attachmentsCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.ATTACHMENTS_CATEGORY,
					fieldLabel: tr.attachmentsCategory,
					labelWidth: CMDBuild.LABEL_WIDTH,
					displayField: 'Description',
					valueField: 'Id',
					forceSelection: true,
					editable: false,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				});

				this.attachmentsFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.saveToAlfresco,
					checkboxToggle: true,
					checkboxName: CMDBuild.ServiceProxy.parameter.ATTACHMENTS_ACTIVE,
					collapsed: true,
					layout: {
						type: 'vbox'
					},
					items: [this.attachmentsCombo],

					listeners: {
						expand: function(fieldset, eOpts) {
							me.delegate.cmOn('onCheckedAttachmentsFieldset');
						}
					}
				});
			// END: Alfresco configuration

			Ext.apply(this, {
				items: [
					this.bodyParsingFieldset,
					this.sendMailFieldset,
					this.attachmentsFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();