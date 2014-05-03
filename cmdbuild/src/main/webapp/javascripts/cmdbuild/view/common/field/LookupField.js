(function() {
Ext.define("CMDBuild.field.LookupCombo", {
	extend: "CMDBuild.field.ErasableCombo",
	plugins: new CMDBuild.SetValueOnLoadPlugin(),
	parentId: '',

	onTrigger2Click: function() {
		if (!this.disabled) {
			this.focus(); // to fire the change event in the single lookup fields
			this.chainedClear();
		}
	},

	chainedClear: function() {
		this.setValue([""]); // if use clearValue the form does not send the value, so it is not possible delete the value on server side
		if (this.childField) {
			this.childField.setParentIdAndFilterStore(undefined);
			this.childField.chainedClear();
		}
	},

	setValueAndUpdateParents: function(value) {
		if (!this.store) {
			// TODO why sometimes there are lookups without store?
			_debug("Lookup without store");
			return;
		}

		if (value == '' || typeof value == 'undefined') {
			this.clearValue();
			this.setParentIdAndFilterStore(undefined);
		} else {
			this.store.clearFilter();
			this.setValue(value);
			var index = this.store.find("Id", value);
			var rec = this.store.getAt(index);
			if (rec) {
				this.setParentIdAndFilterStore(rec.get("ParentId"));
			} else {
				this.setParentIdAndFilterStore(undefined);
			}
		}

		if (this.parentField) {
			this.parentField.setValueAndUpdateParents(this.parentId);
		}
	},

	setParentIdAndFilterStore: function(newParentId) {
		if (this.parentId != newParentId) {
			this.parentId = newParentId;
			this.filterStoreByParentId();
		}
	},

	filterStoreByParentId: function() {
		var parentId = this.parentId || '';
		this.store.filterBy(function(record, id) {
			return record.get("ParentId") == parentId;
		});
	}
});

Ext.define("CMDBuild.field.MultiLevelLookupPanel", {
	extend: "Ext.panel.Panel",
	hiddenField: undefined,
	initComponent: function() {
		if (!this.hiddenField) {
			throw "Error in MultiLevelLookup, hiddenField or CMAttribute is missing";
		}

		this.items = this.items || [];
		this.items.push(this.hiddenField);

		this.callParent(arguments);
	},

	border: false,
	frame: false,
	autoHeight: true,
	hideMode: 'offsets',
	labelWidth: CMDBuild.LABEL_WIDTH,
	bodyCls: "x-panel-default-framed",
	isMultiLevel: true,
	bodyStyle: {
		padding: "0"
	},
	setValue: function(v) {
		this.hiddenField.setValue(v);
	},
	getValue: function() {
		return this.hiddenField.getValue();
	},
	isValid: function() {
		return this.hiddenField.isValid();
	},
	getRawValue: function() {
		var out = "";
		this.items.each(function(subField, index) {
			if (subField !== this.hiddenField) {
				if (index > 0) {
					out += " - ";
				}
				out += subField.getRawValue();
			}
		});

		return out;
	}
});

Ext.define("CMDBuild.Management.LookupCombo", {
	statics: {
		build: function(attribute) {
			var field;
			if (attribute.lookupchain.length == 0) {
				field = buildSingleLookupField(attribute);
				field.isMultiLevel = false;
			}
			else if (attribute.lookupchain.length == 1) {
				field = buildSingleLookupField(attribute);
				field.isMultiLevel = false;
			} else {
				var hiddenField = buildHiddenField(attribute);
				field = new CMDBuild.field.MultiLevelLookupPanel({
					items: buildFieldSetItems(attribute, hiddenField),
					hiddenField: hiddenField,
					name: attribute.name
				});
			}

			return field;
		}
	}
});


//private
function canBeBlank(attribute) {
	// FIXME This should not be duplicated
	return !((attribute.fieldmode == "required") || attribute.isnotnull);
};

Ext.define("CMDBuild.Management.LookupHiddenField", {
	extend: "Ext.form.Hidden",

	updateParentsIfLoaded: function() {
		var value = this.getValue();

		if (value) {
			value = parseInt(value);
		}

		if (this.lastCombo) {
			this.lastCombo.setValueAndUpdateParents(value);
		}
	},

	filterByParentId: function(lastComboId) {
		this.setValueAndFireChange(lastComboId);
	},

	chainedClear: function() {
		this.setValueAndFireChange("");
	},

	// The hidden field is never given the focus, so it never fires the change and blur events,
	// thus not updating filtered references depending on it
	setValueAndFireChange: function(newValue) {
		var oldValue = this.getValue() || "";
		if (oldValue != newValue) {
			this.setValue(newValue, true);
			this.fireEvent('change', this, newValue, oldValue);
			this.fireEvent('blur', this);
		}
	},

	setValue: function(value, dontUpdateParents) {
		if (value != null 
				&& typeof value == "object") {

			value = value.id;
		}

		this.callParent([value]);

		if (!dontUpdateParents) {
			this.updateParentsIfLoaded();
		}
	},

	setParentIdAndFilterStore: Ext.emptyFn
});

// private
function buildHiddenField(attribute) {
	return new CMDBuild.Management.LookupHiddenField({
		CMAttribute: attribute,
		name: attribute.name,
		allowBlank: canBeBlank(attribute),
		value: null
	});
};

//private
var buildFieldSetItems = function(attribute, hiddenField) {
	var lookupChain = attribute.lookupchain,
		fieldSetItems = [],
		parentField = null,
		i, len, currentField;

	for (i=0, len=lookupChain.length; i<len; ++i) {
		var currentLookupType = lookupChain[len-i-1], // reverse order
			forgedAttribute = forgeAttributeForMultilevelLookup(attribute, currentLookupType),
			hideLabel = (i != 0);

		currentField = buildSingleLookupField(forgedAttribute, hideLabel);
		currentField.submitValue = false; // to have only the hidden field value on form submit

		if (parentField) {
			currentField.parentField = parentField;
			parentField.childField = currentField;
		}

		fieldSetItems.push(currentField);

		addEventsToMultilevelLookupCombo(currentField, parentField);
		parentField = currentField;
	}

	bindHiddenFieldToLastCombo(hiddenField, currentField);

	for (i=0, len=fieldSetItems.length; i<len; ++i) {
		var store = currentField.store;
		currentField = fieldSetItems[i];
		store.mon(store, 'load', function() {
			hiddenField.updateParentsIfLoaded();
		});
	}

	return fieldSetItems;
};

//private
var bindHiddenFieldToLastCombo = function(hiddenField, lastCombo) {
	hiddenField.lastCombo = lastCombo;
	lastCombo.childField = hiddenField;
	
	hiddenField.getReadableValue = function() {
		return lastCombo.getRawValue();
	};
};

//private
var buildSingleLookupField = function(attribute, hideLabel) {
	var store = _CMCache.getLookupStore(attribute.lookup);
	var fieldLabel;
	var labelSeparator;
	var padding;

	if (hideLabel) {
		fieldLabel = "";
		labelSeparator = "";
		padding = "0 0 0 " + (CMDBuild.LABEL_WIDTH + 5);
	} else {
		fieldLabel = attribute.description || attribute.name;
		if (!canBeBlank(attribute)) {
			fieldLabel = "* "+fieldLabel;
		}
		labelSeparator = ":";
	}

	var field = new CMDBuild.field.LookupCombo({
		labelAlign: "right",
		labelWidth: CMDBuild.LABEL_WIDTH,
		fieldLabel: fieldLabel,
		labelSeparator: labelSeparator,
		padding: padding,
		name: attribute.name,
		hiddenName: attribute.name,
		store: store,
		queryMode: 'local',
		triggerAction: "all",
		lazyInit: false,
		valueField: 'Id',
		displayField: 'Description',
		allowBlank: canBeBlank(attribute),
		minChars: 1,
		CMAttribute: attribute
	});

	field.filterByParentId = function(parentId) {
		var autoselectedId;
		this.setParentIdAndFilterStore(parentId);
		if (this.store.getCount() == 1) {
			var rec = this.store.getAt(0);
			autoselectedId = rec.get("Id");
			this.setValue(autoselectedId);
		} else {
			this.clearValue();
		}
		
		if (this.childField) {
			this.childField.filterByParentId(autoselectedId);
		}
	};
	
	field.on('select', function(combo, record, index) {
		if (this.childField) {
			this.childField.filterByParentId(record[0].get("Id"));
		}
	}, field);
	
	return field;
};

//private
var forgeAttributeForMultilevelLookup = function(attribute, lookupName) {
	var forgedAttribute = {
		lookup: lookupName,
		isnotnull: attribute.isnotnull,
		fieldmode: attribute.fieldmode,
		description: attribute.description,
		name: attribute.name + "_" + lookupName
	};
	
	return forgedAttribute;
};

//private
var addEventsToMultilevelLookupCombo = function(currentField, parentField) {
	if (parentField) {
		//HACK ComboBox.doQuery calls store.clearFilter()
		currentField.on('beforequery', function(qe) {
			qe.combo.lastQuery = qe.query; // to deny clearing the filter
		});
	}
};

})();
