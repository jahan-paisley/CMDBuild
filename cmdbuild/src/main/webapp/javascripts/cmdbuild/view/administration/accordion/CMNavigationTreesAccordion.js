(function() {

	Ext.define("CMDBuild.view.administration.accordion.CMNavigationTreesAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.trees_navigation, 
		cmName: "navigationTrees",

		initComponent: function() {
			this.callParent(arguments);
			_CMCache.registerOnNavigationTrees(this);
		},
		
		buildTreeStructure: function() {
			var navigationTrees = _CMCache.getNavigationTrees();
			var out = [];

			for (var i = 0; i < navigationTrees.data.length; i++) {
				var d = navigationTrees.data[i];
				out.push(buildNodeConf(d.name, d.description));
			}

			return out;
		},
		
		refresh: function() {
			var navigationTrees = _CMCache.getNavigationTrees();
			this.updateStore(navigationTrees.data);
			if (navigationTrees.lastEntry) {
				this.selectNodeById(navigationTrees.lastEntry);
			}
			else {
				this.selectFirstSelectableNode();
			}
		}
	});

	function buildNodeConf(name, description) {
		return {
			id: name,
			text: description,
			leaf: true,
			cmName: "navigationTrees",
			iconCls: "navigationTrees"
		};
	}

})();