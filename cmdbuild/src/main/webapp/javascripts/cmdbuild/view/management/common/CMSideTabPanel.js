(function() {
	Ext.define("CMDBuild.view.common.CMSideTabPanel", {
		extend: "Ext.panel.Panel",
		frame: false,
		border: false,
		pressedTabCls: "cmdb-pressed-tab",
		tabCls: "cmdb-tab",
		bodyCls: "x-panel-body-default-framed",

		layout: {
			type:'vbox',
			align:'stretchmax'
		},

		defaults:{margins:'2 4 0 0'},

		initComponent: function() {
			if (fixRendereingIssueForIE7()) {
				this.maxTabWidth = 0;
			} else {
				this.autoWidth = true;
			}
			this.callParent(arguments);
		},

		addTabFor: function(panel, additionalCls) {
			var tabCls = this.tabCls;
			var pressedTabCls = this.pressedTabCls;
			var t = new Ext.container.Container({
				text: panel.title,
				cls: tabCls,
				height: 25,
				html: (function(panel, additionalCls) {
					var tmpl;
					if (additionalCls) {
						tmpl = "<div class=\"cmdb-tab-icon {1}\"></div><p>{0}</p>";
						return Ext.String.format(tmpl, panel.tabLabel, additionalCls);
					} else {
						tmpl = "<p>{0}</p>";
						return Ext.String.format(tmpl, panel.tabLabel);
					}
				})(panel, additionalCls),
				targetPanel: panel,
				listeners: {
					render: function(p) {
						p.getEl().on('click', Ext.Function.bind(p.fireEvent,p, ["click", p]));
					}
				}
			});

			t.on("click", onTabClick, this);

			if (fixRendereingIssueForIE7()) {
				t.on("afterlayout", function(p) {
					var tabWidth = p.getWidth();
					if (this.maxTabWidth < tabWidth) {
						this.setWidth(tabWidth + 22);
					}
				}, this, {
					single : true
				});
			}

			panel.on("activate", function() {
				manageToggleTab.call(this, t); // to manage the first activation without a real click
				if (fixRendereingIssueForIE7()) {
					panel.doLayout();
				}
			}, this);
			this.add(t);
		},

		activateFirst: function() {
			var f = this.items.first();
			if (f) {
				onTabClick.call(this, f);
			}
		}
	});

	function manageToggleTab(tab) {
		try {
			if (this.pressedTab) {
				this.pressedTab.removeCls(this.pressedTabCls);
			}
			tab.addClass(this.pressedTabCls);
			this.pressedTab = tab;
		} catch (e) {
			// unknown render issues, no time to investigate
		}
	};

	function fixRendereingIssueForIE7() {
		return Ext.isIE7;
	}

	function onTabClick(tab) {
		manageToggleTab.call(this, tab);
		this.fireEvent("click", tab);
	}
})();