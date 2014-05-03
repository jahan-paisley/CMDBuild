(function() {

	var TITLE_PREFIX = CMDBuild.Translation.management.modcard.title;

	Ext.define("CMDBuild.Management.CardListWindow", {
		extend: "CMDBuild.PopupWindow",

		ClassName: undefined, // passed at instantiation
		idClass: undefined, // passed at instantiation
		filterType: undefined, // passed at instantiation
		readOnly: undefined, // passed at instantiation
		selModel: undefined, // if undefined is used the default selType
		selType: 'rowmodel', // to allow the opportunity to pass a selection model to the grid
		multiSelect: false,
		extraParams: {},
		gridConfig: {}, // passed at instantiation

		initComponent: function() {
			if (typeof this.idClass == "undefined" && typeof this.ClassName == "undefined") {
				throw "There are no Class Id or Class Name to load";
			}

			this.title = TITLE_PREFIX + getClassDescription(this);
			this.grid = new CMDBuild.view.management.common.CMCardGrid(this.buildGrdiConfiguration());
			this.setItems();

			this.callParent(arguments);

			this.mon(this.grid.getSelectionModel(), "selectionchange", this.onSelectionChange, this);
			this.mon(this.grid, "itemdblclick", this.onGridDoubleClick, this);
		},

		show: function() {
			this.callParent(arguments);
			var id = this.getIdClass();
			this.grid.updateStoreForClassId(id);
			
			return this;
		},

		// protected
		setItems: function() {
			this.items = [this.grid];
			
			if (!this.readOnly) {
				this.addCardButton = this.buildAddButton();
				this.tbar = [this.addCardButton];
			}
		},

		buildAddButton: function() {
			var addCardButton = new CMDBuild.AddCardMenuButton();
			var entry = _CMCache.getEntryTypeById(this.getIdClass());
			
			addCardButton.updateForEntry(entry);
			this.mon(addCardButton, "cmClick", function buildTheAddWindow(p) {
				var w = new CMDBuild.view.management.common.CMCardWindow({
					withButtons: true,
					title: p.className
				});
				
				new CMDBuild.controller.management.common.CMCardWindowController(w, {
					cmEditMode: true,
					card: null,
					entryType: p.classId
				});
				w.show();
				
				this.mon(w, "destroy", function() {
					this.grid.reload();
				}, this);
				
			}, this);
			
			return addCardButton;
		},

		getIdClass: function() {
			if (this.idClass) {
				return this.idClass;
			} else {
				var et = _CMCache.getEntryTypeByName(this.ClassName);
				if (et) {
					return et.getId();
				}
			}
			
			throw "No class info for " + Ext.getClassName(this);
		},

		buildGrdiConfiguration: function() {
			var gridConfig = Ext.apply(this.gridConfig, {
				cmAdvancedFilter: false,
				columns: [],
				frame: false,
				border: false,
				selType: this.selType,
				multiSelect: this.multiSelect
			});

			if (typeof this.selModel == "undefined") {
				gridConfig["selType"] = this.selType;
			} else {
				gridConfig["selModel"] = this.selModel;
			}

			return gridConfig;
		},

		onSelectionChange: Ext.emptyFn,
		onGridDoubleClick: Ext.emptyFn
	});

	function getClassDescription(me) {
		var entryType = _CMCache.getEntryTypeById(me.getIdClass());
		var description = "";
		if (entryType) {
			description = entryType.getDescription();
		}

		return description;
	}
})();