(function() {
	/*
	 * The grid must be reload when is shown, so resolve the template and load it
	 * 
	 * If there is a defaultSelection, when the activity form goes in edit mode resolve
	 * the template to calculate the selection and if needed add dependencies to the fields
	 */

	var FILTER = "filter",
		DEFAULT_SELECTION = "defaultSelection",
		TABLE_VIEW_NAME = "table",
		STARTING_VIEW = TABLE_VIEW_NAME,
		widgetReader = CMDBuild.management.model.widget.LinkCardsConfigurationReader;

	Ext.define("CMDBuild.controller.management.common.widgets.CMLinkCardsController", {

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: ".LinkCards"
		},

		constructor: function(view, supercontroller, widget, clientForm, card) {

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.targetEntryType = _CMCache.getEntryTypeByName(widgetReader.className(this.widgetConf));
			this.currentView = STARTING_VIEW;
			this.templateResolverIsBusy = false; // is busy when load the default selection
			this.alertIfChangeDefaultSelection = false;
			this.singleSelect = widgetReader.singleSelect(this.widgetConf);
			this.readOnly = widgetReader.readOnly(this.widgetConf);


			this.callBacks = {
				'action-card-edit': this.onEditCardkClick,
				"action-card-show": this.onShowCardkClick
			};

			this.model = new CMDBuild.Management.LinkCardsModel({
				singleSelect: this.singleSelect
			});
			this.view.setModel(this.model);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: _extractVariablesForTemplateResolver(widget),
				serverVars: this.getTemplateResolverServerVars()
			});

			this.mon(this.view.grid, 'beforeitemclick', cellclickHandler, this);
			this.mon(this.view.grid, 'itemdblclick', onItemDoubleclick, this);
			this.mon(this.view, "select", onSelect, this);
			this.mon(this.view, "deselect", onDeselect, this);

			if (this.view.hasMap()) {
				listenToggleMapEvents(this);
				buildMapController(this);
			}
		},

		// override
		beforeActiveView: function() {
			if (this.targetEntryType == null) {
				return;
			}

			var classId = this.targetEntryType.getId(),
				cqlQuery = widgetReader.filter(this.widgetConf);

			loadTheGridAsSoonAsPossible(this, cqlQuery, classId);
		},

		// override
		onEditMode: function() {
			// for the auto-select
			resolveDefaultSelectionTemplate(this);
		},

		// override
		isBusy: function() {
			return this.templateResolverIsBusy;
		},

		// override
		getData: function() {
			var out = null;
			if (!this.readOnly) {
				out = {};
				out["output"] = convertElementsFromStringToInt(this.model.getSelections());
			}

			return out;
		},

		// override
		isValid: function() {
			if (!this.readOnly &&
					widgetReader.required(this.widgetConf)) {
				return this.model.hasSelection();
			} else {
				return true;
			}
		},

		syncSelections: function() {
			this.model._silent = true;
			this.view.syncSelections();
			this.model._silent = false;
		},

		onEditCardkClick: function(model) {
			var editable = true,
				w = getCardWindow(model, editable);

			w.on("destroy", function() {
				this.view.grid.reload();
			}, this, {single: true});

			w.show();
		},

		onShowCardkClick: function(model) {
			var editable = false,
				w = getCardWindow(model, editable);

			w.show();
		},

		getLabel: function() {
			return widgetReader.label(this.widgetConf);
		}
	});

	// when the linkCard is not busy load the grid
	function loadTheGridAsSoonAsPossible(me, cqlQuery, classId) {
		new _CMUtils.PollingFunction({
			success: function() {
				me.alertIfChangeDefaultSelection = true;

				// CQL filter and regular filter cannot be merged now.
				// The filter button should be enabled only if no other filter is present.
				if (cqlQuery) {
					resolveFilterTemplate(me, cqlQuery, classId);
					me.view.grid.disableFilterMenuButton();
				} else {
					me.view.updateGrid(classId);
					me.view.grid.enableFilterMenuButton();
				}
			},
			failure: function failure() {
				CMDBuild.Msg.error(null,CMDBuild.Translation.errors.busy_wf_widgets, false);
			},
			checkFn: function() {
				// I want exit if I'm not busy
				return !me.isBusy();
			},
			cbScope: me,
			checkFnScope: me
		}).run();
	}

	function listenToggleMapEvents(me) {
		me.mon(me.view, "CM_toggle_map", function() {
			var v = me.view;
			if (v.grid.isVisible()) {
				v.showMap();
				v.mapButton.setIconCls("table");
				v.mapButton.setText(CMDBuild.Translation.management.modcard.add_relations_window.list_tab);
				if (me.mapController) {
					me.mapController.centerMapOnSelection();
				}
			} else {
				v.showGrid();
				v.mapButton.setIconCls("map");
				v.mapButton.setText(CMDBuild.Translation.management.modcard.tabs.map);
				loadPageForLastSelection.call(me, me.mapController.getLastSelection());
			}
		}, me);
	}

	function buildMapController(me) {
		me.mapController = new CMDBuild.controller.management.workflow.widgets.CMLinkCardsMapController({
			view: me.view.mapPanel, 
			ownerController: me,
			model: me.model,
			widgetConf: me.widgetConf
		});
	}

	function getCardWindow(model, editable) {
		var w = new CMDBuild.view.management.common.CMCardWindow({
			cmEditMode: editable,
			withButtons: editable,
			title: model.get("IdClass_value")
		});

		new CMDBuild.controller.management.common.CMCardWindowController(w, {
			entryType: model.get("IdClass"),
			card: model.get("Id"),
			cmEditMode: editable
		});

		return w;
	}

	// used only on toggle the map
	function loadPageForLastSelection(selection) {
		if (selection != null) {
			var params = {
				"retryWithoutFilter": true,
				IdClass: this.widgetConf.ClassId, // FIXME there is no classid
				Id: selection
			}, 
			me = this, 
			grid = this.view.grid;

			me.model._silent = true;

			CMDBuild.ServiceProxy.card.getPosition({
				params: params,
				success: function onGetPositionSuccess(response, options, resText) {
					var position = resText.position,
						found = position >= 0;
	
					if (found) {
						var	pageNumber = grid.getPageNumber(position);
						grid.loadPage(pageNumber, {
							scope: me,
							cb: function() {
								me.model._silent = false;
							}
						});
					}
				}
			});

		} else {
			this.syncSelections();
		}
	}

	function onSelect(cardId) {
		this.model.select(cardId);
	}

	function onDeselect(cardId) {
		this.model.deselect(cardId);
	}


	function alertIfNeeded(me) {
		if (me.alertIfChangeDefaultSelection) {
			CMDBuild.Msg.warn(null, Ext.String.format(CMDBuild.Translation.warnings.link_cards_changed_values
					, widgetReader.label(me.widgetConf) || me.view.id)
					, popup=false);

			me.alertIfChangeDefaultSelection = false;
		}
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var className = event.target.className; 

		if (this.callBacks[className]) {
			this.callBacks[className].call(this, model);
		}
	}

	function onItemDoubleclick(grid, model, html, index, e, options) {
		if (! widgetReader.allowCardEditing(this.widgetConf)) {
			return;
		}

		var priv = _CMUtils.getClassPrivileges(model.get("IdClass"));
		if (priv && priv.write) {
			this.onEditCardkClick(model);
		} else {
			this.onShowCardkClick(model);
		}
	}

	function _extractVariablesForTemplateResolver(widget) {
		var variables = {};
		variables[DEFAULT_SELECTION] = widgetReader.defaultSelection(widget);
		variables[FILTER] = widgetReader.filter(widget);

		Ext.apply(variables, widgetReader.templates(widget));
		_debug("LinkCards template resolver init with:", variables);

		return variables;
	}

	function resolveFilterTemplate(me, cqlQuery, classId) {
		me.templateResolver.resolveTemplates({
			attributes: [FILTER],
			callback: function(out, ctx) {
				var cardReqParams = me.templateResolver.buildCQLQueryParameters(cqlQuery, ctx);
				me.view.updateGrid(classId, cardReqParams);

				me.templateResolver.bindLocalDepsChange(function() {
					me.view.reset();
				});
			}
		});
	}

	function resolveDefaultSelectionTemplate(me) {
		me.templateResolverIsBusy = true;
		me.view.reset();
		alertIfNeeded(me);

		me.templateResolver.resolveTemplates({
			attributes: [DEFAULT_SELECTION],
			callback: function onDefaultSelectionTemplateResolved(out, ctx) {
				var defaultSelection = me.templateResolver.buildCQLQueryParameters(out[DEFAULT_SELECTION], ctx);
				// do the request only if there are a default selection
				if (defaultSelection) {
					CMDBuild.ServiceProxy.getCardList({
						params: defaultSelection,
						callback: function callback(request, options, response) {
							var resp = Ext.JSON.decode(response.responseText);

							if (resp.rows) {
								for ( var i = 0, l = resp.rows.length; i < l; i++) {
									var r = resp.rows[i];
									me.model.select(r.Id);
								}
							}
			
							me.templateResolverIsBusy = false;
						}
					});

					me.templateResolver.bindLocalDepsChange(function() {
						resolveDefaultSelectionTemplate(me);
					});

				} else {
					me.templateResolverIsBusy = false;
				}
			}
		});
	}

	/*
	 * Local solution for a global issue.
	 * The card model is a CMDBuild.Dummymodel,
	 * it takes a map and set all the key as fields of the model,
	 * so there are no type specification.
	 * Server side I want that the Ids are integer, so now
	 * cast it in this function, but the real solution is to
	 * find a way to say to the card that its id is a number.
	 */
	function convertElementsFromStringToInt(input) {
		input = input || [];
		var output = [];
		for (var i=0, l=input.length, element=null; i<l; ++i) {
			element = parseInt(input[i]);
			if (element) {
				output.push(element);
			}
		}

		return output;
	}
})();
