(function() {

	var tr = CMDBuild.Translation.administration.tasks;

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1Delegate', {
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

		getValueGroups: function() {
			return this.view.groups.getValue();
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

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep1', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'event_synchronous',

		border: false,
		height: '100%',
		overflowY: 'auto',

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep1Delegate', this);
			this.typeField = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: CMDBuild.ServiceProxy.parameter.TYPE,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				value: tr.tasksTypes.event + ' ' + tr.tasksTypes.eventTypes.synchronous.toLowerCase(),
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

			this.phase = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.ServiceProxy.parameter.PHASE,
				fieldLabel: tr.taskEvent.phase,
				labelWidth: CMDBuild.LABEL_WIDTH,
				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
					data: [
						['afterCreate', tr.taskEvent.afterCreate],
						['beforeCreate', tr.taskEvent.beforeCreate],
						['afterUpdate', tr.taskEvent.afterUpdate],
						['beforeUpdate', tr.taskEvent.beforeUpdate],
						['beforeDelete', tr.taskEvent.beforeDelete]
					]
				}),
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				queryMode: 'local',
				forceSelection: true,
				editable: false
			});

			this.groups = Ext.create('CMDBuild.view.common.field.CMGroupSelectionList', {
				fieldLabel: tr.taskEvent.groupsToApply,
				height: 300,
				valueField: CMDBuild.ServiceProxy.parameter.NAME,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				considerAsFieldToDisable: true
			});

			this.classe = Ext.create('Ext.form.field.ComboBox', {
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
					this.phase,
					this.groups,
					this.classe
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(view, eOpts) {

				// Disable next button only if class is not selected
				if (this.delegate.isEmptyClass())
					this.delegate.setDisabledButtonNext(true);

				// Select all groups by default
				this.groups.selectAll();
			}
		}
	});

})();