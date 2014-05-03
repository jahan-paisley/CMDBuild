(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyTasks');

	Ext.define('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterForm', {
		extend: 'Ext.form.FieldContainer',

		border: false,
		layout: 'hbox',
		labelWidth: CMDBuild.LABEL_WIDTH,
		width: CMDBuild.CFG_BIG_FIELD_WIDTH,
		considerAsFieldToDisable: true,

		/**
		 * To acquire informations to setup fields before creation
		 *
		 * @param (Object) configuration
		 * @param (Object) configuration.textarea
		 * @param (Object) configuration.button
		 */
		constructor: function(configuration) {
			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormController', this);

			if (typeof configuration != 'undefined' || typeof configuration.fieldContainer != 'undefined') {
				Ext.apply(this, configuration.fieldContainer);
			}

			if (typeof configuration == 'undefined' || typeof configuration.textarea == 'undefined') {
				this.textareaConfig = { delegate: this.delegate };
			} else {
				this.textareaConfig = configuration.textarea;
				this.textareaConfig.delegate = this.delegate;
			}

			if (typeof configuration == 'undefined' || typeof configuration.button == 'undefined') {
				this.buttonConfig = { delegate: this.delegate };
			} else {
				this.buttonConfig = configuration.button;
				this.buttonConfig.delegate = this.delegate;
			}

			this.callParent(arguments);
		},

		initComponent: function() {
			this.textarea = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormTextarea', this.textareaConfig);
			this.delegate.textareaField = this.textarea;

			this.button = Ext.create('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormButton', this.buttonConfig);
			this.delegate.buttonField = this.button;

			Ext.apply(this, {
				items: [this.textarea, this.button]
			});

			this.callParent(arguments);
		}
	});

})();