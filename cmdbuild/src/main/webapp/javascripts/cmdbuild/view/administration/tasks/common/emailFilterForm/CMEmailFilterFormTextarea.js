(function() {

	Ext.define('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormTextarea', {
		extend: 'Ext.form.field.TextArea',

		delegate: undefined,

		// Required
		name: undefined,
		id: undefined,

		readOnly: true,
		flex: 1,

		initComponent: function() {
			this.callParent(arguments);
		}
	});

})();