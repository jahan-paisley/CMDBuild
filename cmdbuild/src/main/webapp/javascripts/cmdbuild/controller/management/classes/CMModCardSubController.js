Ext.define("CMDBuild.controller.management.classes.CMModCardSubController", {

	mixins : {
		observable : "Ext.util.Observable"
	},

	constructor : function(view, supercontroller) {

		this.mixins.observable.constructor.call(this, arguments);

		if (typeof view == "undefined") {
			throw ("OOO snap, you have not passed a view to me");
		} else {
			this.view = view;
		}

		this.superController = supercontroller;
		this.card = null;
		this.entryType = null;

		this.buildCardModuleStateDelegate();
	},

	onEntryTypeSelected : function(entryType) {
		this.entryType = entryType;
	},

	onCardSelected : function(card) {
		this.card = card;
	},

	onAddCardButtonClick : function(classIdOfNewCard) {

	},

	onShowGraphClick: function() {
		var classId = this.card.get("IdClass"),
			cardId = this.card.get("Id");

		CMDBuild.Management.showGraphWindow(classId, cardId);
	},

	onCloneCard: function() {
		if (this.view) {
			this.view.disable();
		}
	},

	buildCardModuleStateDelegate: function() {
		var me = this;

		this.cardStateDelegate = new CMDBuild.state.CMCardModuleStateDelegate();

		this.cardStateDelegate.onEntryTypeDidChange = function(state, entryType) {
			me.onEntryTypeSelected(entryType);
		};

		this.cardStateDelegate.onCardDidChange = function(state, card) {
			Ext.suspendLayouts();
			me.onCardSelected(card);
			Ext.resumeLayouts();
		};

		_CMCardModuleState.addDelegate(this.cardStateDelegate);

		if (this.view) {
			var me = this;
			this.mon(me.view, "destroy", function(view) {
				_CMCardModuleState.removeDelegate(me.cardStateDelegate);
				delete me.cardStateDelegate;
			});
		}

	}
});