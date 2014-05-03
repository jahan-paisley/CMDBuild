(function() {

	CMDBuild.ServiceProxy.attributes = {

		/**
		 *
		 * @param {object} p
		 * @param {string} p.params.className
		 */
		update: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.attribute.update;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {boolean} p.params.active
		 * @param {string} p.params.className
		 */
		read: function(p) {
			p.method = 'GET';
			p.url = CMDBuild.ServiceProxy.url.attribute.read;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.name
		 * @param {string} p.params.className
		 */
		remove: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.attribute.remove;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.className
		 * @param {array[]} p.params.attributes [{name: "", index: ""}]
		 */
		reorder: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.attribute.reorder;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 * @param {object} p
		 * @param {object} p.params
		 * @param {string} p.params.className
		 * @param {object} p.params.attributes {attributename: position, ...}
		 */
		updateSortConfiguration: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.attribute.updateSortConfiguration;
			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();