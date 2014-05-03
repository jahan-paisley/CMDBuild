(function() {
	Ext.define("CMDBuild.view.administration.bim.CMBIMPanel", {
		extend: "CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel",

		title: CMDBuild.Translation.bim + " " + CMDBuild.Translation.projects,

		addButtonText: CMDBuild.Translation.addProject,
		modifyButtonText: CMDBuild.Translation.modifyProject,
		removeButtonText: CMDBuild.Translation.removeProject,
		withRemoveButton: false,
		withEnableDisableButton: true,
		
		//override
		buildGrid: function() {
			var gridConfig = {
				region: "center",
				border: false,
				frame: false,
				withPagingBar: this.withPagingBar	
			};

			if (this.withPagingBar) {
				gridConfig.cls = "cmborderbottom";
			}

			return new CMDBuild.view.administration.bim.CMBimGrid(gridConfig);
		},
	});
})();
