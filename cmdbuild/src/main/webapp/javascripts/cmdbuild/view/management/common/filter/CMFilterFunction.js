(function() {

	Ext.define('Functions', {
		extend: 'Ext.data.Model',
		fields: [
			{ name: 'name', type: 'string' }
		]
	});

	var functionsStore = Ext.create('Ext.data.Store', {
		model: 'Functions',
		proxy: {
			type: 'ajax',
			url: CMDBuild.ServiceProxy.url.functions.getFunctions,
			reader: {
					type: 'json',
					root: 'response'
			}
		},
		autoLoad: true
	});

	Ext.define('CMDBuild.view.management.common.filter.CMFunctions', {
		extend: 'Ext.panel.Panel',

		title: CMDBuild.Translation.management.findfilter.functions,
		bodyCls: 'x-panel-body-default-framed cmbordertop',
		bodyStyle: {
			padding: '5px 5px 0px 5px'
		},
		cls: 'x-panel-body-default-framed',
		labelWidth: CMDBuild.LABEL_WIDTH,
		width: CMDBuild.ADM_BIG_FIELD_WIDTH,

		// configuration
			className: undefined,
		// configuration

		initComponent: function() {
			this.functionsCombo = Ext.create('Ext.form.ComboBox', {
				fieldLabel: CMDBuild.Translation.management.findfilter.functions,
				store: functionsStore,
				name: CMDBuild.ServiceProxy.parameter.FUNCTION,
				displayField: CMDBuild.ServiceProxy.parameter.NAME,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				trigger1Cls: Ext.baseCSSPrefix + 'form-arrow-trigger',
				trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
				hideTrigger1: false,
				hideTrigger2: false,

				onTrigger2Click: function() {
					this.setValue('');
				}
			});
			this.items = [this.functionsCombo];

			this.callParent(arguments);
		},

		setData: function(data) {
			if (data && data.length > 0) {
				this.functionsCombo.setValue(data[0].name);
			} else {
				this.functionsCombo.setValue('');
			}
		},

		getData: function() {
			var functionName = this.functionsCombo.getValue();

			return (!functionName) ? [] : [{ 'name': functionName }];
		}
	});

})();