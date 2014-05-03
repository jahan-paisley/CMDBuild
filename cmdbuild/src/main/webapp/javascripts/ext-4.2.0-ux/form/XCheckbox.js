/*
 * CMDBuild ux
 */
Ext.define("Ext.ux.form.XCheckbox", {
	extend: "Ext.form.field.Checkbox",
	alias: "widget.xcheckbox",
	constructor: function() {
		this.uncheckedValue = "false";
		this.inputValue = "true";
		
		this.callParent(arguments);
	}
});