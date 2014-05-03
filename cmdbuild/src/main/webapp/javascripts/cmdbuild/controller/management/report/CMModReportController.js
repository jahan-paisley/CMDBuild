(function() {

	Ext.define("CMDBuild.controller.management.report.CMModReportController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		onViewOnFront: function(node) {
			if (node) {
				this.view.onReportTypeSelected(node);

				if (node.get("id")
						&& node.get("id") != "custom") {
					// is a node from the menu accordion
					// so ask directly the report
					this.view.requestReport({
						id: node.get("id"),
						type: "CUSTOM",
						extension: (function extracExtension() {
							return node.get("type").replace(/report/i,"");
						})()
					});
				}
			}
		}
	});

})();