(function() {

	/**
	 * Task's wizard tabs index
	 */
	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMTaskTabs', {

		constructor: function() {
			this.step1 = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep1');
			this.step2 = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep2');
			this.step3 = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep3');
		},

		getTabs: function() {
			return [this.step1, this.step2, this.step3];
		}
	});

})();