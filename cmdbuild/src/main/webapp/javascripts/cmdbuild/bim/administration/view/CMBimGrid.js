Ext.define("CMDBuild.view.administration.bim.CMBimGrid", {
	extend: "CMDBuild.view.administration.common.basepanel.CMGrid",
	withPagingBar: true,
	initComponent: function() {
		var me = this;
		this.callParent(arguments);
		this.on('beforeitemclick', cellclickHandler);

	},

});
function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
	var eventName = event.target.className;
	if (eventName == "action-download-ifc") {
		var id = model.get("id");
		var basicFormPanel = Ext.create('Ext.form.FormPanel', {
			hidden: true,
			fileUpload: true,
		    items: [{
		        xtype: 'textfield',
		        fieldLabel: 'Field',
		        name: 'projectId', 
		        value: id
		    }],
		});
		
		var basicForm = basicFormPanel.getForm();
		basicForm.standardSubmit = true;
		basicForm.submit({
			url: 'services/json/bim/download',
			method: "GET",
			target: "_self",
		});
	}
}

