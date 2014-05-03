(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMProcessAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.administration.modWorkflow.tree_title,
		cmName: 'process',

		buildTreeStructure: function() {
			var processes = _CMCache.getProcesses();
			var nodesMap = {};
			var out = [];

			for (var key in processes) {
				var nodeConf =  buildNodeConf(processes[key]);
				nodesMap[nodeConf.id] = nodeConf;
			}

			for (var id in nodesMap) {
				var node = nodesMap[id];
				if (node.parent && nodesMap[node.parent]) {
					linkToParent(node, nodesMap);
				} else {
					out.push(node);
				}
			}
			
			return out;
		},

		afterUpdateStore: function() {
			var root = this.store.getRootNode();
			if (root.childNodes.length == 1) {
				this.store.setRootNode(root.getChildAt(0).remove(destroy=false));
			}
		}
	});

	function buildNodeConf(node) {
		return {
			id: node.get("id"),
			text: node.get("text"),
			tableType: node.get("tableType"),
			leaf: true,
			cmName: "process",
			parent: node.get("parent"),
			iconCls: node.get("superclass") ? "cmdbuild-tree-superprocessclass-icon" : "cmdbuild-tree-processclass-icon"
		};
	}

	function linkToParent(node, nodesMap) {
		var parentNode = nodesMap[node.parent];
		parentNode.children = (parentNode.children || []);
		parentNode.children.push(node);
		parentNode.leaf = false;
	}

})();