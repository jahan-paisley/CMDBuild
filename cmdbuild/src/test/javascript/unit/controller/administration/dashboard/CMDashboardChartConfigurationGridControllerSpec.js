(function() {
	var view,
		store,
		controller,
		delegate;

	describe('CMDashboardChartConfigurationGridController', function() {
		beforeEach(function() {
			view = jasmine.createSpyObj("viewSpy", [
				"setDelegate",
				"clearSelection"
			]);

			store = jasmine.createSpyObj("StoreSpy", [
				"loadData"
			]);

			delegate = new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationGridControllerDelegate();

			controller = new CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationGridController(view, store);
			controller.setDelegate(delegate);
		});

		afterEach(function() {
			delete view;
			delete controller;
		});

		it('is able to fill the store with the charts', function() {
			var charts = getCharts();
			controller.loadCharts(charts);
			expect(store.loadData).toHaveBeenCalledWith(charts);
		});

		it('forward to the delegate the chart selection', function() {
			var chartWasSelected = spyOn(delegate, "chartWasSelected");
			controller.onChartSelect();
			expect(chartWasSelected).toHaveBeenCalled();
		});

		it('is able to clear the grid selection', function() {
			controller.clearSelection();
			expect(view.clearSelection).toHaveBeenCalled();
		});

	});

	function getCharts() {
		return [
		
			new CMDBuild.model.CMDashboardChart({
				id: 1,
				name: "Card opened per group",
				description: "A pie chart with the number of opened card in a year, divided per group"
			}),

			new CMDBuild.model.CMDashboardChart({
				id: 2,
				name: "Card opened per class",
				description: "A pie chart with the number of opened card in a year, divided per class"
			})

		];
	}
})();