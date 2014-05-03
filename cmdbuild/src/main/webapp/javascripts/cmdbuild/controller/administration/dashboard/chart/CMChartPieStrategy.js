(function() {
	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartPieStrategy", {
		extend: "CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy",

		interestedFields: ['singleSeriesField', 'labelField', 'legend'],

		// override
		fillFieldsForChart: function(chart) {
			this.form.fillFieldsWith({
				labelField: chart.getLabelField(),
				singleSeriesField: chart.getSingleSeriesField(),
				legend: chart.withLegend()
			});
		},

		// override
		updateDataSourceDependantFields: function() {
			this.form.setSingleSerieFieldAvailableData(this.getAvailableDsOutputFields(["INTEGER", "DECIMAL", "DOUBLE"]));
			this.form.setLabelFieldAvailableData(this.getAvailableDsOutputFields(["INTEGER", "STRING", "TEXT"]));
		}
	});
})();