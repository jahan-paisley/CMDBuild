/**
 * @class CMDBuild.WidgetBuilders.DateAttribute
 * @extends CMDBuild.WidgetBuilders.RangeQueryAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.DateAttribute = function() {
	this.format = 'd/m/Y';
	this.fieldWidth = CMDBuild.SMALL_FIELD_ONLY_WIDTH;
	this.headerWidth = 60;
};
CMDBuild.extend(CMDBuild.WidgetBuilders.DateAttribute, CMDBuild.WidgetBuilders.RangeQueryAttribute);
/**
 * @override
 * @return object
 */
CMDBuild.WidgetBuilders.DateAttribute.prototype.buildGridHeader = function(attribute) {	
	return {
		header: attribute.description,
		sortable : true,
		dataIndex : attribute.name,
		hidden: !attribute.isbasedsp,
		flex: this.headerWidth,
		//TODO read the format in the config
		format: this.format
	};
};
/**
 * @override
 * @return Ext.form.DateField
 */
CMDBuild.WidgetBuilders.DateAttribute.prototype.buildAttributeField = function(attribute) {
	return new Ext.form.DateField({
		labelAlign: "right",
		labelWidth: CMDBuild.LABEL_WIDTH,
		fieldLabel: attribute.description || attribute.name,
		name: attribute.name,
		allowBlank: !attribute.isnotnull,
		format: this.format, //TODO read the format in the config
		width: CMDBuild.LABEL_WIDTH + this.fieldWidth,
		CMAttribute: attribute
	});
};