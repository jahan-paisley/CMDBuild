(function() {

	var FILTER = _CMProxy.parameter.FILTER;
	var SOURCE_CLASS_NAME = _CMProxy.parameter.SOURCE_CLASS_NAME;

	Ext.define("CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormFieldsManager", {
		extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
			"CMDBuild.delegate.administration.common.dataview.CMFilterDataViewFormDelegate");
			
			this.callParent(arguments);
		},

		/**
		 * @return {array} an array of Ext.component to use as form items
		 */
		build: function() {
			var me = this;

			var fields = this.callParent(arguments);

			Ext.apply(this.description, {
				translationsKeyType: "FilterView", 
				translationsKeyField: "Description"
			});
			this.classes = new CMDBuild.field.ErasableCombo({
				fieldLabel: CMDBuild.Translation.targetClass,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: SOURCE_CLASS_NAME,
				valueField: 'name',
				displayField: 'description',
				editable: false,
				store: _CMCache.getClassesAndProcessesAndDahboardsStore(),
				queryMode: 'local',
				listeners: {
					select: function(combo, records, options) {
						var className = null;
						if (Ext.isArray(records) 
								&& records.length > 0) {
							var record = records[0];
							className = record.get(me.classes.valueField);
						}

						me.callDelegates("onFilterDataViewFormBuilderClassSelected", [me, className]);
					}
				}
			});

			this.filterChooser = new CMDBuild.view.common.field.CMFilterChooser({
				fieldLabel: CMDBuild.Translation.filter,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: FILTER
			});

			fields.push(this.classes);
			fields.push(this.filterChooser);

			return fields;
		},

		setFilterChooserClassName: function(className) {
			this.filterChooser.setClassName(className);
		},

		/**
		 * 
		 * @param {Ext.data.Model} record
		 * the record to use to fill the field values
		 */
		// override
		loadRecord: function(record) {
			this.callParent(arguments);
			var filterConfiguration = Ext.decode(record.get(FILTER));
			var className = record.get(SOURCE_CLASS_NAME);

			this.filterChooser.setFilter(new CMDBuild.model.CMFilterModel({
				configuration: filterConfiguration,
				entryType: className
			}));

			Ext.apply(this.description, {
				translationsKeyName: record.get("name")
			});
			this.classes.setValue(className);
			// the set value programmatic does not fire the select
			// event, so call the delegates manually
			this.callDelegates("onFilterDataViewFormBuilderClassSelected", [this, className]);
		},

		/**
		 * @return {object} values
		 * a key/value map with the values of the fields
		 */
		// override
		getValues: function() {
			var values = this.callParent(arguments);

			values[SOURCE_CLASS_NAME] = this.classes.getValue();
			var filter = this.filterChooser.getFilter();
			if (filter) {
				values[FILTER] = Ext.encode(filter.getConfiguration());
			}

			return values;
		},

		/**
		 * clear the values of his fields
		 */
		// override
		reset: function() {
			this.callParent(arguments);

			this.classes.reset();
			this.filterChooser.reset();
		}
	});
})();
