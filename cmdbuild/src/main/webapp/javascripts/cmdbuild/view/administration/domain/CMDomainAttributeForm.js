(function() {
	
	var tr =  CMDBuild.Translation.administration.modClass.attributeProperties;
	
	Ext.define("CMDBuild.view.administration.domain.CMDomainAttributeFormPanel",{
		extend: "CMDBuild.view.administration.classes.CMAttributeForm",
		domainName : undefined,
		initComponent: function() {
			this.callParent(arguments);
			this.attributeTypeStore.load({
				params : {
					tableType : "DOMAIN"
				}
			});
		},

		onClassSelected: Ext.emptyFn, 

		onDomainSelected: function(cmDomain) {
			this.domainName = cmDomain.getName();
			this.hideContextualFields();
		},

		onAttributeSelected : function(attribute) {
			this.reset();

			if (attribute) {
				var attributeData = attribute.raw || attribute.data;
				this.getForm().setValues(attributeData);
				this.disableModify(enableCMTbar = true);
				this.deleteButton.setDisabled(attribute.get("inherited"));
				this.hideContextualFields();
				this.showContextualFieldsByType(attribute.get("type"));
				Ext.apply(this.attributeDescription, {
					translationsKeyName: this.domainName,
					translationsKeySubName: attribute.get("name")
				});
			}
		},

		buildBasePropertiesPanel: function() {
			this.baseProperties = new Ext.form.FieldSet({
				title : tr.baseProperties,
                padding: "5 5 20 5",
				autoScroll : true,
				defaultType : "textfield",
				flex: 1,
				items : [
					this.attributeName,
					this.attributeDescription,
					this.isBasedsp,
					this.attributeUnique,
					this.attributeNotNull,
					this.isActive,
					{
						xtype: "hidden",
						name: "meta"
					},
					this.fieldMode
				]
			});
			Ext.apply(this.attributeDescription, {
				translationsKeyType: "DomainAttribute", 
				translationsKeyField: "Description"
			});
		}

	});	
})();