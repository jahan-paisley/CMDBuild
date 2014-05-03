(function() {

	var languageStore = null;

	CMDBuild.ServiceProxy.setup = {
		testDBConnection: function(p) {
			p.method = "GET";
			p.url = 'services/json/configure/testconnection';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		applySetup: function(p) {
			p.method = "POST";
			p.url = 'services/json/configure/apply';

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		getLanguageStore: function() {
			if (languageStore == null) {
				languageStore = new Ext.data.JsonStore({
	 				model: "TranslationModel",
					autoLoad: true,
	 				proxy: {
						type: "ajax",
						url: "services/json/utils/listavailabletranslations",
						reader: {
							type: "json",
							root: "translations"
						}
					},
					sorters: {
						property: 'name',
						direction: 'ASC'
					}
	 			});
			}

			return	languageStore;
		}
	};

})();