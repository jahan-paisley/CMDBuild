(function() {

	describe('CMChartPieTypeStrategy', function() {
		var pie, view;

		beforeEach(function() {
			view = jasmine.createSpyObj("CMDashboardChartConfigurationFormSpy", [
				"fillFieldsWith",
				"disableFields",
				"enableFields",
				"hideOutputFields",
				"showFieldsWithName",
				"cleanFields",
				"setDelegate",
				"setLabelFieldAvailableData",
				"setSingleSerieFieldAvailableData"
			]);

			afterEach(function() {
				delete view;
				delete pie;
			});

			pie = new CMDBuild.controller.administration.dashboard.charts.CMChartPieStrategy(view);
		});

		it ('fill the right fields of the form', function() {
			var chart = aPie();
			var dsName = "Foo";
			var getDataSourceOutput = spyOn(_CMCache, "getDataSourceOutput").andReturn([
				{name: "foo", type: "STRING"},
				{name: "bar", type: "INTEGER"},
				{name: "bart", type: "INTEGER"}
			]);

			pie.setChartDataSourceName(dsName);
			pie.fillFieldsForChart(chart);

			expect(view.fillFieldsWith).toHaveBeenCalled();
			var data = view.fillFieldsWith.argsForCall[0][0];
			expect(data.singleSeriesField).toEqual(chart.getSingleSeriesField());
			expect(data.labelField).toEqual(chart.getLabelField());
		});

		it ('say to the view to show the right fields', function() {
			var chart = aPie();
			var dsName = "Foo";
			var getDataSourceOutput = spyOn(_CMCache, "getDataSourceOutput").andReturn([
				{name: "foo", type: "STRING"},
				{name: "bar", type: "INTEGER"},
				{name: "bart", type: "INTEGER"}
			]);

			pie.setChartDataSourceName(dsName);
			pie.showChartFields(chart);

			expect(view.showFieldsWithName).toHaveBeenCalledWith([
				'singleSeriesField',
				'labelField',
				'legend'
			]);

			expect(getDataSourceOutput).toHaveBeenCalledWith(dsName);
		});

		it ('is able to read the right field from the form', function() {
			var chart = aPie();
			var values = pie.extractInterestedValues(chart.data);

			expect(values).toEqual({
				singleSeriesField: "bar",
				labelField: "foo",
				legend: true
			});
		});

		it ('is able to set the chart datasource', function() {
			var dsName = "Foo";
			var getDataSourceOutput = spyOn(_CMCache, "getDataSourceOutput").andReturn([
				{name: "foo", type: "STRING"},
				{name: "bar", type: "INTEGER"},
				{name: "bart", type: "INTEGER"}
			]);

			expect(pie.dataSourceName).toBeNull();

			pie.setChartDataSourceName(dsName);

			expect(view.setSingleSerieFieldAvailableData).toHaveBeenCalledWith([
				[ 'bar' ], [ 'bart' ]
			]);
			expect(view.setLabelFieldAvailableData).toHaveBeenCalledWith([
				[ 'foo' ], [ 'bar' ], [ 'bart' ]
			]);

		});
	});

	function aChart(config) {
		config = Ext.apply({
			id: 2,
			active: true,
			autoload: true,
			name: "Chart foo",
			description: "Description of Foo"
		}, config);

		return new CMDBuild.model.CMDashboardChart(config);
	}

	function aPie() {
		return aChart({
			labelField: 'foo',
			singleSeriesField: 'bar',
			legend: true
		});
	}
})();