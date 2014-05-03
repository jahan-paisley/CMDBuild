(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskConnector;

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep3Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		filterWindow: undefined,
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
				case 'onDbFieldsetExpand':
					return this.onDbFieldsetExpand();

				case 'onLdapFieldsetExpand':
					return this.onLdapFieldsetExpand();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onDbFieldsetExpand: function() {
			this.view.ldapFieldset.collapse();
			this.view.ldapFieldset.reset();
			this.view.dataSourceField.setValue('db');
		},

		onLdapFieldsetExpand: function() {
			this.view.dbFieldset.collapse();
			this.view.dbFieldset.reset();
			this.view.dataSourceField.setValue('ldap');
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.connector.CMStep3', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'connector',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep3Delegate', this);

			this.prefixField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.viewPrefix,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.VIEW_PREFIX,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			this.dataSourceField = Ext.create('Ext.form.field.Hidden', {
				name: 'CMDBuild.ServiceProxy.parameter.DATA_SOURCE'
			});

			// DataSource: relationa databases configuration
				this.dbType = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.DB_TYPE,
					fieldLabel: CMDBuild.Translation.administration.tasks.type,
					labelWidth: CMDBuild.LABEL_WIDTH,
					store: Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.VALUE],
						data: [
							{ 'name': 'MySQL', 'value': 'mysql' },
							{ 'name': 'Oracle', 'value': 'oracle' },
							{ 'name': 'PostgreSQL', 'value': 'postgresql' },
							{ 'name': 'SQLServer', 'value': 'sqlserver' }
						]
					}),
					displayField: CMDBuild.ServiceProxy.parameter.NAME,
					valueField: CMDBuild.ServiceProxy.parameter.VALUE,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					forceSelection: true,
					editable: false
				});

				this.dbAddressField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.ADDRESS,
					fieldLabel: CMDBuild.Translation.address,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.dbPortField = Ext.create('Ext.form.field.Number', {
					name: CMDBuild.ServiceProxy.parameter.PORT,
					fieldLabel: CMDBuild.Translation.port,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.ADM_BIG_FIELD_WIDTH,
					minValue: 1,
					maxValue: 65535,
					allowBlank: true
				});

				this.dbNameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.DB_NAME,
					fieldLabel: tr.dbName,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.dbUsernameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.USERNAME,
					fieldLabel: CMDBuild.Translation.username,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.dbPasswordField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.PASSWORD,
					inputType: 'password',
					fieldLabel: CMDBuild.Translation.password,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.dbFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.datasourceDbFieldset,
					checkboxToggle: true,
					collapsed: true,
					layout: 'vbox',

					items: [
						this.dbType,
						this.dbAddressField,
						this.dbPortField,
						this.dbNameField,
						this.dbUsernameField,
						this.dbPasswordField
					],

					listeners: {
						beforeexpand: function(fieldset, eOpts) {
							me.delegate.cmOn('onDbFieldsetExpand');
						}
					}
				});

				this.dbFieldset.fieldWidthsFix();
			// END - DataSource: relationa databases configuration

			// DataSource: LDAP configuration
				this.ldapAddressField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.ADDRESS,
					fieldLabel: CMDBuild.Translation.address,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.ldapUsernameField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.USERNAME,
					fieldLabel: CMDBuild.Translation.username,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.ldapPasswordField = Ext.create('Ext.form.field.Text', {
					name: CMDBuild.ServiceProxy.parameter.PASSWORD,
					inputType: 'password',
					fieldLabel: CMDBuild.Translation.password,
					labelWidth: CMDBuild.LABEL_WIDTH,
					width: CMDBuild.CFG_BIG_FIELD_WIDTH,
					allowBlank: false
				});

				this.ldapFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.datasourceLdapFieldset,
					checkboxToggle: true,
					collapsed: true,
					layout: 'vbox',

					items: [
						this.ldapAddressField,
						this.ldapUsernameField,
						this.ldapPasswordField
					],

					listeners: {
						beforeexpand: function(fieldset, eOpts) {
							me.delegate.cmOn('onLdapFieldsetExpand');
						}
					}
				});

				this.ldapFieldset.fieldWidthsFix();
			// END - DataSource: LDAP configuration

			Ext.apply(this, {
				items: [
					this.prefixField,
					this.dataSourceField,
					this.dbFieldset
// TODO: future implementation
//					,
//					this.ldapFieldset
				]
			});

			this.callParent(arguments);
		}
	});

})();