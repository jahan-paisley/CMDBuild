(function() {
Ext.override(Ext.form.FormPanel, {
	getInvalidFieldsAsHTML: function() {
		var BEGIN_LIST = "<ul>";
		var END_LIST = "</ul>";
		var out = "";
		this.cascade(function(item) {
			if (item && (item instanceof Ext.form.Field)) {
				if (!item.isValid()) {
					out += "<li>" + item.fieldLabel + "</li>";
				}
			}
		});
		if (out == "") {
			return null;
		} else { 
			return BEGIN_LIST + out + END_LIST;
		}
	},	
	
	isReadOnly: function(field) {
		if (field) {
			return field.initialConfig.CMDBuildReadonly;
		} else {
			return false;
		}
	},

	setFieldsEnabled: function(enableAll) {
		if (!this.MODEL_STRUCTURE) {
			return setFieldsEnabledForLegacyCode.call(this, enableAll);
		}

		var s = this.MODEL_STRUCTURE;
		this.cascade(function(item) {
			if (item && (item instanceof Ext.form.Field)) {
				var name = item._name || item.name; // for compatibility I can not change the name of old attrs
				var toBeEnabled = enableAll || !s[name].immutable;
				if (toBeEnabled) {
					item.enable();
				}
			}
		});

	},

	setFieldsDisabled: function(){
		if (!this.MODEL_STRUCTURE) {
			setFieldsDisabledForLegacyCode.call(this);
		} else {
			var s = this.MODEL_STRUCTURE;
			this.cascade(function(item) {
				if (item && (item instanceof Ext.form.Field) && item.disable) {
					item.disable();
				}
			});
		}
	},

	enableAllField: function() {
		var fields = this.getForm().items.items;
		this.formIsDisable = false;
		for (var i = 0 ; i < fields.length ; i++) {
			fields[i].enable();
		}
	},
	
	disableAllField: function() {
		var fields = this.getForm().items.items;
		this.formIsDisable = true;
		for (var i = 0 ; i < fields.length ; i++) {
			fields[i].disable();
		}
	},
	
	forEachField: function(fn) {
		var fields = this.getForm().items;
		for (var i = 0 ; i < fields.length ; i++) {
			var field = fields[i];
			fn(field);
		} 
	}
}); 

Ext.override(Ext.form.field.HtmlEditor, {
	/*
	 * Override this method because
	 * Ext 4.2.0 has introduced a
	 * bug. Since this functionality
	 * is not necessary, and I have
	 * no time to investigate, simply
	 * disable this method.
	 */
	// override
	initDefaultFont: function() {
		
	}
});

Ext.override(Ext.picker.Date, {

	/*
	 * Override this methods
	 * to be able to handle also the
	 * time.
	 */
	selectToday : function() {
		var me = this, btn = me.todayBtn, handler = me.handler;

		if (btn && !btn.disabled) {
			/**********/
			// me.setValue(Ext.Date.clearTime(new Date()));
			me.setValue(new Date());
			/*********/

			me.fireEvent('select', me, me.value);
			if (handler) {
				handler.call(me.scope || me, me, me.value);
			}
			me.onSelect();
		}
		return me;
	},

	setValue: function(value){
		/**********/
		// this.value = Ext.Date.clearTime(value, true);
		this.value = value;
		/**********/

		return this.update(this.value);
	}

});

Ext.override(Ext.form.Hidden, {
	validateValue: function(value) {
		if (this.allowBlank === false) {
			return (value.length > 0);
		}
		return true;
	}
});

Ext.override( Ext.form.FieldSet, {
	syncSize: function() {
		Ext.form.FieldSet.superclass.syncSize.call(this);
		var items = this.items.items;
		for (var i=0; i<items.length; i++) {
			var item = items[i];
			if (item && item.syncSize) {
				item.syncSize();
			}
		}
	}
});

function setFieldsEnabledForLegacyCode(enableAll) {
	this.cascade(function(item) {
	if (item && (item instanceof Ext.form.Field)
			&& item.isVisible() 
			&& (enableAll || !(item.initialConfig.CMDBuildReadonly)))
		item.enable();
	});
	if (this.buttons) {
		for(var i=0; i<this.buttons.length; i++ ){
			if (this.buttons[i]) {
					this.buttons[i].enable();
			}
		}
	}
}

function setFieldsDisabledForLegacyCode() {
	this.cascade(function(i) {
		if (i && (i instanceof Ext.form.Field) && !(i instanceof Ext.form.DisplayField)){
			var xtype = i.getXType();
			if (xtype!='hidden') {
				i.disable();
			}
		}
	});
	if (this.buttons) {
		for(var i=0; i<this.buttons.length; i++ ){
			if (this.buttons[i]) {
				this.buttons[i].disable();
			}
		}
	}
}

})();