(function() {

	CMDBuild.ServiceProxy.card = {
		/**
		 * retrieve the position on the db of the
		 * requiered card, considering the sorting and
		 * current filter applied on the grid
		 *
		 * @param {object} p
		 * @param {object} p.params
		 * @param {int} p.params.cardId the id of the card
		 * @param {string} p.params.className the name of the class
		 * @param {object} p.params.filter the current filter
		 * @param {object} p.params.sort the current sorting
		 */
		getPosition: function(p) {
			p.method = 'GET';
			p.url = CMDBuild.ServiceProxy.url.card.getPosition;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		get: function(p) {
			adaptGetCardCallParams(p);
			p.method = 'GET';
			p.url = CMDBuild.ServiceProxy.url.card.read,

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		remove: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.card.remove,
			p.important = true;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param p
		 */
		bulkUpdate: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.card.bulkUpdate;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		bulkUpdateFromFilter: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.card.bulkUpdateFromFilter;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {integer} p.id
		 * the id of the card to lock
		 * the className is not required
		 * because the id is unique
		 * in all the db
		 */
		lockCard: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.card.lock;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * @param {integer} p.id
		 * the id of the card to lock
		 */
		unlockCard: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.card.unlock;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		/**
		 *
		 * unlock all the cards that
		 * was be locked
		 */
		unlockAllCards: function(p) {
			p.method = 'POST';
			p.url = CMDBuild.ServiceProxy.url.card.unlockAll;

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

	function adaptGetCardCallParams(p) {
		if (p.params.Id && p.params.IdClass) {
			_deprecated();
			var parameterNames = CMDBuild.ServiceProxy.parameter;
			var parameters = {};
			parameters[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(p.params.IdClass);
			parameters[parameterNames.CARD_ID] = p.params.Id;

			p.params = parameters;
		}
	}

})();