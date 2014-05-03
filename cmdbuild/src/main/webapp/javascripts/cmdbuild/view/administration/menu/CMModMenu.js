(function() {

	Ext.define("CMDBuild.Administration.ModMenu", {
		extend : "Ext.panel.Panel",
		
		cmName: "menu",
		title : CMDBuild.Translation.administration.modmenu.title,
		basetitle : CMDBuild.Translation.administration.modmenu.title + ' - ',
		layout : 'fit',

		initComponent : function() {
			this.mp = new CMDBuild.Administration.MenuPanel();

			Ext.apply(this, {
				frame : false,
				border : true,
				items : [ this.mp ]
			});

			this.callParent(arguments);
		}

	});

})();