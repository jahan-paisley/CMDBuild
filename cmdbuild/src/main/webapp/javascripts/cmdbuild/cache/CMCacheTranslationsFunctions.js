(function() {
	var withTranslations = false;
	var activeTranslations = [];
	var observers = [];
	var translationsToSave = [];
	var translationsInAdding = false;
	Ext.define("CMDBUild.cache.CMCacheTranslationsFunctions", {
		initAddingTranslations: function() {
			translationsToSave = [];
			translationsInAdding = true;
		},
		finishAdding: function() {
			translationsInAdding = false;
			this.flushTranslationsToSave();
		},
		initModifyingTranslations: function() {
			translationsInAdding = false;
			translationsToSave = [];
		},
		flushTranslationsToSave: function(translationsKeyName, translationsKeySubName) {
			for (var i = 0; i < translationsToSave.length; i++) {
				var t = translationsToSave[i];
				saveTranslations(t.translationsKeyType, translationsKeyName, 
						translationsKeySubName, t.translationsKeyField, t.values);
			}
		},
		saveTranslations: function(translationsKeyType, translationsKeyName, 
				translationsKeySubName, translationsKeyField, values) {	
			if (translationsInAdding) {
				var t = {
					"translationsKeyType" : translationsKeyType, 
					//"translationsKeyName": translationsKeyName, during the adding i don't have this value 
					//"translationsKeySubName": translationsKeySubName, during the adding i don't have this value
					"translationsKeyField": translationsKeyField, 
					"values": values
				};
				translationsToSave.push(t);
			}
			else {
				saveTranslations(translationsKeyType, translationsKeyName, 
						translationsKeySubName, translationsKeyField, values);
			}
		},
		isMultiLanguages: function() {
			return withTranslations;
		},
		resetMultiLanguages: function() {
			setActiveTranslations();
		},
		getActiveTranslations: function() {
			return activeTranslations;
		},
		registerTranslatableText: function(text) {
			observers.push(text);
		}
	});
	function callObservers() {
		for (var i = 0; i < observers.length; i++) {
			var text = observers[i];
			text.resetLanguageButton();
		}
	}
	function setActiveTranslations() {
		activeTranslations = [];
		CMDBuild.ServiceProxy.translations.readActiveTranslations({
			scope: this,
			success: function(response){
				var activeLanguages = Ext.JSON.decode(response.responseText).data;
				CMDBuild.ServiceProxy.translations.readAvailableTranslations({
					success : function(response, options, decoded) {
						withTranslations = false;
						for (key in decoded.translations) {
							if (activeLanguages[decoded.translations[key].name] != "on") {
								continue;
							}
							var item = {
								name: decoded.translations[key].name,
								image: "ux-flag-" + decoded.translations[key].name,
								language: decoded.translations[key].value
							};
							activeTranslations.push(item);
							withTranslations = true;
						}
						callObservers();
					}
				});
			}
		});
	}
	function saveTranslations(translationsKeyType, translationsKeyName, 
			translationsKeySubName, translationsKeyField, values) {
		switch(translationsKeyType) {
			case "Class" :
				saveClass(translationsKeyName, translationsKeyField, values);
				break;
			case "ClassAttribute" :
				saveClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values);
				break;
			case "Domain" :
				saveDomain(translationsKeyName, translationsKeyField, values);
				break;
			case "DomainAttribute" :
				saveDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values);
				break;
			case "FilterView" :
				saveFilterView(translationsKeyName, translationsKeyField, values);
				break;
			case "SqlView" :
				saveSqlView(translationsKeyName, translationsKeyField, values);
				break;
			case "Filter" :
				saveFilter(translationsKeyName, translationsKeyField, values);
				break;
			case "InstanceName" :
				saveInstanceName(values);
				break;
			case "Widget" :
				saveWidget(translationsKeyName, translationsKeyField, values);
				break;
			case "Dashboard" :
				saveDashboard(translationsKeyName, translationsKeyField, values);
				break;
			case "Chart" :
				saveChart(translationsKeyName, translationsKeyField, values);
				break;
			case "Report" :
				saveReport(translationsKeyName, translationsKeyField, values);
				break;
			case "Lookup" :
				saveLookup(translationsKeyName, translationsKeyField, values);
				break;
			case "GisIcon" :
				saveGisIcon(translationsKeyName, translationsKeyField, values);
				break;
		}
		
	}
	function saveClass(translationsKeyName, translationsKeyField, values) {
		var params = {
				className: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveClass);
	}
	function saveClassAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values) {
		var params = {
				className: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveClassAttribute);
	}
	function saveDomain(translationsKeyName, translationsKeyField, values) {
		var params = {
				domainName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveDomain);
	}
	function saveDomainAttribute(translationsKeyName, translationsKeySubName, translationsKeyField, values) {
		var params = {
				domainName: translationsKeyName,
				attributeName: translationsKeySubName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveDomainAttribute);
	}
	function saveFilterView(translationsKeyName, translationsKeyField, values) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveFilterView);
	}
	function saveSqlView(translationsKeyName, translationsKeyField, values) {
		var params = {
				viewName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveSqlView);
	}
	function saveFilter(translationsKeyName, translationsKeyField, values) {
		var params = {
				filterName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveFilter);
	}
	function saveInstanceName(values) {
		var params = {
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveInstanceName);
	}
	function saveWidget(translationsKeyName, translationsKeyField, values) {
		var params = {
				widgetId: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveWidget);
	}
	function saveDashboard(translationsKeyName, translationsKeyField, values) {
		var params = {
				dashboardName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveDashboard);
	}
	function saveChart(translationsKeyName, translationsKeyField, values) {
		var params = {
				chartName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveChart);
	}
	function saveReport(translationsKeyName, translationsKeyField, values) {
		var params = {
				reportName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveReport);
	}
	function saveLookup(translationsKeyName, translationsKeyField, values) {
		var params = {
				lookupId: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveLookup);
	}
	function saveGisIcon(translationsKeyName, translationsKeyField, values) {
		var params = {
				iconName: translationsKeyName,
				field: translationsKeyField,
				translations: Ext.JSON.encodeValue(values)
		};
		CMDBuild.ServiceProxy.translations.manageTranslations({params : params}, CMDBuild.ServiceProxy.url.translations.saveGisIcon);
	}
})();