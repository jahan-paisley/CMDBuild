(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController', {

		advancedField: undefined,
		baseField: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onChangeAdvancedRadio':
					return this.onChangeAdvancedRadio(param);

				case 'onChangeBaseRadio':
					return this.onChangeBaseRadio(param);

				case 'onSelectBaseCombo':
					return this.setValueAdvancedFields(param);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param (Array) fields
		 * @return (String) cron expression
		 */
		buildCronExpression: function(fields) {
			var cronExpression = '';

			for (var i = 0; i < fields.length; i++) {
				cronExpression += fields[i];

				if (i < (fields.length - 1))
					cronExpression += ' ';
			}

			return cronExpression;
		},

		/**
		 * Create CMCronTriggerField
		 *
		 * @param (String) name
		 * @param (String) label
		 * @return (Object) CMDBuild.view.common.field.CMCronTriggerField
		 */
		createCronField: function(name, label) {
			var me = this;

			return Ext.create('CMDBuild.view.common.field.CMCronTriggerField', {
				name: name,
				fieldLabel: label,
				cmImmutable: true,
				disabled: true,
				allowBlank: false,

				listeners: {
					change: function(field, newValue, oldValue) {
						me.setValueBase(
							me.buildCronExpression([
								me.advancedField.advancedFields[0].getValue(),
								me.advancedField.advancedFields[1].getValue(),
								me.advancedField.advancedFields[2].getValue(),
								me.advancedField.advancedFields[3].getValue(),
								me.advancedField.advancedFields[4].getValue()
							])
						);
					}
				}
			});
		},

		getBaseCombo: function() {
			return this.baseField.baseCombo;
		},

		/**
		 * Get cron form formatted values
		 *
		 * @param (Boolean) cronInputType
		 * @return (String) cronExpression
		 */
		getValue: function(cronInputType) {
			var cronExpression;

			if (cronInputType) {
				cronExpression = this.buildCronExpression([
					this.advancedField.advancedFields[0].getValue(),
					this.advancedField.advancedFields[1].getValue(),
					this.advancedField.advancedFields[2].getValue(),
					this.advancedField.advancedFields[3].getValue(),
					this.advancedField.advancedFields[4].getValue()
				]);
			} else {
				cronExpression = this.baseField.baseCombo.getValue();
			}

			return cronExpression;
		},

		isEmptyAdvanced: function() {
			if (
				Ext.isEmpty(this.advancedField.advancedFields[0].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[1].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[2].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[3].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[4].getValue())
			)
				return true;

			return false;
		},

		isEmptyBase: function() {
			return Ext.isEmpty(this.baseField.baseCombo.getValue());
		},

		markInvalidAdvancedFields: function(message) {
			for(item in this.advancedField.advancedFields)
				this.advancedField.advancedFields[item].markInvalid(message);
		},

		onChangeAdvancedRadio: function(value) {
			this.setDisabledAdvancedFields(!value);
			this.setDisabledBaseCombo(value);
		},

		onChangeBaseRadio: function(value) {
			this.setDisabledAdvancedFields(value);
			this.setDisabledBaseCombo(!value);
		},

		setValueAdvancedFields: function(cronExpression) {
			var values = cronExpression.split(' ');
			var fields = this.advancedField.advancedFields;

			for (var i = 0; i < fields.length; i++) {
				if (values[i])
					fields[i].setValue(values[i]);
			}
		},

		setValueAdvancedRadio: function(value) {
			this.advancedField.advanceRadio.setValue(value);
		},

		setDisabledAdvancedFields: function(value) {
			for (var key in this.advancedField.advancedFields)
				this.advancedField.advancedFields[key].setDisabled(value);
		},

		setDisabledBaseCombo: function(value) {
			this.baseField.baseCombo.setDisabled(value);
		},

		/**
		 * Try to find the correspondence of advanced cronExpression in baseCombo's store
		 *
		 * @param (String) value
		 */
		setValueBase: function(value) {
			var index = this.baseField.baseCombo.store.find(CMDBuild.ServiceProxy.parameter.VALUE, value);

			if (index > -1) {
				this.baseField.baseCombo.setValue(value);
			} else {
				this.baseField.baseCombo.setValue();
			}
		},

		/**
		 * Cron validation
		 *
		 * @param (Object) wizard - reference to wizard object
		 * @return (Boolean)
		 */
		validate: function(wizard) {
			if (this.isEmptyAdvanced()) {
				this.markInvalidAdvancedFields('This field is required');

				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);

				CMDBuild.LoadMask.get().hide();

				wizard.changeTab(1);
				this.setValueAdvancedRadio(true);

				return false;
			}

			return true;
		}
	});

})();