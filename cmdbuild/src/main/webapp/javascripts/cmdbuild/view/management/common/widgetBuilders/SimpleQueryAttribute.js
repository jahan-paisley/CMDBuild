/**
 * @class CMDBuild.WidgetBuilders.SimpleQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 **/
Ext.ns("CMDBuild.WidgetBuilders"); 
var translation = CMDBuild.Translation.management.findfilter;

CMDBuild.WidgetBuilders.SimpleQueryAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.SimpleQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);
/**
 * @override
 */
CMDBuild.WidgetBuilders.SimpleQueryAttribute.prototype.getQueryOptions = function() {
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	return [
		[operator.EQUAL, translation.equals],
		[operator.NULL, translation.nullo],
		[operator.NOT_NULL, translation.notnull]
	];
};
/**
 * @override
 */
CMDBuild.WidgetBuilders.SimpleQueryAttribute.prototype.needsDoubleFielForQuery = function() {
	return false;
};