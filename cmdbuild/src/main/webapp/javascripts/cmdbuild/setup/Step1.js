(function() {
	var tr = CMDBuild.Translation.configure.step1;
	
	Ext.define("CMDBuild.setup.Step1", {
		extend: "Ext.form.Panel",
		constructor: function() {
			this.title = CMDBuild.Translation.configure.title;

			this.languageCombo = new CMDBuild.field.LanguageCombo({
				name: 'language',
				fieldLabel: tr.choose,
				labelWidth: CMDBuild.LABEL_WIDTH
			});

			this.check = new Ext.ux.form.XCheckbox({
 				name: 'language_prompt',
 				fieldLabel: tr.showLangChoose,
 				labelWidth: CMDBuild.LABEL_WIDTH
 			});

			this.callParent(arguments);
		},

		initComponent: function() {
			this.items = [this.languageCombo, this.check];
			this.frame = true;
 			this.callParent(arguments);
 		}
});
	
})();