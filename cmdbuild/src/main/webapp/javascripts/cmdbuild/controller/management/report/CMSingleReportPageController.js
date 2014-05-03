(function() {

	Ext.define("CMDBuild.controller.management.report.CMSingleReportPageController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		requestReport: function(params) {
			this.view.requestReport(params);
		},
		onViewOnFront: function(node) {
			if (node) {
				if (node.get("id")
						&& node.get("id") != "custom") {
					this.view.setReportId(node.get("id"));
					// is a node from the menu accordion
					// so ask directly the report
					this.requestReport({
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