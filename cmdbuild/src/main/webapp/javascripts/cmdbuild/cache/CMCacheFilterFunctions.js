(function() {

	Ext.define("CMDBuild.cache.CMCacheFilterFunctions", {

		filterStoreByEntryTypeName: function(entryTypeName) {
			var filterBy = entryTypeName || "";

			filterStore.clearFilter();
			filterStore.filter("entryType", filterBy);
		},

		addFilter: function(store, filter, atFirst) {
			if (Ext.getClassName(filter) == "CMDBuild.model.CMFilterModel") {
				removeFilterIfAlreadyExists(store, filter);

				if (atFirst) {
					store.insert(0, filter);
				} else {
					store.add(filter);
				}
			}
		},

		updateFilter: function(store, filter) {
			if (Ext.getClassName(filter) == "CMDBuild.model.CMFilterModel") {
				var storedFilter = getStoredFilter(store, filter);
				if (storedFilter) {
					store.remove(storedFilter);
					store.add(filter);
				}
			}
		},

		removeFilter: function(store, filter) {
			if (Ext.getClassName(filter) == "CMDBuild.model.CMFilterModel") {
				var storedFilter = getStoredFilter(store, filter);
				if (storedFilter) {
					store.remove(storedFilter);
				}
			}
		},

		setFilterApplied: function(store, filter, applied) {
			var storedFilter = getStoredFilter(store, filter);
			if (storedFilter) {
				storedFilter.setApplied(applied);
			}
		}
	});

	function removeFilterIfAlreadyExists(store, filter) {
		var storedFilter = getStoredFilter(store, filter);
		if (storedFilter) {
			store.remove(storedFilter);
		}
	}

	function getStoredFilter(store, filter) {
		var storedFilter = null;
		if (!store) {
			return storedFilter;
		}

		var recordIndex = store.findBy(function(record) {
			return (filter.getName() == record.getName()) && (filter.dirty == record.dirty);
		});
		if (recordIndex >= 0) {
			storedFilter = store.getAt(recordIndex);
		}

		return storedFilter;
	}

	function getFilterByName(me, name, checkDirty) {
		var s = me.getAvailableFilterStore();
		return s.findRecord("name", name);
	}
})();