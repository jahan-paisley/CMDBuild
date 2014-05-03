(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMPingController", {
		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMPing.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);
		},

		// override
		beforeActiveView: function() {
			var me = this;
			me.view.removeAll();

			if (!me.templateResolver) {
				var xaVars = me.widgetConf.templates || {};
				xaVars["_address"] = me.widgetConf.address;

				me.templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: xaVars,
					serverVars: this.getTemplateResolverServerVars()
				});
			}

			resolveTemplate(me);
		},

		destroy: function() {
			this.callParent(arguments);
		}
	});

	function resolveTemplate(me) {
		me.templateResolver.resolveTemplates({
			attributes: ["_address"],
			callback: function(o) {

				var pingParams = {
					className: _CMCache.getEntryTypeNameById(me.card.get("IdClass")),
					id: me.card.get("Id"),
					widgetId: me.widgetConf.id,
					action: "legacytr",
					params: Ext.encode({
						address: o._address
					})
				};

				var el = me.view.getEl();
				if (el) {
					el.mask(CMDBuild.Translation.common.wait_title);
				}

				CMDBuild.Ajax.request({
					url: "services/json/modwidget/callwidget",
					method: "GET",
					params: pingParams,
					success: function(request, action, response) {
						me.view.showPingResult(response.response);
					},
					callback: function() {
						if (el) {
							el.unmask();
						}
					}
				});
			}
		});
	}
})();
