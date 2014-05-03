(function() {

	describe('CMChartGaugeTypeStrategy', function() {
		var gauge, view;

		beforeEach(function() {
			view = jasmine.createSpyObj("CMDashboardChartConfigurationFormSpy", [
				"fillFieldsWith",
				"disableFields",
				"enableFields",
				"hideOutputFields",
				"showFieldsWithName",
				"cleanFields",
				"setDelegate",
				"setSingleSerieFieldAvailableData"
			]);

			afterEach(function() {
				delete view;
				delete gauge;
			});

			gauge = new CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy(view);
		});

		it ('fill the right fields of the form', function() {
			var chart = aGauge();
			var dsName = "Foo";
			var getDataSourceOutput = spyOn(_CMCache, "getDataSourceOutput").andReturn([
				{name: "foo", type: "string"},
				{name: "bar", type: "integer"},
				{name: "bart", type: "integer"}
			]);

			gauge.setChartDataSourceName(dsName);
			gauge.fillFieldsForChart(chart);

			expect(view.fillFieldsWith).toHaveBeenCalled();
			var data = view.fillFieldsWith.argsForCall[0][0];
			expect(data.maximum).toEqual(chart.getMaximum());
			expect(data.minimum).toEqual(chart.getMinimum());
			expect(data.fgcolor).toEqual(chart.getFgColor());
			expect(data.bgcolor).toEqual(chart.getBgColor());
			expect(data.steps).toEqual(chart.getSteps());
			expect(data.singleSeriesField).toEqual(chart.getSingleSeriesField());
		});

		it ('say to the view to show the right fields', function() {
			var chart = aGauge();
			var dsName = "Foo";
			var getDataSourceOutput = spyOn(_CMCache, "getDataSourceOutput").andReturn([
				{name: "foo", type: "STRING"},
				{name: "bar", type: "INTEGER"},
				{name: "bart", type: "INTEGER"}
			]);

			gauge.setChartDataSourceName(dsName);
			gauge.showChartFields(chart);

			expect(view.showFieldsWithName).toHaveBeenCalledWith([
				'maximum',
				'minimum',
				'steps',
				'fgcolor',
				'bgcolor',
				'singleSeriesField'
			]);

			expect(getDataSourceOutput).toHaveBeenCalledWith(dsName);
			expect(view.setSingleSerieFieldAvailableData).toHaveBeenCalled();
			expect(view.setSingleSerieFieldAvailableData).toHaveBeenCalledWith([
				['bar'], ['bart']
			]);
		});

		it ('is able to read the right field from the form', function() {
			var chart = aGauge();
			var values = gauge.extractInterestedValues(chart.data);

			expect(values).toEqual({
				minimum: 1,
				maximum: 1000,
				steps: 20,
				bgcolor: '#ffffff',
				fgcolor: '000000',
				singleSeriesField: "bar"
			});
		});

		it ('is able to set the chart datasource', function() {
			var dsName = "Foo";
			var getDataSourceOutput = spyOn(_CMCache, "getDataSourceOutput").andReturn([
				{name: "foo", type: "STRING"},
				{name: "bar", type: "INTEGER"},
				{name: "bart", type: "INTEGER"}
			]);

			expect(gauge.dataSourceName).toBeNull();

			gauge.setChartDataSourceName(dsName);

			expect(gauge.dataSourceName).toEqual(dsName);
			expect(view.setSingleSerieFieldAvailableData).toHaveBeenCalledWith([
				['bar'], ['bart']
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

	function aGauge() {
		return aChart({
			minimum: 1,
			maximum: 1000,
			steps: 20,
			bgcolor: '#ffffff',
			fgcolor: '000000',
			singleSeriesField: 'bar'
		});
	}
})();