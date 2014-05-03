(function() {
	var point = CMDBuild.Constants.geoTypes.point,
		line = CMDBuild.Constants.geoTypes.line,
		polygon = CMDBuild.Constants.geoTypes.polygon,
		tr_attribute = CMDBuild.Translation.administration.modClass.attributeProperties,
		tr = CMDBuild.Translation.administration.modClass.geo_attributes;

	Ext.define("CMDBuild.view.administration.classes.CMGeoAttributeForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		initComponent: function() {
			buildGenericProperties.call(this);
			buildStyleProperties.call(this);
			buildButtons.call(this);

			Ext.apply(this, {
				plugins : [new CMDBuild.FormPlugin()],
				frame: false,
				border: false,
				cls: "x-panel-body-default-framed cmbordertop",
				bodyCls: 'cmgraypanel',
				tbar: this.cmTBar,
				layout: {
					type: 'hbox',
					align: 'stretch'
				},
				items: [this.genericProperties, this.styleProperties],
				buttonAlign: "center",
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify(enableCMTbar = false);
			
			this.name.on("change", function(fieldname, newValue, oldValue) {
				this.autoComplete(this.description, newValue, oldValue);
			}, this);
		},

		getStyle: function() {
			var out = {};
			for (var fieldName in this.styleFieldsMap) {
				var field = this.styleFieldsMap[fieldName];
				var value = field.getValue();
				if (field.isVisible() && value!="") {
					out[fieldName] = value;
				}
			}
			return out;
		},
		
		onClassSelected: function() {
			this.reset();
			this.hideStyleFields();
			this.disableModify(enableCMTbar = false);
		},
		
		onAttributeSelected: function(attribute) {
			this.reset();

			if (attribute) {
				this.hideStyleFields();
				this.getForm().setValues(attribute.data);
				fillStyleFields.call(this, Ext.decode(attribute.data.style));
				this.disableModify(enableCMTbar = true);
			}
		},
		
		iterateOverStyleFields: function(fn) {
			for (var key in this.styleFieldsMap) {
				fn(this.styleFieldsMap[key]);
			}
		},
		
		hideStyleFields: function() {
			this.iterateOverStyleFields(function(f) {
				f.hide();
			})
		},

		showStyleFieldsByType: function(type) {
			var me = this;

			this.iterateOverStyleFields(function(f) {
				if (f.allowedGeoTypes[type]) {
					f.show();
					f.setDisabled(!me._cmEditMode);
				} else {
					f.hide();
				}
			})
		},

		setDefaults: function() {
			this.styleProperties.setDefaults();
		}
	});
	
	function fillStyleFields(style) {
		if (style) {
			for ( var propName in style) {
				this.styleFieldsMap[propName].setValue(style[propName]);
			}
		}
	}

	function buildGenericProperties() {
		this.name = new Ext.form.TextField({
			fieldLabel: tr_attribute.name,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name: "name",
			cmImmutable: true,
			vtype : 'alphanum',
			allowBlank: false
		});

		this.description = new Ext.form.TextField({
			fieldLabel: tr_attribute.description,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name: "description",
			allowBlank: false
		});

		this.minZoom = new Ext.form.SliderField( {
			fieldLabel: tr.min_zoom,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			minValue: 0,
			maxValue: 25,
			name: "minZoom"
		});

		this.maxZoom = new Ext.form.SliderField( {
			fieldLabel: tr.max_zoom,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			minValue: 0,
			maxValue: 25,
			value: 25,
			name: "maxZoom"
		});

		this.genericProperties = new Ext.form.FieldSet( {
			margin: "0 0 5 5",
			title: tr_attribute.baseProperties,
			flex: 1,
			autoScroll: true,
			items: [this.name, this.description, this.minZoom, this.maxZoom]
		});
	};

	function buildButtons() {
		this.saveButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.save
		});

		this.abortButton = new Ext.button.Button( {
			text: CMDBuild.Translation.common.buttons.abort
		});

		this.cancelButton = new Ext.button.Button({
			text: tr_attribute.delete_attribute,
			iconCls: 'delete'
		});

		this.modifyButton = new Ext.button.Button({
			text: tr_attribute.modify_attribute,
			iconCls: 'modify'
		});

		this.cmTBar = [this.modifyButton, this.cancelButton],
		this.cmButtons = [this.saveButton, this.abortButton]
	};

	function buildStyleProperties() {
		this.types = new Ext.form.ComboBox( {
			store: new Ext.data.SimpleStore( {
				fields: [ "value", "name" ],
				data: [
					[ point, tr.type.point ],
					[ line, tr.type.line ],
					[ polygon, tr.type.polygon ]
				]
			}),
			allowBlank: false,
			autoScroll: true,
			name: "type",
			fieldLabel: tr_attribute.type,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
			valueField: "value",
			displayField: "name",
			queryMode: "local",
			cmImmutable: true
		});
		
		this.types.setValue = Ext.Function.createSequence(this.types.setValue, function(v) {
			if (v) {
				if (typeof v == "string") {
					this.showStyleFieldsByType(v);
				} else {
					// on load record v is an array of models, so take the first (and only)
					if (v.length > 0) {
						this.showStyleFieldsByType(v[0].data.value);
					}
				}
			}
		}, this);
		
		this.styleFieldsMap = {
			externalGraphic: new CMDBuild.IconsCombo({
				store: CMDBuild.ServiceProxy.Icons.getIconStore(),
				name: "externalGraphic",
				hiddenName: "externalGraphic",
				fieldLabel: tr.externalGraphic,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
				allowedGeoTypes: {POINT: true},
				valueField: "path",
				displayField: "description",
				queryMode: "local"
			}),

			pointRadius: new Ext.form.field.Number({
				fieldLabel: tr.pointRadius,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				defaultValue: 6,
				minValue: 0,
				maxValue: 100,
				allowedGeoTypes: {
					POINT: true
				},
				name: 'pointRadius'
			}),

			fillColor: new CMDBuild.form.HexColorField( {
				name: "fillColor",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
				fieldLabel: tr.fillColor,
				allowedGeoTypes: {POINT: true, POLYGON: true}
			}),

			fillOpacity: new Ext.form.SliderField( {
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				minValue: 0,
				maxValue: 1,
				defaultValue: 1,
				decimalPrecision: 1,
				increment: 0.1,
				name: "fillOpacity",
				fieldLabel: tr.fillOpacity,
				allowedGeoTypes: {POINT: true, POLYGON: true},
				clickToChange: true,
				animate: false,
				tipText: function(thumb) {
					return String(thumb.value*100) + '%';
				}
			}),

			strokeColor : new CMDBuild.form.HexColorField( {
				disabled : true,
				name : "strokeColor",
				labelWidth : CMDBuild.LABEL_WIDTH,
				width : CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
				fieldLabel : tr.strokeColor,
				defaultValue : "000000",
				hidden : true,
				allowedGeoTypes : {
					POINT : true,
					POLYGON : true,
					LINESTRING : true
				}
			}),

			strokeOpacity:  new Ext.form.SliderField( {
				minValue: 0,
				maxValue: 1,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				defaultValue: 1,
				decimalPrecision: 1,
				increment: 0.1,
				name: "strokeOpacity",
				fieldLabel: tr.strokeOpacity,
				allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true},
				clickToChange: true,
				animate: false,
				tipText: function(thumb) {
					return String(thumb.value*100) + '%';
				}
			}),

			strokeWidth: new Ext.form.field.Number({
				fieldLabel: tr.strokeWidth,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_SMALL_FIELD_WIDTH,
				name: "strokeWidth",
				defaultValue: 1,
				minValue: 0,
				maxValue: 10,
				allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true}
			}),

			strokeDashstyle: new Ext.form.ComboBox( {
				store: new Ext.data.SimpleStore( {
					fields: ["value", "name"],
					data: [
						["dot", tr.strokeStyles.dot], 
						["dash", tr.strokeStyles.dash],
						["dashdot", tr.strokeStyles.dashdot],
						["longdash", tr.strokeStyles.longdash],
						["longdashdot", tr.strokeStyles.longdashdot],
						["solid", tr.strokeStyles.solid]]
				}),
				autoScroll: true,
				name: "strokeDashstyle",
				hiddenName: "strokeDashstyle",
				fieldLabel: tr.strokeDashstyle,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_MEDIUM_FIELD_WIDTH,
				allowedGeoTypes: {POINT: true, POLYGON: true, LINESTRING: true},
				valueField: "value",
				displayField: "name",
				queryMode: "local"
			})
		};

		var sfm = this.styleFieldsMap;
		this.styleProperties = new Ext.form.FieldSet({
			margin: "0 5 5 5",
			title: tr.style,
			flex: 1,
			autoScroll: true,
			items: [
				this.types,
				sfm.externalGraphic,
				sfm.fillOpacity,
				sfm.fillColor,
				sfm.pointRadius,
				sfm.strokeDashstyle,
				sfm.strokeOpacity,
				sfm.strokeColor,
				sfm.strokeWidth
			],
			setDefaults: function() {
				sfm.fillOpacity.setValue(1);
				sfm.fillColor.setValue("#000000");
				sfm.pointRadius.setValue(3);
				sfm.strokeDashstyle.setValue("solid");
				sfm.strokeOpacity.setValue(1);
				sfm.strokeColor.setValue("#000000");
				sfm.strokeWidth.setValue(1);
			}
		});
	};
})();