CMDBuild.Management.ForeignKeyField = (function() {
	return {
		build: function(attribute) {	
			var store = CMDBuild.Cache.getForeignKeyStore(attribute);
			
			var field = new CMDBuild.Management.SearchableCombo({
				plugins: new CMDBuild.SetValueOnLoadPlugin(),
				fieldLabel: attribute.description,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: attribute.name,
				store: store,
				queryMode: 'local',
				valueField: 'Id',
				displayField: 'Description',
				triggerAction: 'all',
				allowBlank: !attribute.isnotnull,
				grow: true, // XComboBox autogrow
				minChars: 1,
				filtered: false,
				CMAttribute: attribute
			});		
			
			return field;
		}
	};
})();