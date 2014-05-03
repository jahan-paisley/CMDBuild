(function() {
	Ext.define("CMDBuild.view.common.field.CMHtmlEditorField", {
		extend: "Ext.form.field.HtmlEditor",
		enableExpand: true, // to have a button that increase the height

		initComponent: function() {
			// set the defaultValue to empty string,
			// because the Ext default value has encoding problems
			// when used in some query
			this.defaultValue = "";

			this.plugins = Ext.Array.from(this.plugins);
			this.plugins.push(new Ext.ux.form.HtmlEditor.Word({
				langToolTip: CMDBuild.Translation.clean_word_pasted_text
			}));
			this.plugins.push(new Ext.ux.form.HtmlEditor.RemoveFormat({
				langToolTip: CMDBuild.Translation.remove_formatting,
				langTitle: CMDBuild.Translation.remove_formatting
			}));

			this.callParent(arguments);

			/*
			 * Some problems setting the
			 * value since the field is hidden
			 * see the setValue override
			 */
			this.mon(this, "activate", function() {
				if (typeof this.danglingValue != "undefined") {
					this.setValue(this.danglingValue);
				}
			}, this);
		},

		getToolbarCfg: function() {
			var toolbarConfig = this.callParent(arguments);

			if (this.enableExpand) {
				toolbarConfig.items.push("->");
				toolbarConfig.items.push({
					iconCls: "expand",
					scope: this,
					handler: expandButtonHandler
				});
			}

			return toolbarConfig;
		},

		/*
		 * Do not disable also the label
		 */
		// override
		disable: function() {
			var childElements = this.items.items;
			for (var i=0, l=childElements.length, el=null; i<l; ++i) {
				el = childElements[i];
				el.disable();
			}
		},

		// override
		enable: function() {
			var childElements = this.items.items;
			for (var i=0, l=childElements.length, el=null; i<l; ++i) {
				el = childElements[i];
				el.enable();
			}
		},

		/*
		 * There is problem to set value
		 * if the field is not visible
		 */
		// override
		setValue: function(value) {
			if (this.isVisible()) {
				this.callParent(arguments);
				this.danglingValue = undefined;
			} else {
				this.danglingValue = value;
			}
		},

		/*
		 * is not considered the
		 * allowBlank configuration
		 */
		// override
		isValid: function() {
			if (typeof this.allowBlank == "undefined"
				|| this.allowBlank === true) {
					return this.callParent(arguments);
			} else {
				var value = this.getValue();
				value = Ext.String.trim(value);
				return value != "" && value != null;
			}
		}
	});

	function expandButtonHandler() {
		var me = this;
		var conf = me.initialConfig;
		var htmlField = new CMDBuild.view.common.field.CMHtmlEditorField( //
				Ext.apply(conf, {
					hideLabel: true,
					resizable: false,
					enableExpand: false,
					region: "center"
				}) //
			);

		var popup = new CMDBuild.PopupWindow({
			title: conf.fieldLabel,
			items: [{
				xtype: "panel",
				layout: "border",
				border: false,
				frame: false,
				items: htmlField
			}],
			buttonAlign: "center",
			buttons: [{
				text: CMDBuild.Translation.common.buttons.confirm,
				handler: function() {
					me.setValue(htmlField.getValue());
					popup.destroy();
				}
			}, {
				text: CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					popup.destroy();
				}
			}]
		});

		popup.show();
		htmlField.setValue(me.getValue());
	}
})();
