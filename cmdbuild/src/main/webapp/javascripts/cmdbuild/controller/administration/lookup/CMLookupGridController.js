(function() {
	var	LOOKUP_FIELDS = CMDBuild.ServiceProxy.LOOKUP_FIELDS,
		UP = 1,
		DOWN = -1;

	Ext.define("CMDBuild.controller.administration.lookup.CMLookupGridController", {
		constructor: function(view) {
			this.view = view;
			this.sm = this.view.getSelectionModel();
			this.subController = null;
			
			this.view.addButton.on("click", function() {
				this.sm.deselect(this.sm.getSelection());
				notifySubController.call(this, "onAddLookupClick");
			}, this);

			this.sm.on("selectionchange", this._onSelectionChanged, this);
			this.view.mon(this.view, "cm_lookup_moved", this.onLookupMoved, this);
		},

		bindSubController: function(c) {
			this.subController = c;
		},

		onLookupDisabled: function(lookupIdToSelectAfterLoad) {
			this.view.loadData(lookupIdToSelectAfterLoad);
		},

		onLookupSaved: function(lookupIdToSelectAfterLoad) {
			this.view.loadData(lookupIdToSelectAfterLoad);
		},

		_onSelectionChanged: function(sm, selection) {
			if (selection.length > 0) {
				var s = selection[0];
				notifySubController.call(this, "onSelectLookupGrid", s);
			}
		},

		onSelectLookupType: function(lookupType) {
			this.lookupType = lookupType;
			this.view.onSelectLookupType(lookupType);
		},

		onAddLookupTypeClick: function() {},

		onLookupMoved: function(p) {
			var movedRecord = p.data.records[0],
				pivotRecord = p.dropRec,
				dropPosition = p.dropPosition,
				direction, stop, start, recordsToOrder;

			var pivotRecordPosition = parseInt(pivotRecord.get(LOOKUP_FIELDS.Index));
			var movedRecordPosition = parseInt(movedRecord.get(LOOKUP_FIELDS.Index));

			if (movedRecordPosition == pivotRecordPosition) {
				return
			} else if (movedRecordPosition > pivotRecordPosition) {
				direction = UP;
			} else {
				direction = DOWN;
			}

			if (direction == UP) {
				start = dropPosition == "after" ? pivotRecordPosition + 1 : pivotRecordPosition;
				stop = movedRecordPosition - 1;
				recordsToOrder = [{
					id: movedRecord.get(LOOKUP_FIELDS.Id),
					index: start,
					description: movedRecord.get(LOOKUP_FIELDS.Description)
				}];
			} else {
				start = movedRecordPosition + 1;
				stop = dropPosition == "before" ? pivotRecordPosition - 1 : pivotRecordPosition;
				recordsToOrder = [{
					id: movedRecord.get(LOOKUP_FIELDS.Id),
					index: stop,
					description: movedRecord.get(LOOKUP_FIELDS.Description)
				}];
			}

			_debug(start, stop);

			for (var i = start; i<=stop; ++i) {
				var r = this.view.store.findRecord(LOOKUP_FIELDS.Index, i);
				if (r) {
					_debug(r.get(LOOKUP_FIELDS.Description) + " prima " + r.get("Number") + " dopo " + (i+direction));
					recordsToOrder.push({
						id: r.get(LOOKUP_FIELDS.Id),
						index: i + direction,
						description: r.get(LOOKUP_FIELDS.Description)
					});
				}
			}
			
			_debug(recordsToOrder);

			savePosition.call(this, recordsToOrder);
		}
	});

	function notifySubController(event, params) {
		this.subController[event](params);
	}

	function savePosition(rowList) {
		var me = this;
		CMDBuild.Ajax.request({
			url: 'services/json/schema/modlookup/reorderlookup',
			method: 'POST',
			params: {
				type: me.lookupType.id,
				lookuplist: Ext.JSON.encode(rowList)
			},
			callback: function(response,options) {
				me.view.store.load();
			}
		});
	}
})();