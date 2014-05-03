/**
 * @class CMDBuild.WidgetBuilders.IPAddressAttribute
 * @extends CMDBuild.WidgetBuilders.DecimalAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.IPAddressAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.IPAddressAttribute, CMDBuild.WidgetBuilders.DecimalAttribute);
CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.MAXWIDTH = CMDBuild.MEDIUM_FIELD_ONLY_WIDTH;
CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.customVType = "ipv4";
CMDBuild.WidgetBuilders.IPAddressAttribute.prototype.gridRenderer = function(v) {
	return "<div>" + v + "<div>";
};