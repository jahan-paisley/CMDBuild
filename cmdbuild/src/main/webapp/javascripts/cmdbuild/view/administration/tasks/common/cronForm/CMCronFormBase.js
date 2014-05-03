(function() {

	var tr = CMDBuild.Translation.administration.tasks.cronForm;

	Ext.define('CMDBuild.view.administration.tasks.common.cronForm.CMCronFormBase', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		frame: true,
		layout: 'hbox',
		margin: '0 0 5 0',

		initComponent: function() {
			var me = this;

			this.baseRadio = Ext.create('Ext.form.field.Radio', {
				name: CMDBuild.ServiceProxy.parameter.CRON_INPUT_TYPE,
				inputValue: CMDBuild.ServiceProxy.parameter.BASE,
				boxLabel: tr.basic,
				width: CMDBuild.LABEL_WIDTH,

				listeners: {
					change: function(radio, value) {
						me.delegate.cmOn('onChangeBaseRadio', value);
					}
				}
			});

			this.baseCombo = Ext.create('Ext.form.field.ComboBox', {
				name: 'baseCombo',
				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
					data: [
						['0 * * * ?', tr.everyHour],
						['0 0 * * ?', tr.everyDay],
						['0 0 1 * ?', tr.everyMounth],
						['0 0 1 1 ?', tr.everyYear]
					]
				}),
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				queryMode: 'local',
				forceSelection: true,
				editable: false,
				margins: '0 0 0 ' + (CMDBuild.LABEL_WIDTH - 45),

				listeners: {
					select: function(combo, record, index) {
						me.delegate.cmOn('onSelectBaseCombo', record[0].get(CMDBuild.ServiceProxy.parameter.VALUE));
					}
				}
			});

			Ext.apply(this, {
				items: [this.baseRadio, this.baseCombo]
			});

			this.callParent(arguments);
		}
	});

})();