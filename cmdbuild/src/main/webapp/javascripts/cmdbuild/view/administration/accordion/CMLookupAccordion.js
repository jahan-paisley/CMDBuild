(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMLookupAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.administration.modLookup.lookupTypes,
		cmName: "lookuptype",
		buildTreeStructure: function() {
			var lookupTypes = _CMCache.getLookupTypes();
			var out = [];
			var nodesMap = {};

			for (var key in lookupTypes) {
				var nodeConf =  buildNodeConf(lookupTypes[key]);
				nodesMap[nodeConf.id] = nodeConf;
			}

			for (var id in nodesMap) {
				var node = nodesMap[id];
				if (node.parent) {
					var parentNode = nodesMap[node.parent];
					if (parentNode) {
						parentNode.children = (parentNode.children || []);
						parentNode.children.push(node);
						parentNode.leaf = false;
					}
				} else {
					out.push(node);
				}
			}

			function buildNodeConf(lt) {
				return {
					id: lt.get("id"),
					text: lt.get("text"),
					leaf: true,
					cmName: "lookuptype",
					parent: lt.get("parent")
				};
			}

			return out;
		}
	});
})();