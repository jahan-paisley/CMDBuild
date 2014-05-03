(function() {
	Ext.define("CMDBuild.view.administration.classes.CMGeoAttributesPanel", {
		extend: "Ext.panel.Panel",
		
		constructor: function() {
			this.form = new CMDBuild.view.administration.classes.CMGeoAttributeForm({
				region: "center"
			});

			this.grid = new CMDBuild.view.administration.classes.CMGeoAttributesGrid({
				region: "north",
				split: true,
				height: "40%"
			});

			this.callParent(arguments);
		},
		
		initComponent: function() {
			
			Ext.apply(this, {
				layout: "border",
				frame: false,
				border: false,
				items: [this.grid,this.form]
			});
			
			this.callParent(arguments);
		},
		
		onClassSelected: function(idClass) {
			this.form.onClassSelected(idClass);
			this.grid.onClassSelected(idClass);
		},

		isActive: function() {
			if (this.ownerCt.getActiveTab) {
				return this.ownerCt.getActiveTab().id == this.id;
			}
		}
	});
})();