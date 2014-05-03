Ext.ns('CMDBuild');

CMDBuild.FormPlugin = function(config) {
    Ext.apply(this, config);
};

Ext.extend(CMDBuild.FormPlugin, Ext.util.Observable, {
    init: function(formPanel) {
    	var basicForm = formPanel.getForm();
    	
    	/**
    	 * 
    	 * clears the form, 
    	 * to use when trackResetOnLoad = true; in these case the reset() function
    	 * set the form with the last loaded values. If you want to clear completely
    	 * the form call clearForm()
    	 * 
    	 */
		formPanel.clearForm = function() {
			var blankValues = {};
			basicForm.items.each(function(f){
	           blankValues[f.getName()]="";
	        });
			basicForm.setValues(blankValues);
		};
		
		/**
		 * 
		 * Keeps in sync two fields, usually name and description. If the
		 * master field changes and the slave is empty, or it has the same
		 * value as the old value of the master, its value is updated with
		 * the new one.
		 * 
		 * These function has to be used with the change listener,
		 * example: 
		 * 
		 * name.on('change', function(field, newValue, oldValue) {
		 * 		formPanel.autoComplete(fieldToComplete, newValue, oldValue)	
		 * })
		 * 
		 */
		formPanel.autoComplete = function(fieldToComplete, newValue, oldValue) {
			var actualValue = fieldToComplete.getValue();
			if ( actualValue == "" || actualValue == oldValue )
				fieldToComplete.setValue(newValue);
		};
    }
});