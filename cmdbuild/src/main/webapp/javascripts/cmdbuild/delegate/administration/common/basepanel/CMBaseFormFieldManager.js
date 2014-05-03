(function() {
	var NAME = _CMProxy.parameter.NAME;
	var DESCRIPTION = _CMProxy.parameter.DESCRIPTION;

	Ext.define("CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager", {
		extend: "CMDBuild.delegate.administration.common.basepanel.CMFormFiledsManager",

		/**
		 * @return {array} an array of Ext.component to use as form items
		 */
		// override
		build: function() {
			this.name = new Ext.form.TextField({
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: NAME,
				allowBlank: false,
				vtype: "alphanum",
				cmImmutable: true
			});

			this.description= new Ext.form.CMTranslatableText({
				fieldLabel : CMDBuild.Translation.administration.modClass.attributeProperties.description,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: DESCRIPTION,
				allowBlank : false,
				vtype : "cmdbcomment"
			});

			return [this.name, this.description];
		},

		/**
		 * 
		 * @param {Ext.data.Model} record
		 * the record to use to fill the field values
		 */
		// override
		loadRecord: function(record) {
			this.reset();
			this.name.setValue(record.get(NAME));
			this.description.setValue(record.get(DESCRIPTION));
		},

		/**
		 * @return {object} values
		 * a key/value map with the values of the fields
		 */
		// override
		getValues: function() {
			var values = {};
			values[NAME] = this.name.getValue();
			values[DESCRIPTION] = this.description.getValue();

			return values;
		},

		/**
		 * clean up all the fields
		 */
		// override
		reset: function() {
			this.name.reset();
			this.description.reset();
		}
	});
})();