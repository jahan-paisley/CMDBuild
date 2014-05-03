(function() {

	Ext.define('CMDBuild.controller.accordion.CMBaseAccordionController', {

		constructor: function(accordion) {
			this.accordion = accordion;

			this.accordion.on('expand', function() {
				if (this.accordion.cmSilent !== true) {
					this.onAccordionExpanded();
				}
			}, this);

			if (this.accordion.getSelectionModel)
				manageTreeEvents.call(this);
		},

		onAccordionExpanded: function() {
			_CMMainViewportController.bringTofrontPanelByCmName(this.accordion.cmName);
			reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf.call(this);
		},

		onAccordionNodeSelect: function(sm, selections) {
			if (selections.length > 0) {
				var s = selections[0];

				if (_CMMainViewportController.bringTofrontPanelByCmName(s.get('cmName'), s) === false) {

					// If the panel was not brought to front (report from the navigation menu), select the previous node or deselect the tree
					if (this.lastSelection) {
						sm.select(this.lastSelection);
					} else {
						sm.deselectAll(true);
					}
				} else {
					this.lastSelection = selections;
				};
			}
		},

		updateStoreToSelectNodeWithId: function(id) {
			this.accordion.updateStore();
			this.accordion.selectNodeById(id);
		},

		reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf: reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf
	});

	function manageTreeEvents() {
		this.accordionSM = this.accordion.getSelectionModel();

		this.accordionSM.on('selectionchange', this.onAccordionNodeSelect, this);
	}

	function reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf() {
		if (this.accordionSM) {
			var selections = this.accordionSM.getSelection();

			if (selections.length > 0) {
				var toSelect = [selections[0]];
				this.onAccordionNodeSelect(this.accordionSM, toSelect);
			} else {
				this.accordion.selectFirstSelectableNode();
			}
		}
	}

})();