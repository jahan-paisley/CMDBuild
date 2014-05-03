(function() {

	Ext.define('CMDBuild.view.administration.workflow.CMProcessAttributes', {
		extend: 'CMDBuild.view.administration.classes.CMClassAttributesPanel',

		onClassSelected: function(idClass) {
			this.formPanel.onClassSelected(idClass);
			this.gridPanel.onClassSelected(idClass);
		},

		// override
		buildFormPanel: function() {
			return Ext.create('CMDBuild.view.administration.workflow.CMProcessAttributesForm', {
				region: 'center'
			});
		}
	});

})();