Ext.define("CMDBuild.form.HexColorField", {
	extend: "Ext.form.ColorField",

	editable: false,

	setValue: function(value) {
		if (value && value[0] == "#") {
			value = value.slice(1);
		}
		CMDBuild.form.HexColorField.superclass.setValue.call(this, value);
	},

	getValue: function() {
		var value = this.value;
		if (value && value[0] != "#") {
			value = "#"+value;
		}
		return value;
	}
});