(function() {

	Ext.define("CMDBuild.controller.administration.classes.CMModClassController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.buildSubcontrollers();

			this.view.addClassButton.on("click", this.onAddClassButtonClick, this);
			this.view.printSchema.on("click", this.onPrintSchema, this);

			this.registerToCacheEvents();
		},

		//private and overridden in subclasses
		buildSubcontrollers: function() {
			this.subControllers = [
				this.classFormController = new CMDBuild.controller.administration.classes.CMClassFormController(this.view.classForm),
				this.domainTabController = new CMDBuild.controller.administration.classes.CMDomainTabController(this.view.domainGrid),
				this.geoAttributesController = new CMDBuild.controller.administration.classes.CMGeoAttributeController(this.view.geoAttributesPanel),
				this.attributePanelController = new CMDBuild.controller.administration.classes.CMClassAttributeController(this.view.attributesPanel),
				this.widgetDefinitionController = new CMDBuild.controller.administration.widget.CMWidgetDefinitionController(this.view.widgetPanel)
			];
			var me = this;
			this.subControllers.relay = function(fn) {
				Ext.Array.each(me.subControllers, fn);
			}
		},

		//private and overridden in subclasses
		registerToCacheEvents: function() {
			_CMCache.on("cm_class_deleted", this.view.onClassDeleted, this.view);
		},

		//private and overridden in subclasses
		onViewOnFront: function(selection) {
			if (selection) {
				this.view.onClassSelected(selection.data);
				this.subControllers.relay(function(subcontroller, index, subcontrollers) {
					subcontroller.onClassSelected(selection.data.id);
				});
			}
		},

		onPrintSchema: function(format) {
			if (typeof format != "string") { return; }
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				url : 'services/json/schema/modreport/printschema',
				params: {format: format},
				method : 'POST',
				success: function(response) {
					CMDBuild.LoadMask.get().hide();
					var popup = window.open(
						"services/json/management/modreport/printreportfactory",
						"Report",
						"height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable"
					);

					if (!popup) {
						CMDBuild.Msg.warn(CMDBuild.Translation.warnings.warning_message,CMDBuild.Translation.warnings.popup_block);
					}
				},
				failure: function(response) {
					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		onAddClassButtonClick: function () {
			this.subControllers.relay(function(subcontroller, index, subcontrollers) {
				subcontroller.onAddClassButtonClick();
			});

			this.view.onAddClassButtonClick();
			_CMMainViewportController.deselectAccordionByName("class");

		}
	});
})();