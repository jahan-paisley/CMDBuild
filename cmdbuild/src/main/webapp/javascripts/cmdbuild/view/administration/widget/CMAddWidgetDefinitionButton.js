(function() {
	Ext.define("CMDBuild.view.administration.widget.CMAddWidgetDefinitionButton", {
		extend: "Ext.button.Split",
		iconCls: 'add',
		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				text: CMDBuild.Translation.common.buttons.add,
				menu : {items : buildMenuItems(me)},
				handler: function() {
					this.showMenu();
				},
				scope: this
			});

			this.callParent(arguments);
		}
	});

	function buildMenuItems(me) {
		var items = [];

		for (var key in CMDBuild.view.administration.widget.form) {
			var widgetName = CMDBuild.view.administration.widget.form[key].WIDGET_NAME;
			if (widgetName) { // to skip the base class
				items.push({
					text: CMDBuild.Translation.administration.modClass.widgets[widgetName].title,
					WIDGET_NAME: widgetName,
					handler: function() {
						me.fireEvent("cm-add", this.WIDGET_NAME);
					}
				});
			}
		}

		return items;
	}
})();