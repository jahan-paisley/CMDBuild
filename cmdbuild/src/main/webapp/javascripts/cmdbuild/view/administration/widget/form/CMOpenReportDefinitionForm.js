(function() {
	Ext.define("CMDBuild.view.administration.widget.form.CMOpenReportDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".OpenReport"
		},

		initComponent: function() {
			this.callParent(arguments);

			this.addEvents(
				/* fired when is set the report in the combo-box*/
				"cm-selected-report"
			);

			var me = this;
			this.mon(this.reportCode, "select", function(field, records) {
				me.fireEvent("cm-selected-report", records);
			}, this.reportCode);

		},

		// override
		buildForm: function() {
			var tr = CMDBuild.Translation.administration.modClass.widgets;
			var me = this;

			this.callParent(arguments);

			this.reportCode = new CMDBuild.field.CMBaseCombo({
				name: "code",
				fieldLabel: tr[me.self.WIDGET_NAME].fields.report,
				labelWidth: CMDBuild.LABEL_WIDTH,
				valueField: CMDBuild.model.CMReportAsComboItem._FIELDS.value,
				displayField: CMDBuild.model.CMReportAsComboItem._FIELDS.description,
				store: _CMCache.getReportComboStore()
			});

			this.forceFormatCheck = new Ext.form.field.Checkbox({
				flex: 1
			});

			this.forceFormatOptions = new CMDBuild.field.CMBaseCombo({
				store : new Ext.data.ArrayStore({
					autoDestroy: true,
					fields : [ 'value', 'text' ],
					data : [
						[ 'pdf', 'PDF' ],
						[ 'csv', 'CSV' ]
					]
				}),
				displayField: "text",
				valueField: "value",
				queryMode: "local",
				flex: 3
			});

			this.forceFormat = new Ext.form.FieldContainer({
				width: 300,
				fieldLabel: tr[me.self.WIDGET_NAME].fields.force,
				labelWidth: CMDBuild.LABEL_WIDTH,
				layout: 'hbox',
				items: [this.forceFormatCheck, this.forceFormatOptions]
			});

			this.presetGrid = new CMDBuild.view.administration.common.CMKeyValueGrid({
				title: tr[me.self.WIDGET_NAME].fields.presets,
				keyLabel: tr[me.self.WIDGET_NAME].presetGrid.attribute,
				valueLabel: tr[me.self.WIDGET_NAME].presetGrid.value,
				margin: "0 0 0 3"
			});

			// defaultFields is inherited
			this.defaultFields.add(this.reportCode, this.forceFormat);

			Ext.apply(this, {
				layout: {
					type: "hbox"
				},
				items: [this.defaultFields, this.presetGrid]
			});
		},

		fillPresetWithData: function(data) {
			this.presetGrid.fillWithData(data);
		},

		// override
		fillWithModel: function(model) {
			this.callParent(arguments);
			this.reportCode.setValue(model.get("reportCode"));

			var forceFormat = model.get("forceFormat");
			if (forceFormat) {
				this.forceFormatCheck.setValue(true);
				this.forceFormatOptions.setValue(forceFormat);
			}

			this.fillPresetWithData(model.get("preset"));
		},

		// override
		disableNonFieldElements: function() {
			this.presetGrid.disable();
		},

		// override
		enableNonFieldElements: function() {
			this.presetGrid.enable();
		},

		// override
		getWidgetDefinition: function() {
			var me = this;

			return Ext.apply(me.callParent(arguments), {
				forceFormat: (function() {
					if (me.forceFormatCheck.getValue()) {
						return me.forceFormatOptions.getValue();
					}
				})(),
				reportCode: me.reportCode.getValue(),
				preset: me.presetGrid.getData()
			});
		}
	});
})();