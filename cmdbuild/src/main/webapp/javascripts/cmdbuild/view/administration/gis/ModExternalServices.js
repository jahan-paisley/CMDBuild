(function() {
	var tr = CMDBuild.Translation.administration.modcartography.external_services;

	var KEY_FIELD = "key";
	var URL_FIELD = "url";
	var WORKSPACE_FIELD = "workspace";
	var ADMIN_USER_FIELD = "admin_user";
	var ADMIN_PASSWORD_FIELD = { name: "admin_password", inputType: 'password' };

	var services = {
		google: {
			serviceName: "google",
	  		serviceFields: [KEY_FIELD],
	  		withZoom: true
		},
		yahoo: {
	  		serviceName: "yahoo",
	  		serviceFields: [KEY_FIELD],
	  		withZoom: true
		},
		osm: {
	  		serviceName: "osm",
	  		withZoom: true
		},
		geoserver: {
	  		serviceName: "geoserver",
	  		serviceFields: [URL_FIELD, WORKSPACE_FIELD, ADMIN_USER_FIELD, ADMIN_PASSWORD_FIELD]	   		  		
       }
	};

	Ext.define("CMDBuild.Administration.ModExternalServices", {
		extend: "Ext.form.Panel",
		cmName: "gis-external-services",

		constructor: function() {
			this.saveButton = new Ext.Button({
				text: CMDBuild.Translation.common.buttons.save,
				disabled: false,
				scope: this,
				handler: function() {
					var values = this.getValues();
					CMDBuild.ServiceProxy.configuration.save({
						params: values,
						success: function() {
							new CMDBuild.Msg.success();
							try {
								CMDBuild.Config.gis.geoserver = values.geoserver;
							} catch (e) {
								// The configuration object could be undefined 
							}
						}
					}, name="gis");
				}
			});

			this.layout = "border";

			this.frame = true;
			this.buttonAlign = "center";
			this.buttons = [ this.saveButton ];
			this.services = services;
			this.title = tr.title;

			this.cmWrapper = new Ext.panel.Panel({
				region: "center",
				autoScroll: true,
				frame: true,
				items: (function(){
					var items = [];
					for (var service in services) {
						items.push(buildServiceFieldset(services[service]));
					}
					return items;
				})()
			});

			this.items = [this.cmWrapper];
			this.callParent(arguments);
		},

		initComponent: function() {
			this.callParent(arguments);
			this.on("show", getConfigFromServer, this);
		},

		getValues: function() {
			var values = {};
			this.cmWrapper.items.each(function(item) {
				if (typeof item.serviceName == "string") {
					if (item.collapsed) {
						values[item.serviceName] = "off";
					} else {
						values[item.serviceName] = "on";

						item.cascade(function(subItem) {
							if (typeof subItem.getValue == "function") {
								values[subItem.name] = subItem.getValue();
							}
						});
					}
				}
			});

			return values;
		}
	});

	function createServiceField(serviceName, fieldSpec) {
		var fieldName = fieldSpec.name || fieldSpec;
		var fieldInputType = fieldSpec.inputType || 'text';

		return new Ext.form.field.Text({
			name: serviceName+"_"+fieldName,
			fieldLabel: tr[fieldName],
			labelWidth: CMDBuild.LABEL_WIDTH,
			width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			inputType: fieldInputType,
			allowBlank: false
		});
	};

	/*
	 * FIXME change translation name when possible
	 */
	function createSliderField(serviceName, fieldName, translationName) {
		return new Ext.form.SliderField({
		    minValue: 0,
		    maxValue: 18,
		    value: 0,
		    width: 300,
		    name: serviceName+"_"+fieldName,
		    fieldLabel: CMDBuild.Translation.administration.modClass.geo_attributes[translationName],
		    labelWidth: CMDBuild.LABEL_WIDTH,
		    width: CMDBuild.ADM_BIG_FIELD_WIDTH,
		    clickToChange: false,
		    animate: false
		});
	};

	function collapseFieldsets(formPanel, data) {
		function collapseFieldset(serviceName) {
			if (data[serviceName] == "off") {
				formPanel.items.each(function(item) {
					if (item.serviceName == serviceName) {
						item.collapse();
					}
				});
			}
		};
		
		for (var service in formPanel.services) {
			collapseFieldset(service);
		}
	};

	function fillForm(formPanel, data) {
		for (var name in data) {
			var field = formPanel.getForm().findField(name);
			if (field != null) {
				field.setValue(data[name]);
			}
		}
	};
	
	function getConfigFromServer(){
		if (this.loaded) {
			return;
		} else {
			var me = this;
			CMDBuild.ServiceProxy.configuration.read({
				success: function(response){
					var decodedResponse = Ext.JSON.decode(response.responseText);
					me.loaded = true;
					fillForm(me, decodedResponse.data);
					collapseFieldsets(me, decodedResponse.data);
				},
				scope: me
			}, name = "gis");
		}
	};
	
	/*
	 * pass to it an object like: { 
	 * 	    serviceName: String,
	 *      serviceFields: (String|Object)[]
	 *      withZoom: boolean
	 * }
	 */
	function buildServiceFieldset(o) {
		var serviceFields = o.serviceFields || [];
		var items = [];

		for (var i=0, l=serviceFields.length; i<l; ++i) {
			var field = createServiceField(o.serviceName, serviceFields[i]);
			items.push(field);
		}

		if (o.withZoom) {
			var range = new CMDBuild.RangeSlidersFieldSet( {
				minSliderField: createSliderField(o.serviceName, "minzoom", "min_zoom"),
				maxSliderField: createSliderField(o.serviceName, "maxzoom", "max_zoom")
			});
			items.push( range );
		}

		var setDisabled = function(disabled) {
			for (var i in items) {
				try {
					items[i].setDisabled(disabled);
				} catch (Error) {}
			}
		};

		var f = new Ext.form.FieldSet ({
			border : false,
			title : tr.description[o.serviceName],
			checkboxToggle : true,
			collapsed : false,
			autoWidth : true,
			serviceName : o.serviceName,
			items : items,
			checkboxName : o.serviceName
		});

		return f;
	};
})();