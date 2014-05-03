(function() {
	Ext.define("CMDBuild.delegate.administration.bim.CMBIMGridConfigurator", {
		extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseGridConfigurator",
	
		/**
		 * @return a Ext.data.Store to use for the grid
		 */
		getStore: function() {
			if (this.store == null) {
				this.store = CMDBuild.bim.proxy.store();
			}
	
			return this.store;
		},
	
		/**
		 * @return an array of Ext.grid.column.Column to use for the grid
		 */
		getColumns: function() {
			var columns = this.callParent(arguments);
	
			columns.push({
				dataIndex: "active",
				header: CMDBuild.Translation.active,
				flex: 1
			}, {
				dataIndex: "lastCheckin",
				header: CMDBuild.Translation.last_checkin,
				flex: 1
			}, {
				header: '&nbsp', 
				sortable: false, 
				tdCls: "grid-button",
				fixed: true,
				menuDisabled: true,
				hideable: false,
				header: "",
				align: "center",
				width: 40,
				renderer: renderIfcIcon
			});
	
			return columns;
		}
	});
	function renderIfcIcon(value, metadata, record) {
			return '<img style="cursor:pointer" title="'+CMDBuild.Translation.download_ifc_file+'" class="action-download-ifc" src="images/icons/downloadifc.png"/>';
	
	}
})();
