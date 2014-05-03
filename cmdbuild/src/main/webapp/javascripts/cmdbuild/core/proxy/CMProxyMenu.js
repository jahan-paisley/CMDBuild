(function() {

	CMDBuild.ServiceProxy.menu = {
		/**
		 * Read the menu designed for this
		 * group. If there are no menu, a default
		 * menu is returned.
		 * If the configuration of the menu
		 * contains some node but the group
		 * has not the privileges to use it
		 * this method does not add it to the
		 * menu
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.groupName
		 *
		 */
		read: function(p) {
			p.method = 'GET';
			p.url = CMDBuild.ServiceProxy.url.menu.read;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 * Read the full configuration designed for
		 * the given group.
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.groupName
		 */
		readConfiguration: function(p) {
			p.method = 'GET';
			p.url = _CMProxy.url.menu.readConfiguration;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 * Read the items that are not added to the
		 * current menu configuration
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.groupName
		 */
		readAvailableItems: function(p) {
			p.method = 'GET';
			p.url = _CMProxy.url.menu.readAvailableItems;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.groupName
		 * @param {object} p.params.menu
		 */
		save: function(p) {
			p.method = 'POST',
			p.url = CMDBuild.ServiceProxy.url.menu.update;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.groupName
		 */
		remove: function(p) {
			p.method = 'POST',
			p.url = CMDBuild.ServiceProxy.url.menu.remove;

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();