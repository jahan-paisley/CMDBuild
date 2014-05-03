(function() {

	Ext.define("CMDBuild.core.CMDelegable", {
		statics: {
			errors: {
				noInterfaceOnInit: "Define delegate interface name",

				wrongTypeOnAdd: function(delegableClassName, delegateClassName) {
					return Ext.String.format( //
						"A delegate for {0} must implements {1}", //
						delegableClassName, //
						delegateClassName
					);
				}
			}
		},

		/**
		 * The interface to check when add a delegate. Must be passed on creation, otherwise the constructor throws an exception
		 */
		delegateInterfaceName: undefined,

		/**
		 * @constructor
		 * @param {String} delegateInterfaceName The interface that the delegates must implement
		 * @throw an error if create it without an interface for the delegates
		 */
		constructor: function(delegateInterfaceName) {
			if (typeof delegateInterfaceName != "string" || delegateInterfaceName === "")
				throw Ext.getClass(this).errors.noInterfaceOnInit;

			this.delegateInterfaceName = delegateInterfaceName;
			this.delegates = [];
		},

		/**
		 * Add a delegate to the delegates if it implement the right interface
		 *
		 * @param {Object} delegate An object that implement the interface required for this delegable
		 */
		addDelegate: function(delegate) {
			if (!CMDBuild.checkInterface(delegate, this.delegateInterfaceName))
				throw  Ext.getClass(this).errors.wrongTypeOnAdd(Ext.getClassName(this), this.delegateInterfaceName);

			this.delegates.push(delegate);
		},

		/**
		 * Remove a delegate to the delegates if present
		 *
		 * @param {Object} delegate An object that implement the required interface and that must be removed from the list
		 */
		removeDelegate: function(delegate) {
			Ext.Array.remove(this.delegates, delegate);
		},

		/**
		 * Call the method with the given name of each delegate passing to it the given parameters
		 *
		 * @param {String} methodName The name of the method to call.
		 * @param {Array|any} parameters The parameters to pass to the method call.
		 */
		callDelegates: function(methodName, parameters) {
			var args = parameters || [];
			args = Ext.isArray(args) ? args : [args];

			for (var i=0, d=null; i < this.delegates.length; ++i) {
				d = this.delegates[i];
				if (typeof d[methodName] == "function") {
					d[methodName].apply(d, args);
				}
			}
		},

		/**
		 * Call the method with the given name of each delegate returning the first value matched
		 *
		 * @param {String} methodName The name of the method to call.
		 */
		getFromDelegates: function(methodName) {
			for (var i=0, d=null; i < this.delegates.length; ++i) {
				d = this.delegates[i];
				if (typeof d[methodName] == "function") {
					var val = d[methodName].apply(d);
					if (val !== undefined)
						return val;
				}
			}
		},

		countDelegates: function() {
			return this.delegates.length;
		}
	});

})();