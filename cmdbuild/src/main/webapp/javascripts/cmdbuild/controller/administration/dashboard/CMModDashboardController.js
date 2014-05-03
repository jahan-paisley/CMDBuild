(function() {

	Ext.define("CMDBuild.controller.administration.dashboard.CMModDashboardController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		mixins: {
			modDashboardDelegate: "CMDBuild.view.administration.dashboard.CMModDashboardDelegate",
			chartPanelDelegate: "CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelControllerDelegate" 
		},

		statics: {
			cmcreate: function(view) {
				var propertiesPanelController = Ext.createByAlias('controller.cmdashboardproperties', view.getPropertiesPanel());
				var chartsConfigurationController = CMDBuild.controller.administration.dashboard.CMDashboardChartConfigurationPanelController.cmcreate(view.getChartsConfigurationPanel());
				var layoutConfigurationController = Ext.createByAlias('controller.cmdashboardlayoutconf', view.getLayoutConfigurationPanel());
				return new CMDBuild.controller.administration.dashboard.CMModDashboardController(view, propertiesPanelController, chartsConfigurationController, layoutConfigurationController);
			}
		},

		constructor: function(view, propertiesPanelController, chartsConfigurationController, layoutConfigurationController) {
			this.callParent(arguments);
			this.dashboard = null;

			this.subcontrollers = [
				this.propertiesPanelController = propertiesPanelController,
				this.chartsConfigurationController = chartsConfigurationController,
				this.layoutConfigurationController = layoutConfigurationController
			];

			this.view.setDelegate(this);
			this.chartsConfigurationController.setDelegate(this);
		},

		onViewOnFront: function(relatedTreeNode) {
			var title = null;
			this.dashboard = null;

			if (relatedTreeNode) {
				var id = relatedTreeNode.get("id");
				this.dashboard = _CMCache.getDashboardById(id);
			}

			if (this.dashboard) {
				this.view.setTitleSuffix(this.dashboard.get("description"));
				this.callMethodForAllSubcontrollers("dashboardWasSelected", [this.dashboard]);
			}
		},

		// view delegate
		onAddButtonClick: function() {
			this.callMethodForAllSubcontrollers("prepareForAdd", [this.dashboard]);
			this.view.activateFirstTab();
			_CMMainViewportController.deselectAccordionByName(this.view.cmName);
		},

		// chartPanelDelegate
		dashboardChartAreChanged: function() {
			if (this.dashboard) {
				this.layoutConfigurationController.dashboardWasSelected(this.dashboard);
			}
		}
	});

})();