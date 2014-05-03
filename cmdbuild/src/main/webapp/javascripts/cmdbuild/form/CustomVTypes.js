var fullemailspec = /^[\w ]*\<([^\<\>]+)\>[ ]*$/;
var numericRegExp = /^(([+,\-]?[0-9]+)|[0-9]*)(\.[0-9]+)?$/;
var ipv4RegExp = /^0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])$/;
	
var numericValidation = function (value, scale, precision) {
	var out = {
		valid: true,
		message: ''
	};
	
	if (value.match(numericRegExp) == null) {
		out = {
			valid: false,
			message: CMDBuild.Translation.vtype_text.invalid_character
		};
	}
	var splitByDecimalSeparator = value.split(".");
	var integerPart = Math.abs(splitByDecimalSeparator[0]); 
	var decimalPart = splitByDecimalSeparator[1];
	
	if (precision !== undefined) {
		var integerPartMaxlength = precision - (scale || 0);
		if (integerPart && new String(integerPart).length > integerPartMaxlength) {
			out = {
				valid: false,
				message: Ext.String.format(CMDBuild.Translation.vtype_text.wrong_integer_part ,integerPartMaxlength)
			};
		}
	};
	
	if (scale !== undefined) {
		if (decimalPart && decimalPart.length > scale) {
			out = {
				valid: false,
				message: Ext.String.format(CMDBuild.Translation.vtype_text.wrong_decimal_part, scale)
			};
		}
	}
	
	return out;
};

Ext.apply(Ext.form.VTypes, {
    cmdbcomment : function(val, field) {
		return !val.match("[|']");
    },
	cmdbcommentText : CMDBuild.Translation.vtype_text.cmdbcomment || 'Pipe or apostrophe not allowed',

    cmdbcommentrelaxed : function(val, field) {
		return !val.match("[|]");
    },
	cmdbcommentrelaxedText :  CMDBuild.Translation.vtype_text.cmdbcommentrelaxedText || 'Pipe not allowed',

    emailaddrspec : function(v) {
   		var inner = v.match(fullemailspec);
   		if (inner) {
   			v = inner[1];
   		}
   		return Ext.form.VTypes.email(v);
    },
    emailaddrspecText : Ext.form.VTypes.emailText,

    emailaddrspeclist : function(v) {
    	var a = v.split(",");
    	for (var i=0,len=a.length; i<len; ++i) {
    		var sv = Ext.String.trim(a[i]);
    		if (sv && !Ext.form.VTypes.emailaddrspec(sv)) {
    			return false;
    		}
    	}
        return true;
    },
    emailaddrspeclistText : Ext.form.VTypes.emailText,
    
    emailOrBlank: function(v) {
    	return (v.length == 0 || this.email(v));
    },
    
    emailOrBlankText : Ext.form.VTypes.emailText,

    numeric: function(val, field) {
    	var valid = numericValidation(val, field.scale, field.precision);
    	field.vtypeText = valid.message;
    	
    	return valid.valid;
    },
    
    ipv4: function(value, field) {
    	return value.match(ipv4RegExp) != null;
    },
    ipv4Text: CMDBuild.Translation.vtype_text.wrong_ip_address,
    
    time: function(value, field) {
    	field.vtypeText = Ext.String.format(CMDBuild.Translation.vtype_text.wrong_time, value, field.format);
    	return Ext.Date.parse(value, field.format);
    },
    password : function(val, field) {
		if (field.initialPassField) {
			var pwd = Ext.getCmp(field.initialPassField);
			return (val == pwd.getValue());
		}
		return true;
	},
	passwordText : CMDBuild.Translation.configure.step2.msg.pswnomatch
});
