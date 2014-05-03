(function() {

	Ext.define('CMDBuild.controller.common.CMBasePanelController', {
		alternateClassName: 'CMDBuild.controller.CMBasePanelController', // Legacy class name

		constructor: function(view) {
			this.view = view;
			this.view.on('CM_iamtofront', this.onViewOnFront, this);
		},

		onViewOnFront: function(p) {
			CMDBuild.log.info('onPanelActivate ' + this.view.title, this, p);
		},

		callMethodForAllSubcontrollers: function(method, args) {
			if (this.subcontrollers) {
				for (var i = 0; i < this.subcontrollers.length; ++i) {
					var c = this.subcontrollers[i];

					if (c && typeof c[method] == 'function')
						c[method].apply(c, args);
				}
			}
		},

		callback: function() {
			CMDBuild.LoadMask.get().hide();
		}
	});

})();