(function() {

	Ext.define('CMDBuild.selection.CMCheckboxModel', {
		extend: 'Ext.selection.CheckboxModel',

		
		//override
		bind: function(store, initial) {
			this.checkOnly = true; // important to prevent selection issues

			this.callParent(arguments);
			this.cmInverseSelection = false;
			this.cmSelections = [];

			this.mon(store, "load", this.onStoreLoad, this);
		},

		//override
		getHeaderConfig: function() {
			var header = this.callParent(arguments);
			header.tdCls = "grid-button";
			return header;
		},

		//override
		onHeaderClick: function(headerCt, header, e) {
			if (header.isCheckerHd) {
				e.stopEvent(); // We have to supress the event or it will scrollTo the change
				var isChecked = header.el.hasCls(Ext.baseCSSPrefix + 'grid-hd-checker-on');

				this.cmInverseSelection = !isChecked;
				this.cmSelections = [];
				this.cmSelections[this.cmCurrentPage] = this.buildMixedCollection();

				if (isChecked) {
					this.deselectAll(true);
				} else {
					this.selectAll(true);
				}
			}
		},

		// override
		onRowMouseDown: function(view, record, item, index, e) {
			this.callParent(arguments);

			var checker = e.getTarget('.' + Ext.baseCSSPrefix + 'grid-row-checker');

			if (checker) {
				// do something only if click on the check;
				var pageSelections = this.cmSelections[this.cmCurrentPage];
				var selectedRecord = pageSelections.get(record.get("Id"));
	
				if (typeof selectedRecord == "undefined") {
					pageSelections.add(record);
				} else {
					pageSelections.remove(selectedRecord);
				}
			}
		},

		onStoreLoad: function(store, records) {
			this.cmCurrentPage = store.currentPage;

			if (typeof this.cmSelections[this.cmCurrentPage] == "undefined") {
				this.cmSelections[this.cmCurrentPage] = this.buildMixedCollection();
			}

			if (this.cmInverseSelection) {
				//select all the non cmSected records
				this.selectAll();

				var selections = this.cmSelections[this.cmCurrentPage];
				selections.each(function(s) {
					var r = store.find("Id", s.get("Id"));
					if (r != -1) {
						this.deselect(r);
					}
				}, this);

			} else {

				var selections = this.cmSelections[this.cmCurrentPage];
				selections.each(function(s) {
					var r = store.find("Id", s.get("Id"));
					if (r != -1) {
						this.select(r);
					}
				}, this);

			}
			
			this.toggleUiHeader(this.cmInverseSelection);
		},

		getCmSelections: function() {
			var out = [];

			for (var i=0, l=this.cmSelections.length; i<l; ++i) {
				var s = this.cmSelections[i];
				if (s) {
					s.each(function(item) {
						out.push(item);
					});
				}
			}

			return out;
		},

		//private
		buildMixedCollection: function() {
			return new Ext.util.MixedCollection(false, function(el){
				return el.get("Id");
			});
		},

		//override
		onSelectChange: function() {
			this.callParent(arguments);
			this.toggleUiHeader(this.cmInverseSelection);
		},

		cmDeselectAll: function() {
			this.deselectAll(arguments);

			this.cmSelections = [];
			this.cmSelections[this.cmCurrentPage] = this.buildMixedCollection();
			this.cmInverseSelection = false;
		},

		cmHasSelections: function() {
			return this.getCmSelections().length > 0;
		}
	});
})();