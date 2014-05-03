(function() {
	var view,
		controller,
		chartTypeStrategy;

	describe('CMDashboardChartConfigurationFormController', function() {

		beforeEach(function() {
			view = CMDBuild.test.spyObj(CMDBuild.view.administration.dashboard
					.CMDashboardChartConfigurationForm, "CMDashboardChartConfigurationFormSpy", ["hideOutputFields"]);

			controller = CMDBuild.controller.administration.dashboard
				.CMDashboardChartConfigurationFormController.cmcreate(view);

			chartTypeStrategy = new CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategyInterface();
		});

		afterEach(function() {
			delete view;
			delete controller;
			delete chartTypeStrategy;
		});

		it('init well the view', function() {
			controller.initView();

			expect(view.cleanFields).toHaveBeenCalled();
			expect(view.disableFields).toHaveBeenCalled();
			expect(view.hideOutputFields).toHaveBeenCalled();
		});

		it('is able to prepare the form to add a chart', function() {
			controller.prepareForAdd();

			expect(view.cleanFields).toHaveBeenCalled();
			expect(view.hideOutputFields).toHaveBeenCalled();
			expect(view.enableFields).toHaveBeenCalled();
		});

		it('is able to prepare the form to a selected chart', function() {
			var showChartFields = spyOn(chartTypeStrategy, "showChartFields"),
				fillFieldsForChart = spyOn(chartTypeStrategy, "fillFieldsForChart"),
				c = aChart();

			controller.setChartTypeStrategy(chartTypeStrategy);
			controller.prepareForChart(c);

			expect(view.cleanFields).toHaveBeenCalled();
			expect(view.fillFieldsWith).toHaveBeenCalled();
			expect(view.disableFields).toHaveBeenCalled();
			expect(view.hideOutputFields).toHaveBeenCalled();
			expect(view.fillDataSourcePanel).toHaveBeenCalledWith(c.getDataSourceInputConfiguration());

			expect(fillFieldsForChart).toHaveBeenCalledWith(c);
			expect(showChartFields).toHaveBeenCalledWith(c);

			var data = view.fillFieldsWith.argsForCall[0][0];
			expect(data.name).toEqual(c.getName());
			expect(data.description).toEqual(c.getDescription());
			expect(data.active).toEqual(c.isActive());
			expect(data.autoLoad).toEqual(c.isAutoload());
			expect(data.dataSourceName).toEqual("cm_datasource_1");
			expect(data.type).toEqual("none");
		});

		it('is able to enable the fields to modify a chart', function() {
			controller.prepareForModify();
			expect(view.enableFields).toHaveBeenCalledWith(onlyMutable=true);
		});

		it('is able to retrieve the values from the form', function() {
			var extractInterestedValues = spyOn(chartTypeStrategy, "extractInterestedValues");

			controller.setChartTypeStrategy(chartTypeStrategy);

			var data = controller.getFormData();

			expect(view.getFieldsValue).toHaveBeenCalled();
			expect(extractInterestedValues).toHaveBeenCalled();
		});

		it('is able to validate the form', function() {
			view.isValid.andReturn(true);
			expect(controller.isValid()).toBeTruthy();
			expect(view.isValid).toHaveBeenCalled();

			view.isValid.reset();
			view.isValid.andReturn(false);

			expect(controller.isValid()).toBeFalsy();
			expect(view.isValid).toHaveBeenCalled();
		});

		// typeStrategy setting

		it('throws an exception if try to set a strategy that is not istance of CMDashboardChartConfigurationFormControllerTypeStrategy', function() {
			var s = {};
			var expectedError = Ext.String.format(CMDBuild.IS_NOT_CONFORM_TO_INTERFACE, s, "CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategyInterface");

			expect(function() {
				controller.setChartTypeStrategy(s);
			}).toThrow(expectedError);
		});

		it('set the chart type strategy', function() {
			var showChartFields = spyOn(chartTypeStrategy, "showChartFields"),
				setChartDataSourceName = spyOn(chartTypeStrategy, "setChartDataSourceName");

			expect(Ext.getClassName(controller.chartTypeStrategy)).toEqual("CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategyInterface");

			controller.setChartTypeStrategy(chartTypeStrategy);
			expect(controller.chartTypeStrategy).toBe(chartTypeStrategy);
			expect(showChartFields).toHaveBeenCalled();
			expect(setChartDataSourceName).toHaveBeenCalled();
		});

		// view delegate

		it('onTypeChanged istantiate the strategy', function() {
			controller.onTypeChanged("gauge");
			expect(Ext.getClassName(controller.chartTypeStrategy)).toEqual("CMDBuild.controller.administration.dashboard.charts.CMChartGaugeStrategy");
			view.hideOutputFields.reset();

			controller.onTypeChanged("pie");
			expect(Ext.getClassName(controller.chartTypeStrategy)).toEqual("CMDBuild.controller.administration.dashboard.charts.CMChartPieStrategy");
			view.hideOutputFields.reset();

			controller.onTypeChanged("bar");
			expect(Ext.getClassName(controller.chartTypeStrategy)).toEqual("CMDBuild.controller.administration.dashboard.charts.CMChartBarStrategy");
			view.hideOutputFields.reset();

			controller.onTypeChanged(undefined);
			expect(Ext.getClassName(controller.chartTypeStrategy)).toEqual("CMDBuild.controller.administration.dashboard.charts.CMChartTypeStrategyInterface");
			expect(view.hideOutputFields).toHaveBeenCalled();
		});

		it('manage the data sourche change', function() {
			var dsName = "cm_datasource_1",
				input = [
					{name: "input1", type: "date"},
					{name: "input2", type: "integer"},
					{name: "input3", type: "string"}
				],
				getDataSourceInput = spyOn(_CMCache, "getDataSourceInput").andReturn(input),
				setChartDataSourceName = spyOn(controller.chartTypeStrategy, "setChartDataSourceName");

			controller.onDataSourceChanged(dsName);

			expect(getDataSourceInput).toHaveBeenCalledWith(dsName);
			expect(setChartDataSourceName).toHaveBeenCalled();
			expect(view.showDataSourceInputFields).toHaveBeenCalledWith(input);
		});
	});

	function aChart(config) {
		config = Ext.apply({
			id: 2,
			active: true,
			autoload: true,
			name: "Chart foo",
			description: "Description of Foo",
			dataSourceName: "cm_datasource_1",
			dataSourceParameters: [],
			type: "none"
		}, config);

		return new CMDBuild.model.CMDashboardChart(config);
	}

})();