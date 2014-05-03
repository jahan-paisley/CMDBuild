(function() {
	var queryWorkflowStore = Ext.create('Ext.data.Store', {
	    fields: ['Code', 'Description'],
	    data : [
	    ]
	});
	Ext.define("CMDBuild.view.management.common.widgets.CMWorkflowCombo", {
		extend: "Ext.panel.Panel",
		panel: undefined,
		bodyCls: "x-panel-body-default-framed",
		frame : true,
		border: true,
		initComponent: function() {
			this.queryWorkflow = Ext.create('Ext.form.ComboBox', {
			    fieldLabel: CMDBuild.Translation.workflow,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				labelAlign: "right",
			    store: queryWorkflowStore,
			    queryMode: 'local',
			    displayField: 'Description',
			    valueField: 'Code',
			    autoSelect: true,
			    listeners:{
			         scope: this.panel,
			         'select': function(item, param) {
			        	 this.delegate.changeWorkflow(param[0].get("Code"));
			         }
			    }
			});
			this.items = [this.queryWorkflow];
			this.callParent(arguments);
		},
		clearComboValues: function() {
			queryWorkflowStore.removeAll(true);
		},
		loadComboValues: function(data) {
			queryWorkflowStore.loadRawData(data);
		},
		clearCombo: function() {
			this.queryWorkflow.setValue("");
		}
	});
	Ext.define("CMDBuild.view.management.common.widgets.CMWorkflow", {
		extend: "Ext.panel.Panel",

		statics: {
			WIDGET_NAME: ".Workflow"
		},

		formatCombo: {},
		attributeList: [],
		formFields: [],

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;
			this.CMEVENTS = {
					saveButtonClick: "cm-save-click",
					advanceButtonClick: "cm-advance-click"
			};
			this.comboPanel = Ext.create("CMDBuild.view.management.common.widgets.CMWorkflowCombo", {
				region : 'north',
				panel: this,
				hidden: true
			});

			this.widgets = new CMDBuild.view.management.common.widget.CMWidgetButtonsPanelPopup({
				delegate: this,
				region: 'east',
				hideMode: 'offsets',
				cls: "cmborderleft",
				autoScroll: true,
				frame: true,
				border: false,
				items: [],
				_hidden: true,
				hidden: true
			});
			_CMUtils.forwardMethods(this, this.widgets, ["removeAllButtons", "addWidget"]);

			Ext.apply(this, {
				layout: 'border',
				buttonAlign: 'center',
				items: [this.comboPanel, this.widgets],
				cls: "x-panel-body-default-framed",
				border: false,
				frame: false
			});

			this.callParent(arguments);

			this.addEvents(this.CMEVENTS.saveButtonClick);
			this.addEvents(this.CMEVENTS.advanceButtonClick);
		},

		// buttons that the owner panel add to itself
		setDelegate: function(delegate) {
			this.delegate = delegate;
			this.widgets.delegate = delegate;
		},
		getExtraButtons: function() {
			var me = this;
			this.saveButton = new Ext.Button( {
				text : CMDBuild.Translation.common.buttons.save,
				name : 'saveButton',
				hidden: true,
				handler: function() {
					me.fireEvent(me.CMEVENTS.saveButtonClick);
				}
			});
			this.advanceButton =  new Ext.Button( {
				text : CMDBuild.Translation.common.buttons.workflow.advance,
				name : 'advanceButton',
				hidden: true,
				handler: function() {
					me.fireEvent(me.CMEVENTS.advanceButtonClick);
				}
			});
			return [this.saveButton, this.advanceButton];
		},


		// add the required attributes
		configureForm: function(attributes, parameters) {
			this.saveButton.show();
			this.advanceButton.show();
			this.formPanelCreated = true;
			// add fields to form panel
			for (var i=0; i<attributes.length; i++) {
				var attribute = attributes[i];
				var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false);

				if (field) {
					this.formFields[i] = field;
					this.formPanel.add(field);
				}
			}
			this.formPanel.doLayout();
		},

		getFormForTemplateResolver: function() {
			return this.formPanel.getForm();
		},

		getWidgetButtonsPanel: function() {
			return this.widgets;
		},
		fillFormValues : function(parameters) {
			for ( var i = 0; i < this.formFields.length; i++) {
				var field = this.formFields[i], value = parameters[field.name]

				if (value) {
					if (Ext.getClassName(field) == "Ext.form.field.Date") {
						try {
							field.setValue(new Date(value));
						} catch (e) {
							field.setValue(value);
						}
					} else if (Ext.getClassName(field) == "CMDBuild.Management.ReferenceField.Field"
											&& value == parseInt(value)) {
						field.setValue(parseInt(value));
					} else {
						field.setValue(value);
					}
				}
			}
		},
		clearComboValues: function(data) {
			this.comboPanel.loadComboValues(data);
		},
		loadComboValues: function(data) {
			this.comboPanel.loadComboValues(data);
		},
		hideComboPanel: function() {
			this.comboPanel.hide();
		},
		showComboPanel: function() {
			this.comboPanel.show();
		},
		clearWindow: function() {
			this.saveButton.hide();
			this.advanceButton.hide();

			if (this.formPanel) {
				this.formPanel.removeAll(true);
				this.remove(this.formPanel, true);
			}
			this.formPanel = buildFormPanel();
			this.add(this.formPanel);
			this.widgets.removeAllButtons();

		}
	});
	function buildFormPanel() {
		return new Ext.form.FormPanel({
			timeout : CMDBuild.Config.defaultTimeout * 1000,
			monitorValid : true,
			autoScroll : true,
			frame : false,
			border: false,
			region : 'center',
			bodyCls: "x-panel-body-default-framed",
			padding: "5",
			items : []
		});

	}

})();