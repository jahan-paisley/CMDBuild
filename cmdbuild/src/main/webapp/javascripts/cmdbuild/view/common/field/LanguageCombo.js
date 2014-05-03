(function() {

function getCurrentLanguage() {
	var languageParam = Ext.urlDecode(window.location.search.substring(1))['language'];
	return languageParam || CMDBuild.Config.cmdbuild.language;
}

function changeLanguage(lang) {
	window.location = Ext.String.format('?language={0}', lang);
}

Ext.define("CMDBuild.field.LanguageCombo", {
	extend: "CMDBuild.field.CMIconCombo",

	initComponent: function() {
		Ext.apply(this, {
			valueField: "name",
			displayField: "value",
			queryMode: "local",
			store: CMDBuild.ServiceProxy.setup.getLanguageStore()
		});

		this.callParent(arguments);

		this.on({
			select:{scope:this, fn:function(combo, record) {
				var lang = record[0].get("name");
				changeLanguage(lang);
			}}
		});

		this.store.on({
			load:{scope:this, fn:function() {
				var lang = getCurrentLanguage();
				this.setValue(lang);
			}}
		});
	}
});

})();