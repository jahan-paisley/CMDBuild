(function() {

	Ext.define("CMDBuild.administration.utilities.UtilitiesAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		dataUrl: '',

		submodules: {
			changePassword: {
                title: CMDBuild.Translation.management.modutilities.changepassword.title,
	            cmName: 'changepassword'
	        },
	        bulkupdate: {
	            title:CMDBuild.Translation.management.modutilities.bulkupdate.title,
                cmName: 'bulkcardupdate'
	        },
	        importcsv: {
	            title: CMDBuild.Translation.management.modutilities.csv.title,
	            cmName: 'importcsv'
	        },
            exportcsv: {
	            title: CMDBuild.Translation.management.modutilities.csv.title_export,
	            cmName: 'exportcsv'
	        }
		},

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();			
		},
		
		updateStore: function() {
			var structure = [];
			for (var moduleName in this.submodules) {
				var module = this.submodules[moduleName];
				if (this.submoduleIsEnabled(moduleName)) {
					structure.push({
						text: module.title,
						cmName: module.cmName,
						leaf: true
					});
				}
			}

			var root = this.store.getRootNode();
			root.removeAll();
			if (structure.length > 0) {
				root.appendChild(structure);
			}
		},

		submoduleIsEnabled: function(moduleName) {
			if (moduleName == 'changePassword' && !CMDBuild.Runtime.CanChangePassword) {
				return false;
			} else {
				if (typeof _CMUIConfiguration != "undefined") {
					return !_CMUIConfiguration.isModuleDisabled(moduleName);
				} else {
					return false;
				}
			}
		},

		getSubmoduleCMName: function(submodule) {
			var sm = this.submodules[submodule];
			if (sm) {
				return sm.cmName;
			} else {
				return undefined;
			}
		}

	});

})();