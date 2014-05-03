/**
 * @class CMDBuild.WidgetBuilders.ForeignKeyAttribute
 * @extends CMDBuild.WidgetBuilders.ComboAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.ForeignKeyAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.ForeignKeyAttribute, CMDBuild.WidgetBuilders.ComboAttribute);

/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.ForeignKeyCombo
 */
CMDBuild.WidgetBuilders.ForeignKeyAttribute.prototype.buildAttributeField = function(attribute) {
	return CMDBuild.Management.ForeignKeyField.build(attribute);
};