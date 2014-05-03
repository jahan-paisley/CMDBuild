(function() {

var tr = CMDBuild.Translation.administration.modsecurity.uiconfiguration;
var tabLabels = CMDBuild.Translation.management.modcard.tabs;

Ext.define("CMDBuild.view.administration.group.CMGroupUIConfigurationPanel", {
	extend: "Ext.form.Panel",
	mixins: {
		cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
	},

	constructor: function() {

		this.saveButton = new CMDBuild.buttons.SaveButton();
		this.abortButton = new CMDBuild.buttons.AbortButton();
		this.cmButtons = [this.saveButton, this.abortButton];

		this.callParent(arguments);
	},

	initComponent: function() {
		this.title = tr.title;
		this.buttonAlign = "center";
		this.buttons = this.cmButtons;
		this.layout = {
			type: 'vbox',
			align: 'stretch'
		};

		this.items = [
			wrapWithPanel([chechboxGroup(getModulesFromStructure(this), tr.disabled_modules)]),
			wrapWithPanel([chechboxGroup(getClassTabToDisable(this), tr.disabled_class_tabs)]),
			wrapWithPanel([chechboxGroup(getpProcessTabToDisable(this), tr.disabled_process_tabs)]),
			wrapWithPanel([chechboxGroup(getGenericProp(this), tr.other)])
		];

		this.frame = false;
		this.border = false;
		this.cls = "x-panel-body-default-framed";
		this.bodyCls = 'cmgraypanel';
		this.autoScroll = true;

		this.callParent(arguments);
	},

	loadGroupConfiguration: function(uiConfiguration) {
		this.reset();
		var form = this.getForm();
		var fields = form.getFields(); 

		// set the check-box that have the same name of an attribute of the model
		form.loadRecord(uiConfiguration);

		// looking for the checked attributes at the arrays of the model
		if (fields) {
			var checksToCheck = uiConfiguration.getDisabledModules()
			.concat(uiConfiguration.getDisabledCardTabs())
			.concat(uiConfiguration.getDisabledProcessTabs());

			for (var i=0, check = null, fieldName=""; i<checksToCheck.length; ++i) {
				fieldName = checksToCheck[i];
				check = fields.findBy(function(f) {
					return fieldName == f.name;
				});

				if (check) {
					check.setValue(true);
				}
			}
		}
	},

	getUIConfiguration: function() {
		var data = this.getForm().getValues();
		data["disabledModules"] = getCheckedNames(this.disabledModulesChecks);
		data["disabledCardTabs"] = getCheckedNames(this.classTabToDisableCheks);
		data["disabledProcessTabs"] = getCheckedNames(this.processTabToDisableCheks);

		return new CMDBuild.model.CMUIConfigurationModel(data);
	}
});

function getCheckedNames(checkArray) {
	var out = [];
	checkArray = checkArray || [];

	for (var i=0, c=null; i<checkArray.length; ++i) {
		c = checkArray[i];
		if (c && c.getValue()) {
			out.push(c.name);
		}
	}

	return out;
}

function getClassTabToDisable(me) {
	var tabs = CMDBuild.model.CMUIConfigurationModel.cardTabs;

	return me.classTabToDisableCheks = [
		getCheck(tabLabels.detail, tabs.details),
		getCheck(tabLabels.notes, tabs.notes),
		getCheck(tabLabels.relations, tabs.relations),
		getCheck(tabLabels.history, tabs.history),
		getCheck(tabLabels.attachments, tabs.attachments)
	];
}

function getpProcessTabToDisable(me) {
	var tabs = CMDBuild.model.CMUIConfigurationModel.processTabs;

	return me.processTabToDisableCheks = [
		getCheck(tabLabels.notes, tabs.notes),
		getCheck(tabLabels.relations, tabs.relations),
		getCheck(tabLabels.history, tabs.history),
		getCheck(tabLabels.attachments, tabs.attachments)
	];
}

function getGenericProp(me) {
	var l = tr.generic_properties;

	return me.genericPropertiesCheks = [
		getCheck(l.hide_side_panel, "hideSidePanel"),
		getCheck(l.full_screen_navigation, "fullScreenMode"),
		getCheck(l.card_simple_history, "simpleHistoryModeForCard"),
		getCheck(l.process_simple_history, "simpleHistoryModeForProcess"),
		getCheck(l.always_enabled_widgets, "processWidgetAlwaysEnabled")
	];
}

function getModulesFromStructure(me) {
	var moduleCkecks = [];
	var structure = {
		"class": {	
			title:  CMDBuild.Translation.management.modcard.treetitle
		},
		process: {
			title: CMDBuild.Translation.management.modworkflow.treetitle
		},
		report: {
			title: CMDBuild.Translation.management.modreport.treetitle
		},
		dataView: {
			title: CMDBuild.Translation.management.modview.title
		},
		dashboard: {
			title: CMDBuild.Translation.administration.modDashboard.title
		},
		utilities: {
			title: CMDBuild.Translation.management.modutilities.title,
			submodules: {
				changePassword: {
					title: CMDBuild.Translation.management.modutilities.changepassword.title
				},
				bulkupdate: {
					title:CMDBuild.Translation.management.modutilities.bulkupdate.title
				},
				importcsv: {
					title: CMDBuild.Translation.management.modutilities.csv.title
				},
				exportcsv: {
					title: CMDBuild.Translation.management.modutilities.csv.title_export
				}
			}
		}
	};

	for (var moduleKey in structure) {
		var module = structure[moduleKey];
		var title = module.title;
		var submodules = module.submodules;

		if (submodules) {
			for (var sub in submodules) {
				var submodule = module.submodules[sub];
				moduleCkecks.push(new Ext.ux.form.XCheckbox({
					fieldLabel: '',
					labelSeparator: '',
					boxLabel: title+" - "+submodule.title,
					name: sub
				}));
			}
		} else {
			if (title) {
				moduleCkecks.push(new Ext.ux.form.XCheckbox({
					fieldLabel: '',
					labelSeparator: '',
					boxLabel: title,
					name: moduleKey
				}));
			}
		}
	}

	me.disabledModulesChecks = moduleCkecks;
	return moduleCkecks;
}

function wrapWithPanel(items, title) {
	return new Ext.panel.Panel({
		items: items,
		title: title,
		frame: false,
		border: false,
		bodyCls: 'cmgraypanel',
		padding: "5 5 5 5",
		cls: "x-panel-body-default-framed cmborderbottom"
	});
}

function chechboxGroup(items, label) {
	return {
		xtype: 'checkboxgroup',
		labelWidth: CMDBuild.LABEL_WIDTH,
		columns: 1,
		fieldLabel: label,
		items: items
	};
}

function getCheck(label, name) {
	return new Ext.ux.form.XCheckbox({
		labelWidth: CMDBuild.LABEL_WIDTH,
		labelSeparator: '',
		boxLabel: label,
		name: name
	});
}

function setModulesToDisable(me, disabledModules) {
	for (var i = 0, len = disabledModules.length; i < len; ++i) {
		var moduleName = disabledModules[i];
		var moduleCheck = me.modulesCheckInput.toMap[moduleName];
		if (moduleCheck) {
			moduleCheck.setValue(true);
		}
	}
}

})();