Ext.ns('CMDBuild');

/**
 * Validate the field of the cron configuration, 
 * it must be initialize with a configuration object with the form:
 * 		{
 * 			minValue: ...,
			maxValue: ...
 * 		}
 */

CMDBuild.CronFieldValidationPlugin = function(config) {
    this.validator = new CronFieldValidator(config);
};

Ext.extend(CMDBuild.CronFieldValidationPlugin, Ext.util.Observable, {
    init: function(field) {
		field.validator = this.validator.validate.createDelegate(this.validator);
    }
});
