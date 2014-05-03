/**
 * @class CMDBuild.WidgetBuilders.IntegerAttribute
 * @extends CMDBuild.WidgetBuilders.DecimalAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.IntegerAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.IntegerAttribute, CMDBuild.WidgetBuilders.DecimalAttribute);
/**
 * @override
 * @param attribute
 * @return Ext.form.TextField
 */
CMDBuild.WidgetBuilders.IntegerAttribute.prototype.buildAttributeField = function(attribute) {
	var field = CMDBuild.WidgetBuilders.IntegerAttribute.superclass.buildAttributeField(attribute);
	field.scale = 0;
	field.precision = undefined;
	
	return field;
};