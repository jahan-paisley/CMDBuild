(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.workflowForm.CMWorkflowFormController', {

		comboField: undefined,
		gridField: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onSelectAttributeCombo':
					return this.onSelectAttributeCombo(param);

				case 'onSelectWorkflow':
					return this.onSelectWorkflow(param);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Workflow attribute store builder for onWorkflowSelected event
		 */
		buildWorkflowAttributesStore: function(attributes) {
			if (attributes) {
				var data = [];

				for (var key in attributes) {
					data.push({ value: key });
				}

				return Ext.create('Ext.data.Store', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE],
					data: data,
					autoLoad: true
				});
			}
		},

		cleanServerAttributes: function(attributes) {
			var out = {};

			for (var i = 0, l = attributes.length; i < l; ++i) {
				var attr = attributes[i];

				out[attr.name] = '';
			}

			return out;
		},

		getValueCombo: function() {
			return this.comboField.getValue();
		},

		getValueGrid: function() {
			return this.gridField.getData();
		},

		onSelectAttributeCombo: function(rowIndex) {
			this.gridField.cellEditing.startEditByPosition({ row: rowIndex, column: 1 });
		},

		/**
		 * @param (String) name
		 * @param (Boolean) modify
		 */
		onSelectWorkflow: function(name, modify) {
			var me = this;

			if (typeof modify == 'undefined')
				modify = false;

			CMDBuild.core.proxy.CMProxyTasks.getWorkflowAttributes({
				params: {
					className: name
				},
				success: function(response) {
					var decodedResponse = Ext.JSON.decode(response.responseText);

					me.gridField.keyEditorConfig.store = me.buildWorkflowAttributesStore(me.cleanServerAttributes(decodedResponse.attributes));

					if (!modify) {
						me.gridField.store.removeAll();
						me.gridField.store.insert(0, { key: '', value: '' });
						me.gridField.cellEditing.startEditByPosition({ row: 0, column: 0 });
						me.setDisabledAttributesGrid(false);
					}
				}
			});
		},

		setDisabledAttributesGrid: function(state) {
			this.gridField.setDisabled(state);
		},

		setValueCombo: function(workflowName) {
			if (!Ext.isEmpty(workflowName)) {
				this.comboField.setValue(workflowName);
				this.onSelectWorkflow(workflowName, true);
			}
		},

		setValueGrid: function(data) {
			if (!Ext.isEmpty(data))
				this.gridField.fillWithData(data);
		}
	});

})();