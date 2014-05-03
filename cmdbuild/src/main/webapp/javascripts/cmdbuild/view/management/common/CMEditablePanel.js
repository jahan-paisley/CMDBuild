(function() {
	var RIGHT_PADDING_FOR_MAX_FIELD_WIDTH = 40;
	var STATE = {
		edit: "editMode",
		display: "displayMode"
	};

	Ext.define("CMDBuild.Management.EditablePanel", {
		extend: "Ext.panel.Panel",
		attributes: undefined, //passed on new
		layout: "card",
		activeItem: 0,
		hideMode: "offsets",
		autoScroll: false,

		_state: STATE.display,
		_stateSwitchFail: false,

		initComponent : function() {
			var editSubpanel = null,
				displaySubpanel = new CMDBuild.Management.EditablePanel.SubPanel({
					editable: false,
					attributes: this.attributes
				});

			this.items = [displaySubpanel];

			// privileged functions

			// to defer the editSubPanel initializzation
			// is public because is called from outside when
			// a widget need the form
			this.ensureEditPanel = function() {
				if (editSubpanel == null) {
					editSubpanel = new CMDBuild.Management.EditablePanel.SubPanel({
						attributes: this.attributes
					});

					this.add(editSubpanel);
				}
			}

			this.editMode = function() {
				this.ensureEditPanel();

				var layout = this.getLayout();
				this._state = STATE.edit;

				if (layout.setActiveItem) {
					try {
						layout.setActiveItem(editSubpanel.id);
					} catch (e) {
						this._stateSwitchFail = true;
					}
				}
			};

			this.displayMode = function() {
				var layout = this.getLayout();
				this._state = STATE.display;

				if (layout.setActiveItem) {
					try {
						layout.setActiveItem(displaySubpanel.id);
					} catch (e) {
						this._stateSwitchFail = true;
					}
				}
			};

			this.isEmpty = function() {
				return (displaySubpanel.fields().length == 0);
			};

			this.getFields = function() {
				this.ensureEditPanel();
				return editSubpanel.fields();
			};

			this.callParent(arguments);

			this.mon(this, "activate", function() {
				if (this._stateSwitchFail) {
					this[this._state]();
				}
			}, this);
		}
	});

	Ext.define("CMDBuild.Management.EditablePanel.SubPanel", {
		extend: "Ext.panel.Panel",
		frame: false,
		border: false,
		bodyCls: "x-panel-body-default-framed",
		autoScroll: true,
		labelAlign: "right",
		labelWidth: 160,
		attributes: undefined, //configuration
		editable: true,
		hideMode: "offsets",

		fields: function() {
			if (this.items && this.items.items) {
				return this.items.items;
			} else {
				return [];
			}
		},

		initComponent: function() {
			this.callParent(arguments);

			if (this.attributes) {
				addFields.call(this, this.attributes, this.editable);
			}

		}
	});

	CMDBuild.Management.EditablePanel.build = function(conf) {
		var panel = new CMDBuild.Management.EditablePanel(conf);
		if (panel.isEmpty()) {
			delete panel;
			return null;
		} else {
			return panel;
		}
	}

	function addFields(attributes, editable) {
		if (attributes) {
			for (var i=0; i<attributes.length; ++i) {
				var attribute = attributes[i];
				var field;

				if (editable) {
					field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, this.readOnlyForm);
				} else {
					field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, true); //true to have a displayField
				}

				if (field) {
					// add a flag to the field to know directly
					// if belongs to a panel in edit mode
					field._belongToEditableSubpanel = this.editable;
					this.add(field);
				}
			}
		}
	};
})();
