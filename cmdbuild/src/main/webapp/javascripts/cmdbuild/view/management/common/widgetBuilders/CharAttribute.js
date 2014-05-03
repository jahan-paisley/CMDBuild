/**
 * @class CMDBuild.WidgetBuilders.CharAttribute
 * @extends CMDBuild.WidgetBuilders.StringAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.CharAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.CharAttribute, CMDBuild.WidgetBuilders.StringAttribute);
/**
 * @override
 * @param attribute
 * @return Ext.form.TextField
 */
CMDBuild.WidgetBuilders.CharAttribute.prototype.buildAttributeField = function(attribute) {
	var attr = Ext.apply({},attribute);
	attr.len = 1;
	return CMDBuild.WidgetBuilders.CharAttribute.superclass.buildAttributeField(attr);
};