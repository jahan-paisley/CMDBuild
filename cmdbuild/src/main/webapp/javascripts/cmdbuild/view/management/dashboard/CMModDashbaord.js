(function() {
	Ext.define("CMDBuild.view.management.dashboard.CMModDashboard", {

		extend: "Ext.panel.Panel",
		cmName: "dashboard",

		initComponent: function() {
			this.layout = "card";
			this.items = [{xtype: "panel"}];
			this.renderdDashboards = {};
			this.dashbaord = null;
			this.border = false;
			this.callParent(arguments);
		},

		buildDashboardColumns: function(dashboard) {
			if (dashboard) {

				updateTitle(this, dashboard.get("description"));

				if (this.renderdDashboards[dashboard.getId()]) {
					this.getLayout().setActiveItem(this.renderdDashboards[dashboard.getId()]);
				} else {
					var columnsConf = dashboard.getColumns();
					var columns = [];
					var me = this;
	
					this.dashbaord = dashboard;
	
					for (var i=0, l=columnsConf.length, conf; i<l; ++i) {
						conf = columnsConf[i];
	
						columns.push(new CMDBuild.view.management.dashboard.CMDashboardColumn({
							columnWidth : conf.width,
							charts: conf.charts,
							items: [],
							split: true,
							listeners : {
								render: function(column) {
									if (me.delegate) {
										me.delegate.onColumnRender(column);
									}
								}
							}
						}));
					}

					var newDashboard = new Ext.app.PortalPanel({
						items: columns
					});

					this.renderdDashboards[dashboard.getId()] = newDashboard;
					this.add(newDashboard);
					this.getLayout().setActiveItem(newDashboard);
				}
			}
		},

		setDelegate: function(d) {
			this.delegate = d;
		}
	});

	function updateTitle(me, dashboardName) {
		var title =  CMDBuild.Translation.administration.modDashboard.title;
		var titleSeparator = " - ";

		if (dashboardName) {
			title += titleSeparator + dashboardName;
		}

		me.setTitle(title);
	}
})();