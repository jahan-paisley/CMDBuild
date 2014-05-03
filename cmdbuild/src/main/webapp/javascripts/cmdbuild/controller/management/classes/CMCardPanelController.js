(function() {
	Ext.define("CMDBuild.controller.management.classes.CMCardPanelController", {

		mixins : {
			observable : "Ext.util.Observable"
		},

		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

		constructor: function(view, supercontroller, widgetControllerManager) {

			this.callParent(arguments);

			this.mixins.observable.constructor.call(this, arguments);

			this.CMEVENTS = Ext.apply(this.CMEVENTS,  {
				cardRemoved: "cm-card-removed",
				cloneCard: "cm-card-clone"
			});

			this.addEvents(
				this.CMEVENTS.cardRemoved,
				this.CMEVENTS.cloneCard,
				this.CMEVENTS.cardSaved,
				this.CMEVENTS.editModeDidAcitvate,
				this.CMEVENTS.displayModeDidActivate
			);

			var ev = this.view.CMEVENTS;
			this.mon(this.view, ev.removeCardButtonClick, this.onRemoveCardClick, this);
			this.mon(this.view, ev.cloneCardButtonClick, this.onCloneCardClick, this);
			this.mon(this.view, ev.printCardButtonClick, this.onPrintCardMenuClick, this);
			this.mon(this.view, ev.openGraphButtonClick, this.onShowGraphClick, this);
		},

		onEntryTypeSelected: function() {
			this.cloneCard = false;
			this.callParent(arguments);
		},

		onCardSelected: function() {
			this.cloneCard = false;
			this.callParent(arguments);
		},

		onRemoveCardClick: function() {
			var me = this,
				idCard = me.card.get("Id"),
				idClass = me.entryType.get("id");

			function makeRequest(btn) {
				if (btn != 'yes') {
					return;
				}

				CMDBuild.LoadMask.get().show();
				CMDBuild.ServiceProxy.card.remove({
					params : {
						IdClass: idClass,
						Id: idCard
					},
					success : function() {
						me.fireEvent(me.CMEVENTS.cardRemoved, idCard, idClass);
					},
					callback : function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			};

			Ext.Msg.confirm(CMDBuild.Translation.management.findfilter.msg.attention, CMDBuild.Translation.management.modcard.delete_card_confirm , makeRequest, this);
		},

		onCloneCardClick: function() {
			this.cloneCard = true;
			this.onModifyCardClick();
			this.fireEvent(this.CMEVENTS.cloneCard);
		},

		changeClassUIConfigurationForGroup: function(disabledForGroupButtons) {
			this.view.form.modifyCardButton.disabledForGroup = disabledForGroupButtons.modify;
			this.view.form.cloneCardButton.disabledForGroup = disabledForGroupButtons.clone;
			this.view.form.deleteCardButton.disabledForGroup = disabledForGroupButtons.remove;
			if (this.view.form.modifyCardButton.disabledForGroup)
				this.view.form.modifyCardButton.disable();
			else
				this.view.form.modifyCardButton.enable();
			if (this.view.form.cloneCardButton.disabledForGroup)
				this.view.form.cloneCardButton.disable();
			else
				this.view.form.cloneCardButton.enable();
			if (this.view.form.deleteCardButton.disabledForGroup)
				this.view.form.deleteCardButton.disable();
			else
				this.view.form.deleteCardButton.enable();
		},
		
		onModifyCardClick: function() {
			// If wanna clone the card
			// skip the locking
			if (this.cloneCard
					&& this.isEditable(this.card)) {

				this.view.editMode();
			} else {
				this.callParent(arguments);
			}
		},

		onAbortCardClick: function() {
			if (this.cloneCard) {
				// Set the current card to null
				// like if wanna add a new card
				// Than is possible select again
				// the card that you are try to clone
				_CMCardModuleState.setCard(null);
			} else {
				this.callParent(arguments);
			}

			_CMUIState.onlyGridIfFullScreen();
		},

		onSaveSuccess: function() {
			this.cloneCard = false;
			this.callParent(arguments);
			_CMUIState.onlyGridIfFullScreen();
		},

		onPrintCardMenuClick: function(format) {
			if (typeof format != "string") {
				return;
			}

			var me = this;
			var params = {};
			params[_CMProxy.parameter.CLASS_NAME] = me.entryType
					.getName();
			params[_CMProxy.parameter.CARD_ID] = me.card
					.get("Id");
			params[_CMProxy.parameter.FORMAT] = format;

			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				url: 'services/json/management/modreport/printcarddetails',
				params: params,
				method: 'GET',
				scope: this,
				success: function(response) {
					var popup = window.open(
							"services/json/management/modreport/printreportfactory", //
							"Report", //
							"height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable"); //

					if (!popup) {
						CMDBuild.Msg.warn( //
							CMDBuild.Translation.warnings.warning_message, //
							CMDBuild.Translation.warnings.popup_block //
						); //
					}

				},
				callback: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	});
})();