(function() {
	var SINGLE_MODE = "SINGLE";
	/**
	 * The model of the records must implement a getId method
	 * in order to identify the data, not the ext record
	 * Is enough to set the idProperty in the model definition
	 **/
	Ext.define('CMDBuild.selection.CMMultiPageSelectionModel', {
		extend: 'Ext.selection.CheckboxModel',
		alias: 'selection.cmmultipage',
		idProperty: undefined, // passed in configuration and used if not defined in model
		avoidCheckerHeader: false, // to avoid the rendering of the check in the header

		// override
		bindStore: function(store, initial) {
			this.store = store;
			// this.checkOnly = true; // important to prevent selection issues
			this.cmReverse = false;
			this.reset();
			this.cmCurrentPage = undefined;

			this.callParent(arguments);

			if (this.store) {
				this.mon(this.store, "beforeload", function() { this._onBeforeStoreLoad.apply(this, arguments); }, this);
				this.mon(this.store, "load", function() { this._onStoreDidLoad.apply(this, arguments); }, this);
			}
			this.mon(this, "select", function() { this._addSelection.apply(this, arguments); }, this);
			this.mon(this, "deselect", function() { this._removeSelection.apply(this, arguments); }, this);
		},

		_addSelection: function(sm, record) {
			var id = getId(record, this.idProperty);

			if (this.mode == SINGLE_MODE) {
				this.reset();
				callOnRowDeselectForAllThePageEventuallySkipTheGivenRecord(this, this.views, record);
			}

			if (this.cmReverse) {
				if (id && this.cmSelections.hasOwnProperty(id)) {
					delete this.cmSelections[id];
				}
			} else {
				if (id && !this.cmSelections.hasOwnProperty(id)) {
					this.cmSelections[id] = record.copy();
				}
			}
		},

		_removeSelection: function(sm, record) {
			var id = getId(record, this.idProperty);
			if (!this.cmReverse) {
				if (!this.cmFreezedSelections && typeof id != "undefined") {
					delete this.cmSelections[id];
				}
			} else {
				if (id && !this.cmSelections.hasOwnProperty(id)) {
					this.cmSelections[id] = record.copy();
				}
			}
		},

		reset: function() {
			try {
				this.clearSelections();
				this.cmSelections = {};
				this.cmFreezedSelections = undefined;
			} catch (e) {
				// there could be problems if the view is destroyed before
			}
		},

		_onBeforeStoreLoad: function() {
			this.cmFreezedSelections = Ext.clone(this.cmSelections);
		},

		// override
		hasSelection: function() {
			return this.getSelection().length > 0;
		},

		// override
		getCount: function() {
			return this.getSelection().length;
		},

		/**
		 * return the selection if the checkHeader is not
		 * checked, otherwise return the unchecked rows
		 * */
		// override
		getSelection: function() {
			var out = [];
			for (var k in this.cmSelections) {
				out.push(this.cmSelections[k]);
			}

			return out;
		},

		//override
		getHeaderConfig: function() {
			var header = this.callParent(arguments);

			if (this.mode == SINGLE_MODE || this.avoidCheckerHeader) {
				header.isCheckerHd = false;
				header.cls = Ext.baseCSSPrefix + + 'column-header';
			}
			return header;
		},

		//override
		onHeaderClick: function(headerCt, header, e) {
			if (this.mode == SINGLE_MODE) {
				return;
			}

			if (header.isCheckerHd) {
				e.stopEvent();
				this.cmReverse = !header.el.hasCls(Ext.baseCSSPrefix + 'grid-hd-checker-on');
				this.toggleUiHeader(this.cmReverse);

				this.reset();
				this._redoSelection();
			}
		},

		// private
		_onStoreDidLoad: function(store, records) {
			this.cmCurrentPage = store.currentPage;
			if (this.cmFreezedSelections) {
				this.cmSelections = Ext.clone(this.cmFreezedSelections);
				this.cmFreezedSelections = undefined;
			}

			this._redoSelection();
		},

		_redoSelection: function() {
			var me = this,
				views = me.views;

			callOnRowDeselectForAllThePageEventuallySkipTheGivenRecord(me, views);

			if (this.cmReverse) {
				doReverseSelection(me, views);
			} else {
				doSelection(me, views);
			}
		},

		//override
		onSelectChange: function() {
			// bypass the override of the Ext.selection.CheckboxModel that sync the selections
			// with the header status
			Ext.selection.RowModel.prototype.onSelectChange.apply(this, arguments);
		}

	});

	function getId(record, idProperty) {
		var id = undefined;
		if (record && typeof record.getId == "function") {
			id = record.getId();
		}

		if (record && typeof id == "undefined" && typeof idProperty == "string") {
			id = record.get(idProperty);
		}

		return id;
	}

	function callOnRowDeselectForAllThePageEventuallySkipTheGivenRecord(me, views, recordToSkip) {
		var viewsLn = views.length,
			index = 0,
			idOfRecordToSkip = getId(recordToSkip, me.idProperty);

		// if the grid is in a window this method is call after the destroy an the store
		// was already destroyed, so check if exists
		if (me.store) {
			me.store.each(function(recordInThePage) {
				for (var i=0; i < viewsLn; i++) {
					if (typeof recordToSkip == "undefined") {
						views[i].onRowDeselect(index, suppressEvent=true);
					} else if (idOfRecordToSkip 
							&& idOfRecordToSkip != getId(recordInThePage, me.idProperty)) {
	
						views[i].onRowDeselect(index, suppressEvent=true);
					}
				}
				index++;
			});
		}
	}

	function doSelection(me, views) {
		// if the grid is in a window this method is call after the destroy an the store
		// was already destroyed, so check if exists
		if (!me.store) {
			return;
		}

		var recordIndex;
		var viewsLn = views.length;

		for (var currentId in me.cmSelections) {
			recordIndex = me.store.findBy(function(record) {
				if (currentId == getId(record, me.idProperty)) {
					me.selected.add(record); // to sync with the real selection
					return true;
				}
			});

			if (recordIndex != -1) {
				for (var i=0; i < viewsLn; i++) {
					views[i].onRowSelect(recordIndex, suppressEvent=true);
				}
			}
		}
	}

	function doReverseSelection(me, views) {
		var index = 0;
		var viewsLn = views.length;

		me.store.each(function(recordInThePage) {
			if (!me.cmSelections[getId(recordInThePage, me.idProperty)]) {
				me.selected.add(recordInThePage); // to sync with the real selection
				for (var i=0; i < viewsLn; i++) {
					views[i].onRowSelect(index, suppressEvent=true);
				}
			}
			index++;
		});
	}
})();