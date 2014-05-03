(function() {

	Ext.override(Ext.slider.Multi, {
		removeThumb: function(thumbIndex) {
			var tt = this.thumbs;

			try {
				tt[thumbIndex].el.remove();
				Ext.Array.erase(tt, thumbIndex, 1);
			} catch (e) {
				// avoid crash to wrong index
			}
		},

		removeLastThumb: function() {
			this.removeThumb(this.thumbs.length - 1);
		},

		removeAddedThumbs: function() {
			var l = this.thumbs.length;
			while (l>0) {
				this.removeLastThumb();
				l = this.thumbs.length;
			}
		}
	});

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardLayoutPanelDelegate", {
		onAddColumnClick: Ext.emptyFn,
		onRemoveColumnClick: Ext.emptyFn,
		onColumnWidthSliderChange: Ext.emptyFn,
		onColumnRender: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardLayoutPanel", {
		extend: "Ext.panel.Panel",

		alias: "widget.dashboardlayoutconfiguration",

		initComponent : function() {
			var me = this;

			me.portal = new Ext.app.PortalPanel({
				region: "center"
			});

			me.slider = Ext.create('Ext.slider.Multi', {
				width: 250,
				values: [100],
				increment: 5,
				minValue: 0,
				maxValue: 100,

				listeners: {
					changecomplete: {
						fn: function(slider, newValue, thumb, options) {
							me.delegate.onColumnWidthSliderChange(slider, newValue, thumb);
						}
					}
				}
			});

			Ext.apply(this, {
				title: CMDBuild.Translation.administration.modDashboard.layout.title,
				layout: "border",
				border: false,
				style: {
					padding: "0 0 3px 0"
				},
				tbar: [{
					iconCls: "add",
					text: CMDBuild.Translation.administration.modDashboard.layout.add,
					handler: function() {
						me.delegate.onAddColumnClick();
					}
				}, {
					iconCls: "delete",
					text: CMDBuild.Translation.administration.modDashboard.layout.remove,
					handler: function() {
						me.delegate.onRemoveColumnClick();
					}
				},
				"-",
				me.slider],
				items: [me.portal],
				buttonAlign: "center",
				buttons: [{
					text: CMDBuild.Translation.common.buttons.save,
					handler: function() {
						me.delegate.onSaveButtonClick();
					}
				}, {
					text: CMDBuild.Translation.common.buttons.abort,
					handler: function() {
						me.delegate.onAbortButtonClick();
					}
				}]
			});

			this.callParent(arguments);
			this.setDelegate(this.delegate || new CMDBuild.view.administration.dashboard.CMDashboardLayoutPanelDelegate());
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.view.administration.dashboard.CMDashboardLayoutPanelDelegate");
			this.delegate = d;
		},

		addColumn: function(conf, skipColumnsWidthSync) {
			var me = this,
				c = new CMDBuild.view.management.dashboard.CMDashboardColumn({
					columnWidth : conf.width,
					charts: conf.charts,
					items: [],
					listeners : {
						render: function(column) {
							if (me.delegate) {
								me.delegate.onColumnRender(column);
							}
						},
						afterlayout: function() {
							me.syncSliderThumbsToColumnsWidth();
						}
					}
				});

			if (!skipColumnsWidthSync) {
				me.reduceColumnsWidthWithFactor(conf.width);
			}

			me.portal.add(c);
		},

		removeColumn: function(column) {
			this.portal.remove(column);
			this.increaseColumnsWidthWithFactor(column.columnWidth);

			this.syncSliderThumbsToColumnsWidth();
		},

		configureForDashboard: function(dashboardConfiguration) {
			var me = this,
				columnsConf = dashboardConfiguration.getColumns();

			me.clearAll();

			for (var i=0, l=columnsConf.length, conf; i<l; ++i) {
				conf = columnsConf[i];
				me.addColumn(conf, skipColumnsWidthSync = true);
			}
		},

		clearAll: function() {
			if (this.rendered) {
				this.portal.removeAll();
				this.slider.removeAddedThumbs();
			}
		},

		countColumns: function() {
			return this.portal.items.length;
		},

		reduceColumnsWidthWithFactor: function(factor) {
			this.portal.items.each(function(column) {
				column.columnWidth *= (1-factor);
			});

			this.portal.doLayout();
		},

		increaseColumnsWidthWithFactor: function(factor) {
			var columns = this.portal.items.length;
			factor =  columns > 0 ? factor / columns : factor;

			this.portal.items.each(function(column) {
				column.columnWidth += factor;
			});

			this.portal.doLayout();
		},

		removeEmptyColumns: function() {
			var me = this;
			this.portal.items.each(function(column) {
				if (column.items.length == 0) {
					me.removeColumn(column);
				}
			});
		},

		syncSliderThumbsToColumnsWidth: function() {
			var me  = this,
				sum = 0,
				lengths = [];

			this.slider.removeAddedThumbs();

			this.portal.items.each(function(column) {
				if (column.el) {
					sum += column.getWidth();
					lengths.push(column.getWidth());
				}
			});

			if (lengths.length == 1) {
				me.slider.addThumb(this.slider.maxValue);
			} else {
				var value = 0;
				for (var i=0, l=lengths.length -1; i<l; i++) {
					value += (this.slider.maxValue * lengths[i]) / sum;
					me.slider.addThumb(value);
				}
			}
		},

		syncColumnWidthToSliderThumbs: function() {
			var values = this.slider.getValues(),
				ratios = [],
				sum = 0;

			if (this.portal.items.length > 1) {
				for (var i=0, l=values.length; i<l; ++i) {
					var ratio = (values[i] / this.slider.maxValue) - sum;
					ratios.push(ratio);
					sum += ratio;
				}
			}

			ratios.push(1-sum); // for the last column
			var columns = this.portal.removeAll(autodestroy = false);

			for (var i=0, c=null; i<columns.length; ++i) {
				c = columns[i];
				c.columnWidth = ratios[i];
			}

			Ext.suspendLayouts();
			this.portal.add(columns);
			Ext.resumeLayouts(flush=true);
		},

		getColumnsConfiguration: function() {
			var conf = [];

			this.portal.items.each(function(item) {
				var column = {
					width: item.columnWidth,
					charts: []
				};

				item.items.each(function(chart) {
					if (Ext.getClassName(chart) == "CMDBuild.view.management.dashboard.CMChartPortlet") {
						column.charts.push(chart.chartConfiguration.getId());
					}
				});

				conf.push(column);
			});

			return conf;
		},

		isTheActiveTab: function() {
			try {
				var tabpanel = this.ownerCt;
				var layout = tabpanel.getLayout();
				return layout.getActiveItem() == this;
			} catch (e) {
				return false;
			}
		}
	});
})();