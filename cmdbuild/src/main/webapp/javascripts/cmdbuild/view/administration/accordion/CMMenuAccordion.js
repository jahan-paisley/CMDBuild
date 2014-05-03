(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMMenuAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.administration.modmenu.menu_root,
		cmName: "menu",
		buildTreeStructure: function() {
			var groups = _CMCache.getGroups();
			var nodes = [{
				id: 0,
				text: "*Default*",
				leaf: true,
				cmName: "menu",
				iconCls: "cmdbuild-tree-group-icon"
			}];

			for (var key in groups) {
				nodes.push(buildNodeConf(groups[key]));
			}

			return nodes;
		},

		updateStore: function() {
			var root = this.store.getRootNode();
			var treeStructure = this.buildTreeStructure();

			root.removeAll();
			root.appendChild(treeStructure);
			this.update();
			this.store.sort("text", "ASC");
		},

		onGroupAdded: function(group) {
			// register a one-time event, because the
			// updateStore of a collapsed accordion cause
			// a lot of layout problems
			this.on("expand",
				function() {
					this.updateStore();
					var selectionId = this.getSelectionModel().getSelection()[0].get("id");
					if (selectionId) {
						this.selectNodeById(selectionId);
					}
				},
				this, {
					single: true
				}
			);
		}
	});

	function buildNodeConf(g) {
		return {
			id: g.get("id"),
			name: g.get("name"),
			text: g.get("text"),
			leaf: true,
			cmName: "menu",
			iconCls: "cmdbuild-tree-group-icon"
		};
	}

})();