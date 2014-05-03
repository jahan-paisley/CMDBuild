(function() {

	describe('CMChartBarTypeStrategy', function() {
		var bar, view;

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
				"setSingleSerieFieldAvailableData",
				"showAxesFieldSets",
				"hideAxesFieldSets",
				"setCategoryAxesAvailableData",
				"setValueAxesAvailableData"
			]);

			afterEach(function() {
				delete view;
				delete bar;
			});

			bar = new CMDBuild.controller.administration.dashboard.charts.CMChartBarStrategy(view);
		});

		it ('fill the right fields of the form', function() {
			var chart = aBar();
			var dsName = "Foo";
			var getDataSourceOutput = spyOn(_CMCache, "getDataSourceOutput").andReturn([
				{name: "out1", type: "string"},
				{name: "out2", type: "integer"},
				{name: "out3", type: "integer"}
			]);

			bar.setChartDataSourceName(dsName);
			bar.fillFieldsForChart(chart);

			var data = view.fillFieldsWith.argsForCall[0][0];

			expect(data.categoryAxisField).toEqual(chart.getCategoryAxisField());
			expect(data.categoryAxisLabel).toEqual(chart.getCategoryAxisLabel());
			expect(data.valueAxisFields).toEqual(chart.getValueAxisFields());
			expect(data.valueAxisLabel).toEqual(chart.getValueAxisLabel());
			expect(data.chartOrientation).toEqual(chart.getChartOrientation());
			expect(data.legend).toEqual(chart.withLegend());
		});

		it ('say to the view to show the right fields', function() {
			var chart = aBar();
			var dsName = "Foo";
			var getDataSourceOutput = spyOn(_CMCache, "getDataSourceOutput").andReturn([
				{name: "out1", type: "string"},
				{name: "out2", type: "integer"},
				{name: "out3", type: "integer"}
			]);

			bar.setChartDataSourceName(dsName);
			bar.showChartFields(chart);

			expect(getDataSourceOutput).toHaveBeenCalledWith(dsName);
			expect(view.showAxesFieldSets).toHaveBeenCalled();
			expect(view.showFieldsWithName).toHaveBeenCalledWith([
				"legend",
				"categoryAxisField",
				"categoryAxisLabel",
				"valueAxisFields",
				"valueAxisLabel",
				"chartOrientation"
			]);
		});

		it ('is able to read the right field from the form', function() {
			var chart = aBar();
			var values = bar.extractInterestedValues(chart.data);

			expect(values).toEqual({
				categoryAxisField: 'category',
				categoryAxisLabel: 'categories',
				valueAxisFields: ['foo', 'bar'],
				valueAxisLabel: 'value',
				chartOrientation: "vertical",
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

			expect(bar.dataSourceName).toBeNull();
			bar.setChartDataSourceName(dsName);

			expect(view.setCategoryAxesAvailableData).toHaveBeenCalledWith([
				['foo'], [ 'bar' ], [ 'bart' ]
			]);

			expect(view.setValueAxesAvailableData).toHaveBeenCalledWith([
				[ 'bar' ], [ 'bart' ]
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

	function aBar() {
		return aChart({
			categoryAxisField: 'category',
			categoryAxisLabel: 'categories',
			valueAxisFields: ['foo', 'bar'],
			valueAxisLabel: 'value',
			chartOrientation: "vertical",
			legend: true
		});
	}
})();