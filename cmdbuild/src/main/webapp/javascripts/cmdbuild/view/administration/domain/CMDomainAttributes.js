(function() {

	Ext.ns("CMDBuild.administration.domain");
	
	CMDBuild.administration.domain.CMDomainAttribute = Ext.extend(Ext.Panel, {
		initComponent: function() {
			this.form = new CMDBuild.view.administration.domain.CMDomainAttributeFormPanel({
				region: "center"
			});

			this.grid = new CMDBuild.view.administration.domain.CMDomainAttributeGrid({
				region: "north",
				height: "40%",
				split: true,
                border: false
			});

			Ext.apply(this, {
				layout: "border",
				items: [this.form, this.grid]
			});

			this.callParent(arguments);

			this.form.disableModify();
		},

		onDomainSelected: function(id) {
			this.enable();
			this.form.onDomainSelected(id);
			this.grid.onDomainSelected(id);
		},
		
		onAddAttributeClick: function() {
			this.form.onAddAttributeClick(params=null, enableAll=true);
			this.grid.getSelectionModel().deselectAll();
		}
	});
})();