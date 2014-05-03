/**
 * @class CMDBuild.WidgetBuilders.TextualQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 **/
var translation = CMDBuild.Translation.management.findfilter;
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.TextualQueryAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.TextualQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);
/**
 * @override
 */
CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.getQueryOptions = function() {
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	return [
		[operator.EQUAL, translation.equals],
		[operator.NOT_EQUAL, translation.different],
		[operator.CONTAIN, translation.like],
		[operator.NOT_CONTAIN, translation.dontlike],
		[operator.BEGIN, translation.begin],
		[operator.NOT_BEGIN, translation.dontbegin],
		[operator.END, translation.end],
		[operator.NOT_END, translation.dontend],
		[operator.NULL, translation.nullo]
	];
};

CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.getDefaultValueForQueryCombo = function() {
	return CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator.CONTAIN;
};

/**
 * @override
 */
CMDBuild.WidgetBuilders.TextualQueryAttribute.prototype.needsDoubleFielForQuery = function() {
	return false;
};