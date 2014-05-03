(function() {
	var view,
		delegate;

	describe('CMDashboardChartConfigurationGridSpec', function() {

		beforeEach(function() {
			view = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationGrid({
				renderTo: Ext.getBody()
			});

			delegate = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationGridDelegate();

			view.setDelegate(delegate);
			view.store.loadData(charts());
		});

		afterEach(function() {
			delete view;
			delete delegate;
		});

		it('call the delegate on row selection', function() {
			var onChartSelect = spyOn(delegate, "onChartSelect");
			waitsFor(function() {
				return !view.store.isLoading();
			})

			runs(function() {
				expect(view.store.data.getCount()).toBe(2)
				CMDBuild.test.selectGridRow(view, 0);
				expect(onChartSelect).toHaveBeenCalled();
			});
		});

		it('forward the clearSelection method to the selection Model', function() {
			var deselectAll = spyOn(view.getSelectionModel(), 'deselectAll');
			view.clearSelection();
			expect(deselectAll).toHaveBeenCalled();
		});
	});

	function charts() {
		return [
			new CMDBuild.model.CMDashboardChart({
				name: "Chart foo",
				description: "Description of Foo",
				id: 0
			}),
			new CMDBuild.model.CMDashboardChart({
				name: "Chart bar",
				description: "Description of Bar",
				id: 1
			})
		]
	}
})();
