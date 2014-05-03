(function() {
	
	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;
	
	Ext.define("CMDBuild.controller.administration.domain.CMDomainAttributesController", {
		extend: "CMDBuild.controller.administration.CMBaseAttributesController",
		constructor: function(view) {
			this.callParent(arguments);

			this.currentDomain = null;
			this.currentAttribute = null;

			this.gridSM = this.view.grid.getSelectionModel();
			this.gridSM.on('selectionchange', onSelectionChanged , this);

			this.view.form.abortButton.on("click", onAbortButtonClick, this);
			this.view.form.saveButton.on("click", onSaveButtonClick, this);
			this.view.form.deleteButton.on("click", onDeleteButtonClick, this);
			this.view.grid.addAttributeButton.on("click", onAddAttributeClick, this);
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
			params[parameterNames.CLASS_NAME] = _CMCache.getDomainNameById(this.getCurrentEntryTypeId());

			CMDBuild.ServiceProxy.attributes.reorder({
				params: params,
				success: function() {
					me.anAttributeWasMoved(attributes);
				}
			});
		},

		getGrid: function() {
			return this.view.grid;
		},

		getCurrentEntryTypeId: function() {
			return this.currentDomain.get("id");
		},

		onDomainSelected: function(domain) {
			this.currentDomain = domain;
			this.view.onDomainSelected(domain);
		},

		onAddButtonClick: function() {
			this.view.disable();
		},

		// synch the chache
		anAttributeWasMoved: function(savedAttributes) {
			if (this.currentDomain, savedAttributes) {
				var oldAttributes = this.currentDomain.get("attributes");
				for (var i = 0; i<savedAttributes.length; ++i) {
					var newAttr = savedAttributes[i];

					for (var j=0; j<oldAttributes.length; ++j) {
						oldAttr = oldAttributes[j];
						if (oldAttr.name == newAttr.name) {
							oldAttr.index = newAttr.index;
							break;
						}
					}
				}
			}
		}
	});

	function onSelectionChanged(selection) {
		if (selection.selected.length > 0) {
			_debug(selection.selected.items[0])
			this.currentAttribute = selection.selected.items[0];
			this.view.form.onAttributeSelected(this.currentAttribute);
		}
	}

	function onAddAttributeClick() {
		this.currentAttribute = null;
		this.view.onAddAttributeClick();
		_CMCache.initAddingTranslations();
	}
	
	function onAbortButtonClick() {
		if (this.currentAttribute == null) {
			this.view.form.disableModify();
			this.view.form.reset();
		} else {
			this.view.form.onAttributeSelected(this.currentAttribute);
		}
	}
	
	function onSaveButtonClick() {
		var nonValid = this.view.form.getNonValidFields();
		if (nonValid.length > 0) {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			return;
		}

		var data = this.view.form.getData(withDisabled = true);
		data.className = this.currentDomain.get("name");

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.administration.domain.attribute.save({
			params: data,
			scope: this,
			success: function(response, request, decoded) {
				this.currentAttribute = null;
				this.view.form.disableModify();
				_CMCache.onDomainAttributeSaved(this.currentDomain.get("id"), decoded.attribute);
				this.view.grid.selectAttributeByName(decoded.attribute.name);
				_CMCache.flushTranslationsToSave(this.currentDomain.get("name"), decoded.attribute.name);
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
	
	function onDeleteButtonClick() {
		Ext.Msg.show({
			title: translation.delete_attribute,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteAttribute.call(this);
				}
			}
		});
	}
	
	function deleteAttribute() {
		if (!this.currentDomain || !this.currentAttribute) {
			return;
		}

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.administration.domain.attribute.remove({
			params: {
				className: this.currentDomain.get("name"),
				name: this.currentAttribute.get("name")
			},
			scope : this,
			success : function(form, action) {
				_CMCache.onDomainAttributeDelete(this.currentDomain.get("id"), this.currentAttribute.data);
				this.currentAttribute = null;
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
})();