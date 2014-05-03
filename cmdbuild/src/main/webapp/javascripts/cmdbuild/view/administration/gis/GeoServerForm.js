(function() {
	var tr_attribute = CMDBuild.Translation.administration.modClass.attributeProperties;
	var tr_geoserver = CMDBuild.Translation.administration.modcartography.geoserver;
	var tr = CMDBuild.Translation.administration.modClass.geo_attributes;
	var TYPES = {
		geotiff: "GEOTIFF",
		worldimage: "WORLDIMAGE",
		shpe: "SHAPE"
	};

	Ext.define("CMDBuild.Administration.GeoServerForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		hideMode: "offsets",
		fileUpload: true,
		plugins: [new CMDBuild.FormPlugin(), new CMDBuild.CallbackPlugin()],

		initComponent: function() {
			this.cls = "x-panel-body-default-framed cmbordertop";
			this.bodyCls = 'cmgraypanel';
			this.buttonAlign = "center";
			this.tbar = buildTBarTools(this);
			this.items = items(this);
			this.buttons = buildButtons(this);

			this.callParent(arguments);
			this.setFieldsDisabled();

			this.disableModify();
		},

		onAddLayer: function() {
			this.lastSelection = undefined;
			this.setCardBinding([]);
			this.getForm().reset();
			this.enableModify(all = true);
		},

		onLayerSelect: function(layerModel) {
			this.lastSelection = layerModel;
			this.getForm().loadRecord(layerModel);
			this.setCardBinding(layerModel.getCardBinding());
			this.disableModify(enableTBar = true);
		}

	});

	Ext.define("CMDBuild.Administration.GeoServerForm.BindCardFieldset", {
		extend: "Ext.form.FieldSet",

		initComponent: function() {
			var me = this;
			this.border = false;
			this.cls = "cmbordertop";
			this.style = {
				"border-color": "#D0D0D0"
			};

			this.margin = "5px 0 0 0";

			this.layout = {
				type : 'vbox',
				align : 'stretch'
			};

			this.defaults = {
				padding : "5px 0 0 0"
			};

			this.baseItem = new CMDBuild.Administration.GeoServerForm.BindCardFieldsetItem({
				delegate: me,
				first: true
			});

			this.items = [this.baseItem];
			this.callParent(arguments);
		},

		getValue: function() {
			var out = [];
			this.items.each(function(item) {
				var value = item.getValue();
				if (value != null) {
					out.push(value);
				}
			});

			return out;
		},

		setValue: function(cardBinding) {
			this.removeItems();

			var values = [].concat(cardBinding);
			var v = values.pop();
			var first = true;

			while (v) {
				if (first) {
					this.baseItem.setValue(v);
					first = false;
				} else {
					var item = this.bindCardPanelPlusButtonClick();
					item.setValue(v);
				}

				v = values.pop();
			}
		},

		removeItems: function() {
			var me = this;
			this.items.each(function(item){
				if (!item.first) {
					me.remove(item);
				}
			});
		},

		bindCardPanelPlusButtonClick: function() {
			return this.add(new CMDBuild.Administration.GeoServerForm.BindCardFieldsetItem({
				delegate: this,
				isFirst: false
			}));
		},

		bindCardPanelRemoveButtonClick: function(item) {
			this.remove(item);
		}
	});

	Ext.define("CMDBuild.Administration.GeoServerForm.BindCardFieldsetItem", {
		extend: "Ext.container.Container",

		delegate: null, // pass it on creation
		isFirst: true,

		initComponent: function() {
			var me = this;

			this.classCombo =  new CMDBuild.field.ErasableCombo({
				fieldLabel: me.isFirst ? tr.card_binding : " ",
				labelSeparator: me.isFirst ? ":" : "",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField : 'name',
				displayField : 'description',
				editable: false,
				store : _CMCache.getClassesAndProcessesAndDahboardsStore(),
				queryMode: 'local',
				margin: "0 5px 0 0",
				listeners: {
					change: function(combo, newValue) {
						me.remove(me.cardCombo);
						delete me.cardCombo;
						if (newValue) {
							me.cardCombo = CMDBuild.Management.ReferenceField.build({
								referencedClassName: newValue,
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

							me.insert(1, me.cardCombo);
						}
					},
					enable: function() {
						me.items.each(function(item) {
							if (Ext.getClassName(item) == "Ext.button.Button") {
								item.enable();
								item.show();
							}
						});
					},
					disable: function() {
						me.items.each(function(item) {
							if (Ext.getClassName(item) == "Ext.button.Button") {
								item.disable();
								item.hide();
							}
						});
					}
				}
			});

			var plusButton = new Ext.button.Button({
				iconCls: "add",
				handler: function() {
					me.delegate.bindCardPanelPlusButtonClick();
				}
			});

			this.items = [this.classCombo, this.cardCombo, plusButton];

			if (!this.isFirst) {
				this.items.push(new Ext.button.Button({
					iconCls: "delete",
					padding: "4px 0 0 4px",
					handler: function() {
						me.delegate.bindCardPanelRemoveButtonClick(me);
					}
				}));
			}

			this.layout = "hbox";

			this.callParent(arguments);
		},

		getValue: function() {
			var value = null;
			if (this.classCombo && this.cardCombo) {
				value =  {
					className: this.classCombo.getValue(),
					idCard: this.cardCombo.getValue()
				};
			}

			return value;
		},

		setValue: function(v) {
			this.classCombo.setValue(v.className);
			this.cardCombo.setValue(parseInt(v.idCard));
		}
	});

	function items(me) {
		// TODO: a custom vtype to deny
		// white spaces. GEOServer does not like them
		var name = new Ext.form.TextField({
			fieldLabel : tr_attribute.name,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : "name",
			allowBlank : false,
			cmImmutable: true,
			disabled: true
		});

		name.on("change", function(fieldname, newValue, oldValue) {
			me.autoComplete(description, newValue, oldValue);
		}, me);

		me.getName = function() {
			return name.getValue();
		};

		var types = new Ext.form.ComboBox({
			store : new Ext.data.SimpleStore( {
				fields : [ "value", "name" ],
				data : [ [ TYPES.geotiff, "GeoTiff" ],
						[ TYPES.worldimage, "WorldImage" ],
						[ TYPES.shpe, "Shape" ] ]
			}),
			allowBlank : false,
			autoScroll : true,
			name : "type",
			fieldLabel : tr_attribute.type,
			labelWidth: CMDBuild.LABEL_WIDTH,
			valueField : "value",
			displayField : "name",
			hiddenName : "type",
			queryMode : "local",
			triggerAction: "all",
			cmImmutable: true,
			disabled: true
		});

		var minZoom = new Ext.form.SliderField({
			fieldLabel : tr.min_zoom,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			minValue : 0,
			maxValue : 25,
			name : "minZoom",
			disabled: true
		});

		var maxZoom = new Ext.form.SliderField({
			fieldLabel : tr.max_zoom,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			minValue : 0,
			maxValue : 25,
			value : 25,
			name : "maxZoom",
			disabled: true
		});

		var range = new CMDBuild.RangeSlidersFieldSet( {
			maxSliderField: maxZoom,
			minSliderField: minZoom,
			disabled: true
		});

		var description = new Ext.form.TextField({
			xtype : "textfield",
			fieldLabel : tr_attribute.description,
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			name : "description",
			allowBlank : false,
			disabled: true
		});

		var file = new Ext.form.field.File({
			fieldLabel: tr_geoserver.file,
			labelWidth: CMDBuild.LABEL_WIDTH,
			name: "file",
			form: this,
			disabled: true
		});

		var bindToCardFieldset = new CMDBuild.Administration.GeoServerForm.BindCardFieldset({
			padding: "5px 0 0 0"
		});

		me.getCardsBinding = function() {
			return bindToCardFieldset.getValue();
		};

		me.setCardBinding = function(cardBinding) {
			bindToCardFieldset.setValue(cardBinding);
		};

		return [name, description, file, types, range, bindToCardFieldset];
	};

	function buildButtons(me) {
		me.cmButtons = [
			me.saveButton = new CMDBuild.buttons.SaveButton(),
			me.abortButton = new CMDBuild.buttons.AbortButton()
		];

		return me.cmButtons;
	};

	function buildTBarTools(me) {
		me.cmTBar = [
			me.modifyButton = new Ext.Button({
				text: tr_geoserver.modify_layer,
				iconCls: "modify",
				handler: function() {
					me.enableModify();
				}
			}),
			me.deleteButton = new Ext.button.Button({
				text: tr_geoserver.delete_layer,
				iconCls: 'delete'
			})
		];

		return me.cmTBar;
	};
})();