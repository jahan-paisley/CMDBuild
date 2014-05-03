(function() {
	var POST = "POST";
	var GET = "GET";
	var url = _CMProxy.url.dataView;

	_CMProxy.dataView = {
		
		/**
		 * read all the data view available
		 * for the logged user
		 */
		read: function(config) {
			config.url = url.read;
			config.method = GET;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		sql: {
			/**
			 * Retrieves the SQL view stored
			 * 
			 * @param {object} config
			 */
			read: function(config) {
				config.url = url.sql.read;
				config.method = GET;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Create a new SQL view
			 * 
			 * @param {object} config
			 * @param {string} config.name The name of the new View
			 * @param {string} config.description The description of the new View
			 * @param {string} config.functionName The name of the SQL function to 
			 * use as data store
			 */ 
			create: function(config) {
				config.url = url.sql.create;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Update a stored SQL view
			 * 
			 * @param config
			 * @param {string} config.name The name of the view to update
			 * @param {string} config.description The new description
			 * @param {string} config.functionName The name of the new SQL function
			 */
			update: function(config) {
				config.url = url.sql.update;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Delete a stored SQL view
			 * 
			 * @param config
			 * @param {string} config.name The name of the view to remove
			 */
			remove: function(config) {
				config.url = url.sql.remove;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * @return {Ext.data.Store} a store of CMDBuild.model.CMDataViewModel
			 */
			store: function() {
				return new Ext.data.Store({
					model: "CMDBuild.model.CMDataViewModel",
					autoLoad: false,
					proxy: {
						type: "ajax",
						url: url.sql.read,
						reader: {
							type: 'json',
							root: 'views'
						}
					}
				});
			}
		},

		filter: {
			/**
			 * Retrieves the Filter view stored
			 * 
			 * @param {object} config
			 */
			read: function(config) {
				config.url = url.filter.read;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Create a new Filter view
			 * 
			 * @param {string} config.params.name The name of the new View
			 * @param {string} config.param.description The description of the new View
			 * @param {string} config.param.className The name of the target class
			 * @param {object} config.param.filter The Filter configuration to
			 * use for the new view
			 */
			create: function(config) {
				config.url = url.filter.create;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Update a Filter view
			 * 
			 * @param {string} config.param.id The id of the View to update
			 * @param {string} config.param.name The name of the View to update
			 * @param {string} config.param.description The new description
			 * @param {string} config.param.className The new origin class
			 * @param {object} config.param.filter The new filter
			 */
			update: function(config) {
				config.url = url.filter.update;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * Delete a stored Filter view
			 * 
			 * @param config
			 * @param {string} config.name The name of the view to remove
			 */
			remove: function(config) {
				config.url = url.filter.remove;
				config.method = POST;

				CMDBuild.ServiceProxy.core.doRequest(config);
			},

			/**
			 * @return {Ext.data.Store} a store of CMDBuild.model.CMDataViewModel
			 */
			store: function() {
				return new Ext.data.Store({
					model: "CMDBuild.model.CMDataViewModel",
					autoLoad: false,
					proxy: {
						type: "ajax",
						url: url.filter.read,
						reader: {
							type: 'json',
							root: 'views'
						}
					}
				});
			}
		}
	};
})();