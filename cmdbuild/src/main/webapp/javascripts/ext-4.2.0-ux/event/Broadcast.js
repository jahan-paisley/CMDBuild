(function() {
	var bus = new Ext.util.Observable();
	
	_CMEventBus = {
		/**
		 * This function is <b>addListener</b> analog for broadcasted messages. It accept the same parameters and have the same functionality. For further details please refer to <b>Ext.util.Observable</b> documentation
		 * @name subscribe
		 * @methodOf Ext.ux.event.Broadcast 
		 * @param {String} eventName
		 * @param {Function} fn
		 * @param {Object} scope
		 * @param {Object} o
		 */
		subscribe: function(eventName, fn, scope, o) {
			bus.addEvents(eventName);
			bus.on(eventName, fn, scope, o);
		},

		unsubscribe: function(eventName, fn, scope) {
			bus.un(eventName, fn, scope);
		},

		/**
		 * This function is <b>fireEvent</b> analog for broadcasted messages. It accept the same parameters and have the same functionality. For further details please refer to <b>Ext.util.Observable</b> documentation
		 * @name publish
		 * @methodOf Ext.ux.event.Broadcast 
	     * @param {String} eventName
	     * @param {Object} args Variable number of parameters are passed to handlers
	     * @return {Boolean} returns false if any of the handlers return false otherwise it returns true
		 */
		publish : function() {        
			if (bus.eventsSuspended !== true) {
				var ce = bus.events ? bus.events[arguments[0].toLowerCase()] : false;
				if(typeof ce == "object"){
					return ce.fire.apply(ce, Array.prototype.slice.call(arguments, 1));
				}
			}
			return true;
		},

		/**
		 * This function is <b>removeListener</b> analog for broadcasted messages.
		 * @name removeSubscriptionsFor
		 * @methodOf Ext.ux.event.Broadcast 
	     * @param {String}   eventName     The type of event which subscriptions will be removed. If this parameter is evaluted to false, then ALL subscriptions for ALL events will be removed.
	     */
		removeSubscriptionsFor : function(eventName) {
			for(var evt in bus.events) {
				if ( (evt == eventName) || (!eventName) ) {
					if (typeof bus.events[evt] == "object"){
						bus.events[evt].clearListeners();
					}
				}
			}
		}
	};

})();