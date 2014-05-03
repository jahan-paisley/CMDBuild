(function() {
	Ext.define("Ext.form.CMTranslatableText", {
		extend: "Ext.container.Container",
		layout: "hbox",
		fieldLabel : "no data",
		labelWidth: 0,
		padding: "0 0 5px 0",
		width: 0,
		name : 'no name',
		translationsKeyType: "", 
		translationsKeyName: "",
		translationsKeySubName: "",
		allowBlank: true,
		considerAsFieldToDisable: true,
		translationsKeyField: "",
		textArea : false,
		vtype : '',
		setValue: function(value) {
			return this.text.setValue(value);
		},
		getValue: function() {
			return this.text.getValue();
		},
		isValid: function() {
			return this.text.isValid();
		},
		reset: function() {
			this.text.reset();
		},
		enable: function() {
			this.text.enable();
			this.translationsButton.enable();
		},
		disable: function() {
			this.text.disable();
			this.translationsButton.disable();
		},
		resetLanguageButton: function() {
			if (_CMCache.isMultiLanguages()) {
				this.translationsButton.show();
			}
			else {
				this.translationsButton.hide();
			}
		},
		createTextItem: function() {
			return new Ext.form.field.Text( {
				fieldLabel : this.fieldLabel,
				labelWidth: this.labelWidth,
				width: this.width,
				name : this.name,
				allowBlank : this.allowBlank,
				vtype : this.vtype,
			});
		},
		setButtonMargin: Ext.emptyFn,
		initComponent : function() {
			this.text = this.createTextItem();
			this.width += 22;
			var me = this;
			this.translationsButton = new Ext.Button( {
				iconCls: 'translate',
				width: 22,
				tooltip: CMDBuild.Translation.translations,
				considerAsFieldToDisable: true,
				handler: function() {
					var translationsWindow = new CMDBuild.view.common.CMTranslationsWindow({
						title: CMDBuild.Translation.translations,
						translationsKeyType: me.translationsKeyType, 
						translationsKeyName: me.translationsKeyName,
						translationsKeySubName: me.translationsKeySubName,
						translationsKeyField: me.translationsKeyField,
						textArea: me.textArea
					});
					translationsWindow.show();
				}
			});
			this.setButtonMargin();
			this.items = [this.text, this.translationsButton];
			_CMCache.registerTranslatableText(this);
			this.callParent(arguments);
			this.resetLanguageButton();
		}
	});
	Ext.define("Ext.form.CMTranslatableTextArea", {
		extend: "Ext.form.CMTranslatableText",
		textArea : true,
		createTextItem: function() {
			return new Ext.form.field.TextArea( {
				fieldLabel : this.fieldLabel,
				labelWidth: this.labelWidth,
				width: this.width,
				name : this.name,
				allowBlank : this.allowBlank,
				vtype : this.vtype,
			});
		},
		setButtonMargin: function() {
			this.translationsButton.margin ="2 0 0 0";
		},
	});
})();