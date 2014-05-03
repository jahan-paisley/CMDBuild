(function() {

	/**
	 * Task's wizard tabs index
	 */
	Ext.define('CMDBuild.view.administration.tasks.connector.CMTaskTabs', {

		constructor: function() {
			this.step1 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep1');
			this.step2 = Ext.create('CMDBuild.view.administration.tasks.common.CMStepCronConfiguration');
			this.step3 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep3');
			this.step4 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep4');
			this.step5 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep5');
			this.step6 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep6');
			this.step7 = Ext.create('CMDBuild.view.administration.tasks.connector.CMStep7');
		},

		getTabs: function() {
			return [this.step1, this.step2, this.step3, this.step4, this.step5, this.step6, this.step7];
		}
	});

})();