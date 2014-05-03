(function() {

	CMDBuild.ServiceProxy.group = {
		read: function(p) {
			p.method = 'GET';
			p.url = "services/json/schema/modsecurity/getgrouplist";
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		save: function(p) {
			p.method = 'POST';
			p.url = "services/json/schema/modsecurity/savegroup";
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		saveClassUiConfiguration: function(p) {
			p.method = 'POST';
			p.url = _CMProxy.url.privileges.classes.saveClassUiConfiguration;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		loadClassUiConfiguration: function(p) {
			p.method = 'GET';
			p.url = _CMProxy.url.privileges.classes.loadClassUiConfiguration;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		clearRowAndColumnPrivileges: function(p) {
			p.method = 'POST';
			p.url = _CMProxy.url.privileges.classes.clearRowAndColumnPrivileges;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		setRowAndColumnPrivileges: function(p) {
			p.method = 'POST';
			p.url = _CMProxy.url.privileges.classes.setRowAndColumnPrivileges;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		getClassPrivilegesGridStore: function(pageSize) {
			return getGridPrivilegeStore(_CMProxy.url.privileges.classes.read);
		},

		getDataViewPrivilegesGridStore: function() {
			return getGridPrivilegeStore(_CMProxy.url.privileges.dataView.read);
		},

		getFilterPrivilegesGridStore: function() {
			return getGridPrivilegeStore(_CMProxy.url.privileges.filter.read);
		},

		getUserPerGroupStoreForGrid: function() {
			return new Ext.data.Store({
				model : "CMDBuild.cache.CMUserForGridModel",
				autoLoad : false,
				proxy : {
					type : 'ajax',
					url : 'services/json/schema/modsecurity/getgroupuserlist',
					reader : {
						type : 'json',
						root : 'users'
					}
				},
				sorters : [ {
					property : 'username',
					direction : "ASC"
				}]
			});
		},

		getUserStoreForGrid: function() {
			return new Ext.data.Store({
				model : "CMDBuild.cache.CMUserForGridModel",
				autoLoad : true,
				proxy : {
					type : 'ajax',
					url : "services/json/schema/modsecurity/getuserlist",
					reader : {
						type : 'json',
						root : 'rows'
					}
				},
				sorters : [ {
					property : 'username',
					direction : "ASC"
				}]
			});
		},

		getUIConfiguration: function(cbs) {
			cbs = cbs || {};

			CMDBuild.ServiceProxy.core.doRequest({
				url: "services/json/schema/modsecurity/getuiconfiguration",
				method: 'GET',
				success: cbs.success || Ext.emptyFn,
				failure: cbs.failure || Ext.emptyFn,
				callback: cbs.callback || Ext.emptyFn
			});
		},

		getGroupUIConfiguration: function(groupId, cbs) {
			cbs = cbs || {};

			CMDBuild.ServiceProxy.core.doRequest({
				url: "services/json/schema/modsecurity/getgroupuiconfiguration",
				params: {id: groupId},
				method: 'GET',
				success: cbs.success || Ext.emptyFn,
				failure: cbs.failure || Ext.emptyFn,
				callback: cbs.callback || Ext.emptyFn
			});
		},

		saveUIConfiguration: function(groupId, uiConfiguration, cbs) {
			cbs = cbs || {};

			CMDBuild.ServiceProxy.core.doRequest({
				url: "services/json/schema/modsecurity/savegroupuiconfiguration",
				params: {
					id: groupId,
					uiConfiguration: uiConfiguration
				},
				method: 'POST',
				success: cbs.success || Ext.emptyFn,
				failure: cbs.failure || Ext.emptyFn,
				callback: cbs.callback || Ext.emptyFn
			});
		}
	};

	function getGridPrivilegeStore(url) {
		return new Ext.data.Store({
			model : "CMDBuild.cache.CMPrivilegeModel",
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : url,
				reader : {
					type : 'json',
					root : 'privileges'
				}
			},
			sorters : [ {
				property : _CMProxy.parameter.PRIVILEGED_OBJ_DESCRIPTION,
				direction : "ASC"
			}]
		});
	}

})();