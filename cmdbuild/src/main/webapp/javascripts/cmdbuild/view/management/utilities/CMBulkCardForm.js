(function() {

var check_suffix = "_check";

Ext.define("CMDBuild.view.management.utilities.CMBulkCardFormPanel", {
	extend: "Ext.form.Panel",
	autoScroll: true,

	mixins: {
		cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
	},

	constructor : function() {
        this.formFields = [];
		this.frame = true;

		this.callParent(arguments);
	},

	fillWithFieldsForClassId: function(classId) {
		this.removeAll();
		_CMCache.getAttributeList(classId, Ext.bind(fillForm, this));
	},
	
	getCheckedValues: function() {
		var out = {};
		this.items.each(function(item) {
			if (item.cmCheck.getValue()) {
				var f = item.cmCheck.field;
				out[f.name] = f.getValue();
			}
		});
		return out;
	}
});

	function fillForm(attributeList) {
		for (var i=0; i<attributeList.length; i++) {
			var attribute = attributeList[i];

			if (attribute.name != "Notes") {
				var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute);

				// FIXME find the reason why
				// HTML fields breaks the UI
				if (Ext.getClassName(field) == "Ext.form.field.HtmlEditor"
					|| Ext.getClassName(field) == "CMDBuild.view.common.field.CMHtmlEditorField") {

					continue;
				}

				if (field) {
					field.disable();
					field.margin = "0 0 0 5";

					var check = new Ext.ux.form.XCheckbox({
						// for the combo send the hiddenName
						name: (field.hiddenName || field.name) + check_suffix,
						field: field,
						labelSeparator: '',
						handler: function(box, checked) {
							this.field.setDisabled(!checked);
							if (checked) {
								this.field.focus(selectText = true);
							}
						}
					});

					var fieldSet = new Ext.panel.Panel({
						padding: "4 0 4 6",
						margin: "0 5 5 0",
						field: field,
						border: false,
						frame: true,
						layout: {
							type: 'hbox',
							align:'center'
						},
						items: [check, field],
						cmCheck: check // handle to the check to simplify the getCheckedValues method
					});

					field.disable();

					this.formFields[i] = fieldSet;
					this.add(fieldSet);
				}
			}
		}
		this.doLayout();
	}

})();