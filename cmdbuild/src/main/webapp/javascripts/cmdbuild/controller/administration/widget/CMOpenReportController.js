(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMOpenReportController", {

		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMOpenReportDefinitionForm.WIDGET_NAME
		},

		constructor: function() {
			this.callParent(arguments);

			this.mon(this.view, "cm-selected-report", onReportSelected, this);

			// to enable/disable the combo-box with the related check 
			this.view.forceFormatCheck.setValue = Ext.Function.createSequence(this.view.forceFormatCheck.setValue,
				function(v) {
					if (!this.forceFormatCheck.disabled) {
						this.forceFormatOptions.setDisabled(!v);
						if (v && typeof this.forceFormatOptions.getValue() != "string") {
							var f = this.forceFormatOptions;
							f.setValue(f.store.first().get(f.valueField));
						}
					}
				},
				this.view
			);
		},

		// override
		setDefaultValues: function() {
			this.callParent(arguments);
			this.view.forceFormatCheck.setValue(true);
		}

	});

	function onReportSelected(selectedReport) {
		var reportCode = getReportCode(selectedReport);

		Ext.Ajax.request({
			url: 'services/json/management/modreport/createreportfactory',
			params: {
				id: reportCode,
				type: "CUSTOM",
				extension: "pdf"
			},
			success: function(response) {
				var ret = Ext.JSON.decode(response.responseText),
					hasAttributeToSet = !ret.filled,
					data = [];

				if (hasAttributeToSet) {
					data = cleanServerAttributes(ret.attribute);
				}

				this.view.fillPresetWithData(data);
			},
			scope: this
		});
	}

	function getReportCode(selectedReport) {
		var reportCode = selectedReport;
		if (Ext.isArray(selectedReport)) {
			reportCode = selectedReport[0];
		}

		if (reportCode.self && reportCode.self.$className == "CMDBuild.model.CMReportAsComboItem") {
			reportCode = reportCode.get(CMDBuild.model.CMReportAsComboItem._FIELDS.id);
		}

		return reportCode;
	}

	function cleanServerAttributes(attributes) {
		var out = {};

		for (var i=0, l=attributes.length; i<l; ++i) {
			var attr = attributes[i];

			out[attr.name] = "";
		}

		return out;
	}

})();