Ext.define("CMDBuild.delegate.administration.common.dataview.CMSqlDataViewFormFieldsManager", {
	extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager",

	/**
	 * @return {array} an array of Ext.component to use as form items
	 */
	build: function() {

		this.dataSource = new Ext.form.field.ComboBox({
			name: _CMProxy.parameter.SOURCE_FUNCTION,
			fieldLabel: CMDBuild.Translation.administration.modDashboard.charts.fields.dataSource,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			valueField: "name",
			displayField: "name",
			queryMode: "local",
			editable: false,
			allowBlank: false,
			store: _CMCache.getAvailableDataSourcesStore()
		});

		var fields = this.callParent(arguments);
		Ext.apply(this.description, {
			translationsKeyType: "SqlView", 
			translationsKeyField: "Description"
		});
		fields.push(this.dataSource);

		return fields;
	},

	/**
	 * 
	 * @param {Ext.data.Model} record
	 * the record to use to fill the field values
	 */
	loadRecord: function(record) {
		this.callParent(arguments);
		this.dataSource.setValue(record.get(_CMProxy.parameter.SOURCE_FUNCTION));
		Ext.apply(this.description, {
			translationsKeyName: record.get("name")
		});
	},

	/**
	 * @return {object} values
	 * a key/value map with the values of the fields
	 */
	// override
	getValues: function() {
		var values = this.callParent(arguments);
		values[_CMProxy.parameter.SOURCE_FUNCTION] = this.dataSource.getValue();
		return values;
	},

	/**
	 * clear the values of his fields
	 */
	reset: function() {
		this.callParent(arguments);
		this.dataSource.reset();
	}
});