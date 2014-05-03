(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMDashboardAccordionDelegate", {
		onChartDropped: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.administration.accordion.CMDashboardAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.administration.modDashboard.title,

		cmName: "dashboard",

		initComponent: function() {
			var me = this;
			this.viewConfig = {
				plugins: {
					ptype: 'treeviewdragdrop',
					dragGroup: 'dasboardTreeDGroup',
					dropGroup: 'chartGridDDGroup',
					enableDrag: false
				},
				listeners: {
					beforedrop: function(node, data, dropRec, dropPosition) {
						if (me.delegate) {
							var dashobardId = null,
								chartId = null;

							if (dropRec && typeof dropRec.getId == "function") {
								dashobardId = dropRec.getId();
							}

							if (data && data.records && data.records.length > 0) {
								chartId = data.records[0].getId();
							}

							me.delegate.onChartDropped(chartId, dashobardId);
						}
						return false;
					}
				}
			};

			this.callParent(arguments);
		},

		// override
		buildTreeStructure: function() {
			var domains = _CMCache.getDashboards();
			var out = [];

			for (var key in domains) {
				out.push(buildNodeConf(domains[key]));
			}

			return out;
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.view.administration.accordion.CMDashboardAccordionDelegate");
			this.delegate = d;
		}
	});

	function buildNodeConf(d) {
		return {
			id: d.get("id"),
			text: d.get("description"),
			leaf: true,
			cmName: "dashboard",
			iconCls: "cmdbuild-tree-dashboard-icon"
		};
	}

})();