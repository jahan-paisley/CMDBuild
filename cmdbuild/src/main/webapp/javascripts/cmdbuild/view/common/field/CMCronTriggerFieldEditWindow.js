(function() {

	var translation = CMDBuild.Translation.cron.triggerFieldWindow;

	Ext.define('CMDBuild.view.common.field.CMCronTriggerFieldEditWindow', {
		extend: 'Ext.Window',

		modal: true,
		resizable: false,
		width: 360, // TODO should be computed at runtime
		selectedRadio: undefined,

		initComponent: function() {
			var me = this;

			this.each = Ext.create('Ext.form.field.Radio', {
				name: 'criteria',
				boxLabel: translation.every + this.title.toLowerCase(),
				listeners: {
					change: function(radio, value) {
						me.selectedRadio = 'each';
					}
				}
			});

			this.step = Ext.create('Ext.form.field.Radio', {
				name: 'criteria',
				boxLabel: translation.inastepwith,
				listeners: {
					change: function(radio, value) {
						me.selectedRadio = 'step';
						me.stepField.setDisabled(!value);
					}
				}
			});

			this.stepField = Ext.create('Ext.form.field.Text', {
				fieldLabel: this.title,
				name: 'rangeField',
				disabled: true
			});

			this.range = Ext.create('Ext.form.field.Radio', {
				name: 'criteria',
				boxLabel: translation.range,
				listeners: {
					change: function(radio, value) {
						me.selectedRadio = 'range';
						me.rangeFrom.setDisabled(!value);
						me.rangeTo.setDisabled(!value);
					}
				}
			});

			this.rangeFrom = Ext.create('Ext.form.field.Text', {
				fieldLabel: CMDBuild.Translation.from,
				name: 'rangeFrom',
				disabled: true
			});

			this.rangeTo = Ext.create('Ext.form.field.Text', {
				fieldLabel: CMDBuild.Translation.to,
				name: 'rangeTo',
				disabled: true
			});

			this.exactly = Ext.create('Ext.form.field.Radio', {
				name: 'criteria',
				boxLabel: translation.exactly,
				listeners: {
					change: function(radio, value) {
						me.selectedRadio = 'exactly';
						me.exactField.setDisabled(!value);
					}
				}
			});

			this.exactField = Ext.create('Ext.form.field.Text', {
				fieldLabel: this.title,
				name: 'rangeTo',
				disabled: true
			});

			this.form = Ext.create('Ext.panel.Panel', {
				frame: true,
				border: false,
				items: [
					this.each,
					this.step,
					{
						frame: true,
						labelWidth: CMDBuild.LABEL_WIDTH,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [this.stepField]
					},
					this.range,
					{
						frame: true,
						labelWidth: CMDBuild.LABEL_WIDTH,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [
							this.rangeFrom,
							this.rangeTo
						]
					},
					this.exactly,
					{
						frame: true,
						labelWidth: CMDBuild.LABEL_WIDTH,

						layout: {
							type: 'vbox',
							align: 'stretch'
						},

						items: [this.exactField]
					}
				],
				buttonAlign: 'center',
				buttons: [
					{
						scope: this,
						text: CMDBuild.Translation.common.buttons.save,
						handler: this.onSave
					},
					{
						scope: this,
						text: CMDBuild.Translation.common.buttons.abort,
						handler: function() {
							this.destroy();
						}
					}
				]
			});

			Ext.apply(this, {
				items: [this.form]
			});

			this.callParent(arguments);
		},

		disableFields: function() {
			this.exactField.setDisabled(true);
			this.rangeFrom.setDisabled(true);
			this.rangeTo.setDisabled(true);
			this.stepField.setDisabled(true);
		},

		possibleValue: function(method) {
			var me = this;

			switch (method) {
				case 'each':
					return '*';

				case 'exactly':
					return me.exactField.getValue();

				case 'range':
					return me.rangeFrom.getValue() + '-' + me.rangeTo.getValue();

				case 'step': {_debug(this);_debug(me.stepField.getValue());_debug(this.stepField.getValue());
					return '0/' + me.stepField.getValue();
				}
				default:
					throw 'CMCronTriggerFieldEditWindow error: unable to recognize possibleValue method.';
			}
		},

		onSave: function() {
			if (this.selectedRadio)
				this.parentField.setValue(this.possibleValue(this.selectedRadio));

			this.destroy();
		},

		listeners: {
			show: function(view, eOpts) {
				this.disableFields();
			}
		}
	});

})();