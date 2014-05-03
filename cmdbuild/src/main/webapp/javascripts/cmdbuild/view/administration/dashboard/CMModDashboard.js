(function() {

	var BASE_TITLE = CMDBuild.Translation.administration.modDashboard.title;
		TITLE_SUFFIX_SEPARATOR = " - ";

	// interface of the view
	Ext.define("CMDBuild.view.administration.dashboard.CMModDashboardInterface", {
		cmName:'dashboard',
		setTitleSuffix: Ext.emptyFn,
		getPropertiesPanel: Ext.emptyFn,
		getChartsConfigurationPanel: Ext.emptyFn,
		setDelegate: Ext.emptyFn,
		toString: function() {
			return Ext.getClassName(this);
		}
	});

	// interface of the view delegate
	Ext.define("CMDBuild.view.administration.dashboard.CMModDashboardDelegate", {
		onAddButtonClick: Ext.emptyFn
	});

	// view implementation
	Ext.define("CMDBuild.view.administration.dashboard.CMModDashboard", {
		extend: "Ext.panel.Panel",

		mixins: {
			cminterface: "CMDBuild.view.administration.dashboard.CMModDashboardInterface"
		},

		constructor: function() {
			this.callParent(arguments);
			this.delegate = new CMDBuild.view.administration.dashboard.CMModDashboardDelegate();
		},

		setDelegate: function(d) {
			CMDBuild.validateInterface(d, "CMDBuild.view.administration.dashboard.CMModDashboardDelegate");
			this.delegate = d;
		},

		initComponent : function() {
			var me = this;

			this.addButton = new Ext.button.Button({
				text: CMDBuild.Translation.administration.modDashboard.properties.add,
				iconCls: "add",
				handler: function() {
					me.delegate.onAddButtonClick();
				}
			});

			Ext.apply(this, {
				title: BASE_TITLE,
				layout: "border",
				frame: false,
				border: true,
				items: [{
					xtype: "tabpanel",
					region: "center",
					border: false,
					frame: false,
					items: [
						propertiesPanel(me),
						chartsConfigurationPanel(me),
						layoutConfigurationPanel(me)
					]
				}],
				tbar: [this.addButton]
			});

			this.callParent(arguments);
		},

		setTitleSuffix: function(suffix) {
			var title = BASE_TITLE;
			if (suffix) {
				title += TITLE_SUFFIX_SEPARATOR + suffix;
			}

			this.setTitle(title);
		},

		activateFirstTab: function() {
			var t = this.items.first();
			if (t) {
				var l = t.getLayout();
				if (l) {
					l.setActiveItem(0);
				}
			}
		}
	});

	function propertiesPanel(me) {
		var p = Ext.createByAlias('widget.dashboardproperties');

		me.getPropertiesPanel = function() {
			return p;
		};

		return p;
	}

	function chartsConfigurationPanel(me) {
		var p = Ext.createByAlias('widget.dashboardchartsconfiguration', {
			disabled: true
		});

		me.getChartsConfigurationPanel = function() {
			return p;
		};

		return p;
	}

	function layoutConfigurationPanel(me) {
		var p = Ext.createByAlias('widget.dashboardlayoutconfiguration', {
			disabled: true
		});

		me.getLayoutConfigurationPanel = function() {
			return p;
		};

		return p;
	}

})();