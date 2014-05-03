Ext.define("CMDBuild.controller.management.common.CMModClassAndWFCommons", {
	/*
	 * retrieve the form to use as target for the
	 * templateResolver asking it to its view
	 * 
	 * returns null if something is not right
	 */

	getFormForTemplateResolver: function() {
		var form = null;
		if (this.view) {
			var wm = this.view.getWidgetManager();
			if (wm && typeof wm.getFormForTemplateResolver == "function") {
				form = wm.getFormForTemplateResolver() || null;
			}
		}

		return form;
	}
});