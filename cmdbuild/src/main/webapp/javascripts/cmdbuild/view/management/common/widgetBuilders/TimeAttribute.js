/**
 * @class CMDBuild.WidgetBuilders.TimeAttribute
 * @extends CMDBuild.WidgetBuilders.StringAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.TimeAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.TimeAttribute, CMDBuild.WidgetBuilders.DateAttribute);
CMDBuild.WidgetBuilders.TimeAttribute.prototype.format = "H:i:s";
/**
 * @override
 * @param attribute
 * @return
 */
CMDBuild.WidgetBuilders.TimeAttribute.prototype.buildAttributeField = function(attribute) {
	return new Ext.form.TextField({
		labelWidth: CMDBuild.LABEL_WIDTH,
		labelAlign: "right",
		fieldLabel: attribute.description || attribute.name,
		name: attribute.name,
		allowBlank: !attribute.isnotnull,
		format: this.format,
		vtype: "time",
		width: CMDBuild.SMALL_FIELD_WIDTH,
		CMAttribute: attribute
	});	
};