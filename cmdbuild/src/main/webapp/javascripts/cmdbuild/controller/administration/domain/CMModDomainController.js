(function() {
	Ext.ns("CMDBuild.administration.domain");
	var ns = CMDBuild.administration.domain;
	
	Ext.define("CMDBuild.controller.administration.domain.CMModDomainController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		constructor: function() {
			this.callParent(arguments);
			this.domain = null;
			this.formController = new CMDBuild.controller.administration.domain.CMDomainFormController(this.view.domainForm);
			this.attributesController = new CMDBuild.controller.administration.domain.CMDomainAttributesController(this.view.domainAttributes);
			
			this.view.addButton.on("click", this.onAddDomainButtonClick, this);
			_CMCache.on("cm_domain_deleted", this.view.onDomainDeleted, this.view);
		},

		onViewOnFront: function(selection) {
			if (selection) {
				this.domain = _CMCache.getDomainById(selection.get("id"));
				this.formController.onDomainSelected(this.domain);
				this.attributesController.onDomainSelected(this.domain);
				this.view.setTitleSuffix(this.domain.get("description"));
			}
		},
		
		onAddDomainButtonClick: function() {
			_CMMainViewportController.deselectAccordionByName("domain");
			this.view.selectPropertiesTab();
			this.formController.onAddButtonClick();
			this.attributesController.onAddButtonClick();
		}
	});
	
	function onAttributeSaved(jsonAttribute) {
		if (this.domain != null) {
			try {
				var attributeLibary = this.domain.getAttributeLibrary();
				var newAttribute = CMDBuild.core.model.CMAttributeModel.buildFromJson(jsonAttribute);
				var oldAttribute = attributeLibary.get(newAttribute.getname());
				
				if (oldAttribute == null) {
					this.attributesController.beforeAddAttributeToLibrary(newAttribute);
					attributeLibary.add(newAttribute);
				} else {
					oldAttribute.update(newAttribute);
				}
			} catch (e) {
				_debug(e);
			}
		}
	};

})();