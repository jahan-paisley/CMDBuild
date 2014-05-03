(function() {
	Ext.define("CMDBuild.controller.common.chart.CMChartPortletControllerPreviewStrategy", {
		doRequest: function(caller, cb) {
			var me = caller;	
			var chartId = me.chartConfiguration.getId();
			var params = me.readParamsFromForm();
			var dsName = me.chartConfiguration.getDataSourceName();
			var loaded = false;

			function success(data) {
				me.store.loadData(data);
				loaded = true;
			}
	
			function callback() {
				if (cb && typeof cb == "function") {
					cb(loaded);
				}
			}

			CMDBuild.ServiceProxy.Dashboard.chart.getDataForPreview(dsName, params, success, callback);
		}
	});

	Ext.define("CMDBuild.controller.common.chart.CMChartPortletControllerDefaultStrategy", {
		doRequest: function(caller, cb) {
			var me = caller;
			var dashbaordId = me.dashboardId;
			var chartId = me.chartConfiguration.getId();
			var params = me.readParamsFromForm();
			var loaded = false;
	
			function success(data) {
				me.store.loadData(data);
				loaded = true;
			}
	
			function callback() {
				if (cb && typeof cb == "function") {
					cb(loaded);
				}
			}

			CMDBuild.ServiceProxy.Dashboard.chart.getData(dashbaordId, chartId, params, success, callback);
		}
	});

	Ext.define("CMDBuild.controller.common.chart.CMChartPortletController", {
		statics: {
			buildStoreForChart: function(chartConfiguration) {
				var fields = [];
				if (chartConfiguration.getSingleSeriesField()) {
					fields = [chartConfiguration.getSingleSeriesField()];
					if (chartConfiguration.getLabelField()) {
						fields = fields.concat(chartConfiguration.getLabelField());
					}
				} else {
					fields = [chartConfiguration.getCategoryAxisField()].concat(chartConfiguration.getValueAxisFields());
				}
	
				return Ext.create('Ext.data.JsonStore', {
					fields : fields,
					data : []
				});
			},

			build: function(view, chartConfiguration, store, dashboardId) {
				var strategy = new CMDBuild.controller.common.chart.CMChartPortletControllerDefaultStrategy();
				var controller = new CMDBuild.controller.common.chart.CMChartPortletController(view, chartConfiguration, store, dashboardId, strategy);

				return controller;
			},

			buildForPreview: function(view, chartConfiguration, store, dashboardId) {
				var strategy = new CMDBuild.controller.common.chart.CMChartPortletControllerPreviewStrategy();
				var controller = new CMDBuild.controller.common.chart.CMChartPortletController(view, chartConfiguration, store, dashboardId, strategy);

				return controller;
			}
		},

		constructor: function(view, chartConfiguration, store, dashboardId, strategy) {
			this.dashboardId = dashboardId;
			this.view = view;
			this.chartConfiguration = chartConfiguration;
			this.store = store;
			this.strategy = strategy;

			this.view.setDelegate(this);

			if (chartConfiguration.isAutoload()) {
				this.onFormLoadButtonClick();
			} else {
				this.view.showParamsForm(toggle=true);
			}
		},

		readParamsFromForm: function() {
			var parametersToSend = {};
			var parametersConfiguration = this.chartConfiguration.getDataSourceInputConfiguration();

			for (var	i=0,
						l=parametersConfiguration.length,
						p,
						field; i<l; ++i) {

				p = parametersConfiguration[i];
				field = this.view.findField(p.name);
				if (field) {
					parametersToSend[p.name] = field.getValue();
				}
			}

			return parametersToSend;
		},

		doRequest: function(cb) {
			var me = this;
			cb = Ext.Function.createSequence(cb||Ext.emptyFn, function() {
				me.unmaskView();
			});

			this.maskView();
			this.strategy.doRequest(this, cb);
		},

		maskView: function() {
			var el = this.view.getEl();
			if (el) {
				el.mask(CMDBuild.Translation.common.loading);
			}
		},

		unmaskView: function() {
			var el = this.view.getEl();
			if (el) {
				el.unmask();
			}
		},

		// as view delegate

		onReloadButtonClick: function(cb) {
			var me = this;
			this.view.checkStoreLoad(function() {
				if (me.view.formIsValid()) {
					me.doRequest(cb);
				} else {
					me.view.showParamsForm(toggle=true);
				}
			});
		},

		onFormLoadButtonClick: function() {
			var me = this;
			this.onReloadButtonClick(function (loaded) {
				if (!me.view.chartRendered && loaded) {
					me.view.renderChart();
				}

				if (me.chartConfiguration.getDataSourceInputConfiguration().length == 0) {
					// The user has clicked the load button in chart without input parameters
					// so there are no reasons to leave the panel showed
					me.view.hideParamsForm(toggle=true);
				}
			});
		}
	});
})();