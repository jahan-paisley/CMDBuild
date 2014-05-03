Ext.define("CMDBuild.controller.common.CMDashboardColumnController", {

	buildChart: function(chartConf, column) {
		var store = CMDBuild.controller.common.chart.CMChartPortletController.buildStoreForChart(chartConf);
		var chartView = column.addChart(chartConf, store);
	
		if (chartView) {
			CMDBuild.controller.common.chart.CMChartPortletController.build(chartView, chartConf, store, this.dashboard.getId());
		}
	},

	// delegate
	onColumnRender: function(column) {
		var me = this;
		for (var i=0, l=column.charts.length, chartConf; i<l; ++i) {
			chartConf = this.dashboard.getChartWithId(column.charts[i]);
	
			if (chartConf) {
				// Defer the calls to create a queue of add request
				// do this to have the rendering of a single chart per time
				// instead to wait all the charts before the rendering
				Ext.Function.createDelayed(
					fn = me.buildChart,
					delay = 1,
					scope = me,
					arguments = [chartConf, column]
				)();
			}
		}
	}
});