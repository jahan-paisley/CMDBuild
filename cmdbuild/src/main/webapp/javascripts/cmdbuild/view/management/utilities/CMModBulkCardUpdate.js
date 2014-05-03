(function() {

	Ext.define("CMDBuild.view.management.utilites.CMModBulkCardUpdate", {
		extend: "Ext.panel.Panel",
		cmName: 'bulkcardupdate',
		title : CMDBuild.Translation.management.modutilities.bulkupdate.title,
		frame: false,
		border: true,

		constructor: function() {
			this.cardSelected = [];

			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save
			});

			this.abortButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.abort
			});

			this.classTree = new CMDBuild.view.common.classes.CMClassAccordion({
				cmName: "",
				title: undefined,
				region: "west",
				width: 200,
				border: true,
				split: true,
				excludeSimpleTables: true
			});

			this.cardGrid = new CMDBuild.view.management.common.CMCardGrid({
				region: "center",
				border: true,
				cmAddGraphColumn: false,
				selModel: new CMDBuild.selection.CMMultiPageSelectionModel({
					idProperty: "Id" // required to identify the records for the data and not the id of ext
				}),
				columns: []
			});

			this.cardForm = new CMDBuild.view.management.utilities.CMBulkCardFormPanel({
				region: "south",
				split: true,
				border: true,
				height: 200
			});
    	
			this.frame = true;
			this.layout = "border";
			this.buttonAlign = "center";
			this.buttons = [this.saveButton,this.abortButton];
			this.items = [{
					xtype: "panel",
					region: "center",
					layout: "border",
					frame: false,
					border: false,
					items: [this.cardGrid, this.cardForm]
				},
				this.classTree
			];

			this.callParent(arguments);
			this.firstShow = true;
		},

		beforeBringToFront : function() {
			if (this.firstShow) {
				this.classTree.updateStore();
				this.firstShow = false;
			}

			return true;
		},

		onClassTreeSelected: function(classId) {
			this.cardForm.fillWithFieldsForClassId(classId);
		},

		saveCardsChanges: function() {
			if (this.cardList.isFiltered()) {

				Ext.Msg.show({
					title : CMDBuild.Translation.warnings.warning_message,
					msg : CMDBuild.Translation.warnings.only_filtered,
					buttons : Ext.Msg.OKCANCEL,
					fn : doSaveRequest,
					icon : Ext.MessageBox.WARNING,
					scope : this
				});

			} else {
				doSaveRequest.call(this, confirm="ok");
			}
		},

		abortCardsChanges: function() {
			this.clearAll();
		},

		clearAll: function() {
			this.cardList.clearFilter();
			this.cardList.getSelectionModel().clearSelections();
			this.cardList.getSelectionModel().clearPersistentSelections();
			this.cardSelected = [];
			this.clearForm();
		},

		clearForm: function() {
			this.attributesPanel.resetForm();
		},

		disableSaveBtnIfSelectionIsEmpty: function() {
			if (this.cardSelected.length < 1){
				this.saveBtn.disable();
			}
		},

		getFilter: function() {
			var filter = {};
			var store = this.cardGrid.getStore();
			if (store
					&& store.proxy
					&& store.proxy.extraParams) {

				filter = store.proxy.extraParams.filter;
			}

			return filter;
		}
	});
})();