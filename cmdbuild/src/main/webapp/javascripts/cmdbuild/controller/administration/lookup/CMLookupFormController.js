(function() {
	var LOOKUP_FIELDS = CMDBuild.ServiceProxy.LOOKUP_FIELDS;
	
	Ext.define("CMDBuild.controller.administration.lookup.CMLookupFormController", {
		constructor: function(view) {
			this.view = view;
			this.currentLookup = null;
			this.currentLookupType = null;
			this.subController = null;

			this.view.abortButton.on("click", function() {
				var modifyMode = this.currentLookup != null;
				this.view.disableModify(enableTBar = modifyMode);
				reloadLookup.call(this);
			}, this);
			
			this.view.saveButton.on("click", onSaveClick, this);
			this.view.disabelButton.on("click", onDisableButtonClick, this);
		},

		bindSubController: function(c) {
			this.subController = c;
		},

		onSelectLookupGrid: function(selection) {
			this.currentLookup = selection;
			this.view.onSelectLookupGrid(selection);
			this.view.description.translationsKeyName = selection.get("Id");
		},
		
		onAddLookupClick: function() {
			this.currentLookup = null;
			this.view.onAddLookupClick();
			_CMCache.initAddingTranslations();
			this.view.description.translationsKeyName = "";
		},
		
		onSelectLookupType: function(lookupType) {
			this.currentLookupType = lookupType;
			this.view.onSelectLookupType(lookupType);
		},
		
		onAddLookupTypeClick: function() {
			this.currentLookupType = null;
			this.view.disableModify();
			this.view.reset();
		}
	});
	
	function reloadLookup() {
		if (this.currentLookup != null) {
			this.view.onSelectLookupGrid(this.currentLookup);
		}
	}
	
	function notifySubController(event, params) {
		this.subController[event](params);
	}
	
	function onSaveClick() {
		CMDBuild.LoadMask.get().show();
		var data = this.view.getData();
		data.Type = this.currentLookupType;
		
		CMDBuild.ServiceProxy.lookup.saveLookup({
			params: data,
			scope : this,
			success : function(a, b, decoded) {
				notifySubController.call(this, "onLookupSaved", decoded.lookup.Id);
				_CMCache.flushTranslationsToSave(decoded.lookup.Id);
			},
			failure : function() {
				reloadLookup.call(this);
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
	
	function onDisableButtonClick() {
		CMDBuild.LoadMask.get().show();
		var disable = this.view.activeCheck.getValue();
		var id = this.view.getForm().findField(LOOKUP_FIELDS.Id).value;
		CMDBuild.ServiceProxy.lookup.setLookupDisabled({
			params : {
				id : id
			},
			scope : this,
			success : function(a, b, decoded) {
				this.view.updateDisableEnableLookup(disable);
				notifySubController.call(this, "onLookupDisabled", id);
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		}, disable);
	}
})();