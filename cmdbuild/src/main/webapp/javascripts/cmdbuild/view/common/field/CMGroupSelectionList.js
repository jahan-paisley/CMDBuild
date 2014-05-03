(function() {

	Ext.define('CMDBuild.view.common.field.CMGroupSelectionList', {
		extend: 'Ext.ux.form.MultiSelect',

		fieldLabel: CMDBuild.Translation.administration.modreport.importJRFormStep1.enabled_groups,
		name: CMDBuild.ServiceProxy.parameter.GROUPS,
		dataFields: [
			CMDBuild.ServiceProxy.parameter.NAME,
			CMDBuild.ServiceProxy.parameter.ID,
			CMDBuild.ServiceProxy.parameter.DESCRIPTION
		],
		valueField: CMDBuild.ServiceProxy.parameter.ID,
		displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
		allowBlank: true,

		initComponent: function() {
			if (!this.store) {
				if (
					_CMCache
					&& typeof _CMCache.getActiveGroupsStore == 'function'
				) {
					this.store = _CMCache.getActiveGroupsStore();
				} else {
					this.store = Ext.create('Ext.data.Store', {
						fields: ['fake'],
						data: []
					});
				}
			}

			this.callParent(arguments);
		},

		// The origianl multiselect set the field as readonly if disabled.
		// We don't want this behabiour.
		updateReadOnly: Ext.emptyFn,

		reset: function() {
			this.setValue([]);
		},

		selectAll: function() {
			var arrayGroups = [];

			this.store.data.each(function(item, index, totalItems) {
				arrayGroups.push(item.data.name);
			});

			this.setValue(arrayGroups);
		}
	});

})();