(function() {
	Ext.define("CMDBuild.controller.management.classes.CMNoteController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",
		constructor: function(view, supercontroller) {

			this.mixins.observable.constructor.call(this, arguments);

			this.callParent(arguments);

			this.CMEVENTS = {
				noteWasSaved: "cm-note-saved"
			};

			this.mon(this.view, this.view.CMEVENTS.modifyNoteButtonClick, this.onModifyNoteClick, this);
			this.mon(this.view, this.view.CMEVENTS.saveNoteButtonClick, this.onSaveNoteClick, this);
			this.mon(this.view, this.view.CMEVENTS.cancelNoteButtonClick, this.onCancelNoteClick, this);

			this.addEvents(this.addEvents.noteWasSaved);
		},

		onEntryTypeSelected: function() {
			this.unlockCard();
			this.callParent(arguments);
			this.view.disable();
		},

		onCardSelected: function(card) {
			this.unlockCard();
			this.callParent(arguments);
			this.updateView(card);

			if (this.disableTheTabBeforeCardSelection(card)) {
				this.view.disable();
			} else {
				this.view.enable();
				this.view.loadCard(card);
			}
		},

		disableTheTabBeforeCardSelection: function(card) {
			return !card || CMDBuild.Utils.isSimpleTable(card.get("IdClass"));
		},

		updateView: function(card) {
			this.updateViewPrivilegesForCard(card);
			this.view.reset();
			this.view.disableModify();
		},

		updateViewPrivilegesForCard: function(card) {
			var privileges = _CMUtils.getEntryTypePrivilegesByCard(card);
			this.view.updateWritePrivileges(privileges.write);
		},

		onSaveNoteClick: function() {
			var me = this,
				form = me.view.getForm(),
				params = me._getSaveParams();

			if (form.isValid() && me.beforeSave(me.card)) {
				CMDBuild.LoadMask.get().show();
				form.submit({
					method : 'POST',
					url : 'services/json/management/modcard/updatecard',
					params: params,
					success : function() {
						CMDBuild.LoadMask.get().hide();
						me.view.disableModify(enableToolbar = true);
						var val = me.view.syncForms();
						me.syncSavedNoteWithModel(me.card, val);
						me.fireEvent(me.CMEVENTS.noteWasSaved, me.card);
					},
					failure: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		},

		onCancelNoteClick: function() {
			this.onCardSelected(this.card);
			this.view.disableModify(couldModify = isEditable(this.card));
		},

		onModifyNoteClick: function() {
			if (isEditable(this.card)) {
				var me = this;

				this.lockCard(function() {
					me.view.enableModify();
				});
			}
		},

		// called before the save request
		// override in subclass, return false to avoid the save
		beforeSave: function(card) {
			return true;
		},

		_getSaveParams: function() {
			var params = {};
			var me = this;
			if (this.card) {
				params[_CMProxy.parameter.CARD_ID] = me.card.get("Id");
				params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.card.get("IdClass"));
			}

			return params;
		},

		syncSavedNoteWithModel: function(card, val) {
			card.set("Notes", val);
			card.commit();
			if (card.raw) {
				card.raw["Notes"] = val;
			}
		},

		lockCard: function(success) {
			if (_CMUtils.lockCard.isEnabled()) {
				if (this.card) {
					var id = this.card.get("Id");
					_CMProxy.card.lockCard({
						params: {
							id: id
						},
						success: success,
						failure: function() {
							return false;
						}
					});
				}
			} else {
				success();
			}
		},

		unlockCard: function() {
			if (_CMUtils.lockCard.isEnabled()) {
				if (this.card
						&& this.view.isInEditing()) {
					
					var id = this.card.get("Id");
					_CMProxy.card.unlockCard({
						params: {
							id: id
						}
					});
				}
			}
		},
	});

	function isEditable(card) {
		return _CMUtils.getEntryTypePrivilegesByCard(card).write;
	}

	Ext.define("CMDBuild.view.management.common.CMNoteWindowController", {
		extend: "CMDBuild.controller.management.classes.CMNoteController",
		constructor: function() {
			this.callParent(arguments);
		},

		onCardSelected: function(card) {
			this.callParent(arguments);
			var title = "";

			if (this.card) {
				title = Ext.String.format("{0} - {1}"
					, CMDBuild.Translation.management.modcard.tabs.notes 
					, this.card.get("Description"));
			}

			this.view.setTitle(title);
		}
	});
})();