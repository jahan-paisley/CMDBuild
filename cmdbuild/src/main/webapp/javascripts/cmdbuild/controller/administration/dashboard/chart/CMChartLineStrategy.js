(function() {
	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartLineStrategy", {
		extend: "CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategy",

		constructor: function(form) {
			this.form = form;
		},

		interestedFields: [
			"legend",
			"categoryAxisField",
			"categoryAxisLabel",
			"valueAxisFields",
			"valueAxisLabel"
		],

		// override
		fillFieldsForChart: function(chart) {
			this.form.fillFieldsWith({
				legend: chart.withLegend(),
				categoryAxisField: chart.getCategoryAxisField(),
				categoryAxisLabel: chart.getCategoryAxisLabel(),
				valueAxisFields: chart.getValueAxisFields(),
				valueAxisLabel: chart.getValueAxisLabel()
			});
		},

		// override
		updateDataSourceDependantFields: function(dsName) {
			this.form.setCategoryAxesAvailableData(this.getAvailableDsOutputFields());
			this.form.setValueAxesAvailableData(this.getAvailableDsOutputFields(["INTEGER", "DECIMAL", "DOUBLE"]));
		},

		// override
		showChartFields: function() {
			this.callParent(arguments);
			this.form.showAxesFieldSets();
		}
	});
})();