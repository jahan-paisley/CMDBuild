(function() {

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardPropertiesPanelInterface", {
		disableTBarButtons: Ext.emptyFn,
		enableTBarButtons: Ext.emptyFn,
		disableFields: Ext.emptyFn,
		enableFields: Ext.emptyFn,
		disableButtons: Ext.emptyFn,
		enableButtons: Ext.emptyFn,
		cleanFields: Ext.emptyFn,
		fillFieldsWith: Ext.emptyFn,
		getFieldsValue: Ext.emptyFn,
		setDelegate: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardPropertiesDelegate", {
		onAbortButtonClick:Ext.emptyFn,
		onSaveButtonClick: Ext.emptyFn,
		onModifyButtonClick: Ext.emptyFn,
		onRemoveButtonClick: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardPropertiesPanel", {
		extend: "Ext.panel.Panel",

		alias: "widget.dashboardproperties",

		mixins: {
			cminterface: "CMDBuild.view.administration.dashboard.CMDashboardPropertiesPanelInterface"
		},

		constructor: function() {
			this.callParent(arguments);
		},

		initComponent : function() {
			var me = this;

			Ext.apply(this, {
				title: CMDBuild.Translation.administration.modClass.tabs.properties,
				layout: "border",
				buttonAlign: "center",
				tbar: tbar(me),
				frame: false,
				border: false,
				cls: "x-panel-body-default-framed",
				bodyCls: 'cmgraypanel',
				items: [],
				buttons: buttons(me)
			});

			this.on("render", function() {
				me.add({
					xtype: "form",
					region: "center",
					frame : true,
					border : true,
					autoScroll: true,
					items: fields(me)
				});
			}, {single: true});

			this.callParent(arguments);
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.view.administration.dashboard.CMDashboardPropertiesDelegate");
			this.delegate = d;
		},

		disableTBarButtons: function() {
			this.modifyButton.disable();
			this.removeButton.disable();
		},

		enableTBarButtons: function() {
			this.modifyButton.enable();
			this.removeButton.enable();
		},

		disableFields: function() {
			this.nameField.disable();
			this.descriptionField.disable();
			this.groupsSelectionList.disable();
		},

		enableFields: function(all) {
			if (all) {
				this.nameField.enable();
			}
			this.descriptionField.enable();
			this.groupsSelectionList.enable();
		},

		disableButtons: function() {
			this.saveButton.disable();
			this.abortButton.disable();
		},

		enableButtons: function() {
			this.saveButton.enable();
			this.abortButton.enable();
		},

		cleanFields: function() {
			this.nameField.reset();
			this.descriptionField.reset();
			this.groupsSelectionList.reset();
		},

		fillFieldsWith: function(obj) {
			if (typeof obj != "object") {
				obj = {};
			}

			Ext.applyIf(obj, {
				name: "",
				description: "",
				groups: []
			});

			this.nameField.setValue(obj.name);
			this.descriptionField.setValue(obj.description);
			this.groupsSelectionList.setValue(obj.groups);
		},

		getFieldsValue: function() {
			/*
			 * to mark as wrong if empty
			 */
			this.nameField.isValid();
			this.descriptionField.isValid();

			return {
				name: this.nameField.getValue(),
				description: this.descriptionField.getValue(),
				groups: this.groupsSelectionList.getValue()
			}
		}
	});

	function tbar(me) {

		me.modifyButton = new Ext.button.Button({
			text: CMDBuild.Translation.administration.modDashboard.properties.modify,
			iconCls: "modify",
			disabled: true,
			handler: function() {
				me.delegate.onModifyButtonClick();
			}
		});

		me.removeButton = new Ext.button.Button({
			text: CMDBuild.Translation.administration.modDashboard.properties.remove,
			iconCls: "delete",
			disabled: true,
			handler: function() {
				me.confirm = Ext.Msg.show({
					title: CMDBuild.Translation.administration.modDashboard.properties.remove,
					msg: CMDBuild.Translation.common.confirmpopup.areyousure,
					buttons: Ext.Msg.YESNO,
					fn: function(button) {
						if (button == "yes") {
							me.delegate.onRemoveButtonClick();
						}
					}
				});
			}
		});

		return [me.modifyButton, me.removeButton];
	}

	function fields(me) {
		me.nameField = new Ext.form.field.Text({
			fieldLabel: CMDBuild.Translation.administration.modDashboard.properties.fields.name,
			name: "name",
			allowBlank: false,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			disabled: true
		});

		me.descriptionField = new Ext.form.CMTranslatableText({
			fieldLabel:CMDBuild.Translation.administration.modDashboard.properties.fields.description,
			name: "description",
			allowBlank: false,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			translationsKeyType: "Dashboard", 
			translationsKeyField: "Description",
			disabled: true
		});

		me.groupsSelectionList = new CMDBuild.view.common.field.CMGroupSelectionList({
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			dataFields : ['name', 'description'],
			valueField : 'name',
			height: 300,
			disabled: true
		});

		return [me.nameField, me.descriptionField, me.groupsSelectionList];
	}

	function buttons(me) {
		me.saveButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.save,
			disabled: true,
			handler: function() {
				me.delegate.onSaveButtonClick();
			}
		});

		me.abortButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.abort,
			disabled: true,
			handler: function() {
				me.delegate.onAbortButtonClick();
			}
		});

		return [me.saveButton, me.abortButton];
	}
})();