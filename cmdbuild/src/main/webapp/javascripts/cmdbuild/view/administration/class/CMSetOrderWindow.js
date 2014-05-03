Ext.define("CMDBuild.Administration.SetOrderWindow", {
	extend: "CMDBuild.PopupWindow",
	height: 300,
	width: 300,
	initComponent: function() {

		this.saveBtn = new CMDBuild.buttons.SaveButton({
			handler: this.onSave,
			scope: this
		});

		this.abortBtn = new CMDBuild.buttons.AbortButton({
			handler: this.onAbort,
			scope: this
		});

		this.grid = new CMDBuild.Administration.AttributeSortingGrid({
			idClass: this.idClass,
			border: false
		});

		this.items = [this.grid];
		this.buttonAlign = 'center';
		this.buttons = [this.saveBtn, this.abortBtn];

		this.callParent(arguments);
	},

	onSave: function() {
		var editPlugin = this.grid.plugins[0];

		if (editPlugin) {
			editPlugin.completeEdit(); // to update the record
		}

		this.hide();
		var records = this.grid.getStore().getRange();
		var attributes = {};

		for (var order = 0, i = 0, len=records.length; i<len; i++) {
			var rec = records[i];
			if (rec.data.classOrderSign == 0) {
				continue;
			}
			++order;
			attributes[rec.data.name] = (rec.data.classOrderSign > 0 ? order : -order);
		}

		var parameterNames = CMDBuild.ServiceProxy.parameter;
		var params = {};
		var me = this;
		params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.idClass);
		params[parameterNames.ATTRIBUTES] = Ext.encode(attributes);

		CMDBuild.ServiceProxy.attributes.updateSortConfiguration({
			params: params,
			waitTitle : CMDBuild.Translation.common.wait_title,
			waitMsg : CMDBuild.Translation.common.wait_msg,
			callback: function() {
				me.onAbort();
			}
		});
	},

	onAbort: function() {
		try {
			this.close();
		} catch (e) {
			_debug(e);
		}
	}
});