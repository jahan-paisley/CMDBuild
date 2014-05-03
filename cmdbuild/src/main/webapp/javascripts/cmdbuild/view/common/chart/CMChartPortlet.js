(function() {
	Ext.define("CMDBuild.view.management.dashboard.CMChartPortlet", {
		extend: "Ext.app.Portlet",

		chartConfiguration: null, // pass when create it
		store: null, // pass when create it

		initComponent: function() {

			var buttons = [];
			this.withParamsForm = this.chartConfiguration.getDataSourceInputConfiguration().length > 0;

			if (this.withParamsForm) {
				this.editParamsButton = new Ext.button.Button({
					tooltip: CMDBuild.Translation.common.tooltip.editParameters,
					enableToggle: true,
					iconCls: "edit_form",
					toggleHandler: function(button, toggle) {
						if (toggle) {
							me.showParamsForm();
						} else {
							me.hideParamsForm();
						}
					}
				});

				buttons.push(this.editParamsButton);
			}

			this.chart = new Ext.panel.Panel({
				region: 'center',
				layout: 'fit',
				border: false
			});

			this.tableView = new CMDBuild.view.management.dashboard.CMChartPortletTableView({
				cls: "cmbordertop",
				region: 'south',
				split: true,
				autoScroll: true,
				height: 0,
				border: false,
				store: Ext.create('Ext.data.JsonStore', {
					fields : ["fake"],
					data : []
				}),
				columns: [{header: " ", dataIndex: "fake"}]
			});

			this.form = new CMDBuild.view.management.dashboard.CMChartPortletForm({
				chartConfiguration: this.chartConfiguration,
				region: 'north',
				frame: false,
				bodyCls: "x-panel-body-default-framed cmborderbottom",
				cls: "cmborderbottom",
				style: {
					padding: "0 0 3px 0"
				},
				bodyStyle: {
					padding: "5px 0 0 0"
				},
				buttons: [{
					text: CMDBuild.Translation.common.buttons.load,
					handler: function() {
						if (me.delegate) {
							me.delegate.onFormLoadButtonClick(this.form);
						}
					}
				}]
			});

			var me = this;

			this.reloadButton = new Ext.button.Button({
				disabled: true,
				iconCls: 'arrow_refresh',
				tooltip: CMDBuild.Translation.common.tooltip.reload,
				handler: function() {
					if (me.delegate) {
						me.delegate.onReloadButtonClick();
					}
				}
			});

			this.showTableViewButton = new Ext.button.Button({
				iconCls: "table",
				tooltip: CMDBuild.Translation.common.tooltip.showData,
				enableToggle: true,
				disabled: true,
				toggleHandler: function(button, toggle) {

					if (me.tableView.cmFake) {
						me.tableView.updateForStore(me.store);
						me.tableView.setHeight(120);
					}

					var h = me.getHeight();
					if (toggle) {
						me.tableView.show();
						h+=me.tableView.getHeight();
					} else {
						h-=me.tableView.getHeight();
						me.tableView.hide();
					}

					me.adjustSize(h);

					me.doLayout();
				}
			});

			buttons.push(this.showTableViewButton, this.reloadButton);
			this.chartBuilder = new CMDBuild.view.management.dashboard.CMChartConfigurationReader();

			
			this.title = this.chartConfiguration.getDescription() || this.chartConfiguration.getName() || "";
			this.layout = 'border';
			this.height = 100;
			this.items = [this.chart, this.tableView, this.form];
			this.tbar = buttons;
			this.resizable = {
				handles : 'n s'
			};

			this.callParent(arguments);
		},

		renderChart: function() {
			if (this.chartConfiguration && this.store) {
				this.chart.removeAll();
				this.chart.add(
						this.chartBuilder.buildChart(this.chartConfiguration, this.store));

				this.showTableViewButton.enable();
				this.reloadButton.enable();
				this.doLayout();
				if (!this.chartRendered) {
					try {
						this.chart.setHeight(250);
						this.adjustSize(this.getHeight() + 250);
						this.chartRendered = true;
					} catch (e) {
						_debug("Chart portlet: Rendering issue");
					}
				}
			}
		},

		showParamsForm: function(toggle) {
			if (toggle && this.editParamsButton) {
				this.editParamsButton.toggle(true);
			} else {
				this.form.show();
				this.adjustSize(this.getHeight() + this.form.getHeight());
			}
		},

		hideParamsForm: function(toggle) {
			if (toggle && this.editParamsButton) {
				this.editParamsButton.toggle(false);
			} else {
				this.adjustSize(this.getHeight() - this.form.getHeight());
				this.form.hide();
			}
		},

		setDelegate: function(d) {
			this.delegate = d;
		},

		formIsValid: function() {
			return this.form.getForm().isValid();
		},

		formIsLoading: function() {
			return this.form.isLoading();
		},

		checkStoreLoad: function(cb) {
			this.form.checkStoreLoad(cb);
		},

		adjustSize: function(height) {
			this.setSize({
				height: height, 
				width: this.getWidth()	// looks useless but without this there
										// could be rendering problems if the chart
										// was moved between two columns
			});
		},

		findField: function(name) {
			return this.form.getForm().findField(name);
		}
	});


	/**
	 *  it is a component of the CMChartPortlet, start with a fake store
	 *  because it is not displayed at the beginning.
	 *  With updateForStore it is possible to redefine the columns to show
	 *  the fields of the store and to sign that is now it is not a fake
	 */
	Ext.define("CMDBuild.view.management.dashboard.CMChartPortletTableView", {
		extend: "Ext.grid.Panel",

		cmFake: true,

		updateForStore: function(store) {
			var dataModel = store.model.create();
			var columns = [];
			for (var name in dataModel.data) {
				columns.push({
					header: name,
					dataIndex: name,
					flex: 1
				});
			}

			this.reconfigure(store, columns);
			this.cmFake = false;
		}
	});

	Ext.define("CMDBuild.view.management.dashboard.CMDashboardColumn", {
		extend: "Ext.app.PortalColumn",
		addChart: function(chartConf, store, alsoInactive) {
			if (alsoInactive || chartConf.isActive()) {
				var c = new CMDBuild.view.management.dashboard.CMChartPortlet({
					chartConfiguration: chartConf,
					store: store
				});

				this.add(c);
				return c;
			}

			return null;
		}
	});

	Ext.define("CMDBuild.view.management.dashboard.CMChartWindow", {
		extend: "CMDBuild.PopupWindow",

		initComponent: function() {
			this.chartPortlet = new CMDBuild.view.management.dashboard.CMChartPortlet({
				chartConfiguration: this.chartConfiguration,
				store: this.store,
				closable: false,
				collapsible: false,
				draggable: false,
				resizable: false
			});

			Ext.apply(this, {
				items: [this.chartPortlet],
				layout : 'fit'
			});

			this.callParent(arguments);
		}
	});

	/**
	 * it is a component of the CMChartPortlet that read the CMDBUild
	 * chart configuration and return the ExtJs chart configuartion
	 * to render it
	 */
	Ext.define("CMDBuild.view.management.dashboard.CMChartConfigurationReader", {
		buildChart: function(chartConfiguration, store) {
			var readers = {
				gauge: gauge,
				bar: bar,
				line: line,
				pie: pie 
			};

			if (typeof readers[chartConfiguration.getType()] == "function") {
				return readers[chartConfiguration.getType()](chartConfiguration, store);
			} else {
				return {
					xtype: "panel"
				};
			}
		}
	});

	function gauge(chartConfiguration, store) {
		var bgcolor = chartConfiguration.getBgColor() || '#ffffff';
		var fgcolor = chartConfiguration.getFgColor() || '#99CC00';

		return {
			xtype : 'chart',
			layout: 'fit',
			store: store,
			insetPadding : 25,
			flex : 1,
			animate : {
				easing : 'elasticIn',
				duration : 1000
			},
			axes : [{
				type : 'gauge',
				position : 'gauge',
				minimum : chartConfiguration.getMinimum() || 0,
				maximum : chartConfiguration.getMaximum() || 1000,
				steps : chartConfiguration.getSteps() || 20,
				margin: 5
			}],
			series : [{
				type : 'gauge',
				field : chartConfiguration.getSingleSeriesField(),
				donut : 60,
				colorSet : [fgcolor, bgcolor]
			}]
		};
	}

	function bar(chartConfiguration, store) {
		var vertical = chartConfiguration.getChartOrientation() == "vertical",
		axesConf = {};

		if (vertical) {
			axesConf.category = "bottom";
			axesConf.values = "left";
			axesConf.type = "column";
		} else {
			axesConf.category = "left";
			axesConf.values = "bottom";
			axesConf.type = "bar";
		}

		return {
			xtype : 'chart',
			legend : chartConfiguration.withLegend(),
			store : store,
			animate : {
				easing : 'elasticIn',
				duration : 1000
			},
			axes : [ {
				type : 'Numeric',
				position : axesConf.values,
				fields : chartConfiguration.getValueAxisFields(),
				title : chartConfiguration.getValueAxisLabel(),
				minimum: 0,
				grid : true
			}, {
				type : 'Category',
				position : axesConf.category,
				fields : chartConfiguration.getCategoryAxisField(),
				title : chartConfiguration.getCategoryAxisLabel()
			} ],
			series : [ {
				type : axesConf.type,
				axis : axesConf.values,
				tips : {
					trackMouse : true,
					width: 200,
					height : 25,
					renderer : function(storeItem, item) {
						this.setTitle(item.value[0] + ": "+ item.value[1]);
					}
				},
				highlight : true,
				xField : chartConfiguration.getCategoryAxisField(),
				yField : chartConfiguration.getValueAxisFields()
			} ]
		};
	}

	function line(chartConfiguration, store) {
		function getLineSerie(xField,yField) {
			return {
				type : 'line',
				highlight : {
					size : 7,
					radius : 7
				},
				axis : 'left',
				xField : xField,
				yField : yField,
				tips : {
					trackMouse : true,
					width: 200,
					height : 25,
					renderer : function(storeItem, item) {
						this.setTitle(storeItem.get(xField) + ": "+ storeItem.get(yField));
					}
				}
			};
		}

		var series = [],
			valaueAxis = chartConfiguration.getValueAxisFields() || [],
			xField = chartConfiguration.getCategoryAxisField();

		for (var i=0, l=valaueAxis.length; i<l; ++i) {
			var yField = valaueAxis[i];

			series.push(getLineSerie(xField,yField));
		}

		return {
			xtype : 'chart',
			legend : chartConfiguration.withLegend(),
			store : store,
			animate : {
				easing : 'elasticIn',
				duration : 1000
			},
			axes : [ {
				type : 'Numeric',
				position : 'left',
				fields : chartConfiguration.getValueAxisFields(),
				title : chartConfiguration.getValueAxisLabel(),
				minimum: 0,
				grid : true
			}, {
				type : 'Category',
				position : 'bottom',
				fields : chartConfiguration.getCategoryAxisField(),
				title : chartConfiguration.getCategoryAxisLabel()
			} ],
			series : series
		};
	}

	function pie(chartConfiguration, store) {
		return {
			xtype : 'chart',
			legend: chartConfiguration.withLegend(),
			animate : {
				easing : 'elasticIn',
				duration : 2000
			},
			store : store,
			series : [ {
				type : 'pie',
				field : chartConfiguration.getSingleSeriesField(),
				showInLegend : true,
				highlight : {
					segment : {
						margin : 5
					}
				},
				label : {
					field : chartConfiguration.getLabelField(),
					display : 'rotate',
					contrast : true,
					font : '1.3em Arial'
				},
				tips : {
					trackMouse : true,
					width : 140,
					height : 28,
					renderer : function(storeItem, item) {
						// calculate and display percentage on hover
						var total = 0;
						store.each(function(rec) {
							total += rec.get(chartConfiguration.getSingleSeriesField());
						});
						this.setTitle(storeItem.get(chartConfiguration.getLabelField())
								+ ': '
								+ Math.round(storeItem.get(chartConfiguration.getSingleSeriesField()) / total
										* 100) + '%');
					}
				}
			} ]
		};
	}
})();