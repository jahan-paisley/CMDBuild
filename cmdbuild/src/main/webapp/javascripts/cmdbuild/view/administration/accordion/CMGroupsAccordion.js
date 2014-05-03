(function() {

	Ext.define('CMDBuild.view.administration.accordion.CMGroupsAccordion', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: CMDBuild.Translation.administration.modsecurity.title,
		cmName: 'group',

		buildTreeStructure: function() {
			var groups = _CMCache.getGroups();
			var nodes = [];

			for (var key in groups)
				nodes.push(buildNodeConf(groups[key]));

			return [
				{
					text: CMDBuild.Translation.administration.modsecurity.groups,
					leaf: false,
					cmName: 'group',
					children: nodes,
					iconCls: 'cmdbuild-tree-user-group-icon'
				},
				{
					text: CMDBuild.Translation.administration.modsecurity.users,
					cmName: 'users',
					leaf: true,
					iconCls: 'cmdbuild-tree-user-icon'
				}
			];

		}
	});

	function buildNodeConf(g) {
		return {
			id: g.get('id'),
			text: g.get('text'),
			leaf: true,
			cmName: 'group',
			iconCls: 'cmdbuild-tree-group-icon'
		};
	}

})();