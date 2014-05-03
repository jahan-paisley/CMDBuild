(function() {
	var tr = CMDBuild.Translation.administration.modClass.classProperties;
	
	Ext.define("CMDBuild.controller.administration.workflow.CMProcessFormController", {
		extend: "CMDBuild.controller.administration.classes.CMClassFormController",

		constructor: function(view) {
			this.callParent(arguments);

			this.view.downloadXPDLSubitButton.on("click", onDownloadSubmitClick, this);
			this.view.uploadXPDLSubitButton.on("click", onUploadSubmitClick, this);
		},

		onProcessSelected: function(id) {
			this.selection = _CMCache.getProcessById(id);
			if (this.selection) {
				this.view.onClassSelected(this.selection);

				// disable the XPDL fields if the process is a superclass
				if (this.selection.get("superclass")) {
					this.view.xpdlForm.hide();
				} else {
					this.view.xpdlForm.show();
				}

				// Fill the version combo
				CMDBuild.Ajax.request({
					url : 'services/json/workflow/xpdlversions',
					method: 'POST',
					params: {idClass : id},
					scope: this.view,
					success: function(response, options, json) {
						CMDBuild.LoadMask.get().hide();
						var versions = json.response;
						var store = this.versionCombo.store;

						store.removeAll();
						for(var i=0; i<versions.length; i++) {
							var v = versions[i];
							store.add({id: v, index: v});
						}
						store.add({id: "template", index: 0});
			
						store.sort([
							{
								property : "index",
								direction: 'DESC'
							}
						]);
			
						this.versionCombo.setValue(store.getAt(0).getId());
					},

					failure: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		},
		
		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.onAddClassButtonClick();
			this.view.xpdlForm.hide();
		},

		// override
		buildSaveParams: function() {
			var params = this.callParent(arguments)
			params.isprocess = true;

			return params;
		},

		//override
		saveSuccessCB: function(r) {
			var result = Ext.JSON.decode(r.responseText);
			this.selection = _CMCache.onProcessSaved(result.table);
		},

		//override
		deleteSuccessCB: function(r) {
			var removedClassId = this.selection.get("id");
			_CMCache.onProcessDeleted(removedClassId);

			this.selection = null;
		}

	});

	function onUploadSubmitClick() {
		CMDBuild.LoadMask.get().show();

		var basicForm = this.view.xpdlForm.getForm();
		basicForm.standardSubmit = false;

		basicForm.submit({
			url: 'services/json/workflow/uploadxpdl',
			params: {
				idClass: this.selection.getId()
			},
			scope: this,
			success: function(form, action) {
				CMDBuild.LoadMask.get().hide();
				var messages = (Ext.decode(action.response.responseText) || {}).response;
				if (messages && messages.length > 0) {
					var msg = "";
					for (var i=0, len=messages.length; i<len; ++i) {
						msg += "<p>" 
							+ CMDBuild.Translation.administration.modWorkflow.xpdlUpload[messages[i]]
							+ "<p>";
					}
					CMDBuild.Msg.info(CMDBuild.Translation.common.success, msg);
				}
			},
			failure: function() {
				CMDBuild.LoadMask.get().hide();
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure,
					CMDBuild.Translation.administration.modWorkflow.xpdlUpload.error, true);
			}
		});
	}
	
	function onDownloadSubmitClick() {
		var version = this.view.versionCombo.getValue(),
		url = "";

		if (version == 'template' || !version) {
			url = 'services/json/workflow/downloadxpdltemplate';
		} else {
			url = 'services/json/workflow/downloadxpdl';
		}

		var basicForm = this.view.xpdlForm.getForm();
		basicForm.standardSubmit = true;

		basicForm.submit({
			url: url,
			method: "GET",
			target: "_self",
			params: {
				idClass: this.selection.getId()
			}
		});
	}
})();