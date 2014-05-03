(function() {
	Ext.define("CMDBuild.view.administration.configuration.CMTranslatableCheck", {
		extend: "Ext.container.Container",
		layout: "hbox",
		padding: "0 0 0 5",
		width: "100%",
		name : 'no name',
		allowBlank : false,
		vtype : '',
		setValue: function(value) {
			this.text.setValue(value);
		},
		getValue: function() {
			return this.text.getValue();
		},
		initComponent : function() {
			var me = this;
			this.check = new Ext.form.field.Checkbox( {
				fieldLabel : me.language,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : me.name,
			});
			this.width += 22;
			this.translationsButton = new Ext.form.field.Display( {
				iconCls: me.image,
				renderer : function(){
				    return '<div style="background-repeat:no-repeat;background-position:center;" class="' + me.image + '">&#160;</div>';
				},
				width: 22
			});
			this.items = [this.translationsButton, this.check];
			this.callParent(arguments);
		}
	});
	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationTranslations", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: CMDBuild.Translation.translations_enabled,
//		alias: "widget.configuregis",
		configFileName: 'translations',
		languages: [],
		constructor: function() {
			var me = this;
			me.callParent(arguments);
			this.languages = [];
			CMDBuild.ServiceProxy.translations.readAvailableTranslations({
				success : function(response, options, decoded) {
					for (key in decoded.translations) {
						var item = Ext.create("CMDBuild.view.administration.configuration.CMTranslatableCheck", {
								name: decoded.translations[key].name,
								image: "ux-flag-" + decoded.translations[key].name,
								language: decoded.translations[key].value
							});
						me.languages.push(decoded.translations[key].name);
						me.add(item);
					}
				}
			});
		}
	});
})();