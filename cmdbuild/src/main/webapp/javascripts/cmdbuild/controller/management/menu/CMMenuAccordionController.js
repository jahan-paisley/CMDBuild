(function() {
	Ext.define("CMDBuild.controller.management.menu.CMMenuAccordionController", {
		extend: "CMDBuild.controller.management.common.CMFakeIdAccordionController",
		
		constructor: function() {
			this.callParent(arguments);
		},

		onAccordionExpanded: function() {
			_CMMainViewportController.bringTofrontPanelByCmName("class");
			this.reselectCurrentNodeIfExistsOtherwiseSelectTheFisrtLeaf.call(this);
		}

	});

})();