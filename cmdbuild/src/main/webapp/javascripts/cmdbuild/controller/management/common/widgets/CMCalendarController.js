(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMCalendarController", {

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMCalendar.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.reader = new CMDBuild.controller.management.common.widgets.CMCalendarControllerWidgetReader();

			if (!this.reader.getStartDate(this.widgetConf) ||
					!this.reader.getTitle(this.widgetConf)) {

				CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
						CMDBuild.Translation.management.modworkflow.extattrs.calendar.wrong_config);

				this.skipLoading = true;
				return;
			} else {
				this.eventMapping = {
					id: "Id",
					start: this.reader.getStartDate(this.widgetConf),
					end: this.reader.getEndDate(this.widgetConf),
					title: this.reader.getTitle(this.widgetConf)
				};
			}

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.clientForm,
				xaVars: this.widgetConf,
				serverVars: this.getTemplateResolverServerVars()
			});

		},

		// override
		beforeActiveView: function() {
			this.view.clearStore();

			openDefaultDate(this);

			if (this.skipLoading) {
				return;
			}

			var me = this,
				cqlQuery = this.templateResolver.getVariable("xa:" + me.reader.getFilterVarName());

			if (cqlQuery) {
				this.filteredWithCQL = true;
				this.templateResolver.resolveTemplates({
					attributes: [me.reader.getFilterVarName()],
					scope: me.view,
					callback: function(out, ctx) {
						var filterParams = me.templateResolver.buildCQLQueryParameters(cqlQuery, ctx);
						doRequest(me, filterParams);
					}
				});
			} else {
				this.filteredWithCQL = false;
				me.updatePaginationQuery();
				doRequest(me);
			}

			if (!this._alreadyOpene) {
				this.mon(this.view, "eventclick", onEventClick, this);
				this.mon(this.view, "viewchange", onViewChange, this);

				this._alreadyOpened = true;
			}
		},

		updatePaginationQuery: function() {
			function addSingleQuote(s) {
				return "'" + s + "'";
			}

			var me = this,
				viewBounds = this.view.getWievBounds(),
				className = me.reader.getEventClass(me.widgetConf);

			var e_start = me.eventMapping.start,
				e_end = me.eventMapping.end,
				v_start = getCMDBuildDateStringFromDateObject(viewBounds.viewStart),
				v_end = getCMDBuildDateStringFromDateObject(viewBounds.viewEnd);

			var out = "SELECT " +
				me.eventMapping.id + "," +
				me.eventMapping.title + "," +
				e_start + ",";

			if (me.eventMapping.end) {
				out += me.eventMapping.end;

				out += " FROM " + className +
				" WHERE " + e_start + " <= " + addSingleQuote(v_end) +
				" AND " + e_end + " >= " + addSingleQuote(v_start);
			} else {
				// the event has no end so we want
				// only the ones that starts in the temporal window

				out += " FROM " + className +
				" WHERE " + e_start + " >= " + addSingleQuote(v_start) +
				" AND " + e_start + " <= " + addSingleQuote(v_end) + "\"";
			}

			this.paginationQuery = out;
		},

		destroy: function() {
			this.mun(this.view, "eventclick", onEventClick, this);
			this.mun(this.view, "viewchange", onViewChange, this);
		}
	});

	function openDefaultDate(me) {
		var defaultDateAttr = me.reader.getDefaultDate(me.widgetConf);
		if (defaultDateAttr) {
			var defaultDate = me.templateResolver.getVariable("client:" + defaultDateAttr);
			var date = buildDate(defaultDate);
			if (date) {
				me.view.setStartDate(date);
			}
		}
	}

	// my expectation is a string in the form:
	// d/m/Y or d/m/Y H:i:s
	// the Date object accept a string in the format m/d/Y or m/d/Y H:i:s
	// so invert the d with m and return a Date object
	function buildDate(stringDate) {
		if (stringDate) {
			var chunks = stringDate.split(" ");
			var date = chunks[0];
			var time = chunks[1] || "00:00:00";

			return  Ext.Date.parse(date + " " + time, "d/m/Y H:i:s");
		} else {
			return new Date();
		}
	}

	function getCMDBuildDateStringFromDateObject(d) {
		// d.getMonth return the month 0-11
		return d.getDate() + "/" + (d.getMonth() + 1) + "/" + d.getFullYear();
	}

	function doRequest(me, filterParams) {
		var params = filterParams || {};

		if (!filterParams) {
			params.className = me.reader.getEventClass(me.widgetConf);
			params.filter = Ext.encode({
				CQL: me.paginationQuery
			});
		}

		CMDBuild.ServiceProxy.getCardList({
			params: params,
			success: function(response, operation, decodedResponse) {
				me.view.clearStore();
				var _eventData = decodedResponse.rows || [];
				for (var i=0, l=_eventData.length; i<l; ++i) {
					var eventConf = {},
					rawEvent = _eventData[i],
					calMapping = Extensible.calendar.data.EventMappings;

					eventConf[calMapping.EventId.name] = rawEvent[me.eventMapping.id];
					eventConf[calMapping.StartDate.name] = buildDate(rawEvent[me.eventMapping.start]);
					eventConf[calMapping.Title.name] = rawEvent[me.eventMapping.title];

					if (me.eventMapping.end) {
						eventConf[calMapping.EndDate.name] = buildDate(rawEvent[me.eventMapping.end]);
					} else {
						eventConf[calMapping.EndDate.name] = buildDate(rawEvent[me.eventMapping.start]);
					}

					var event = new Extensible.calendar.data.EventModel(eventConf);
					if (event) {
						me.view.addEvent(event);
					}
				}
			}
		});
	}

	function onEventClick(panel, model, el) {
		var me = this,
			target = _CMCache.getEntryTypeByName(me.reader.getEventClass(me.widgetConf));

		if (target) {
			var w = new CMDBuild.view.management.common.CMCardWindow({
				cmEditMode: false,
				withButtons: false,
				title: model.get("Title")
			});

			new CMDBuild.controller.management.common.CMCardWindowController(w, {
				entryType: target.get("id"), // classid of the destination
				card: model.get("EventId"), // id of the card destination
				cmEditMode: false
			});
			w.show();
		}
	}

	function onViewChange() {
		if (this.filteredWithCQL) {
			return;
		} else {
			this.updatePaginationQuery();
			doRequest(this);
		}
	}

})();