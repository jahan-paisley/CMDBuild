(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMGridController", {
		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMGrid.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);
			this.ownerController = ownerController;
			this.classType = _CMCache.getEntryTypeByName(widgetDef.className);
			this.view = view;
			this.view.delegate = this;

			var me = this;
			CMDBuild.Management.FieldManager.loadAttributes( //
				this.classType.get("id"), //
				function(attributes) { //
					me.view.cardAttributes = attributes;
					me.view.setColumnsForClass();
				} //
			);

		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case "onAdd" :
					this.view.newRow();
					break;
				case "onDelete" :
					this.view.deleteRow(param.rowIndex);
					break;
				case "onEdit" :
					this.openEditWindow(param.record, this.view.cardAttributes);
					break;
				case "onEditClosed" :
					this.saveEditWindow();
					this.editWindow.destroy();
					break;
				default: {
					if (
						this.parentDelegate
						&& typeof this.parentDelegate === 'object'
					) {
						return this.parentDelegate.cmOn(name, param, callBack);
					}
				}
			}
			return undefined;
		},
		
		openEditWindow: function(record, cardAttributes) {
			this.editWindow = Ext.create("CMDBuild.view.management.common.widgets.CMGridEdit", {
				title: CMDBuild.Translation.row_edit,
				record: record,
				cardAttributes: cardAttributes,
				delegate: this
			});
			this.editWindow.show();
		},

		saveEditWindow: function() {
			this.editWindow.saveData();
		},

		getCurrentClass: function() {
			return this.classType;
		},

		// override
		getData: function() {
			var out = null;
			if (!this.readOnly) {
				out = {};
				out["output"] = this.view.getData();
			}

			return out;
		},

		destroy: function() {
			this.callParent(arguments);
		}
	});
})();
