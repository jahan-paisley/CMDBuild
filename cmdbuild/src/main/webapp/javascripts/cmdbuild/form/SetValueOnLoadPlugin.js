/**
 * Resets the combobox value after the store is loaded
 */

Ext.define("CMDBuild.SetValueOnLoadPlugin", {
	extend: "Ext.util.Observable",
	init: function(field) {
		field.valueNotFoundText = "";//CMDBuild.Translation.common.loading;
		field.store.on('load', function() {
			this.valueNotFoundText = this.initialConfig.valueNotFoundText;
			if (this.store) {
				//the store is null if the field is not rendered
				this.setValue(this.getValue());
			}
		}, field);
	}
});
