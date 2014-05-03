(function() {

	var addEventName = "add";
	var groups = {};
	var activeGroupsStore = new Ext.data.Store( {
		model: "CMDBuild.cache.CMGroupModel",
		data: []
	});

	Ext.define("CMDBUild.cache.CMCacheGroupsFunctions", {
		addGroups: function(etypes) {
			for (var i=0, l=etypes.length; i<l; ++i) {
				this.addGroup(etypes[i]);
			}
		},

		addGroup: function(g) {
			var group = Ext.create("CMDBuild.cache.CMGroupModel", g);
			groups[g.id] = group;

			if (group.isActive()) {
				activeGroupsStore.suspendEvent(addEventName);
				activeGroupsStore.add(group);
				activeGroupsStore.resumeEvent(addEventName);
			}

			return group;
		},

		getGroups: function() {
			return groups;
		},

		getActiveGroupsStore: function() {
			return activeGroupsStore;
		},

		getGroupById: function(id) {
			return groups[id] || null;
		},

		onGroupSaved: function(group) {
			var g = this.addGroup(group);

			this.fireEvent("cm_group_saved", g);
		}
	});
})();