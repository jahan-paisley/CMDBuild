Ext.define("CMDBuild.controller.management.common.CMCardWindowController", {

	extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

	mixins: {
		observable : "Ext.util.Observable"
	},

	/**
	 * conf: {
	 * 	entryType: id of the entry type,
	 *  card: id of the card,
	 *  cmEditMode: boolean
	 * }
	 * */
	constructor: function(view, conf) {
		if (typeof conf.entryType == "undefined") {
			return;
		}

		this.mixins.observable.constructor.call(this, arguments);

		this.callParent(arguments);
		this.onEntryTypeSelected(_CMCache.getEntryTypeById(conf.entryType));

		this.cmEditMode = conf.cmEditMode;

		var me = this;
		this.mon(me.view, "show", function() {
			me.loadFields(conf.entryType, function() {
				if (conf.card) {
					var parameterNames = CMDBuild.ServiceProxy.parameter;
					var params = {};
					params[parameterNames.CARD_ID] = conf.card;
					params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(conf.entryType);

					me.loadCard(loadRemote=true, params, function(card) {
						me.onCardLoaded(me, card);
					});
				} else {
					me.editModeIfPossible();
				}
			});
		});

		this.mon(this.view, "destroy", function() {
			me.unlockCard();
		});
	},

	getForm: function() {
		return this.view.cardPanel.getForm();
	},

	onSaveCardClick: function() {
		var form = this.getForm(),
			params = this.buildSaveParams();

		this.beforeRequest(form);

		if (form.isValid()) {
			this.doFormSubmit(params);
		}
	},

	onAbortCardClick: function() {
		this.view.destroy();
	},

	onEntryTypeSelected: function(entryType) {
		this.callParent(arguments);
		this.view.setTitle(this.entryType.get("text"));
	},

	// protected
	buildSaveParams: function() {
		var parameter = _CMProxy.parameter;
		var params = {};
		params[parameter.CLASS_NAME] = this.entryType.getName();
		params[parameter.CARD_ID] = this.card ? this.card.get("Id") : -1;

		return params;
	},

	// protected
	onSaveSuccess: function(form, action) {
		CMDBuild.LoadMask.get().hide();
		_CMCache.onClassContentChanged(this.entryType.get("id"));
		this.view.destroy();
	},

	// protected
	onCardLoaded: function(me, card) {
		me.card = card;
		me.view.loadCard(card);
		if (me.widgetControllerManager) {
			me.widgetControllerManager.buildControllers(card);
		}

		me.editModeIfPossible();
	},

	// template to override in subclass
	beforeRequest: Ext.emptyFn,

	editModeIfPossible: function() {
		var me = this;

		if (!me.card) {
		// here add a new card, so there is
		// nothing to lock
			me.view.editMode();
		} else if (me.cmEditMode) {
			me.lockCard(function() {
				me.view.editMode();
			});
		} else {
			me.view.displayMode();
		}
	}

});