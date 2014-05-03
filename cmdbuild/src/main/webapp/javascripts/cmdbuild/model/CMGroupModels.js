(function() {
	Ext.define("CMDBuild.cache.CMGroupModel", {
		statics: {
			type: {
				NORMAL: "normal",
				ADMIN: "admin",
				CLOUD_ADMIN: "restrictedAdmin"
			}
		},
		extend: 'Ext.data.Model',
		fields: [
			{name: "description", type: "string"},
			{name: "id", type: "string"},
			{name: "isActive", type: "boolean"},
			{name: "isAdministrator", type: "boolean"},
			{name: "isCloudAdministrator", type: "boolean"},
			{name: "disabledModules", type: "auto"},
			{name: "name", type: "string"},
			{name: "email", type: "string"},
			{name: "text", type: "string"},
			{name: "startingClass", type: "string"}
		],

		isActive: function() {
			return this.get("isActive");
		},

		isAdmin: function() {
			return this.get("isAdministrator");
		},

		isCloudAdmin: function() {
			return this.get("isCloudAdministrator");
		},

		getType: function() {
			var type = CMDBuild.cache.CMGroupModel.type.NORMAL;
			if (this.isAdmin()) {
				if (this.isCloudAdmin()) {
					type = CMDBuild.cache.CMGroupModel.type.CLOUD_ADMIN;
				} else {
					type = CMDBuild.cache.CMGroupModel.type.ADMIN;
				}
			}

			return type;
		}
	});

	Ext.define("CMDBuild.cache.CMGroupModelForCombo", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "description", type: "string"},
			{name: "id", type: "int"},
			{name: "isdefault", type: "boolean"}
		]
	});

	Ext.define("CMDBuild.cache.CMPrivilegeModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "groupId", type: "string"},
			{name: "privilegedObjectId", type: "string"},
			{name: "privilegedObjectName", type: "string"},
			{name: "privilegedObjectDescription", type: "string"},
			{name: "privilegeFilter", type: "auto"},
			{name: "attributesPrivileges", type: "auto"},
			{name: "none_privilege", type: "boolean"},
			{name: "read_privilege", type: "boolean"},
			{name: "write_privilege", type: "boolean"}
		],

		getGroupId: function() {
			return this.get("groupId");
		},

		getPrivilegedObjectId: function() {
			return this.get("privilegedObjectId");
		},

		getPrivilegeFilter: function() {
			return this.get("privilegeFilter");
		},

		getAttributePrivileges: function() {
			return this.get("attributesPrivileges") || {};
		},

		setPrivilegeFilter: function(privilegeFilter) {
			try {
				this.set("privilegeFilter", privilegeFilter);
				this.commit();
			} catch (error) {
				// may be rendering issues
			}
		},

		setAttributePrivileges: function(attributePrivileges) {
			try {
				this.set("attributesPrivileges", attributePrivileges);
				this.commit();
			} catch (error) {
				// may be rendering issues
			}
		}

	});

	Ext.define("CMDBuild.cache.CMUserForGridModel", {
		extend: 'Ext.data.Model',
		fields: [
			{name: "description", type: "string"},
			{name: "username", type: "string"},
			{name: "isActive", type: "boolean"},
			{name: "userid", type: "string"},
			{name: "email", type: "string"},
			{name: "defaultgroup", type: "int"}
		]
	});

	Ext.define("CMDBuild.model.CMUIConfigurationModel", {
		extend: 'Ext.data.Model',

		statics: {
			cardTabs: {
				details: "classDetailTab",
				notes: "classNoteTab",
				relations: "classRelationTab",
				history: "classHistoryTab",
				attachments: "classAttachmentTab"
			},

			processTabs: {
				notes: "processNoteTab",
				relations: "processRelationTab",
				history: "processHistoryTab",
				attachments: "processAttachmentTab"
			}
		},

		fields: [
			{name: "disabledModules", type: "auto"},
			{name: "disabledCardTabs", type: "auto"},
			{name: "disabledProcessTabs", type: "auto"},
			{name: "fullScreenMode", type: "boolean"},
			{name: "hideSidePanel", type: "boolean"},
			{name: "processWidgetAlwaysEnabled", type: "boolean"},
			{name: "simpleHistoryModeForCard", type: "boolean"},
			{name: "simpleHistoryModeForProcess", type: "boolean"},
			{name: "cloudAdmin", type: "boolean"}
		],

		/*
		 * GETTERS
		 */
		getDisabledModules: function() {
			return this.get("disabledModules") || [];
		},

		getDisabledCardTabs: function() {
			return this.get("disabledCardTabs") || [];
		},

		getDisabledProcessTabs: function() {
			return this.get("disabledProcessTabs") || [];
		},

		isFullScreenMode: function() {
			return this.get("fullScreenMode") || false;
		},

		isHideSidePanel: function() {
			return this.get("hideSidePanel") || false;
		},

		isProcessWidgetAlwaysEnabled: function() {
			return this.get("processWidgetAlwaysEnabled") || false;
		},

		isSimpleHistoryModeForCard: function() {
			return this.get("simpleHistoryModeForCard") || false;
		},

		isSimpleHistoryModeForProcess: function() {
			return this.get("simpleHistoryModeForProcess") || false;
		},

		isCloudAdmin: function() {
			return this.get("cloudAdmin") || false;
		},

		/*
		 * SETTERS
		 */
		setDisabledModules: function(disabledModules) {
			return this.set("disabledModules", disabledModules);
		},

		setDisabledCardTabs: function(disabledCardTabs) {
			return this.set("disabledCardTabs", disabledCardTabs);
		},

		setDisabledProcessTabs: function(disabledProcessTabs) {
			return this.set("disabledProcessTabs", disabledProcessTabs);
		},

		setFullScreenMode: function(fullScreenMode) {
			return this.set("fullScreenMode", fullScreenMode);
		},

		setHideSidePanel: function(hideSidePanel) {
			return this.set("hideSidePanel", hideSidePanel);
		},

		setProcessWidgetAlwaysEnabled: function(processWidgetAlwaysEnabled) {
			return this.set("processWidgetAlwaysEnabled", processWidgetAlwaysEnabled);
		},

		setSimpleHistoryModeForCard: function(simpleHistoryModeForCard) {
			return this.set("simpleHistoryModeForCard", simpleHistoryModeForCard);
		},

		setSimpleHistoryModeForProcess: function(simpleHistoryModeForProcess) {
			return this.set("simpleHistoryModeForProcess", simpleHistoryModeForProcess);
		},

		/*
		 * OTHERS
		 */

		isCardTabDisabled: function(name) {
			return Ext.Array.contains(this.getDisabledCardTabs(), name);
		},

		isProcessTabDisabled: function(name) {
			return Ext.Array.contains(this.getDisabledProcessTabs(), name);
		},

		isModuleDisabled: function(name) {
			return Ext.Array.contains(this.getDisabledModules(), name);
		},

		toString: function() {
			return Ext.encode(this.getData());
		}
	});
})();