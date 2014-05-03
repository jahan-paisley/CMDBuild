(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4Delegate', {
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
			if (this.getWorkflowDelegate().getValueCombo())
				return true;

			return false;
		},

		getWorkflowDelegate: function() {
			return this.view.workflowForm.delegate;
		},

		getValueAttributeGrid: function() {
			return this.getWorkflowDelegate().getValueGrid();
		},

		getValueWorkflowFieldsetCheckbox: function() {
			return this.view.workflowFieldset.checkboxCmp.getValue();
		},

		setDisabledAttributesGrid: function(state) {
			this.getWorkflowDelegate().setDisabledAttributesGrid(state);
		},

		setValueWorkflowAttributesGrid: function(data) {
			this.getWorkflowDelegate().setValueGrid(data);
		},

		setValueWorkflowCombo: function(value) {
			this.getWorkflowDelegate().setValueCombo(value);
		},

		setValueWorkflowFieldsetCheckbox: function(value) {
			if (value) {
				this.view.workflowFieldset.expand();
			} else {
				this.view.workflowFieldset.collapse();
			}
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.email.CMStep4', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'email',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.email.CMStep4Delegate', this);

			this.workflowForm = Ext.create('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowForm', {
				combo: {
					name: CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME
				}
			});

			this.workflowFieldset = Ext.create('Ext.form.FieldSet', {
				title: tr.startWorkflow,
				checkboxToggle: true,
				checkboxName: CMDBuild.ServiceProxy.parameter.WORKFLOW_ACTIVE,
				collapsed: true,

				layout: {
					type: 'vbox',
					align: 'stretch'
				},

				items: [this.workflowForm]
			});

			Ext.apply(this, {
				items: [this.workflowFieldset]
			});

			this.callParent(arguments);
		}
	});

})();