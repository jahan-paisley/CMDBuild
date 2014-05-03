(function() {

	CMDBuild.ServiceProxy.url.relations = {
		create: 'services/json/management/modcard/createrelations',
		read: 'services/json/management/modcard/getrelationlist',
		update: 'services/json/management/modcard/modifyrelation',
		remove: 'services/json/management/modcard/deleterelation',
		removeDetail: "services/json/management/modcard/deletedetail"
	};

	CMDBuild.ServiceProxy.relations = {
		/**
		 * 
		 * @param {object} p
		 * @param {object} p.params
		 * @param {int} p.params.cardId
		 * @param {string} p.params.className
		 * @param {int} p.params.domainId
		 * @param {string} p.params.src "_1" | "_2"
		 * @param {int} p.params.domainlimit
		 */
		getList: function(p) {
			adaptGetListRequestParameter(p);
			p.method = "GET";
			p.url = CMDBuild.ServiceProxy.url.relations.read;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		modify: function(p) {
			p.method = "POST";
			p.url = CMDBuild.ServiceProxy.url.relations.update;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		add: function(p) {
			p.method = "POST";
			p.url = CMDBuild.ServiceProxy.url.relations.create;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		remove: function(p) {
			p.method = "POST";
			p.url = CMDBuild.ServiceProxy.url.relations.remove;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		removeDetail: function(p) {
			p.method = "POST";
			p.url = CMDBuild.ServiceProxy.url.relations.removeDetail;

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

	/* Currently the getList receive an object like this
	{
		Id: me.card.get("Id"),
		IdClass: me.entryType.getId(),
		domainId: me.view.detail.get("id"),
		src : me.view.detail.getDetailSide()
	}
	* for server refactoring we want
	* 
	* cardId = Id
	* className -> take it from cache
	* domainId = domainId
	* src
	*/
	function adaptGetListRequestParameter(p) {
		var parameterName = CMDBuild.ServiceProxy.parameter;
		if (p.params 
				&& p.params.IdClass) {
			_debug("DEPRECATED: CMDBuild.ServiceProxy.relations.getList will change the request params as soon as possible", p.params);

			p.params[parameterName.CARD_ID] = p.params.Id;
			delete p.params.Id;
			p.params[parameterName.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.params.IdClass);
			delete p.params.IdClass;
		}
	}
})();