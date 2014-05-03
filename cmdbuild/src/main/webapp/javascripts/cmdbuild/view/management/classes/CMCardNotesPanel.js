(function() {

Ext.define("CMDBuild.view.management.classes.CMCardNotesPanel", {
	extend: "Ext.panel.Panel",

	translation : CMDBuild.Translation.management.modcard,

	withButtons: true, // used in the windows to have specific buttons
	
	initComponent : function() {
		this._editMode = false;
		
		this.CMEVENTS = {
			saveNoteButtonClick: "cm-save-clicked",
			cancelNoteButtonClick: "cm-cancel-clicked",
			modifyNoteButtonClick: "cm-modify.clicked"
		};

		var me = this;
		this.modifyNoteButton = new Ext.button.Button( {
			iconCls : 'modify',
			text : this.translation.modify_note,
			handler : function() {
				me.fireEvent(me.CMEVENTS.modifyNoteButtonClick);
			}
		});

		var htmlField = new Ext.form.field.HtmlEditor({
			name : 'Notes',
			border: false,
			frame: false,
			hideLabel: true,
			enableLinks: false,
			enableSourceEdit: false,
			enableFont: false
		});

		this.actualForm = new Ext.form.Panel({
			hideMode: "offsets",
			layout: 'fit',
			border: false,
			frame: false,
			bodyCls: "x-panel-body-default-framed",
			hideMode: 'offsets',
			items: [htmlField],
			setValue: function(v) {
				htmlField.setValue(v || "");
			},
			getValue: function() {
				return htmlField.getValue();
			}
		});

		var displayField = new Ext.form.field.Display({
			padding: "0 0 5px 5px",
			name : 'Notes',
			xtype : 'displayfield',
			anchor: '95%'
		});

		this.displayPanel = new Ext.form.Panel({
			hideMode: "offsets",
			autoScroll: true,
			hideMode: "offsets",
			frame: false,
			bodyCls: "x-panel-body-default-framed",
			items: [displayField],
			setValue: function(v) {
				displayField.setValue(v);
			},
			getValue: function() {
				return displayField.getValue();
			}
		});

		this.buildButtons();

		Ext.apply(this, {
			hideMode: "offsets",
			frame: false,
			border: false,
			cls: "x-panel-body-default-framed",
			layout: 'card',
			tbar: [this.modifyNoteButton],
			items: [
				this.displayPanel
				,this.actualForm
			],
			buttonAlign: 'center'
		});

		this.callParent(arguments);
	},

	buildButtons: function() {
		if (this.withButtons) {
			var me = this;
			this.buttons = [
				this.saveButton = new Ext.button.Button({
					text : CMDBuild.Translation.common.buttons.save,
					name: 'saveButton',
					formBind : true,
					handler: function() {
						me.fireEvent(me.CMEVENTS.saveNoteButtonClick);
					}
				}),

				this.cancelButton = new Ext.button.Button({
					text : CMDBuild.Translation.common.buttons.abort,
					name: 'cancelButton',
					handler : function() {
						me.fireEvent(me.CMEVENTS.cancelNoteButtonClick);
					}
				})
			];
			var extra = this.getExtraButtons();
			if (extra) {
				if (Ext.isArray(extra)) {
					this.buttons = this.buttons.concat(extra);
				} else {
					this.buttons.push(extra);
				}
			}
		}
	},

	reset: function() {
		this.actualForm.getForm().reset();
		this.displayPanel.getForm().reset();
	},

	loadCard: function(card) {
		this.actualForm.getForm().loadRecord(card);
		this.displayPanel.getForm().loadRecord(card);
	},

	getForm: function() {
		return this.actualForm.getForm();
	},

	syncForms: function() {
		var v = this.actualForm.getValue();
		this.displayPanel.setValue(v);

		return v;
	},

	disableModify: function() {
		if (this.privWrite) {
			this.modifyNoteButton.enable();
		} else {
			this.modifyNoteButton.disable();
		}

		if (this.withButtons) {
			this.saveButton.disable();
			this.cancelButton.disable();
		}

		this.getLayout().setActiveItem(this.displayPanel);
		this._editMode = false;
	},

	enableModify: function() {
		this.modifyNoteButton.disable();
		if (this.withButtons) {
			this.saveButton.enable();
			this.cancelButton.enable();
		}
		this.getLayout().setActiveItem(this.actualForm);
		this.actualForm.setValue(this.displayPanel.getValue());
		this._editMode = true;
	},

	updateWritePrivileges: function(privWrite) {
		this.privWrite = privWrite;
	},

	isInEditing: function() {
		return this._editMode;
	},

	// to implement in subclass to have extra button on instantiation
	getExtraButtons: Ext.emptyFn,

	
	// DEPRECATED

	reloadCard: function(eventParams) { _deprecated();
		this.enable();
	},

	onAddCardButtonClick: function() { _deprecated();
		this.disable();
	},

	onClassSelected: function() { _deprecated();
		this.disableModify();
		this.disable();
	},
	
	onCardSelected: function(card) { _deprecated();
		var idClass = card.raw.IdClass;
		if (CMDBuild.Utils.isSimpleTable(idClass)) {
			this.disable();
			return;
		} else {
			this.enable();
		}
	
		this.currentCardId = card.get("Id");
		this.currentCardPrivileges = {
			create: card.raw.priv_create,
			write: card.raw.priv_write
		};
	
		this.reset();
		this.loadCard(card);
	
		this.disableModify();
	}
});

})();