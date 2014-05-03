(function() {

	var GET = "GET", POST = "POST";
	CMDBuild.ServiceProxy.url.translations = {
			listAvailableTranslations :	"services/json/utils/listavailabletranslations",
			getConfiguration: "services/json/schema/setuptranslations/getconfiguration",
			setup : "services/json/schema/setuptranslations/",

			saveConfiguration: "services/json/schema/setuptranslations/saveconfiguration",
			saveClass:  "services/json/schema/setuptranslations/saveclass",
			saveClassAttribute:  "services/json/schema/setuptranslations/saveclassattribute",
			saveDomain:  "services/json/schema/setuptranslations/savedomain",
			saveDomainAttribute:  "services/json/schema/setuptranslations/savedomainattribute",
			saveFilterView:  "services/json/schema/setuptranslations/savefilterview",
			saveSqlView:  "services/json/schema/setuptranslations/savesqlview",
			saveFilter:  "services/json/schema/setuptranslations/savefilter",
			saveInstanceName:  "services/json/schema/setuptranslations/saveinstancename",
			saveWidget:  "services/json/schema/setuptranslations/savewidget",
			saveDashboard:  "services/json/schema/setuptranslations/savedashboard",
			saveChart:  "services/json/schema/setuptranslations/savechart",
			saveReport:  "services/json/schema/setuptranslations/savereport",
			saveLookup:  "services/json/schema/setuptranslations/savelookup",
			saveGisIcon: "services/json/schema/setuptranslations/savegisicon"

	};

	CMDBuild.ServiceProxy.translations = {
		readAvailableTranslations: function(p) {
			p.method = GET,
			p.url = CMDBuild.ServiceProxy.url.translations.listAvailableTranslations;

			CMDBuild.ServiceProxy.core.doRequest(p);
		},	
		readActiveTranslations: function(p) {
			p.method = GET;
			p.url = CMDBuild.ServiceProxy.url.translations.getConfiguration;
	
			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		saveActiveTranslations: function(p) {
			p.method = POST;
			p.url = CMDBuild.ServiceProxy.url.translations.saveConfiguration;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		/*Remove or save translations*/
		manageTranslations: function(p, url) {
			p.method = POST;
			p.url = url;
			CMDBuild.ServiceProxy.core.doRequest(p);
		}	
	};
})();