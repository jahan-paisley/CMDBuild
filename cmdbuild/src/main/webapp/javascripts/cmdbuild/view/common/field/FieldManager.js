(function() {
	var attributesMap = {
		BOOLEAN: new CMDBuild.WidgetBuilders.BooleanAttribute(),
		DECIMAL: new CMDBuild.WidgetBuilders.DecimalAttribute(),
		INTEGER: new CMDBuild.WidgetBuilders.IntegerAttribute(),
		DOUBLE: new CMDBuild.WidgetBuilders.DoubleAttribute(),
		DATE: new CMDBuild.WidgetBuilders.DateAttribute(),
		TIMESTAMP: new CMDBuild.WidgetBuilders.DateTimeAttribute(),
		TIME: new CMDBuild.WidgetBuilders.TimeAttribute(),
		LOOKUP: new CMDBuild.WidgetBuilders.LookupAttribute(),
		REFERENCE: new CMDBuild.WidgetBuilders.ReferenceAttribute(),
		FOREIGNKEY: new CMDBuild.WidgetBuilders.ForeignKeyAttribute(),
		STRING: new CMDBuild.WidgetBuilders.StringAttribute(),
		TEXT: new CMDBuild.WidgetBuilders.TextAttribute(),
		CHAR: new CMDBuild.WidgetBuilders.CharAttribute(),
		INET: new CMDBuild.WidgetBuilders.IPAddressAttribute(),
		LIST: new CMDBuild.WidgetBuilders.CustomListAttribute()
	};

	function attributeTypeIsNotHandled(attribute) {
		return !attributesMap[attribute.type];
	}

	Ext.define("CMDBuild.Management.FieldManager", {
	statics: {

		loadAttributes: function(classId, callback) {
			CMDBuild.Cache.getAttributeList(classId, callback);		
		},

		getAttributesMap: function () {
			return attributesMap;
		}, 
		
		getFieldForAttr: function(attribute, readonly, skipSubAttributes) {
			if (attribute.fieldmode == "hidden" || attributeTypeIsNotHandled(attribute)) { 
				return undefined;
			}

			if (readonly || attribute.fieldmode == "read") {
				return attributesMap[attribute.type].buildReadOnlyField(attribute);
			} else {
				return attributesMap[attribute.type].buildField(attribute, hideLabel=undefined, skipSubAttributes);
			}
		},

		getHeaderForAttr: function(attribute) {
			if (attribute.fieldmode == "hidden" || attributeTypeIsNotHandled(attribute)) {
				return undefined;
			} else {
				return attributesMap[attribute.type].buildGridHeader(attribute);
			}
		},

		getFieldSetForFilter: function(attribute) {
			if (attributeTypeIsNotHandled(attribute)) {
				return undefined;
			}
			return attributesMap[attribute.type].getFieldSetForFilter(attribute);
		},

		getCellEditorForAttribute: function(attribute) {
			if (attributeTypeIsNotHandled(attribute)) {
				return undefined;
			}
			return attributesMap[attribute.type].buildCellEditor(attribute);
		}
	}
});

})();