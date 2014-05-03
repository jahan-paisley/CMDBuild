(function() {
	Ext.define("CMDBuild.controller.management.common.CMWidgetManagerController", {

		constructor: function(view) {
			this.view = view;
			this.controllers = {};

			initBuilders(this);
		},

		setDelegate: function(delegate) {
			this.delegate = delegate;
		},

		buildControllers: function(card) {
			var me = this;
			me.removeAll();
	
			if (card) {
				var definitions = me.takeWidgetFromCard(card);
				for (var i=0, l=definitions.length, w=null, ui=null; i<l; ++i) {
					w = definitions[i];
					ui = me.view.buildWidget(w, card);

					if (ui) {
						var wc = me.buildWidgetController(ui, w, card);
						if (wc) {
							me.controllers[me.getWidgetId(w)] = wc;
						}
					}
				}
			}
		},
	
		onWidgetButtonClick: function(w) {
			this.delegate.ensureEditPanel();
			var me = this;
			Ext.defer(function() {
				var wc = me.controllers[me.getWidgetId(w)];
				if (wc) {
					me.view.showWidget(wc.view, me.getWidgetLable(w));
					wc.beforeActiveView();
				}
			}, 1);
		},
	
		onCardGoesInEdit: function() {
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
				if (typeof wc.onEditMode == "function") {
					wc.onEditMode();
				}
			}
		},
	
		getWrongWFAsHTML: function getWrongWFAsHTML() {
			var out = "<ul>",
				valid = true;
	
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
				if (!wc.isValid()) {
					valid = false;
					out += "<li>" + wc.getLabel() + "</li>";
				}
			}
			out + "</ul>";
	
			if (valid) {
				return null;
			} else {
				return out;
			}
		},
	
		removeAll: function clearWidgetControllers() {
			this.view.reset();
			for (var wcId in this.controllers) {
				var wc = this.controllers[wcId];
				wc.destroy();
				delete this.controllers[wcId];
				delete wc;
			}
		},
	
		areThereBusyWidget: function areThereBusyWidget() {
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
				if (wc.isBusy()) {
					return true;
				} else {
					continue;
				}
			}
	
			return false;
		},

		waitForBusyWidgets: function waitForBusyWidgets(cb, cbScope) {
			var me = this;
	
			new _CMUtils.PollingFunction({
				success: cb,
				failure: function failure() {
					CMDBuild.Msg.error(null,CMDBuild.Translation.errors.busy_wf_widgets, false);
				},
				checkFn: function() {
					// I want exit if there are no busy wc
					return !me.areThereBusyWidget();
				},
				cbScope: cbScope,
				checkFnScope: this
			}).run();
		},
	
		getData: function(advance) {
			var ww = {};
			for (var wc in this.controllers) {
				wc = this.controllers[wc];
	
				if (typeof wc.getData == "function") {
					var wcData = wc.getData(advance);
					if (wcData != null) {
						ww[wc.getWidgetId()] = wcData;
					}
				}
			}
	
			return ww;
		},
	
		hideWidgetsContainer: function() {
			this.view.widgetsContainer.hide();
		},
	
		buildWidgetController: function buildWidgetController(ui, widgetDef, card) {
			var me = this,
				controllerClass = me.controllerClasses[widgetDef.type];

			if (controllerClass && typeof controllerClass == "function") {
				return new controllerClass(
					ui,
					superController = me,
					widgetDef,
					clientForm = me.view.getFormForTemplateResolver(),
					card
				);
			} else {
				return null;
			}
		},

		hideWidgetsContainer: function() {
			this.view.hideWidgetsContainer();
		},
	
		takeWidgetFromCard: function(card) {
			var widgets = [];
			if (Ext.getClassName(card) == "CMDBuild.model.CMActivityInstance") {
				widgets = card.getWidgets();
			} else {
				var et = _CMCache.getEntryTypeById(card.get("IdClass"));
				if (et) {
					widgets = et.getWidgets();
				}
			}

			return widgets;
		},
	
		getWidgetId: function(widget) {
			return widget.id;
		},

		getWidgetLable: function(widget) {
			return widget.label;
		},

		activateFirstTab: function() {
			this.view.activateFirstTab();
		}
	});

	function initBuilders(me) {
		var commonControllers = CMDBuild.controller.management.common.widgets;
		me.controllerClasses = {};

		function addControllerClass(controller) {
			me.controllerClasses[controller.WIDGET_NAME] = controller;
		}

		// openNote
		addControllerClass(commonControllers.CMOpenNoteController);

		// openAttachment
		addControllerClass(commonControllers.CMOpenAttachmentController);

		// createModifyCard
		addControllerClass(commonControllers.CMCreateModifyCardController);

		// calendar
		addControllerClass(commonControllers.CMCalendarController);

		// workflow
		addControllerClass(commonControllers.CMWorkflowController);

		// navigationTree
		addControllerClass(commonControllers.CMNavigationTreeController);

		// grid
		addControllerClass(commonControllers.CMGridController);

		// openReport
		addControllerClass(commonControllers.CMOpenReportController);

		// linkCards
		addControllerClass(commonControllers.CMLinkCardsController);

		// manageRelation
		addControllerClass(commonControllers.CMManageRelationController);

		// manageRelation
		addControllerClass(commonControllers.CMManageEmailController);

		// ping
		addControllerClass(commonControllers.CMPingController);

		// webService
		addControllerClass(commonControllers.CMWebServiceController);

		// presetFromCard
		addControllerClass(commonControllers.CMPresetFromCardController);
	}
	Ext.define("CMDBuild.controller.management.common.CMWidgetManagerControllerPopup", {
		extend: "CMDBuild.controller.management.common.CMWidgetManagerController",
		buildControllers: function(widgets) {
			var me = this;
			me.removeAll();
	
			for (var w in widgets) {
				ui = me.view.buildWidget(widgets[w], undefined);

				if (ui) {
					var wc = me.buildWidgetController(ui, widgets[w], undefined);
					if (wc) {
						me.controllers[me.getWidgetId(widgets[w])] = wc;
					}
				}
			}
		}
	});
})();