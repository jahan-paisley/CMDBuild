(function() {

	var tr = CMDBuild.Translation.administration.modClass.attributeProperties;

	Ext.define('CMDBuild.view.administration.workflow.CMProcessAttributesForm', {
		extend: 'CMDBuild.view.administration.classes.CMAttributeForm',

		// override
		buildBasePropertiesPanel: function() {
			this.baseProperties = Ext.create('Ext.form.FieldSet', {
				title: tr.baseProperties,
				margins: '0 0 0 3',
				autoScroll: true,

				items: [
					this.attributeName,
					this.attributeDescription,
					/*
					 * Business roule 11/01/2013
					 * The attributeGroup was removed because
					 * considered useless for a process
					 *
					 * Now it is considered useful, so it
					 * is enabled again
					 */
					this.attributeGroup,
					this.isBasedsp,
					this.attributeUnique,
					this.isActive,
					{
						xtype: 'hidden',
						name: 'meta'
					},
					this.fieldMode
				]
			});
		},

		// override
		takeDataFromCache: function(idClass) {
			return _CMCache.getProcessById(idClass);
		}
	});

})();