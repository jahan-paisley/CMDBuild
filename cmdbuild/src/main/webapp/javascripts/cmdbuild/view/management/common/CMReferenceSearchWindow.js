Ext.define("CMDBuild.Management.ReferenceSearchWindow", {
	extend: "CMDBuild.Management.CardListWindow",
	
	initComponent: function() {
		this.selection = null;

		this.saveButton = new Ext.Button({
			text : CMDBuild.Translation.common.buttons.save,
			name: 'saveButton',
			disabled: true,
			handler : this.onSave,
			scope : this
		});

		this.buttonAlign = "center";
		this.buttons = [this.saveButton];

		this.callParent(arguments);

		// after show the window
		// get the focus to the
		// quick filter, and set to it
		// a starting value, if the windows
		// was opened typing on the reference combo
		this.on("show", function() {
			if (this.grid.gridSearchField) {
				var me = this;

				Ext.Function.createDelayed(function() {
					me.grid.gridSearchField.focus();
					me.grid.gridSearchField.setValue(me.searchFieldValue);
				}, 100)();

			}
		}, this, {
			single: true
		});
	},

	// override
	buildGrdiConfiguration: function() {
		var config = this.callParent(arguments);
		var storeParams = this.extraParams;

		return Ext.apply(config, {
			getStoreExtraParams: function() {
				return storeParams;
			}
		});

	},

	// override
	onSelectionChange: function(sm, selection) {
		if (selection.length > 0) {
			this.saveButton.enable();
			this.selection = selection[0];
		} else {
			this.saveButton.disable();
			this.selection = null;
		}
	},

	// override
	onGridDoubleClick: function() {
		this.onSave();
	},

	// private
	onSave: function() {
		if (this.selection != null) {
			this.fireEvent('cmdbuild-referencewindow-selected', this.selection);
		}
		this.destroy();
	}
});