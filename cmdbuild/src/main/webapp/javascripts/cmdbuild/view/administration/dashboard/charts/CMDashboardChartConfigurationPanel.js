(function() {

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanelInterface", {
		enableTBarButtons: Ext.emptyFn,
		disableTBarButtons: Ext.emptyFn,
		enableButtons: Ext.emptyFn,
		disableButtons: Ext.emptyFn,
		setDelegate: Ext.emptyFn,
		getFormPanel: Ext.emptyFn,
		getGridPanel: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanelDelegate", {
		onModifyButtonClick: Ext.emptyFn,
		onAddButtonClick: Ext.emptyFn,
		onRemoveButtonClick: Ext.emptyFn,
		onPreviewButtonClick: Ext.emptyFn,
		onSaveButtonClick: Ext.emptyFn,
		onAbortButtonClick: Ext.emptyFn
	});

	var tr = CMDBuild.Translation.administration.modDashboard.charts;

	Ext.define("CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanel", {
		extend : "Ext.panel.Panel",
		alias: "widget.dashboardchartsconfiguration",

		mixins: {
			cminterface: "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanelInterface"
		},

		constructor : function() {
			this.callParent(arguments);
		},

		initComponent : function() {
			var me = this;

			Ext.apply(this, {
				title: tr.title,
				layout: "border",
				cls: 'cmgraypanel-nopadding',
				bodyCls: 'cmgraypanel-nopadding',
				tbar : tbar(me),
				buttons: buttons(me),
				border: false,
				items: [grid(me),form(me)]
			});

			this.callParent(arguments);
		},

		// Toolbar
		enableTBarButtons: function(onlyAdd) {
			this.addButton.enable();
			var disable = onlyAdd === true; // check that is a true boolean

			this.modifyButton.setDisabled(disable);
			this.removeButton.setDisabled(disable);
			this.previewButton.setDisabled(disable);
		},

		disableTBarButtons: function() {
			this.addButton.disable();
			this.modifyButton.disable();
			this.removeButton.disable();
			// this.previewButton.disable();
		},

		// buttons
		enableButtons: function() {
			this.saveButton.enable();
			this.abortButton.enable();
		},

		disableButtons: function() {
			this.saveButton.disable();
			this.abortButton.disable();
		},

		// delegate
		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanelDelegate");
			this.delegate = d;
		}
	});

	function form(me) {
		var form = Ext.createByAlias("widget.dashboardchartsconfigurationform", {
			region: "center",
			frame: false,
			border: false,
			bodyCls: 'cmgraypanel',
			cls: 'cmbordertop',
			style: {
				"padding": "5px"
			}
		});

		me.getFormPanel = function() {
			return form;
		}

		return form;
	}

	function grid(me) {
		var grid = Ext.createByAlias("widget.dashboardchartsconfigurationgrid", {
			region: "north",
			split: true,
			border: false,
			frame: false,
			cls: 'cmborderbottom',
			height: "40%"
		});

		me.getGridPanel = function() {
			return grid;
		}

		return grid;
	}

	function tbar(me) {
		me.addButton = new Ext.button.Button({
			text : tr.buttons.add,
			iconCls : "add",
			disabled: true,
			handler: function() {
				me.delegate.onAddButtonClick();
			}
		});

		me.modifyButton = new Ext.button.Button({
			text : tr.buttons.modify,
			iconCls : "modify",
			disabled: true,
			handler: function() {
				me.delegate.onModifyButtonClick();
			}
		});

		me.removeButton = new Ext.button.Button({
			text : tr.buttons.remove,
			iconCls : "delete",
			disabled: true,
			handler: function() {
				me.confirm = Ext.Msg.show({
					title: CMDBuild.Translation.administration.modDashboard.properties.remove,
					msg: CMDBuild.Translation.common.confirmpopup.areyousure,
					buttons: Ext.Msg.YESNO,
					fn: function(button) {
						if (button == "yes") {
							me.delegate.onRemoveButtonClick();
						}
					}
				});
			}
		});

		me.previewButton = new Ext.button.Button({
			text: tr.buttons.preview,
			iconCls: "preview",
			disabled: true,
			handler: function() {
				me.delegate.onPreviewButtonClick();
			}
		});

		return [me.addButton, me.modifyButton, me.removeButton, "-", me.previewButton];
	}

	function buttons(me) {
		me.saveButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.save,
			disabled: true,
			handler: function() {
				me.delegate.onSaveButtonClick();
			}
		});

		me.abortButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.abort,
			disabled: true,
			handler: function() {
				me.delegate.onAbortButtonClick();
			}
		});

		me.buttonAlign = "center";
		return [me.saveButton, me.abortButton];
	}
})();