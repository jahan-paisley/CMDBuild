(function() {

	var MY_USER_ID = -1;
	var MY_USER_LABEL = "* " + CMDBuild.Translation.loggedUser + " *";
	var MY_USER_TEMPLATE = "@MY_USER";

	var MY_GROUP_ID = -1;
	var MY_GROUP_LABEL = "* " + CMDBuild.Translation.loggedGroup + " *";
	var MY_GROUP_TEMPLATE = "@MY_GROUP";

/**
 * @class CMDBuild.WidgetBuilders.ReferenceAttribute
 * @extends CMDBuild.WidgetBuilders.ComboAttribute
 */
Ext.ns("CMDBuild.WidgetBuilders"); 
CMDBuild.WidgetBuilders.ReferenceAttribute = function() {};
CMDBuild.extend(CMDBuild.WidgetBuilders.ReferenceAttribute, CMDBuild.WidgetBuilders.ComboAttribute);

/**
 * @override
 * @param attribute
 * @return Ext.form.FieldSet
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.getFieldSetForFilter = function(attribute) {
	var attributeCopy = Ext.apply({}, {
		fieldmode: "write", //change the field mode because in the filter must write on this field
		name: attribute.name,
		filter: false, // avoid the CQL filtering on the Search Window,
		oneTime: true
	}, attribute);

	var field = this.buildField(attributeCopy, hideLabel = true, skipSubAttributes = true);
	manageCalculatedValues(field, attribute);
	var conditionCombo = this.getQueryCombo(attributeCopy); 

	return this.buildFieldsetForFilter(field, conditionCombo, attributeCopy);
};

// Manage "calculated" values to filter by
// current User and current Group
function manageCalculatedValues(field, attribute) {
	var store = field.getStore();
	var model = null;
	var calculatedValueId = 0;

	if (attribute.referencedClassName == "User") {
		model = new CMDBuild.cache.CMReferenceStoreModel({Id: MY_USER_ID, Description: MY_USER_LABEL});
		calculatedValueId = MY_USER_ID;
	} else if (attribute.referencedClassName == "Role") {
		model = new CMDBuild.cache.CMReferenceStoreModel({Id: MY_GROUP_ID, Description: MY_GROUP_LABEL});
		calculatedValueId = MY_GROUP_ID;
	}

	if (model != null) {
		// Template for the dropdown menu.
		// Note the use of "x-boundlist-item" class,
		// this is required to make the items selectable.
		field.tpl = Ext.create('Ext.XTemplate',
			'<tpl for=".">',

				'<tpl if="Id != ' + calculatedValueId + '">',
					'<div class="x-boundlist-item">{Description}</div>',
				'</tpl>',

				'<tpl if="Id == ' + calculatedValueId + '">',
					'<div class="x-boundlist-item cm-calculated-field">{Description}</div>',
				'</tpl>',

			'</tpl>'
		);

		// Override the  ensureToHaveTheValueInStore
		// to deny a getCard() call with -1 as id that
		// throws a not found exception
		var originalEnsureToHaveTheValueInStore = field.ensureToHaveTheValueInStore;
		field.ensureToHaveTheValueInStore = function(value) {
			if (value == MY_USER_ID 
				|| value == MY_USER_ID) {

				return;
			}

			originalEnsureToHaveTheValueInStore.call(field, value);

		}

		// Override the setValue function to
		// change the template with the id of
		// the model (The model id must be a number)
		var originalSetValue = field.setValue;
		field.setValue = function(value) {
			if (value == MY_USER_TEMPLATE) {
				value = MY_USER_ID;
			} else if (value == MY_GROUP_TEMPLATE) {
				value = MY_GROUP_ID;
			}

			originalSetValue.call(field, value);
		}

		store.on("load", function(s, records) {
			this.sort("Description");
			this.insert(0, model);
			// Some ExtJS mechanism call the sorting
			// after the the ComboBox List expand() call
			// So, we have already sort the items to add
			// the new one as first, then override
			// the sort function to do nothing
			this.sort = function() { }

			field.setValue(field.getValue());
		}, store, {single: true});


		
	}
}

/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.ReferenceCombo
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildField = function(attribute, hideLabel, skipSubAttributes) {
	var field = this.buildAttributeField(attribute, skipSubAttributes);
	field.hideLabel = hideLabel;
	return this.markAsRequired(field, attribute);
};

/**
 * @override
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildCellEditor = function(attribute) {
	return CMDBuild.Management.FieldManager.getFieldForAttr(attribute, readOnly = false, skipSubFields = true);
};

/**
 * @override
 * @param attribute
 * @return CMDBuild.Management.ReferenceCombo
 */
CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildAttributeField = function(attribute, skipSubAttributes) {
	var subFields = [];
	if (!skipSubAttributes) {
		subFields = getSubFields(attribute, display = false);
	}

	return CMDBuild.Management.ReferenceField.build(attribute, subFields);
};

CMDBuild.WidgetBuilders.ReferenceAttribute.prototype.buildReadOnlyField = function(attribute) {
	var field = new Ext.form.DisplayField ({
		labelAlign: "right",
		labelWidth: CMDBuild.LABEL_WIDTH,
		width: CMDBuild.BIG_FIELD_WIDTH,
		fieldLabel: attribute.description || attribute.name,
		submitValue: false,
		name: attribute.name,
		disabled: false
	});

	var subFields = getSubFields(attribute, display = true);

	if (subFields.length > 0) {
		var fieldContainer = {
			xtype : 'container',
			layout : 'hbox',
			items : [
				new CMDBuild.field.CMToggleButtonToShowReferenceAttributes({
					subFields: subFields
				}),
				field
			]
		};

		field.labelWidth -= 20;

		return new Ext.container.Container({
			items: [fieldContainer].concat(subFields)
		});

	} else {
		return field;
	}
};

function getSubFields(attribute, display) {
	var d = _CMCache.getDomainById(attribute.idDomain),
		fields = [];

	if (d) {
		var attrs = d.data.attributes || [];
		for (var i=0, a=null; i<attrs.length; ++i) {
			a = attrs[i];
			if (a.isbasedsp) {
				var conf = Ext.apply({}, a);
				conf.name = "_" + attribute.name + "_" + conf.name;

				var f = CMDBuild.Management.FieldManager.getFieldForAttr(conf, display);
				f.margin = "0 0 0 5";
				if (f) {
					// Mark the sub fields with a flag "cmDomainAttribute" because
					// if a form has one of these fields can know to do a request to
					// populate it. This is needed because the values of the relations attributes
					// are not serialized in the grid data.
					// As an alternative, we can use an event to notify to the controller
					// that the form is ready (has all the fields), so the controller can
					// look for this kind of attributes and make the request if needed
					f.cmDomainAttribute = true;

					fields.push(f);
				}
			}
		}
	}

	return fields;
}

})();