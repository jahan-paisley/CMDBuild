/**
 * @class CMDBuild.WidgetBuilders.BooleanAttribute
 * @extends CMDBuild.WidgetBuilders.SimpleQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders");
CMDBuild.WidgetBuilders.BooleanAttribute = function(){};
CMDBuild.extend(CMDBuild.WidgetBuilders.BooleanAttribute, CMDBuild.WidgetBuilders.SimpleQueryAttribute);
/**
 * @override
 * @return Ext.grid.CheckColumn
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildGridHeader = function(attribute) {
	var headerWidth =  attribute.name.length * 9;

	var h = new Ext.ux.CheckColumn({
		header : attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden : !attribute.isbasedsp,
		width : headerWidth,
		cmReadOnly: true
	});
	return h;
};
/**
 * @override
 * @return Ext.form.BooleanDisplayField
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildReadOnlyField = function(attribute) {
	return new Ext.form.BooleanDisplayField ({
		labelAlign: "right",
		labelWidth: CMDBuild.LABEL_WIDTH,
		fieldLabel: attribute.description,
		name: attribute.name,
		disabled: false
	});
};
/**
 * @override
 * @return Ext.ux.form.XCheckbox
 */
CMDBuild.WidgetBuilders.BooleanAttribute.prototype.buildAttributeField = function(attribute) {
	return new Ext.ux.form.XCheckbox({
		labelAlign: "right",
		fieldLabel: attribute.description || attribute.name,
		labelWidth: CMDBuild.LABEL_WIDTH,
		name: attribute.name,
		CMAttribute: attribute
	});
};