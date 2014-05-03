(function() {

	var tr = CMDBuild.Translation.administration.tasks.cronForm;

	Ext.define('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormAdvanced', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		frame: true,
		layout: 'hbox',
		margin: '0 0 5 0',

		initComponent: function() {
			var me = this;

			this.advanceRadio = Ext.create('Ext.form.field.Radio', {
				name: CMDBuild.ServiceProxy.parameter.CRON_INPUT_TYPE,
				inputValue: CMDBuild.ServiceProxy.parameter.ADVANCED,
				boxLabel: tr.advanced,
				width: CMDBuild.LABEL_WIDTH,

				listeners: {
					change: function(radio, value) {
						me.delegate.cmOn('onChangeAdvancedRadio', value);
					}
				}
			});

			this.advancedFields = [
				this.delegate.createCronField(CMDBuild.ServiceProxy.parameter.MINUTE, tr.minute),
				this.delegate.createCronField(CMDBuild.ServiceProxy.parameter.HOUR, tr.hour),
				this.delegate.createCronField(CMDBuild.ServiceProxy.parameter.DAY_OF_MOUNTH, tr.dayOfMounth),
				this.delegate.createCronField(CMDBuild.ServiceProxy.parameter.MOUNTH, tr.mounth),
				this.delegate.createCronField(CMDBuild.ServiceProxy.parameter.DAY_OF_WEEK, tr.dayOfWeek)
			];

			Ext.apply(this, {
				items: [
					this.advanceRadio,
					{
						xtype: 'container',
						frame: false,
						border: false,
						items: this.advancedFields
					}
				]
			});

			this.callParent(arguments);
		}
	});

})();