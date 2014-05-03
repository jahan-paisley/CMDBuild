(function() {
	Ext.define("CMDBuild.controller.management.classes.CMBaseCardPanelController", {

		mixins : {
			observable : "Ext.util.Observable"
		},

		extend: "CMDBuild.controller.management.classes.CMModCardSubController",

		cardDataProviders: [],

		constructor: function(view, supercontroller, widgetControllerManager) {

			this.callParent(arguments);

			this.mixins.observable.constructor.call(this, arguments);

			var ev = this.view.CMEVENTS;

			if (widgetControllerManager) {
				this.widgetControllerManager = widgetControllerManager;
			} else {
				var widgetManager = new CMDBuild.view.management.common.widgets.CMWidgetManager(this.view);
				this.widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerController(widgetManager);
			}

			this.widgetControllerManager.setDelegate(this);

			this.CMEVENTS = {
				cardSaved: "cm-card-saved",
				abortedModify: "cm-card-modify-abort",
				editModeDidAcitvate: ev.editModeDidAcitvate,
				displayModeDidActivate: ev.displayModeDidActivate
			};

			this.addEvents(this.CMEVENTS.cardSaved, this.CMEVENTS.abortedModify, ev.editModeDidAcitvate, ev.displayModeDidActivate);
			this.relayEvents(this.view, [ev.editModeDidAcitvate, ev.displayModeDidActivate]);

			this.mon(this.view, ev.modifyCardButtonClick, function() { this.onModifyCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.saveCardButtonClick, function() { this.onSaveCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.abortButtonClick, function() { this.onAbortCardClick.apply(this, arguments); }, this);
			this.mon(this.view, ev.widgetButtonClick, this.onWidgetButtonClick, this);
			this.mon(this.view, ev.editModeDidAcitvate, this.onCardGoesInEdit, this);
		},

		onEntryTypeSelected: function() {
			this.unlockCard();

			if (this.view.isInEditing()) {
				this.view.displayMode();
			}

			this.callParent(arguments);
			this.loadFields(this.entryType.get("id"));

			if (this.widgetControllerManager) {
				this.widgetControllerManager.removeAll();
			}
		},

		onCardSelected: function(card) {
			this.unlockCard();

			this.callParent(arguments);

			if (this.view.isInEditing()) {
				this.view.displayMode();
			}

			this.view.reset();

			if (!this.entryType || !this.card) { return; }

			// If the current entryType is a superclass the record has only the value defined
			// in the super class. So, load the card remotly and pass it to the form.
			var loadRemoteData = this.entryType.get("superclass") && this.card.get("Id") != -1;

			// If the entryType id and the id of the card are different
			// the fields are not right, refill the form before the loadCard
			var reloadFields = this.entryType.get("id") != this.card.get("IdClass");

			// defer this call to release the UI event manage
			Ext.defer(buildWidgetControllers, 1, this, [card]);

			var me = this;
			if (reloadFields) {
				this.loadFields(this.card.get("IdClass"), function() {
					me.loadCard(loadRemoteData);
				});
			} else {
				me.loadCard(loadRemoteData);
			}
		},

		onModifyCardClick: function() {
			if (this.isEditable(this.card)) {
				var me = this;

				this.lockCard(function() {
					me.view.editMode();
				});
			}
		},

		onSaveCardClick: function() {

			var me = this;
			var params = {};
			params[_CMProxy.parameter.CARD_ID] = this.cloneCard ? -1 : this.card.get("Id");
			params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.card.get("IdClass"));

			addDataFromCardDataPoviders(me, params);

			if (thereAraNotWrongAttributes(me)) {
				this.doFormSubmit(params);
			}
		},

		doFormSubmit: function(params) {
			var form = this.view.getForm();
/*			var values = form.getFieldValues();
			var arValues = [];
			for (var key in values) {
				if (values[key][0] === undefined)
					break;
				if (values[key][1] === undefined) {
					var ob = {id: key, value: values[key][0]};
					arValues.push(ob);
				}
				else {
					var ob = {id: key, value: values[key][1]};
					arValues.push(ob);
					
				}
			}
			form.setValues(arValues);*/
			CMDBuild.LoadMask.get().show();
			form.submit({
				method : 'POST',
				url : 'services/json/management/modcard/updatecard',
				scope: this,
				params: params,
				success : this.onSaveSuccess,
				failure : function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		onSaveSuccess: function(form, operation) {
			var me = this;
			CMDBuild.LoadMask.get().hide();
			me.view.displayMode();
			var cardData = {
				Id: operation.result.id || me.card.get("Id"),// if is a new card, the id is given by the request
				IdClass: me.entryType.get("id")
			};

			me.fireEvent(me.CMEVENTS.cardSaved, cardData);
		},

		onAbortCardClick: function() {
			if (this.card && this.card.get("Id") == -1) {
				this.onCardSelected(null);
			} else {
				this.onCardSelected(this.card);
			}

			this.fireEvent(this.CMEVENTS.abortedModify);
		},

		onAddCardButtonClick: function(classIdOfNewCard) {
			if (!classIdOfNewCard) {
				return;
			}

			this.onCardSelected(new CMDBuild.DummyModel({
				IdClass: classIdOfNewCard,
				Id: -1
			}));

			this.view.editMode();
		},

		addCardDataProviders: function(dataProvider) {
			this.cardDataProviders.push(dataProvider);
		},

		loadFields: function(entryTypeId, cb) {
			var me = this;
			_CMCache.getAttributeList(entryTypeId, function(attributes) {
				me.view.fillForm(attributes, editMode = false);
				if (cb) {
					cb();
				}
			});
		},

		loadCard: function(loadRemoteData, params, cb) {
			var me = this;
			var cardId;

			if (params) {
				cardId = params.Id || params.cardId;
			} else {
				cardId = me.card.get("Id");
			}

			if (cardId && cardId != "-1" 
				&& (loadRemoteData || me.view.hasDomainAttributes())) {

				if (!params) {
					var parameterNames = CMDBuild.ServiceProxy.parameter;
					var params = {};
					params[parameterNames.CARD_ID] = me.card.get("Id");
					params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.card.get("IdClass"));
				}

				CMDBuild.ServiceProxy.card.get({
					params: params,
					success: function(a,b, response) {
						var data = response.card;
						if (me.card) {
							// Merge the data of the selected card with
							// the remote data loaded from the server.
							// the reason is that in the activity list
							// the card have data that are not returned from the
							// server, so use the data already in the record.
							// For activities, the privileges returned from the
							// server are of the class and not of the activity
							data = CMDBuild.Utils.mergeCardsData((me.card.raw || me.card.data), data);
						}

						addRefenceAttributesToDataIfNeeded(response.referenceAttributes, data);
						var card = new CMDBuild.DummyModel(data);
						(typeof cb == "function") ? cb(card) : me.loadCardStandardCallBack(card)
					}
				});
			} else {
				me.loadCardStandardCallBack(me.card);
			}
		},

		loadCardStandardCallBack: function(card) {
			var me = this;
			me.view.loadCard(card);
			if (card) {
				if (me.isEditable(card)) {
					if (card.get("Id") == -1 || me.cmForceEditing) {
						me.view.editMode();
						me.cmForceEditing = false;
					} else {
						me.view.displayMode(enableTBar = true);
					}
				} else {
					me.view.displayModeForNotEditableCard();
				}
			}
		},

		isEditable: function(card) {
			return _CMUtils.getEntryTypePrivilegesByCard(card).create;
		},

		setWidgetManager: function(wm) {
			this.widgetManager = wm;
		},

		onWidgetButtonClick: function(w) {
			if (this.widgetControllerManager) {
				this.widgetControllerManager.onWidgetButtonClick(w);
			}
		},

		onCardGoesInEdit: function() {
			if (this.widgetControllerManager) {
				this.widgetControllerManager.onCardGoesInEdit();
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

		// override
		onCloneCard: Ext.emptyFn,

		// widgetManager delegate
		ensureEditPanel: function() {
			this.view.ensureEditPanel();
		}
	});

	Ext.define("CMDBuild.controller.management.classes.CMCardDataProvider", {
		/*
		 * Extending this class you have to
		 * get a value to the cardDataName attribute
		 */
		cardDataName: null,

		getCardDataName: function() {
			return this.cardDataName;
		},

		/*
		 * Implement it on subclasses
		 */
		getCardData: function() {
			throw "You have to implement the getCardData method in " + this.$className;
		}
	});

	function buildWidgetControllers(card) {
		if (this.widgetControllerManager) {
			this.widgetControllerManager.buildControllers(card);
		}
	}

	function addDataFromCardDataPoviders(me, params) {
		for (var provider in me.cardDataProviders) {
			provider = me.cardDataProviders[provider];
			if (typeof provider.getCardData == "function") {
				var values = provider.getCardData();
				if (values) {
					params[provider.getCardDataName()] = values;
				}
			}
		}

		return params;
	}

	function thereAraNotWrongAttributes(me) {
		var form = me.view.getForm();
		var invalidAttributes = CMDBuild.controller.common.CardStaticsController.getInvalidAttributeAsHTML(form);
		if (invalidAttributes != null) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + invalidAttributes, false);
			return false;
		} else {
			return true;
		}
	}

	function addRefenceAttributesToDataIfNeeded(referenceAttributes, data) {
		// the referenceAttributes are like this:
		//	referenceAttributes: {
		//		referenceName: {
		//			firstAttr: 32,
		//			secondAttr: "Foo"
		//		},
		//		secondReference: {...}
		//	}
		var ra = referenceAttributes;
		if (ra) {
			for (var referenceName in ra) {
				var attrs = ra[referenceName];
				for (var attribute in attrs) {
					data["_" + referenceName + "_" + attribute] = attrs[attribute];
				}
			}
		}
	}

})();