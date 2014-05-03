(function() {

	Ext.define("CMDBuild.view.management.common.widgets.CMPresetFromCardGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmAdvancedFilter: false,
		cmAddPrintButton: false,
		cmAddGraphColumn: false,

		selType: "checkboxmodel",
		selModel: {
			mode: "SINGLE"
		},

		// override
		buildExtraColumns: function() {
			return [{
				header: '&nbsp',
				width: 30,
				tdCls: "grid-button",
				fixed: true,
				sortable: false,
				align: 'center',
				dataIndex: 'Id',
				menuDisabled: true,
				hideable: false,
				renderer: function() {
					return '<img style="cursor:pointer" class="action-card-show" src="images/icons/zoom.png"/>';
				}
			}]
		}
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMPresetFromCardDelegate", {
		/**
		 *
		 * Called after the click on save button
 		 * @param {CMDBuild.view.management.common.widgets.CMPresetFromCard} presetFromCardWidget
		 */
		onPresetFromCardSaveButtonClick: function(presetFromCardWidget) {}
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMPresetFromCard", {
		extend: "Ext.panel.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		statics: {
			WIDGET_NAME: ".PresetFromCard"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call( //
				this, //
				"CMDBuild.view.management.common.widgets.CMPresetFromCardDelegate" //
				);

			this.callParent(arguments);
		},

		initComponent: function() {
			this.grid = new CMDBuild.view.management.common.widgets.CMPresetFromCardGrid({
				autoScroll : true,
				hideMode: "offsets",
				region: "center",
				border: false
			});

			this.frame = false;
			this.border = false;
			this.layout = "border";
			this.items = [this.grid];

			this.callParent(arguments);
		},

		updateGrid: function(classId, cqlParams) {
			this.grid.CQL = cqlParams;
			this.grid.store.proxy.extraParams = this.grid.getStoreExtraParams();
			this.grid.updateStoreForClassId(classId);
		},

		getSelection: function() {
			var selection = null;
			var sm = this.grid.getSelectionModel();
			if (sm) {
				selections = sm.getSelection();
				if (selections.length > 0) {
					selection = selections[0];
				}
			}

			return selection;
		},

		// buttons that the owner panel add to itself
		getExtraButtons: function() {
			var me = this;
			return [ //
				new Ext.Button({
					text: CMDBuild.Translation.common.buttons.confirm,
					name: 'saveButton',
					handler: function() {
						me.callDelegates("onPresetFromCardSaveButtonClick", [me]);
					}
				})
			];
		},
	});

})();