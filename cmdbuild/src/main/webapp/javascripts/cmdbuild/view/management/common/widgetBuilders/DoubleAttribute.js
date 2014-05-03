/**
 * @class CMDBuild.WidgetBuilders.DoubleAttribute
 * @extends CMDBuild.WidgetBuilders.DecimalAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.DoubleAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.DoubleAttribute, CMDBuild.WidgetBuilders.DecimalAttribute)
/**
 * @override
 * @param attribute
 * @return Ext.form.TextField
 */
CMDBuild.WidgetBuilders.DoubleAttribute.prototype.buildAttributeField = function(attribute) {
	var field = CMDBuild.WidgetBuilders.DoubleAttribute.superclass.buildAttributeField(attribute);
	field.scale = undefined;
	field.precision = undefined;
	
	return field;
};