(function() {

	Ext.define("CMDBuild.view.common.report.CMReportAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.administration.modreport.title,
		cmName: "report",
		buildTreeStructure: function() {
			var reports = _CMCache.getReports();
			var nodes = [];

			for (var key in reports) {
				nodes.push(buildNodeConf(reports[key]));
			}

			return nodes;

		}
	});
	
	function buildNodeConf(r) {
		return {
			id: r.get("id"),
			text: r.get("text"),
			leaf: true,
			cmName: "report",
			group: r.get("group"),
			type: r.get("type")
		};
	}

})();