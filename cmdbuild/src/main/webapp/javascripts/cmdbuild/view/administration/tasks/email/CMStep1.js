(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1Delegate', {
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
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		getFromAddressFilterDelegate: function() {
			return this.view.fromAddresFilter.delegate;
		},

		getSubjectFilterDelegate: function() {
			return this.view.subjectFilter.delegate;
		},

		getValueId: function() {
			return this.view.idField.getValue();
		},

		setDisabledTypeField: function(state) {
			this.view.typeField.setDisabled(state);
		},

		setValueActive: function(value) {
			this.view.activeField.setValue(value);
		},

		setValueDescription: function(value) {
			this.view.descriptionField.setValue(value);
		},

		setValueEmailAccount: function(emailAccountName) {
			this.view.emailAccountCombo.setValue(emailAccountName);
		},

		setValueFilterFromAddress: function(filterString) {
			this.getFromAddressFilterDelegate().setValue(filterString);
		},

		setValueFilterSubject: function(filterString) {
			this.getSubjectFilterDelegate().setValue(filterString);
		},

		setValueId: function(value) {
			this.view.idField.setValue(value);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: CMDBuild.Translation.administration.tasks.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				value: tr.tasksTypes.email,
				disabled: true,
				cmImmutable: true,
				readOnly: true,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.ServiceProxy.parameter.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.ServiceProxy.parameter.ACTIVE,
				fieldLabel: CMDBuild.Translation.administration.tasks.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			this.emailAccountCombo = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT,
				fieldLabel: tr.taskEmail.emailAccount,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: CMDBuild.core.proxy.CMProxyEmailAccounts.getStore(),
				displayField: CMDBuild.ServiceProxy.parameter.NAME,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				forceSelection: true,
				editable: false
			});

			this.fromAddresFilter = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm', {
				fieldContainer: {
					fieldLabel: tr.taskEmail.fromAddressFilter
				},
				textarea: {
					name: CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS,
					id: 'FromAddresFilterField'
				},
				button: {
					titleWindow: tr.taskEmail.fromAddressFilter
				}
			});

			this.subjectFilter = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm', {
				fieldContainer: {
					fieldLabel: tr.taskEmail.subjectFilter
				},
				textarea: {
					name: CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT,
					id: 'SubjectFilterField'
				},
				button: {
					titleWindow: tr.taskEmail.subjectFilter
				}
			});

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.emailAccountCombo,
					this.fromAddresFilter,
					this.subjectFilter
				]
			});

			this.callParent(arguments);
		}
	});

})();