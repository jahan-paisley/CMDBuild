(function() {
	Ext.define("CMDBuild.view.management.common.widgets.CMManageRelation", {
		extend: "CMDBuild.view.management.classes.CMCardRelationsPanel",

		statics: {
			WIDGET_NAME: ".ManageRelation"
		},

		// used by the controller to identify the
		// selected rows
		CHECK_NAME: "manage_relation_check",

		constructor: function(c) {
			this.widgetConf = c.widget;
			this.callParent(arguments);
		},

		initComponent: function() {
			var reader = CMDBuild.management.model.widget.ManageRelationConfigurationReader;

			this.CHECK_NAME += reader.id(this.widgetConf);
			this.border= false;
			this.frame = false;
			this.cls = "x-panel-body-default-framed";
			this.cmWithAddButton = reader.canCreateAndLinkCard(this.widgetConf) 
					|| reader.canCreateRelation(this.widgetConf);

			this.callParent(arguments);
		},

		renderRelationActions: function(value, metadata, record) {
			if (record.get("depth") == 1) { // the domains node has no icons to render
				return "";
			}

			var actionsHtml = '',
				reader = CMDBuild.management.model.widget.ManageRelationConfigurationReader,
				widget = this.widgetConf,
				isSel = (function(record) {
					var id = parseInt(record.get('CardId'));
					if (typeof widget.currentValue == "undefined") {
						return false;
					} else {
						return widget.currentValue.indexOf(id) >= 0;
					}
				})(record);

			if (reader.singleSelection(widget) 
					|| reader.multiSelection(widget)) {

				var type = reader.singleSelection(widget) ? 'radio' : 'checkbox';

				actionsHtml += '<input type="' + type + '" name="'
						+ this.CHECK_NAME + '" value="'
						+ record.get('dst_id') + '"';

				if (isSel) {
					actionsHtml += ' checked="true"';
				}

				actionsHtml += '/>';
			}

			if (reader.canModifyARelation(widget)) {
				actionsHtml += getImgTag("edit", "link_edit.png");
			}

			if (reader.canRemoveARelation(widget)) {
				actionsHtml += getImgTag("delete", "link_delete.png");
			}

			if (reader.canModifyALinkedCard(widget)) {
				actionsHtml += getImgTag("editcard", "modify.png");
			}

			if (reader.canDeleteALinkedCard(widget)) {
				actionsHtml += getImgTag("deletecard", "delete.png");
			}

			return actionsHtml;
		}
	});

	function getImgTag(action, icon) {
		return '<img style="cursor:pointer" class="action-relation-'+ action +'" src="images/icons/' + icon + '"/>';
	}

})();