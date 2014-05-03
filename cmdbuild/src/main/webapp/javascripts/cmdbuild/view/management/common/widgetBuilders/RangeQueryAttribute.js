/**
 * @class CMDBuild.WidgetBuilders.RangeQueryAttribute
 * @extends CMDBuild.WidgetBuilders.BaseAttribute
 * */
var translation = CMDBuild.Translation.management.findfilter;
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.RangeQueryAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.RangeQueryAttribute, CMDBuild.WidgetBuilders.BaseAttribute);

/**
 * @override
 */
CMDBuild.WidgetBuilders.RangeQueryAttribute.prototype.getQueryOptions = function() {
	var operator = CMDBuild.WidgetBuilders.BaseAttribute.FilterOperator;
	return [
		[operator.EQUAL, translation.equals],
		[operator.NULL, translation.nullo],
		[operator.NOT_NULL, translation.notnull],
		[operator.NOT_EQUAL, translation.different],
		[operator.GREATER_THAN, translation.major],
		[operator.LESS_THAN, translation.minor],
		[operator.BETWEEN, translation.between]
	];
};

/**
 * @override
 */
CMDBuild.WidgetBuilders.RangeQueryAttribute.prototype.buildFieldsetForFilter = function(field, query, originalFieldName) {
	var field2 = field.cloneConfig();
	field2.hideLabel = true;
	field2.disable();

	return this.genericBuildFieldsetForFilter([field, field2], query, originalFieldName);
};