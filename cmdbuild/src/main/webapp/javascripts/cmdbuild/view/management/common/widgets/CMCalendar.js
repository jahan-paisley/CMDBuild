(function() {
	Ext.override(Extensible.calendar.view.AbstractCalendar, {
		onClick : function(e, t) {
			// 				I don't want to return if is read only
			//				if (this.readOnly === true) {
			//					return true;
			//				}

			if(this.dropZone) {
				this.dropZone.clearShims();
			}
			if(this.menuActive === true) {
				// ignore the first click if a context menu is active (let it
				// close)
				this.menuActive = false;
				return true;
			}
			var el = e.getTarget(this.eventSelector, 5);
			if(el) {
				var id = this.getEventIdFromEl(el), rec = this.getEventRecord(id);

				// 				I want only the event, not the event editor
				if(this.fireEvent('eventclick', this, rec, el) !== false) {
					//					this.showEventEditor(rec, el);
				}
				return true;
			}
		}
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMCalendar", {
		extend : "Ext.panel.Panel",
		withButtons : true,

		statics : {
			WIDGET_NAME : ".Calendar"
		},

		constructor : function() {
			this.eventStore = new Extensible.calendar.data.MemoryEventStore({
				data : []
			});

			this.calendar = new Extensible.calendar.CalendarPanel({
				eventStore : this.eventStore,
				hideMode: "offsets",
				region : "center",
				frame : false,
				border : false,

				showTodayText : true,
				readOnly : true,
				showNavToday : false
			});

			this.addEvents("eventclick");
			this.addEvents("viewchange");

			this.relayEvents(this.calendar, ["eventclick", "viewchange"]);

			Ext.apply(this, {
				frame : false,
				border : false,
				items : [this.calendar],
				layout : "border",
				cls : "x-panel-body-default-framed"
			});

			this.callParent(arguments);
		},

		addEvent : function(event) {
			this.eventStore.add(event);
		},

		clearStore : function() {
			this.eventStore.removeAll();
		},

		getWievBounds : function() {
			var info,
				view = getCurrentView(this);

			if (view) {
				if (view.getViewBounds) {
					var vb = view.getViewBounds();
					info = {
						activeDate : view.getStartDate(),
						viewStart : vb.start,
						viewEnd : vb.end
					};
				};
			}

			return info;
		},

		setStartDate: function(date) {
			this.calendar.setStartDate(date);
		}
	});

	function getCurrentView(me) {
		var v = null;
		if(me.calendar.layout
			&& me.calendar.layout.getActiveItem) {

			v = me.calendar.layout.getActiveItem();
		}
		return v;
	}
})();
