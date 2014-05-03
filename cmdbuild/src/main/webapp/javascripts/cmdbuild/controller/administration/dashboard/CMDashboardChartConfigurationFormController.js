(function() {

	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategyInterface", {
		extractInterestedValues: Ext.emptyFn,
		showChartFields: Ext.emptyFn,
		setChartDataSourceName: Ext.emptyFn,
		getAvailableDsOutputFields: Ext.emptyFn,
		fillFieldsForChart: Ext.emptyFn,
		getChartData: Ext.emptyFn,
		updateDataSourceDependantFields: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategy", {

		extend: "CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategyInterface",

		interestedFields: [], 	// fill this array in the subclasses with the name of the
								// form attributes managed from the strategy

		dataSourceName: null,

		constructor: function(form) {
			this.form = form;
		},

		extractInterestedValues: function(data) {
			var out = {},
				me = this;
	
			for (var i=0, l=me.interestedFields.length; i<l; ++i) {
				out[me.interestedFields[i]] = data[me.interestedFields[i]];
			}
	
			return out;
		},
	
		showChartFields: function() {
			this.form.showFieldsWithName(this.interestedFields);
		},

		setChartDataSourceName: function(dsName) {
			this.dataSourceName = dsName;
			this.updateDataSourceDependantFields();
		},

		getAvailableDsOutputFields: function (allowedTypes) {
			var dataSourceOutput = this.dataSourceName ? _CMCache.getDataSourceOutput(this.dataSourceName) : [],
				allowedTypes = allowedTypes || ["STRING", "INTEGER", "DATE"],
				out = [];

			for (var i=0, l=dataSourceOutput.length, d; i<l; ++i) {
				d = dataSourceOutput[i];
				if (Ext.Array.contains(allowedTypes, d.type)) {
					out.push([d.name]);
				}
			}

			return out;
		},

		fillFieldsForChart: Ext.emptyFn,
		getChartData: Ext.emptyFn,
		updateDataSourceDependantFields: Ext.emptyFn
	});

	Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationFormController", {

		mixins: {
			viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationFormDelegate"
		},

		statics: {
			cmcreate: function(view) {

				function getStrategyFor(type, view) {
					var strategis = {
						"gauge": CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy,
						"pie": CMDBuild.controller.administration.dashboard.charts.CMChartPieStrategy,
						"bar": CMDBuild.controller.administration.dashboard.charts.CMChartBarStrategy,
						"line": CMDBuild.controller.administration.dashboard.charts.CMChartLineStrategy
					};

					if (typeof strategis[type] == "function") {
						return new strategis[type](view);
					} else {
						return null;
					}
				}

				return new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationFormController(view, getStrategyFor);
			}
		},

		constructor : function(view, strategiesMapping) {
			this.callParent(arguments);
			this.view = view;
			this.view.setDelegate(this);
			this.setChartTypeStrategy(null);
			this.chartDataSourceName = null;

			if (typeof strategiesMapping == "function") {
				this._getStrategy = strategiesMapping;
			} else {
				this._getStrategy = function() {
					return null;
				};
			}
		},

		initComponent : function() {
			this.callParent(arguments);
		},

		initView: function() {
			this.view.disableFields();
			this.view.cleanFields();
			this.view.hideOutputFields();
		},

		prepareForAdd: function() {
			this.view.cleanFields();
			this.view.hideOutputFields();
			this.view.enableFields();
		},

		prepareForChart: function(chart) {
			this.initView();
			this.view.fillFieldsWith({
				name: chart.getName(),
				description: chart.getDescription(),
				active: chart.isActive(),
				autoLoad: chart.isAutoload(),
				dataSourceName: chart.getDataSourceName(),
				type: chart.getType()
			});

			this.view.fillDataSourcePanel(chart.getDataSourceInputConfiguration());

			this.chartTypeStrategy.showChartFields(chart);
			this.chartTypeStrategy.fillFieldsForChart(chart);
		},

		prepareForModify: function() {
			this.view.enableFields(onlyMutable=true);
		},

		setChartTypeStrategy: function(s) {
			this.chartTypeStrategy = s || new CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategyInterface();

			this.view.hideOutputFields();

			CMDBuild.validateInterface(this.chartTypeStrategy, "CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategyInterface");
			this.chartTypeStrategy.setChartDataSourceName(this.chartDataSourceName);
			this.chartTypeStrategy.showChartFields();
		},

		getFormData: function() {
			var data = this.view.getFieldsValue() || {};
			var chartSpecificData = {};

			chartSpecificData = this.chartTypeStrategy.extractInterestedValues(data);

			return Ext.apply(extractGeneralData(data), chartSpecificData);
		},

		isValid: function() {
			return this.view.isValid();
		},

		// viewDelegate

		onTypeChanged: function(type) {
			var s = this._getStrategy(type, this.view);
			this.setChartTypeStrategy(s);
		},

		onDataSourceChanged: function(dsName) {
			var input = _CMCache.getDataSourceInput(dsName);
			this.chartDataSourceName = dsName;
			this.view.showDataSourceInputFields(input);
			this.chartTypeStrategy.setChartDataSourceName(this.chartDataSourceName);
		},

		onDataSourceInputFieldTypeChanged: function(value, fieldset) {
			var callbacks = {
				free: function(fieldset) {
					fieldset.resetFieldset();	
					fieldset.addDefaultFieldFromFieldManager();
				},
				classes: function(fieldset) {
					fieldset.addClassesFieldForDefault();
				},
				lookup: function(fieldset) {
					fieldset.addLookupTypesField();
				},
				user: function(fieldset) {
					fieldset.resetFieldset();
				},
				group: function(fieldset) {
					fieldset.resetFieldset();
				},
				card: function(fieldset) {
					fieldset.addClassesFieldForReferenceWidget();
				}
			};

			callbacks[value](fieldset);
		}
	});

	function extractGeneralData(data) {
		return {
			name: data.name,
			description: data.description,
			active: data.active,
			autoLoad: data.autoLoad,
			type: data.type,
			dataSourceName: data.dataSourceName,
			dataSourceParameters: data.dataSourceParameters
		};
	}
	
})();