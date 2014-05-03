(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMWorkflowController", {
		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMWorkflowDefinitionForm.WIDGET_NAME
		},

		// override
		constructor: function() {
			this.callParent(arguments);

			this.mon(this.view, "cm-selected-workflow", onWorkflowSelected, this);

		},

		// override
		setDefaultValues: function() {
			this.callParent(arguments);
		}

	});
	function onWorkflowSelected(selectedWorkflow) {
		var workflowCode = selectedWorkflow[0].data.id;
		var me = this;
		_CMCache.getAttributeList(workflowCode, function(attributes) {
			me.cardAttributes = attributes;
			Ext.Ajax.request({
				url : 'services/json/workflow/getstartactivity',
				params : {
					classId: selectedWorkflow[0].data.id
				},
				success : function(response) {
					var ret = Ext.JSON.decode(response.responseText);
					var attributes = CMDBuild.controller.common.WorkflowStaticsController.filterAttributesInStep(me.cardAttributes, ret.response.variables);
					attributes = cleanServerAttributes(attributes);
					me.view.fillPresetWithData(attributes);
				}
			});
		});
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