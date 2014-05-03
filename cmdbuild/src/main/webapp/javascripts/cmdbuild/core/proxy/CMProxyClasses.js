(function() {

	CMDBuild.ServiceProxy.classes = {
		read: function(p) {
			p.method = 'GET';
			p.url = CMDBuild.ServiceProxy.url.classes.read;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		save: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.classes.update;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {object} p.params.className
		 */
		remove: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.classes.remove;
			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();