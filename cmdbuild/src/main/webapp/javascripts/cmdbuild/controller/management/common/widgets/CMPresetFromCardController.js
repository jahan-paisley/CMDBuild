(function() {
	var FILTER = "_filter";

	Ext.define("CMDBuild.controller.management.common.widgets.CMPresetFromCardController", {

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController",
			cmPresetFromCardDelegate: "CMDBuild.view.management.common.widgets.CMPresetFromCardDelegate"
		},

		statics: {
			WIDGET_NAME: ".PresetFromCard"
		},

		constructor: function(view, supercontroller, widget, clientForm, card) {

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			view.addDelegate(this);

			this.callBacks = {
				"action-card-show": this.onShowCardkClick
			};

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: _extractVariablesForTemplateResolver(widget),
				serverVars: this.getTemplateResolverServerVars()
			});

			this.mon(this.view.grid, 'beforeitemclick', cellclickHandler, this);
		},

		onShowCardkClick: function(model) {
			var w = new CMDBuild.view.management.common.CMCardWindow({
				cmEditMode: false,
				withButtons: false,
				title: model.get("IdClass_value")
			});

			new CMDBuild.controller.management.common.CMCardWindowController(w, {
				entryType: model.get("IdClass"),
				card: model.get("Id"),
				cmEditMode: false
			});

			w.show();
		},

		// override
		beforeActiveView: function() {
			resolveFilterTemplate(this);
		},

		// as cmPresetFromCardDelegate
		onPresetFromCardSaveButtonClick: function(widget) {
			var selectedCard = widget.getSelection();
			var fields = this.clientForm.getFields();
			var mapping = this.widgetConf.presetMapping;

			if (selectedCard 
				&& fields
				&& mapping) {

				fields.each(function(field) {
					if (field._belongToEditableSubpanel 
						&& mapping[field.name]) {
							field.setValue(selectedCard.get(mapping[field.name]));
						}
				});
			}

			// to dispose the widget window
			if (typeof this.ownerController.hideWidgetsContainer == "function") {
				this.ownerController.hideWidgetsContainer();
			}

		}
	});

	// called with scope this
	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className; 

		if (this.callBacks[className]) {
			this.callBacks[className](model);
		}
	}

	function _extractVariablesForTemplateResolver(widget) {
		var out = widget.templates || {};
		out[FILTER] = widget.filter;

		return out;
	}

	function resolveFilterTemplate(me) {
		var widget = me.widgetConf;
		var entryType = _CMCache.getEntryTypeByName(widget.className);
		if (!entryType) {
			// TODO say to the user that something
			// went wrong

			return;
		}

		var classId = entryType.getId();
		var cqlQuery = widget.filter;
		me.templateResolver.resolveTemplates({
			attributes: [FILTER],
			callback: function(out, ctx) {
				var cardReqParams = me.templateResolver.buildCQLQueryParameters(cqlQuery, ctx);
				me.view.updateGrid(classId, cardReqParams);
			}
		});
	}
})();