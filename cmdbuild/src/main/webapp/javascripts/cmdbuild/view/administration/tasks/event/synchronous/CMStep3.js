(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep3Delegate', {
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
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		checkWorkflowComboSelected: function() {
			if (this.getValueWorkflowCombo())
				return true;

			return false;
		},

		getWorkflowDelegate: function() {
			return this.view.workflowForm.delegate;
		},

		getValueAttributeGrid: function() {
			return this.getWorkflowDelegate().getValueGrid();
		},

		getValueWorkflowCombo: function() {
			return this.getWorkflowDelegate().getValueCombo();
		},

		setDisabledAttributesGrid: function(state) {
			this.getWorkflowDelegate().setDisabledAttributesGrid(state);
		},

		setValueAttributesGrid: function(data) {
			this.getWorkflowDelegate().setValueGrid(data);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep3', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'event_synchronous',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep3Delegate', this);

			// SendMail configuration
				this.emailTemplateCombo = Ext.create('Ext.form.field.ComboBox', {
					name: CMDBuild.ServiceProxy.parameter.EMAIL_TEMPLATE,
					fieldLabel: tr.template,
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
					title: tr.sendMail,
					checkboxToggle: true,
					collapsed: true,
					layout: {
						type: 'vbox'
					},
					items: [this.emailTemplateCombo]
				});
			// END: SendMail configuration

			// Workflow configuration
				this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
					combo: {
						name: CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME
					}
				});

				this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
					title: tr.startWorkflow,
					checkboxToggle: true,
					collapsed: true,

					layout: {
						type: 'vbox',
						align: 'stretch'
					},

					items: [this.workflowForm]
				});
			// END: Workflow configuration

			Ext.apply(this, {
				items: [
					this.sendMailFieldset,
					this.workflowFieldset
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable attribute table to correct malfunction that enables on class select
			 */
			show: function(view, eOpts) {
				if (!this.delegate.checkWorkflowComboSelected())
					this.delegate.setDisabledAttributesGrid(true);
			}
		}
	});

})();