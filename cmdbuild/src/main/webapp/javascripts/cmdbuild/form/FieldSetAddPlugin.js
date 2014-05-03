Ext.ns('CMDBuild');

/**
 * Adds and removes fields even if they are nested in a FieldSet.
 * 
 * If the FieldSet has a "name" configuration value, only that field
 * will be add to the basic form.
 */

CMDBuild.FieldSetAddPlugin = function(config) {
    Ext.apply(this, config);
};

Ext.extend(CMDBuild.FieldSetAddPlugin, Ext.util.Observable, {
    init: function(formPanel) {
		formPanel.on('add', function(fp, field, index) {
			if (field.isFormField)
				return;
			var fieldName = field.name;
			var basicForm = fp.getForm();
	        var fn = function(c) {
	        	if (!c.isFormField)
	        		return;
	        	var currentFieldName = c.hiddenName ? c.hiddenName : c.name;
	            if(typeof fieldName == 'undefined' || fieldName == currentFieldName) {
	            	basicForm.add(c);
	            }
	        }
	        field.items.each(fn);
		});
		formPanel.on('remove', function(fp, field) {
			if (field.isFormField)
				return;
			var fieldName = field.name;
			var basicForm = fp.getForm();
	        var fn = function(c) {
	        	if (!c.isFormField)
	        		return;
	        	var currentFieldName = c.hiddenName ? c.hiddenName : c.name;
	            if(typeof fieldName == 'undefined' || fieldName == currentFieldName) {
	            	basicForm.remove(c);
	            }
	        }
	        field.items.each(fn);
		});
    }
});
