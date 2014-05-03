 Ext.define("CMDBuild.field.LocaleSearchField", {
	extend: "CMDBuild.field.GridSearchField",

	onTrigger1Click : function() {
		var query = this.getRawValue().toUpperCase();

		this.grid.getStore().filterBy(function(record, id) {
			for ( var attr in record.data) {
				var attribute = (record.data[attr] + "").toUpperCase();
				var searchIndex = attribute.search(query);
				if (searchIndex != -1) {
					return true
				}
			}

			return false;
		});
	}
});