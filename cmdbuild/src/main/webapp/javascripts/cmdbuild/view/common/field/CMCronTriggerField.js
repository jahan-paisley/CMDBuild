(function() {

	Ext.define('CMDBuild.view.common.field.CMCronTriggerField', {
		extend: 'Ext.form.field.Trigger',

		fieldLabel: undefined,
		parentField: undefined,

		triggerCls: 'trigger-edit',

		onTriggerClick: function() {
			if (!this.disabled) {
				Ext.create('CMDBuild.view.common.field.CMCronTriggerFieldEditWindow', {
					title: this.fieldLabel,
					parentField: this
				}).show();
			}
		}
	});

})();