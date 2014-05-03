(function() {
	var TRIGGER_LENGTH = 20;
	var PADDING = 20;

Ext.define("CMDBuild.field.CMBaseCombo", {
	alias: "cmbasecombo",
	extend: "Ext.form.field.ComboBox",

	cmGreatestItem: "",

	initComponent : function() {
		this.callParent(arguments);

		this.mon(this.store, 'load', function(store, records, successful, operation) {
			if (operation && operation.add) {
				this._growSizeFix(records || []);
			} else {
				this._growSizeFix();
			}
		}, this);

		this.mon(this.store, 'add', function(store, records) {
			this._growSizeFix(records || []);
		}, this);

		this.mon(this, "render", function() {
			this._growSizeFix();
		}, this);

		/*
		 * when _growSizeFix abort because the combo
		 * is not visible (this.isVisibile(deep=true)), 
		 * there are no way to know when it return to be 
		 * shown. So, use this event to eventually 
		 * adjust the size when the combo is actually used
		 */
		this.mon(this, "focus", function() {
			if (this._growSizeFixFail) {
				this._growSizeFix();
			}
		}, this);

		/*
		 * On focus out, clear the store filtering
		 */
		this.on("blur", function() {
			this.clearStoreFilteringInSafeMode();
		}, this);
	},

	_growSizeFix: function(added) {
		if (!this.isVisible(deep = true)) {
			this._growSizeFixFail = true;
			return;
		}

		// compare the size of the added records with the max already
		// in the store. If no added find the max size over all the records
		var data = added || this.store.getRange();

		for (var i=0,
				l=data.length,
				rec,
				value
				; i<l; ++i) {

			rec = data[i];
			value = rec.get(this.displayField);

			this.setGreatestItem(value);
		}

		this.setSizeLookingTheGreatestItem();
		this._growSizeFixFail = false;
	},

	setGreatestItem: function(item) {
		if (this.cmGreatestItem.length < item.length) {
			this.cmGreatestItem = item;
		}
	},

	// used by the template resolver to know if a field is a combo
	// and to take the value of multilevel lookup
	getReadableValue: function() {
		return this.getRawValue();
	},

	setSizeLookingTheGreatestItem: function() {
		if (this.cmGreatestItem && this.bodyEl) {
			var tm = new Ext.util.TextMetrics();
			var length = tm.getWidth(this.cmGreatestItem) + "px";

			this.bodyEl.dom.firstChild.style.width = length;
			this.bodyEl.dom.style.width = length;

			var fieldLength = tm.getWidth(this.cmGreatestItem);//this.bodyEl.dom.clientWidth;

			if (this.labelEl) {
				fieldLength += this.labelEl.dom.clientWidth;
			}

			var triggersLength = this.getTriggersLength();
			var widthToSet = fieldLength + triggersLength + PADDING;

			this.setWidth(widthToSet);
			tm.destroy();
		}
	},

	getTriggersLength: function() {
		try {
			return this.triggerEl.elements.length * TRIGGER_LENGTH;
		} catch (e) {
			return 0;
		}
	},

	// override
	setValue: function(v) {
		this.callParent([this.extractIdIfValueIsObject(v)]);
	},

	// private
	extractIdIfValueIsObject: function(v) {
		if (v != null 
				&& typeof v == "object" // the new serialization of reference and lookup
				&& !Ext.isArray(v) // is an array when select a value from the UI
				) {

			v = v.id;
		}

		return v;
	},

	// override
	onKeyUp: function(e, t) {
		if (e.isNavKeyPress()) {
			return this.callParent(arguments);
		}

		/*
		 * Ext does not clear the filter
		 * if delete the typed text
		 */
		var rawValue = this.getRawValue();
		if (rawValue == "") {
			return this.clearStoreFilteringInSafeMode();
		} else {
			return this.callParent(arguments);
		}
	},

	/*
	 * To reset the store filtering
	 * without break the filtering
	 * mechanisms. If you call
	 * store.clearFilters the store
	 * throws away the object that models
	 * the combo filtering.
	 */
	// protected
	clearStoreFilteringInSafeMode: function() {
		var store = this.getStore();
		this.queryFilter.setValue("");
		store.filter();
	}
});

})();