(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMDomainAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.administration.modClass.tabs.domains,
		cmName: "domain",

		buildTreeStructure: function() {
			var domains = _CMCache.getDomains();
			var out = [];

			for (var key in domains) {
				out.push(buildNodeConf(domains[key]));
			}

			return out;
		}
	});

	function buildNodeConf(d) {
		return {
			id: d.get("id"),
			text: d.get("description"),
			leaf: true,
			cmName: "domain",
			iconCls: "domain"
		};
	}

})();