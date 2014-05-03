(function() {
	
	Ext.define("CMDBuild.controller.administration.lookup.CMLookupTypeFormController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		
		constructor: function() {
			this.currentLookupType = null;

			this.callParent(arguments);

			this.view.saveButton.on("click", this.onSaveButtonClick, this);

			this.view.abortButton.on("click", function() {
				var modifyMode = this.currentLookupType != null;
				this.view.disableModify(enableTBar = modifyMode);
				reloadLookupType.call(this);
			}, this);
		},

		onSaveButtonClick: function() {
			CMDBuild.LoadMask.get().show();
			var values = this.view.getValues();
			var cl = this.currentLookupType
			values.orig_type = (function() {
				if (cl) {
					return cl.id;
				}
			})();

			CMDBuild.ServiceProxy.lookup.saveLookupType({
				form: this.view.getForm(),
				scope: this,
				params: values,
				success: onSaveSuccess,
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		},

		onSelectLookupType: function(lookupType) {
			this.currentLookupType = lookupType;
			this.view.onSelectLookupType(lookupType);
		},
		
		onAddLookupTypeClick: function() {
			this.currentLookupType = null;
			this.view.onNewLookupType();
		}
	});

	function reloadLookupType() {
		this.view.getForm().reset();
		if (this.currentLookupType) {
			this.view.onSelectLookupType(this.currentLookupType);
		}
	}

	function onSaveSuccess(response) {
		var json = Ext.JSON.decode(response.responseText);
		var lType = json.lookup;

		if (json.isNew) {
			_CMCache.onNewLookupType(lType);
		} else {
			_CMCache.onModifyLookupType(lType);
		}

		this.currentLookupType = lType;//need to manage the reload after abort
		this.view.disableModify(enableTBar = true);
		CMDBuild.LoadMask.get().hide();
	}
})();