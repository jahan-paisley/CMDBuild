(function() {

	Ext.define("CMDBuild.view.administration.widget.CMWidgetDefinitionPanelInterface", {
		extend: "Ext.panel.Panel",
		EXCEPTIONS: {
			notAWidget: function(widgetName) { return widgetName + " is not a widget configuration module"; },
			notAWidgetModel: function(o) { return o + " is not a CMDBuild.model.CMWidgetDefinitionModel"; }
		},
		initComponent: function() {
			this.callParent(arguments);
			this.addEvents("cm-add", "cm-save", "cm-abort", "cm-modify", "cm-remove", "select", "deselect");
		},
		getWidgetDefinition: Ext.emptyFn,
		buildWidgetForm: Ext.emptyFn,
		addRecordToGrid: Ext.emptyFn,
		removeRecordFromGrid: Ext.emptyFn,
		enableModify: Ext.emptyFn,
		disableModify: Ext.emptyFn,
		reset: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.administration.widget.CMWidgetDefinitionPanel", {
		extend: "CMDBuild.view.administration.widget.CMWidgetDefinitionPanelInterface",
		title: CMDBuild.Translation.administration.modClass.widgets.title,
		initComponent: function() {

			this.form = new CMDBuild.view.administration.widget.CMWidgetDefinitionForm({
				frame: false,
				border: true,
				region: "center",
				bodyCls: "x-panel-body-default-framed cmborder"
			});

			this.addButton = new CMDBuild.view.administration.widget.CMAddWidgetDefinitionButton();

			this.grid = new CMDBuild.view.administration.widget.CMWidgetDefinitionGrid({
				region: "north",
				height: "30%",
				split: true,
				tbar: [this.addButton]
			});

			var me = this;
			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save,
				handler: function() {
					me.fireEvent("cm-save");
				}
			});

			this.abortButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					me.fireEvent("cm-abort");
				}
			});

			Ext.apply(this, {
				layout: "border",
				border: false,
				frame: true,
				buttonAlign: "center",
				buttons: [this.saveButton, this.abortButton],
				items: [this.form, this.grid]
			});

			this.callParent(arguments);
			this.relayEvents(this.form, ["cm-modify", "cm-remove"]);
			this.relayEvents(this.grid, ["select", "deselect"]);
			this.relayEvents(this.addButton, ["cm-add"]);

			this.disableModify(enableToolbar = false);
		},

		buildWidgetForm: function(widgetName) {
			var widgetClass = findWidgetClass(widgetName);
			if (widgetClass) {
				var widget = widgetClass.create({
					border: false,
					frame: false,
					padding: 5,
					bodyCls: "x-panel-body-default-framed"
				});
			} else {
				delete this.widgetForm;
				throw this.EXCEPTIONS.notAWidget(widgetName);
			}

			this.form.removeAll();
			this.form.add(widget);
			this.widgetForm = widget;

			return widget;
		},

		addRecordToGrid: function(record, selectAfter) {
			if (record.$className == "CMDBuild.model.CMWidgetDefinitionModel") {
				this.grid.addRecord(record, selectAfter);
			} else {
				throw this.EXCEPTIONS.notAWidgetModel(record);
			}
		},

		removeRecordFromGrid: function(recordId) {
			this.grid.removeRecordWithId(recordId);
		},

		enableModify: function() {
			this.form.enableModify();

			this.saveButton.enable();
			this.abortButton.enable();
			this.fireEvent("cm-enable-modify");
		},

		disableModify: function(enableCMTbar) {
			this.saveButton.disable();
			this.abortButton.disable();
			this.form.disableModify(enableCMTbar);
		},

		reset: function(removeAllRecords) {
			if (removeAllRecords) {
				this.grid.removeAllRecords();
			} else {
				this.grid.clearSelection();
			}
			this.form.reset();
			this.disableModify(enableToolbar = false);
		},

		getWidgetDefinition: function() {
			if (this.widgetForm) {
				return this.widgetForm.getWidgetDefinition();
			} else {
				return {};
			}
		}
	});

	function findWidgetClass(widgetName) {
		var widgetClass = null;
		var _package = CMDBuild.view.administration.widget.form;
		for (var key in _package) {
			if (_package[key].WIDGET_NAME == widgetName) {
				widgetClass = _package[key];
				break;
			}
		}

		return widgetClass;
	}
})();