/**
 * @class CMDBuild.WidgetBuilders.DecimalAttribute
 * @extends CMDBuild.WidgetBuilders.RangeQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.DecimalAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.DecimalAttribute, CMDBuild.WidgetBuilders.RangeQueryAttribute);
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.MAXWIDTH = CMDBuild.SMALL_FIELD_ONLY_WIDTH;
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.customVType = "numeric";
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.gridRenderer = function(v) {
	return "<div class=\"numeric_column\">" + v + "<div>";
};
/**
 * @override
 * @param attribute
 * @return object
 */
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.buildGridHeader = function(attribute) {
	return {
		header: attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		flex: this.MAXWIDTH,
		renderer: this.gridRenderer
	};
};
/**
 * @override
 * @param attribute
 * @return Ext.form.TextField
 */
CMDBuild.WidgetBuilders.DecimalAttribute.prototype.buildAttributeField = function(attribute) {
	return new Ext.form.TextField({
		labelAlign: "right",
		labelWidth: CMDBuild.LABEL_WIDTH,
		fieldLabel: attribute.description || attribute.name,
		name: attribute.name,
		allowBlank: !attribute.isnotnull,
		width: CMDBuild.LABEL_WIDTH + this.MAXWIDTH,
		scale: attribute.scale,
		precision: attribute.precision,
		vtype: this.customVType,
		CMAttribute: attribute
	});
};