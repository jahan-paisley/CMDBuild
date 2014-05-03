(function() {

	Ext.define("CMDBuild.controller.management.common.CMFakeIdAccordionController", {
		extend: "CMDBuild.controller.accordion.CMBaseAccordionController",

		onAccordionNodeSelect: function(selectionModel, selection) {

			// is allowed only single select
			if (selection.length != 1) {
				return;
			}

			var selectedNode = selection[0];
			var data = Ext.apply({}, selectedNode.data);
			var id = data.id;

			// a node without id is not manageable
			if (typeof id == "undefined") {
				return;
			}

			// split the real id to the menu sequential
			// id number
			var idParts = id.split("#");
			if (idParts.length != 2) {
				// the id generation in menu accordion
				// went wrong. There is no way to manage this case
				return;
			} else {
				data.id = idParts[1];
			}

			var normalizedNode = new CMDBuild.view.common.CMAccordionStoreModel(data);
			this.callParent([selectionModel, [normalizedNode]]);
		}

	});

})();