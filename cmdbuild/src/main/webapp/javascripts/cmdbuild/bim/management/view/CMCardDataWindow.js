(function() {

	Ext.define("CMDBuild.bim.view.CMCardDataWinodwDelegate", {
		/**
		 * @param {CMDBuild.bim.view.CMCardDataWindow} cardDataWindow
		 */
		cardDataWindowOpenCardButtonWasClicked: function(cardDataWindow) {}
	});

	Ext.define("CMDBuild.bim.view.CMCardDataWindow", {
		extend: "Ext.window.Window",
		modal: true,
		// configuration
		cmCardData: undefined,
		delegate: undefined,
		attributeConfigurations: [],
		// configuration

		initComponent: function() {

			this.title = this.cmCardData.IdClass_value;
			this.autoScroll = true;
			this.width = 400;
			this.height = 300;

			this.delegate = this.delegate || new CMDBuild.bim.view.CMCardDataWinodwDelegate();

			var me = this;
			this.tbar = [{
				text: CMDBuild.Translation.display_more_attributes,
				enableToggle: true,
				toggleHandler: function(button, state) {
					if (state) {
						button.setText(CMDBuild.Translation.display_less_attributes);
						addAttributes(me, false);
					} else {
						button.setText(CMDBuild.Translation.display_more_attributes);
						me.items.each(function(item) {
							if (!item.basedsp) {
								me.remove(item);
							}
						});
					}
				}
			}, {
				text: CMDBuild.Translation.management.modcard.open_relation,
				iconCls: "arrow_right",
				handler: function() {
					me.delegate.cardDataWindowOpenCardButtonWasClicked(me);
				}
			}];
			this.fbar = [{
				xtype: 'tbspacer',
				flex : 1
			},
			{
				type: 'button', 
				text: CMDBuild.Translation.common.buttons.confirm,
				handler : function() {
					me.delegate.cardDataWindowOkButtonWasClicked(me);
				}
			},
			{
				xtype: 'tbspacer',
				flex : 1
			}];
			this.items = [];
			this.callParent(arguments);

			addAttributes(this, true);
		}
	});

	function addAttributes(me, basedsp) {
		for (var i = 0, l = me.attributeConfigurations.length; i < l; ++i) {
			var attribute = me.attributeConfigurations[i];

			if (basedsp != attribute.isbasedsp) {
				continue;
			}

			var value = me.cmCardData[attribute.name];
			if (typeof value == "object") {
				value = value.description;
			}

			value = value || "";

			me.add({
				xtype: 'displayfield',
				fieldLabel: attribute.description,
				labelAlign: "right",
				value: value,
				basedsp: basedsp
			});
		}
	}

})();