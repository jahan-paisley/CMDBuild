/**
 * 
 *  the configuration must have the fields
 *  
 *  maxValue: 
 *  minValue:
 * 
 * **/

CronFieldValidator = function(config) {
	this.maxValue = config.maxValue;
	this.minValue = config.minValue;
};

CronFieldValidator.prototype = {
		
	validate: function(value) {
		var splittedValues = value.split(',');
		var msg;
		for (var i = 0; i<splittedValues.length; i++) {
			msg = this.checkRange(value);
			if (msg !== true) {
				return msg;
			}
		}
		return msg;
	},

	checkRange: function(value) {
		var msg = this.isNumeric(value);
		if (msg === true) {
			if (value < this.minValue) {
				msg = "valore minimo " + this.minValue; 
			} else if (value > this.maxValue) {
				msg = "valore massimo " + this.maxValue;
			}
		}
		return msg;
	},
	
	isNumeric: function(value) {
		var numericRegExp = (/^[0-9]+$/);
		var msg = numericRegExp.test(value);
		if (!msg) {
			return msg = value + " valore numerico non valido";
		}
		return true;
	}
};