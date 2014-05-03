(function() {

	Ext.define("CMDBuild.delegate.administration.bim.CMBIMFormFieldsManager", {
		extend: "CMDBuild.delegate.administration.common.basepanel.CMBaseFormFiledsManager",

		/**
		 * @return {array} an array of Ext.component to use as form items
		 */
		build: function() {
			var fields = this.callParent(arguments);

			this.activeCheckBox = new Ext.ux.form.XCheckbox( {
				fieldLabel: CMDBuild.Translation.active,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: "active"
			});

			this.fileField = new Ext.form.field.File({
				fieldLabel: CMDBuild.Translation.ifc_file,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				name: "fileIFC"
			});

			this.cardBinding = new CMDBuild.Administration.bim.BindCardFieldsetItem({
			});

			fields.push(this.activeCheckBox, this.fileField, this.cardBinding);
			return fields;
		},
		
		/**
		 * 
		 * @param {Ext.data.Model} record
		 * the record to use to fill the field values
		 */
		// override
		loadRecord: function(record) {
			// TODO: set active
			this.callParent(arguments);
			this.activeCheckBox.setValue(record.get("active"));
			this.cardBinding.setValue(record.get("cardBinding"));
		},

		/**
		 * @return {object} values
		 * a key/value map with the values of the fields
		 */
		// override
		getValues: function() {
			var values = this.callParent(arguments);
			values["active"] = this.activeCheckBox.getValue();
			var ret = "";
			if (this.cardBinding) {
				ret = this.cardBinding.getValue();
			}
			values["card"] = ret;
			return values;
		},

		/**
		 * clear the values of his fields
		 */
		// override
		reset: function() {
			this.callParent(arguments);

			this.activeCheckBox.reset();
			this.fileField.reset();
			this.cardBinding.setValue("");
		},

		enableFileField: function() {
			this.fileField.enable();
		}
	});
	Ext.define("CMDBuild.Administration.bim.BindCardFieldsetItem", {
		extend: "Ext.container.Container",
		layout: "hbox",
		delegate: null, // pass it on creation
		cls: "x-panel-body-default-framed cmbordertop",		
		padding: "10px 0 0 0",
		itemId: "bimCardBinding",
		rootClassName: undefined,
		constructor: function(view) {
			this.callParent(arguments);
			// this must be loaded with BIM configuration
			// before to initialize the application
		},
		setValue: function(value) {
			if (value <= 0) {
				value = "";
			}
			if(this.cardCombo) {
				this.cardCombo.setValue(value);
			}
		},
		getValue: function() {
			var ret = "";
			if(this.cardCombo) {
				ret = this.cardCombo.getValue();
			}
			return ret;
		},
		initializeItems: function(rootClassName) {
			this.remove(this.className);
			this.remove(this.cardCombo);
			delete this.className;
			delete this.cardCombo;
			if (!rootClassName) {
				return;
			}
			this.rootClassName = rootClassName;
			this.className = new Ext.form.field.Text({
				margin: "0 5px 0 0",
				fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes.card_binding,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				disabled: true,
				cmImmutable: true,
				value: rootClassName
			});
			if(this.rootClassName) {
				this.cardCombo = CMDBuild.Management.ReferenceField.build({
					name: "card",
					referencedClassName: this.rootClassName,
					isnotnull: true
				}, null, {
					margin: "0 5px 0 0",
					gridExtraConfig: {
						cmAdvancedFilter: false,
						cmAddGraphColumn: false,
						cmAddPrintButton: false
					},
					searchWindowReadOnly: true
				});
				Ext.apply(this.cardCombo, {
					allowBlank: true,
				});

			}
			else {
				this.cardCombo = undefined;
			}
			this.insert(0, this.className);
			this.className.setDisabled(true);
			if (this.cardCombo) {
				this.cardCombo.fieldLabel = "";
				this.cardCombo.labelAlign = "right";
				this.cardCombo.labelWidth = 0;
				this.insert(1, this.cardCombo);
				this.cardCombo.setDisabled(true);
			}
		}
	});
})();
