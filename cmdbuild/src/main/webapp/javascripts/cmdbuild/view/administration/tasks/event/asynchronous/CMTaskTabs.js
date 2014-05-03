(function() {

	/**
	 * Task's wizard tabs index
	 */
	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMTaskTabs', {

		constructor: function() {
			this.step1 = Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1');
			this.step2 = Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep2');
			this.step3 = Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration');
			this.step4 = Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep4');
		},

		getTabs: function() {
			return [this.step1, this.step2, this.step3, this.step4];
		}
	});

})();