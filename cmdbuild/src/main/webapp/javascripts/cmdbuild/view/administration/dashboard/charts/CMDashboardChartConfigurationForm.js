(function() {

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationFormInterface", {
		enableFields: Ext.emptyFn,
		disableFields: Ext.emptyFn,
		fillFieldsWith: Ext.emptyFn,
		getFieldsValue: Ext.emptyFn,
		cleanFields: Ext.emptyFn,
		hideFieldsWithName: Ext.emptyFn,
		showFieldsWithName: Ext.emptyFn,

		showDataSourceInputFields: Ext.emptyFn,
		showSingleOutoputDSMapping: Ext.emptyFn,

		showAxesFieldSets: Ext.emptyFn,
		hideAxesFieldSets:Ext.emptyFn,
		setCategoryAxesAvailableData: Ext.emptyFn,
		setValueAxesAvailableData: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationFormDelegate", {
		onTypeChanged: Ext.emptyFn,
		onDataSourceChanged: Ext.emptyFn,
		onDataSourceInputFieldTypeChanged: Ext.emptyFn
	});

	var tr = CMDBuild.Translation.administration.modDashboard.charts;

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationForm", {
		extend : "Ext.form.Panel",

		mixins : {
			cminterface: "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationFormInterface"
		},

		alias : "widget.dashboardchartsconfigurationform",

		constructor : function() {
			this.callParent(arguments);
		},

		initComponent : function() {
			var me = this,
				subPanelConf = {
					padding: '5 0 5 0',
					anchor: "-30",
					border: false,
					frame: false,
					bodyCls: 'cmgraypanel'
				};

			this.dataSourcePanel = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationDataSourcePanel(Ext.apply(subPanelConf, {
				cls: 'cmborderbottom',
				afterComboValueChanged: function(value) {
					if (me.delegate) {
						me.delegate.onDataSourceChanged(cleanComboValue(value, "name"));
					}
				},
				afterInputFieldTypeChanged: function(value, fieldset) {
					if (me.delegate) {
						me.delegate.onDataSourceInputFieldTypeChanged(cleanComboValue(value, "value"), fieldset);
					}
				}
			}));

			Ext.apply(this, {
				autoScroll: true,
				layout: 'anchor',
				defaults: subPanelConf,
				items: [{
					padding: '0 0 5 0',
					items: mainPropertiesItems(me),
					cls: 'cmborderbottom'
				}, 
					this.dataSourcePanel
				, {
					items: outputConfiguretionItems(me)
				}]
			});

			this.callParent(arguments);

			addSequenceToTypeSetValue(me);
		},

		isValid: function() {
			return this.getForm().isValid();
		},

		// fields management

		enableFields: function(onlyMutable) {
			this._disabled = false;
			iterateOverFields(this, function(field) {
				if ((onlyMutable && field.cmImmutable) 
						|| !field.isVisible()) {
					return;
				}

				field.enable();
			});
		},

		disableFields: function() {
			this._disabled = true;
			iterateOverFields(this, function(field) {
				field.disable();
			});
		},

		fillFieldsWith: function(data) {
			iterateOverFields(this, function(field) {
				if (data[field.name]) {
					field.setValue(data[field.name]);
				}
			});
		},

		fillDataSourcePanel: function(dataSourceInputConf) {
			this.dataSourcePanel.fillInputFieldsWith(dataSourceInputConf);
		},

		getFieldsValue: function() {
			var out = {};
			iterateOverFields(this, function(field) {
				out[field.name]= field.getValue();
			});

			out.dataSourceParameters = this.getDataSourceConfiguration();

			return out;
		},

		cleanFields: function() {
			this.dataSourcePanel.removeDataSourceInputFields();
			iterateOverFields(this, function(field) {
				field.reset();
			});
		},

		hideFieldsWithName: function(names) {
			callFunctionForItemWithNames(this, names, "hide");
			callFunctionForItemWithNames(this, names, "disable");
		},

		showFieldsWithName: function(names) {
			callFunctionForItemWithNames(this, names, "show");
			if (this._disabled) {
				callFunctionForItemWithNames(this, names, "disable");
			} else {
				callFunctionForItemWithNames(this, names, "enable");
			}
		},

		// data source management

		getDataSourceConfiguration: function() {
			return this.dataSourcePanel.getData();
		},

		showDataSourceInputFields: function(inputConf) {
			this.dataSourcePanel.setDataSourceInputFields(inputConf);
		},

		setSingleSerieFieldAvailableData: function(data) {
			this.singleSerieField.reset();
			this.singleSerieField.setAvailableFields(data);
		},

		setLabelFieldAvailableData: function(data) {
			this.labelField.setAvailableFields(data);
			this.labelField.reset();
		},

		// axes fieldSets

		showAxesFieldSets: function() {
			this.categoryAxesFieldSet.show();
			this.valueAxesFieldSet.show();
		},

		hideAxesFieldSets: function() {
			this.categoryAxesFieldSet.hide();
			this.valueAxesFieldSet.hide();
		},

		setCategoryAxesAvailableData: function(data) {
			this.categoryAxesFieldSet.setAvaiableData(data);
		},

		setValueAxesAvailableData: function(data) {
			this.valueAxesFieldSet.setAvaiableData(data);
		},

		// delegate

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationFormDelegate");
			this.delegate = d;
		}
	});

	function mainPropertiesItems(me) {
		return [
			me.nameField = new Ext.form.field.Text({
				fieldLabel: tr.fields.name,
				name: "name",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				cmImmutable: true,
				disabled: true
			}),

			me.descriptionArea = new Ext.form.CMTranslatableTextArea({
				fieldLabel: tr.fields.description,
				name: "description",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				translationsKeyType: "Chart", 
				translationsKeyField: "Description",
				disabled: true
			}),

			me.activeCheck = new Ext.form.field.Checkbox({
				fieldLabel: tr.fields.active,
				name: "active",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				disabled: true
			}),

			me.autoLoadCheck = new Ext.form.field.Checkbox({
				fieldLabel: tr.fields.autoload,
				name: "autoLoad",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				disabled: true
			})
		];
	}

	function outputConfiguretionItems(me) {
		var outPutItems = [
			me.typeField = new Ext.form.field.ComboBox({
				fieldLabel: tr.fields.chartType,
				name: "type",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField: "value",
				displayField: "name",
				queryMode: "local",
				editable: false,
				allowBlank: false,
				store: new Ext.data.SimpleStore({
					fields: ["value", "name"],
					data : [
						["gauge", tr.availableCharts.gauge],
						["pie", tr.availableCharts.pie],
						["bar", tr.availableCharts.bar],
						["line", tr.availableCharts.line]
					]
				}),
				disabled: true
			}),

			me.showLegend = new Ext.form.field.Checkbox({
				fieldLabel: tr.fields.legend,
				name: "legend",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				disabled: true,
				hidden: true
			}),

			me.maximumField = new Ext.form.field.Number({
				name: "maximum",
				fieldLabel: tr.fields.max,
				minValue: 0,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				disabled: true,
				hidden: true
			}),

			me.minimumField = new Ext.form.field.Number({
				name: "minimum",
				fieldLabel: tr.fields.min,
				minValue: 0,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				disabled: true,
				hidden: true
			}),
	
			me.stepsField = new Ext.form.field.Number({
				name: "steps",
				fieldLabel: tr.fields.steps,
				minValue: 1,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				disabled: true,
				hidden: true
			}),
	
			me.fgColorField = new CMDBuild.form.HexColorField( {
				name: "fgcolor",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
				fieldLabel: tr.fields.foreground,
				disabled: true,
				hidden: true
			}),
	
			me.bgColorField = new CMDBuild.form.HexColorField( {
				name: "bgcolor",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
				fieldLabel: tr.fields.background,
				disabled: true,
				hidden: true
			}),

			me.singleSerieField = getConfigurableCombo("singleSeriesField",  tr.fields.valueField),
			me.labelField = getConfigurableCombo("labelField", tr.fields.labelField),

			me.orientationField = new Ext.form.field.ComboBox({
				fieldLabel: tr.fields.orientation.label,
				name: "chartOrientation",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField: "value",
				displayField: "description",
				queryMode: "local",
				editable: false,
				allowBlank: false,
				store: new Ext.data.SimpleStore({
					fields: ["value", "description"],
					data : [
						["horizontal", tr.fields.orientation.values.horizontal],
						["vertical", tr.fields.orientation.values.vertical]
					]
				}),
				disabled: true,
				hidden: true
			}),

			me.categoryAxesFieldSet = new CMDBuild.view.administration.dashboard._CategoryAxesConfigFieldset({
				hidden: true
			}),

			me.valueAxesFieldSet = new CMDBuild.view.administration.dashboard._ValueAxesConfigFieldset({
				hidden: true
			})
		];

		me.hideOutputFields = function() {
			me.hideFieldsWithName([
				"legend",
				"maximum",
				"minimum",
				"steps",
				"fgcolor",
				"bgcolor",
				"singleSeriesField",
				"labelField",
				"categoryAxisField",
				"categoryAxisLabel",
				"chartOrientation",
				"valueAxisFields",
				"valueAxisLabel"
			]);
			me.hideAxesFieldSets();
		};

		return outPutItems;
	}

	Ext.define("CMDBuild.view.administration.dashboard._CategoryAxesConfigFieldset", {

		extend: "Ext.form.FieldSet",
		title: tr.fields.categoryFieldset,
		baseCls: "cmfieldset",

		initComponent: function() {
			this.categoryAxesField = getConfigurableCombo("categoryAxisField", tr.fields.valueField);
			this.categoryAxesLabel = new Ext.form.field.Text({
				fieldLabel: tr.fields.title,
				name: "categoryAxisLabel",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				disabled: true,
				hidden: true
			}),

			Ext.apply(this, {
				items: [this.categoryAxesLabel, this.categoryAxesField]
			});

			this.callParent(arguments);
		},

		setAvaiableData: function(data) {
			this.categoryAxesField.store.loadData(data);
			this.categoryAxesField.reset();
		}
	});

	Ext.define("CMDBuild.view.administration.dashboard._ValueAxesConfigFieldset", {

		extend: "Ext.form.FieldSet",
		title: tr.fields.valueFieldset,
		baseCls: "cmfieldset",

		initComponent: function() {
			this.valueAxesFields = new CMDBuild.view.common.field.CMGroupSelectionList({
				fieldLabel: tr.fields.valueField,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				value: [],
				store: new Ext.data.SimpleStore({
					fields: ["value"],
					data: []
				}),
				name : "valueAxisFields",
				dataFields : ['value'],
				valueField : 'value',
				displayField : 'value',
				allowBlank: false,
				disabled: true,
				hidden: true
			});

			this.valueAxesLabel = new Ext.form.field.Text({
				fieldLabel: tr.fields.title,
				name: "valueAxisLabel",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				disabled: true,
				hidden: true
			}),

			Ext.apply(this, {
				items: [this.valueAxesLabel, this.valueAxesFields]
			});

			this.callParent(arguments);
		},

		setAvaiableData: function(data) {
			try {
				this.valueAxesFields.reset();
				this.valueAxesFields.store.loadData(data);
				if (data.length > 0) {
					this.valueAxesFields.setValue(data[0]);
				}
			} catch (e) {
				// I'm not able to understand what crashes
				// after these operations :.(
			}
		}
	});

	function iterateOverFields(me, fn) {
		if (typeof fn != "function") {
			return;
		}

		me.cascade(function(item) {
			if (item 
				&& isAField(item)) {

				fn(item);
			}
		});
	}

	function isAField(item) {
		return ((item instanceof Ext.form.Field) || (item instanceof Ext.ux.form.MultiSelect) || (item instanceof Ext.form.CMTranslatableText));
	}

	function addSequenceToTypeSetValue(me) {
		me.typeField.setValue = Ext.Function.createSequence(me.typeField.setValue, function(value) {
			if (me.delegate) {
				me.delegate.onTypeChanged(cleanComboValue(value, "value"));
			}
		});
	}

	function cleanComboValue(value, valueField) {
		if (typeof value != "undefined") {
			value = Ext.isArray(value) ? value[0] : value;
			value = typeof value.get == "function" ? value.get(valueField) : value;
		}
		return value;
	}

	function callFunctionForItemWithNames(me, names, fnName) {
		if (!Ext.isArray(names)) {
			names = [names];
		}

		me.cascade(function(item) {
			if (item 
				&& isAField(item)
				&& Ext.Array.contains(names, item.name)) {

				item[fnName]();
			}
		});
	}

	function getConfigurableCombo(name, label, visible) {
		return new Ext.form.field.ComboBox({
			fieldLabel: label,
			name: name,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			valueField: "value",
			displayField: "value",
			queryMode: "local",
			editable: false,
			allowBlank: false,
			store: new Ext.data.SimpleStore({
				fields: ["value"],
				data : []
			}),
			setAvailableFields: function(data) {
				this.store.loadData(data);
			},
			disabled: true,
			hidden: !visible
		});
	}
})();