(function() {

	var tr = CMDBuild.Translation.administration.modsecurity;

	Ext.define("CMDBuild.view.administration.user.CMModUser", {
		extend : "Ext.panel.Panel",
		cmName : "users",

		initComponent : function() {

			this.addUserButton = new Ext.button.Button( {
				iconCls : 'add',
				text : tr.user.add_user
			});

			this.userGrid = new CMDBuild.view.administration.user.CMUserGrid( {
				region : "center",
				border: false
			});

			this.userForm = new CMDBuild.view.administration.user.CMUserForm( {
				region : "south",
				height : "65%",
				split : true
			});

			Ext.apply(this, {
   				tbar : [ this.addUserButton ],
				title : tr.user.title,
				modtype : 'user',
				basetitle : tr.user.title + ' - ',
				layout : 'border',
				frame : false,
				border : true,
				items : [ this.userGrid, this.userForm ]
			});

			this.callParent(arguments);
		},

		selectUser : function(eventParams) {

		}

	});

})();