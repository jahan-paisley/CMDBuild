Ext.define("CMDBuild.controller.management.common.CMAttachmentsWindowController", {

	mixins: {
		observable: "Ext.util.Observable"
	},

	constructor: function(view, cardInfo) { // cardifo Description, Id, ClassId
		this.view = view;
		if (!cardInfo) {
			throw "Must set the card info to the controller"
		}
		this.gridController = new CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController(this.view.grid);

		var fakeCard = {
			data: cardInfo,
			get: function(key) {
				return this.data[key];
			}
		};

		this.mon(this.view, "show", function() {
			this.gridController.onCardSelected(fakeCard);
			this.setViewTitle();
		}, this);

	},

	setViewTitle: function() {
		var title = Ext.String.format("{0} - {1}"
				, CMDBuild.Translation.management.modcard.tabs.attachments 
				, this.gridController.card.get("Description"));

		this.view.setTitle(title);
	}
});