(function() {
	var tr = CMDBuild.Translation.management.modutilities.csv;
	Ext.define("CMDBuild.controller.management.utilities.CMModImportCSVController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		
		constructor: function() {
			this.callParent(arguments);

			this.view.form.uploadButton.on("click", onUploadButtonClick, this);
			this.view.form.classList.on("select", onClassListSelect, this);
			this.view.updateButton.on("click", onUpdateButtonClick, this);
			this.view.confirmButton.on("click", onConfirmButtonClick, this);
			this.view.abortButton.on("click", onAbortButtonClick, this);
		},

		onViewOnFront: function() {}
	});

	function onClassListSelect(combo, selections) {
		if (selections.length > 0) {
			this.currentClass = selections[0].get("id");
			this.view.grid.updateStoreForClassId(this.currentClass);
		}
	}

	function onUploadButtonClick() {
		CMDBuild.LoadMask.get().show();
		this.view.form.getForm().submit({
			method: 'POST',
			url : 'services/json/management/importcsv/uploadcsv',
			scope: this,
			success: updateGridRecords,
			failure: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function onUpdateButtonClick() {
		var records = this.view.grid.getRecordToUpload();
		if (records.length == 0) {
			CMDBuild.Msg.warn(tr.warning, tr.noupdate);
		} else {
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				method : 'POST',
				url : 'services/json/management/importcsv/updatecsvrecords',
				params : {
					data : Ext.JSON.encode(records)
				},
				scope : this,
				success : updateGridRecords,
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	}

	function onConfirmButtonClick() {
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			method: 'POST',
			url : 'services/json/management/importcsv/storecsvrecords',
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			timeout: 600000,
			scope: this,
			success: function(a,b,c) {
				CMDBuild.LoadMask.get().hide();
				CMDBuild.Msg.info(tr.info, tr.importsuccess);
				updateGridRecords.call(this);
			},
			failure: function(a,b,c) {
				CMDBuild.LoadMask.get().hide();
				CMDBuild.Msg.error(tr.error, tr.importfailure, true);
			}
		});
	}

	function onAbortButtonClick() {
		this.view.form.reset();
		this.view.grid.removeAll();
	}

	// callback called after the upload of the csv file
	// and after the update of the grid records
	function updateGridRecords() {
		CMDBuild.Ajax.request({
			method: 'GET',
			url : 'services/json/management/importcsv/getcsvrecords',
			scope: this,
			success: function(a,b,c) {
				this.view.grid.configureHeadersAndStore(c.headers);
				this.view.grid.loadData(c.rows);
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

})();