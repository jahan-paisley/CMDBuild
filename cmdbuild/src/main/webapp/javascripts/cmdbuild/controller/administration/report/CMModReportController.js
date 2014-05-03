(function() {
	var activePanel;
	var tr = CMDBuild.Translation.administration.modreport.importJRFormStep2;

	Ext.define("CMDBuild.controller.administration.report.CMModReportController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			
			this.currentReport = null;
			this.currentReportType = null;
			
			this.grid = this.view.grid;
			this.gridSM = this.grid.getSelectionModel();
			this.form = this.view.form;
			
			this.gridSM.on("selectionchange", onSelectionChange, this);
			this.view.addReportButton.on("click", onAddReportClick, this);
			this.form.modifyButton.on("click", onModifyButtonClick, this);
			this.form.deleteButton.on("click", onDeleteButtonClick, this);
			this.form.saveButton.on("click", onSaveButtonClick, this);
			this.form.abortButton.on("click", onAbortButtonClick, this);
			
			activePanel = this.form.step1.id;
		},

		onViewOnFront: function(type) {
			this.currentReportType = type;
			this.view.onReportTypeSelected(type);
		}

	});
	
	function onSelectionChange(selection) {
		if (selection.selected.length > 0) {
			activePanel = this.form.step1.id;
			this.currentReport = selection.selected.items[0];
			this.form.onReportSelected(this.currentReport);
		}
	}
	
	function onAddReportClick() {
		this.gridSM.deselectAll();
		this.form.step1.fileField.allowBlank = false;
		this.currentReport = null;
		this.form.reset();
		this.form.enableModify(all = true);
		_CMCache.initAddingTranslations();
	}
	
	function onModifyButtonClick() {
		this.form.step1.fileField.allowBlank = true;
		this.form.enableModify();
		_CMCache.initModifyingTranslations();
	}
	
	function onSaveButtonClick() {
		if (activePanel == this.form.step1.id) {
			analizeReport.call(this);
		} else {
			insertJasperReport.call(this);
		}
		this.gridSM.deselectAll();
	}
	
	function onAbortButtonClick() {
		this.form.disableModify();
		this.form.reset();
		if (this.currentReport != null) {
			this.form.onReportSelected(this.currentReport);
		}
	}
	
	function analizeReport() {
		this.form.step1.name.enable();
		CMDBuild.LoadMask.get().show();
		
		this.form.step1.getForm().submit({
			method : 'POST',
			url : 'services/json/schema/modreport/analyzejasperreport',
			scope: this,
			params: {
				reportId: this.currentReport == null ? -1 : this.currentReport.get("id")
			},
			success : function(form, action) {
				var r = action.result;
				if (r.skipSecondStep) {
					saveJasperReport.call(this);
				} else {
					this.form.step2.setFormDetails(r);
					if (this.form.step2.items.length == 0) {
						insertJasperReport.call(this);
					} else {
						activePanel = this.form.step2.id;
						this.form.showStep2();
					}
				}
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function successCB() {
		resetSession.call(this);
		this.grid.load();
		_CMCache.reloadReportStores();
		this.form.disableModify();
		_CMCache.flushTranslationsToSave(this.form.step1.name.getValue());
		this.form.reset();
		this.form.step2.removeAll();
		this.form.showStep1();
	}

	function insertJasperReport() {
		CMDBuild.LoadMask.get().show();
		this.form.step2.getForm().submit({
			method : 'POST',
			url : 'services/json/schema/modreport/importjasperreport',
			scope: this,
			success : successCB,
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	};

	function saveJasperReport() {
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modreport/savejasperreport',
			scope: this,
			success : successCB,
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function resetSession() {
		CMDBuild.Ajax.request({
			url : 'services/json/schema/modreport/resetsession',
			method: 'POST',
			params: {},
			scope: this,
			success: function() {
				activePanel = this.form.step1.id
			}
		});
	}

	function onDeleteButtonClick() {
		Ext.Msg.show({
			title: CMDBuild.Translation.administration.modreport.remove,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button){
				if (button == 'yes'){
					deleteReport.call(this);
				}
			}
		});
	}

	function deleteReport() {
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			url : 'services/json/schema/modreport/deletereport',
			params : {
				"id": this.currentReport.get("id")
			},
			method : 'POST',
			scope : this,
			success : successCB,
			callback: function() {
				CMDBuild.LoadMask.get().hide(); 
			}
		});
	}
})();