(function() {

	Ext.define("CMDBuild.controller.administration.CMBaseAttributesController", {
		constructor: function(view) {
			this.view = view;
			this.getGrid().on("cm_attribute_moved", this.onAttributeMoved, this);
		},

		onAttributeMoved: function() {
			var parameterNames = CMDBuild.ServiceProxy.parameter;
			var attributes = [];
			var store = this.getGrid().getStore();

			for (var i=0, l=store.getCount(); i<l; i++) {
				var rec = store.getAt(i);
				var attribute = {};
				attribute[parameterNames.NAME] = rec.get("name");
				attribute[parameterNames.INDEX] = i+1;
				attributes.push(attribute);
			}

			var me = this;
			var params = {};
			params[parameterNames.ATTRIBUTES] = Ext.JSON.encode(attributes);
			params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.getCurrentEntryTypeId());

			CMDBuild.ServiceProxy.attributes.reorder({
				params: params,
				success: function() {
					me.anAttributeWasMoved(attributes);
				}
			});
		},

		getGrid: function() {
			throw "Unimplemented";
		},

		getCurrentEntryTypeId: function() {
			throw "Unimplemented";
		},

		anAttributeWasMoved: function(attributeList) {}
	});

	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;
	
	Ext.define("CMDBuild.controller.administration.classes.CMClassAttributeController", {
		extend: "CMDBuild.controller.administration.CMBaseAttributesController",
		constructor: function(view) {
			this.callParent(arguments);

			this.currentClassId = null;

			this.gridSM = this.view.gridPanel.getSelectionModel();
			this.gridSM.on('selectionchange', onSelectionChanged , this);

            this.view.on("activate", onViewActivate, this);

			this.view.formPanel.abortButton.on("click", onAbortClick, this);
			this.view.formPanel.saveButton.on("click", onSaveClick, this);
			this.view.formPanel.deleteButton.on("click", onDeleteClick, this);
			this.view.gridPanel.addAttributeButton.on("click", onAddAttributeClick, this);
			this.view.gridPanel.orderButton.on("click", buildOrderingWindow, this);
            this.view.gridPanel.store.on("load", onAttributesAreLoaded, this);
		},

		getGrid: function() {
			return this.view.gridPanel;
		},

		getCurrentEntryTypeId: function() {
			return this.currentClassId;
		},

		onClassSelected: function(classId) {
			this.currentClassId = classId;
			this.view.enable();
			if (tabIsActive(this.view)) {
				this.toLoad = false;
				this.view.onClassSelected(this.currentClassId);
			} else {
				this.toLoad = true;
			}
		},

		onAddClassButtonClick: function() {
			this.view.disable();
		}

	});

	function onAttributesAreLoaded(store, records) {
		this.view.formPanel.fillAttributeGroupsStore(records);
	}

	function onViewActivate() {
		if (this.toLoad) {
			this.view.onClassSelected(this.currentClassId);
		}
	}

	function onSaveClick() {
		var nonValid = this.view.formPanel.getNonValidFields();
		if (nonValid.length > 0) {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			return;
		}

		var data = this.view.formPanel.getData(withDisabled = true);
		data[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.currentClassId);

		if (this.view.formPanel.referenceFilterMetadataDirty) {
			data.meta = Ext.JSON.encode(this.view.formPanel.referenceFilterMetadata);
		}

		var me = this;
		CMDBuild.LoadMask.get().show();
		_CMProxy.attributes.update({
			params : data,
			success : function(form, action, decoded) {
				me.view.gridPanel.refreshStore(me.currentClassId, decoded.attribute.index);
				_CMCache.flushTranslationsToSave(_CMCache.getEntryTypeNameById(me.currentClassId), decoded.attribute.name);
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function onAbortClick() {
		if (this.currentAttribute == null) {
			this.view.formPanel.reset();
			this.view.formPanel.disableModify();
		} else {
			onSelectionChanged.call(this, null, [this.currentAttribute]);
		}
	}

	function onDeleteClick() {
		Ext.Msg.show({
			title: tr.delete_attribute,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteCurrentAttribute.call(this);
				}
			}
		});
	}

	function deleteCurrentAttribute() {
		if (this.currentAttribute == null) {
			return; //nothing to delete
		}

		var me = this;
		var params = {};
		var parameterNames = CMDBuild.ServiceProxy.parameter;
		params[parameterNames.NAME] = me.currentAttribute.get("name");
		params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.currentClassId);

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.attributes.remove({
			params: params,
			callback : function() {
				CMDBuild.LoadMask.get().hide();
				me.view.formPanel.reset();
				me.view.formPanel.disableModify();
				me.view.gridPanel.refreshStore(me.currentClassId);
			}
		});
	}

	function onSelectionChanged(sm, selection) {
		if (selection.length > 0) {
			this.currentAttribute = selection[0];
			this.view.formPanel.onAttributeSelected(this.currentAttribute);
		}
	}

	function onAddAttributeClick() {
		this.currentAttribute = null;
		this.view.formPanel.onAddAttributeClick();
		this.view.gridPanel.onAddAttributeClick();
		_CMCache.initAddingTranslations();
	}
	
	function buildOrderingWindow() {
		if (this.currentClassId) {
			var win = new CMDBuild.Administration.SetOrderWindow( {
				idClass : this.currentClassId
			}).show(); 
		}
	}
    
    function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}
})();