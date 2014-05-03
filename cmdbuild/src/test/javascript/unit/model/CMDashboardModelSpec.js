(function() {

	var dashboard;

	describe('CMDashboardModel', function() {

		beforeEach(function() {
			dashboard = new CMDBuild.model.CMDashboard({
				id : 1,
				name : "Foo",
				groups : [14],
				description : "Amazing dashboard for amazing people",
				charts : []
			});
		});

		afterEach(function() {
			delete dashboard;
		});

		it('is able to add a chart to the existings', function() {
			expect(dashboard.getCharts().length).toBe(0);

			var chart = new CMDBuild.model.CMDashboardChart();
			dashboard.addChart(chart);
			expect(dashboard.getCharts().length).toBe(1);
		});

		it('is able to remove a chart', function() {
			var chart1 = new CMDBuild.model.CMDashboardChart({
				id: 1
			});

			var chart2 = new CMDBuild.model.CMDashboardChart({
				id: 2
			});

			var chart3 = new CMDBuild.model.CMDashboardChart({
				id: 3
			});

			dashboard.addChart(chart1);
			dashboard.addChart(chart2);
			dashboard.addChart(chart3);

			expect(dashboard.getCharts().length).toBe(3);
			dashboard.removeChart(2);

			var charts = dashboard.getCharts();
			expect(charts.length).toBe(2);
			expect(charts[0].getId()).toBe('1');
			expect(charts[1].getId()).toBe('3');
		});

		it('is able to replace a chart', function() {
			dashboard.addChart(new CMDBuild.model.CMDashboardChart({
				id: 1
			}));

			dashboard.addChart(new CMDBuild.model.CMDashboardChart({
				id: 2
			}));

			dashboard.replaceChart(1, new CMDBuild.model.CMDashboardChart({
				id: 3
			}));

			var charts = dashboard.getCharts();
			expect(charts.length).toBe(2);
			expect(charts[0].getId()).toBe('3');
		});

		it('is able to retrieve a chart', function() {
			dashboard.addChart(new CMDBuild.model.CMDashboardChart({
				id: 1
			}));

			dashboard.addChart(new CMDBuild.model.CMDashboardChart({
				id: 2
			}));

			dashboard.replaceChart(1, new CMDBuild.model.CMDashboardChart({
				id: 3
			}));

			var chart = dashboard.getChartWithId(2);
			expect(chart.getId()).toBe('2');
		});
	});
})();