(function() {

	Ext.define("CMDBuild.controller.common.WorkflowStaticsController", {
		statics: {

			// iterate over all the attributes of the Process and take only the ones defined as variables for this step
			filterAttributesInStep: function(attributes, variables) {
				var out = [],
					modeConvertionMatrix = {
						READ_ONLY: 'read',
						READ_WRITE: 'write',
						READ_WRITE_REQUIRED: 'required'
					};

				for (var j = 0, variable = null; j < variables.length; ++j) {
					variable = variables[j];

					for (var i = 0, attr = null; i < attributes.length; ++i) {
						attr = attributes[i];
						if (attr.name == variable.name) {
							attr.fieldmode = modeConvertionMatrix[variable.type];
							out.push(attr);
							break;
						}
					}
				}

				return out;
			}
		}
	});

	Ext.define("CMDBuild.controller.common.CardStaticsController", {
		statics: {
			getInvalidField: function(cmForm) {
				var fields = cmForm.getFields(),
					invalid = [];

				fields.each(function(field) {
					if (!field.isValid()) {
						invalid.push(field);
					}
				});

				return invalid;
			},

			getInvalidAttributeAsHTML: function(cmForm) {
				var fields = CMDBuild.controller.common.CardStaticsController.getInvalidField(cmForm);
				var alreadyAdded = {};

				if (fields.length == 0) {
						return null;
				} else {
					var out = '<ul>';
					for (var i = 0, l = fields.length; i < l; ++i) {
						var attribute = fields[i].CMAttribute,
							item = '';

						if (attribute) {
							if (alreadyAdded[attribute.description]) {
								continue;
							} else {
								alreadyAdded[attribute.description] = true;
								if (attribute.group) {
									item = attribute.group + ' - ';
								}
								out += '<li>' + item + attribute.description + '</li>';
							}
						}
					}

					return out + '</ul>';
				}
			}
		}
	});

})();