(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.workflowForm.CMWorkflowFormCombo', {
		extend: 'Ext.form.field.ComboBox',

		// Required
		delegate: undefined,
		name: undefined,

		valueField: CMDBuild.ServiceProxy.parameter.NAME,
		displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
		store: CMDBuild.core.proxy.CMProxyTasks.getWorkflowsStore(),
		width: (CMDBuild.CFG_BIG_FIELD_WIDTH - CMDBuild.LABEL_WIDTH - 5), // FIX: To solve a problem of width
		forceSelection: true,
		editable: false,

		listeners: {
			select: function() {
				this.delegate.cmOn('onSelectWorkflow', this.getValue());
			}
		},

		initComponent: function() {
			this.callParent(arguments);
		}
	});

})();