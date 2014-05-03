(function() {
	Ext.define("CMDBuild.core.buttons.CMClassesMenuButtonDelegate", {
		/**
		 * 
		 * @param {Ext.menu.Menu} menu
		 * @param {CMDBUild EntryType} entryType 
		 */
		onCMClassesMenuButtonItemClick: function(menu, entryType){}
	});

	Ext.define("CMDBuild.core.buttons.CMClassesMenuButton", {
		extend: "Ext.button.Button",
		menu: [],

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.core.buttons.CMClassesMenuButtonDelegate");

			this.callParent(arguments);
		},

		readClasses: function() {
			var entryTypes = _CMCache.getEntryTypes();
			var keys = Ext.Object.getKeys(entryTypes);
			keys = Ext.Array.sort(keys);
			for (var i=0, l=keys.length; i<l; ++i) {
				var entryType = entryTypes[keys[i]];
				if (entryType 
						&& !entryType.isProcess()
						&& entryType.get("name") != "Class") { // ugly condition

					var item = this.menu.add({
						text: entryType.get("text"),
						entryType: entryType
					});

					this.mon(item, 'click', onMenuItemClick, this);
				}
			}
		}
	});

	function onMenuItemClick(item) {
		var et = item.entryType;
		this.callDelegates("onCMClassesMenuButtonItemClick", [this, et]);
	}
})();