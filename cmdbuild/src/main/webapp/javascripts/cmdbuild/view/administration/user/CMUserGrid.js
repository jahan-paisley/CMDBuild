(function() {

	var tr = CMDBuild.Translation.administration.modsecurity.user;

	Ext.define("CMDBuild.view.administration.user.CMUserGrid", {
		extend : "Ext.grid.Panel",

		initComponent : function() {

			Ext.apply(this, {
				columns : [ {
					header : tr.username,
					dataIndex : 'username',
					flex : 1
				}, {
					header : tr.description,
					dataIndex : 'description',
					flex : 1
				} ],
				store : CMDBuild.ServiceProxy.group.getUserStoreForGrid()
			});

			this.callParent(arguments);
		}
	});

})();