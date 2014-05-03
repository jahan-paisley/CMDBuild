(function() {
	////////////////////////
	// TODO we have disabled the attribute filter, fixit
	///////////////////////

	Ext.define("CMDBuild.view.management.common.CMTabPanelWithCardGridAndFilter", {
		extend: "Ext.tab.Panel",

		// configuration
		selType: "rowmodel",
		multiSelect: false,
		filterButton: undefined,
		filterType: undefined,
		idClass: undefined,
		// configuration

		initComponent: function() {
			this.filterButton = new Ext.Button({
				text: CMDBuild.Translation.management.findfilter.go_filter,
				iconCls: 'ok',
				handler: this.onFilterButtonClick,
				scope: this
			});

			this.grid = new CMDBuild.view.management.common.CMCardGrid({
				cmAdvancedFilter: false,
				columns: [],
				title: CMDBuild.Translation.management.findfilter.list,
				frame: false,
				border: "0 0 1 0",
				selType: this.selType,
				multiSelect: this.multiSelect,
				cmAddGraphColumn: false
	 		});

			// FIXME TODO: does not work

//			this.filter = new CMDBuild.view.management.common.filter.CMFilterAttributes({
//				attributeList: {},
//				IdClass: this.idClass,
//				filterButton: this.filterButton,
//				title: CMDBuild.Translation.management.findfilter.filter,
//				frame: false,
//				border: false
//			});

//			this.filter.resetFilterButton.on("click", this.onResetFilterButtonClick, this);

			Ext.apply(this, {
				items: [this.grid
//				        , this.filter
				        ]
			});

			this.callParent(arguments);
		},

		updateForClassId: function(classId) {
			this.idClass = classId;
			this.grid.updateStoreForClassId(classId);
//			this.filter.updateMenuForClassId(classId);
//			this.filter.removeAllFieldsets();
		},

		onResetFilterButtonClick: function() {
//			this.filter.removeAllFieldsets();
			this.grid.clearFilter();
			this.setActiveTab(this.grid);
		},

		onFilterButtonClick: function() {
//			var params = this.filter.getForm().getValues();

			params['IdClass'] =  this.idClass;
			params['FilterCategory'] = this.filterType;
			params['FilterSubcategory'] = this.id;

			CMDBuild.Ajax.request({
				url: 'services/json/management/modcard/setcardfilter',
				params: params,
				method: 'POST',
				scope: this,
				success: function(response) {
					this.setActiveTab(this.grid);
					this.grid.reload();
				}
			});
		}
	})
})();