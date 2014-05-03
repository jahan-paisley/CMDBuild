(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		getValueId: function() {
			return this.view.idField.getValue();
		},

		isEmptyClass: function() {
			if (this.view.classe.getValue())
				return false;

			return true;
		},

		setDisabledButtonNext: function(state) {
			this.parentDelegate.setDisabledButtonNext(state);
		},

		setDisabledTypeField: function(state) {
			this.view.typeField.setDisabled(state);
		},

		setValueActive: function(value) {
			this.view.activeField.setValue(value);
		},

		setValueDescription: function(value) {
			this.view.descriptionField.setValue(value);
		},

		setValueId: function(value) {
			this.view.idField.setValue(value);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'event',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.asynchronous.CMStep1Delegate', this);

			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				value: tr.tasksTypes.event + ' ' + tr.tasksTypes.eventTypes.asynchronous.toLowerCase(),
				disabled: true,
				cmImmutable: true,
				readOnly: true
			});

			this.idField = Ext.create('Ext.form.field.Hidden', {
				name: CMDBuild.ServiceProxy.parameter.ID
			});

			this.descriptionField = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				fieldLabel: CMDBuild.Translation.description_,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.activeField = Ext.create('Ext.form.field.Checkbox', {
				name: CMDBuild.ServiceProxy.parameter.ACTIVE,
				fieldLabel: tr.startOnSave,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH
			});

			this.className = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.CLASS_NAME,
				fieldLabel: CMDBuild.Translation.targetClass,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: _CMCache.getClassesAndProcessesAndDahboardsStore(),
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				queryMode: 'local',
				allowBlank: false,
				forceSelection: true,
				editable: false,

				listeners: {
					select: function(combo, records, options) {
						me.delegate.cmOn('onClassSelected', { className: records[0].get(CMDBuild.ServiceProxy.parameter.NAME) });
					}
				}
			});

			Ext.apply(this, {
				items: [
					this.typeField,
					this.idField,
					this.descriptionField,
					this.activeField,
					this.className
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Disable next button only if class is not selected
			 */
			show: function(view, eOpts) {
				if (this.delegate.isEmptyClass())
					this.delegate.setDisabledButtonNext(true);
			}
		}
	});

})();