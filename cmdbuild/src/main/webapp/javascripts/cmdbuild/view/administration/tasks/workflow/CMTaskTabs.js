(function() {

	/**
	 * Task's wizard tabs index
	 */
	Ext.define('CMDBuild.view.administration.tasks.workflow.CMTaskTabs', {

		constructor: function() {
			this.step1 = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep1');
			this.step2 = Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration');
		},

		getTabs: function() {
			return [this.step1, this.step2];
		}
	});

})();