(function() {

	var url = _CMProxy.url.filter;
	var GET = "GET";
	var POST = "POST";

	CMDBuild.ServiceProxy.Filter = {

		create: function(filter, config) {
			var fullParams = true;
			doRequest(filter, config, url.create, POST, fullParams);
		},

		update: function(filter, config) {
			var fullParams = true;
			doRequest(filter, config, url.update, POST, fullParams);
		},

		remove: function(filter, config) {
			var fullParams = false;
			doRequest(filter, config, url.remove, POST, fullParams);
		},

		position: function(filter, config) {
			var fullParams = false;
			doRequest(filter, config, url.position, GET, fullParams);
		},

		/**
		 * Returns a store with the filters
		 * for a given group
		 */
		newGroupStore: function(groupId) {
			return new Ext.data.Store({
				model: "CMDBuild.model.CMFilterModel",
				pageSize: _CMUtils.grid.getPageSize(),
				proxy: {
					url: url.groupStore,
					type: "ajax",
					reader: {
						root: "filters",
						type: "json",
						totalProperty: "count"
					}
				},
				autoLoad: true
			});
		},

		/**
		 * Return the store of the current
		 * logged user
		 * 
		 * @returns {Ext.data.Store} store
		 */
		newUserStore: function() {
			return new Ext.data.Store({
				model: "CMDBuild.model.CMFilterModel",
				autoLoad: false,
				proxy: {
					type: "ajax",
					url: url.userStore,
					reader: {
						idProperty: 'id',
						type: 'json',
						root: 'filters'
					}
				}
			 });
		},

		newSystemStore: function(className) {
			return new Ext.data.Store({
				model: "CMDBuild.model.CMFilterModel",
				pageSize: _CMUtils.grid.getPageSize(),
				proxy: {
					url: url.read,
					type: "ajax",
					reader: {
						root: "filters",
						type: "json",
						totalProperty: "count"
					},
					extraParams: {
						className: className
					}
				},
				autoLoad: true
			});
		}
	};

	function doRequest(filter, config, url, method, fullParams) {
		if (Ext.getClassName(filter) != "CMDBuild.model.CMFilterModel") {
			return; // TODO alert
		}

		var request = config || {};

		request.url = url;
		request.method = method;
		request.params = getParams(filter, fullParams);

		CMDBuild.Ajax.request(config);
	}

	function getParams(filter, full) {
		var params = {};

		params.id = filter.getId();

		if (full) {
			params.className = filter.getEntryType();
			params.configuration = Ext.encode(filter.getConfiguration());
			params.description = filter.getDescription();
			params.name = filter.getName();
			params.template = filter.isTemplate();
		}

		return params;
	}
})();