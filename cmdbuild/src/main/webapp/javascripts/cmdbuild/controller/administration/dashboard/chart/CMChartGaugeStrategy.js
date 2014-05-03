(function() {
	Ext.define("CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy", {
		extend: "CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategy",

		interestedFields: ['maximum', 'minimum', 'steps', 'fgcolor', 'bgcolor', 'singleSeriesField'],

		// override
		fillFieldsForChart: function(chart) {
			this.form.fillFieldsWith({
				maximum: chart.getMaximum(),
				minimum: chart.getMinimum(),
				fgcolor: chart.getFgColor(),
				bgcolor: chart.getBgColor(),
				steps: chart.getSteps(),
				singleSeriesField: chart.getSingleSeriesField()
			});
		},

		// override
		updateDataSourceDependantFields: function() {
			this.form.setSingleSerieFieldAvailableData(this.getAvailableDsOutputFields(["INTEGER", "DECIMAL", "DOUBLE"]));
		}
	});
})();