(function() {
	var VALUE_FIELD_NAME = "Id";

	/**
	 * @class CMDBuild.WidgetBuilders.CustomListAttribute
	 * @extends CMDBuild.WidgetBuilders.ComboAttribute
	 */
	Ext.ns("CMDBuild.WidgetBuilders"); 
	CMDBuild.WidgetBuilders.CustomListAttribute = function() {};
	CMDBuild.extend(CMDBuild.WidgetBuilders.CustomListAttribute, CMDBuild.WidgetBuilders.ComboAttribute);

	/**
	 * @override
	 * @param attribute
	 * @return Ext.form.field.ComboBox
	 */
	CMDBuild.WidgetBuilders.CustomListAttribute.prototype.buildAttributeField = function(attribute) {
		return new CMDBuild.field.ErasableCombo({
			labelAlign: "right",
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel: attribute.description || attribute.name,
			labelSeparator: ":",
			name: attribute.name,
			hiddenName: attribute.name,
			store: new Ext.data.Store({
				fields: [VALUE_FIELD_NAME],
				data: ataptAttributeValuesForStore(attribute.values)
			}),
			queryMode: 'local',
			triggerAction: "all",
			valueField: VALUE_FIELD_NAME,
			displayField: VALUE_FIELD_NAME,
			allowBlank: !attribute.isnotnull,
			CMAttribute: attribute
		});
	};

	function ataptAttributeValuesForStore(values) {
		var out = [];
		var data = values || [];

		for (var i=0; i<data.length; ++i) {
			var item = {};
			item[VALUE_FIELD_NAME] = data[i];
			out.push(item);
		}

		return out;
	}
})();
