(function() {

	Ext.define("CMDBuild.state.CMCardModuleStateDelegate", {
		/**
		 * @param {CMDBuild.state.CMCardModuleState} state The state that calls the delegate
		 * @param {CMDBuild.cache.CMEntryTypeModel} entryType The new entryType
		 * @param {Object} danglingCard, the configuration to open a card.
		 * @see CMDBuild.controller.management.common.CMCardGridController
		 */
		onEntryTypeDidChange: function(state, entryType, danglingCard, filter) {},

		/**
		 * @param {CMDBuild.state.CMCardModuleState} state The state that calls the delegate 
		 * @param {object} card The data of the new selected card
		 */
		onCardDidChange: function(state, card) {}
	});

	Ext.define("CMDBuild.state.CMCardModuleState", {

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
			"CMDBuild.state.CMCardModuleStateDelegate");

			this.entryType = null;
			this.card = null;
		},

		// TODO manage dangling card
		setEntryType: function(entryType, danglingCard, filter) {
			if ((entryType === this.entryType && this.filter) 
					|| danglingCard
					|| filter
					|| this.entryType !== entryType) {

				this.entryType = entryType;
				this.filter = filter || null;
				// reset the stored card because it could
				// not be of the new entry type
				this.setCard(null);

				this.callDelegates("onEntryTypeDidChange", [this, entryType, danglingCard, filter]);
			}
		},

		setCard: function(card, cb) {
			if (this.card === card) {
				return;
			}

			if (card != null 
					&& typeof card.data == "undefined") {

				CMDBuild.ServiceProxy.card.get({
					params: card,
					scope: this,
					success: function(a,b, response) {
						var raw = response.card;
						if (raw) {
							var c = new CMDBuild.DummyModel(response.card);
							c.raw = raw;
							this.setCard(c, cb);
						}
					}
				});
			} else {
				this.card = card;
				this.callDelegates("onCardDidChange", [this, card]);
				if (typeof cb == "function") {
					cb(card);
				}
			}
		}

	});

	// Define a global variable
	_CMCardModuleState = new CMDBuild.state.CMCardModuleState();
})();