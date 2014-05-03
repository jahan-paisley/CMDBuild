/**
 * @class CMDBuild.WidgetBuilders.TimeStampAttribute
 * @extends CMDBuild.WidgetBuilders.DateAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.DateTimeAttribute = function() {
	this.format = 'd/m/Y H:i:s';
	this.fieldWidth = CMDBuild.MEDIUM_FIELD_ONLY_WIDTH;
	this.headerWidth = 100;
};
CMDBuild.extend(CMDBuild.WidgetBuilders.DateTimeAttribute, CMDBuild.WidgetBuilders.DateAttribute);