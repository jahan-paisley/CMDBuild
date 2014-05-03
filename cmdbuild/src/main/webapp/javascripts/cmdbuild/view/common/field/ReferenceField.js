(function() {

    var FILTER_FIELD = "_SystemFieldFilter",
    	CHANGE_EVENT = "change";

    Ext.define("CMDBuild.Management.ReferenceField", {
        statics: {
            build: function(attribute, subFields, extraFieldConf) {
                var templateResolver;
                var extraFieldConf = extraFieldConf || {};

                if (attribute.filter) { // is using a template
                    var xaVars = CMDBuild.Utils.Metadata.extractMetaByNS(attribute.meta, "system.template.");
                    xaVars[FILTER_FIELD] = attribute.filter;
                    templateResolver = new CMDBuild.Management.TemplateResolver({
                        getBasicForm: function() {
                            return getFormPanel(field).getForm();
                        },
                        xaVars: xaVars
                    });
                }

                var field = Ext.create("CMDBuild.Management.ReferenceField.Field", Ext.apply(extraFieldConf,{
                    attribute: attribute,
                    templateResolver: templateResolver
                }));

                if (subFields && subFields.length > 0) {
                    return buildReferencePanel(field, subFields);
                } else {
                    return field;
                }
            }
        }
    });

    function getFormPanel(field) {
        return field.findParentByType("form");
    }

    function buildReferencePanel(field, subFields) {
        // If the field has no value the relation attributes must be disabled
        field.mon(field, CHANGE_EVENT, function(combo, val) {
            var disabled = val == "";
            for (var i=0, sf=null; i<subFields.length; ++i) {
                sf = subFields[i];
                if (sf) {
                    sf.setDisabled(disabled);
                }
            }
        });

        var fieldContainer = {
            xtype : 'container',
            layout : 'hbox',
			margin : "0 0 5 0",
            items : [
                 new CMDBuild.field.CMToggleButtonToShowReferenceAttributes({
                     subFields: subFields
                 }),
                 field
            ]
        };

        field.labelWidth -= 20;

        return new Ext.container.Container({
			margin : "0 0 5 0",
            items: [fieldContainer].concat(subFields),
            resolveTemplate: function() {
                field.resolveTemplate();
            }
        });
    }

    Ext.define("CMDBuild.Management.ReferenceField.Field", {
        extend: "CMDBuild.Management.SearchableCombo",
        attribute: undefined,

        initComponent: function() {
            var attribute = this.attribute;
            var store = CMDBuild.Cache.getReferenceStore(attribute);

            store.on("loadexception", function() {
                field.setValue('');
            });

            Ext.apply(this, {
                plugins: new CMDBuild.SetValueOnLoadPlugin(),
                fieldLabel: attribute.description || attribute.name,
                labelWidth: CMDBuild.LABEL_WIDTH,
                name: attribute.name,
                store: store,
                queryMode: "local",
                valueField: "Id",
                displayField: 'Description',
                allowBlank: !attribute.isnotnull,
                grow: true, // XComboBox autogrow
                minChars: 1,
                filtered: false,
                CMAttribute: attribute,
                listConfig: {
                	loadMask: false
                }
            });

            this.callParent(arguments);
		},

		getErrors : function(rawValue) {
			if (this.templateResolver && this.store) {
				var value = this.getValue();
				if (value && this.store.find(this.valueField, value) == -1) {
					return [ CMDBuild.Translation.errors.reference_invalid ];
				}
			}

			return this.callParent(arguments);
		},

		setValue : function(v) {
			if (!this.store) {
				return;
			}

			v = this.extractIdIfValueIsObject(v);

			if (this.ensureToHaveTheValueInStore(v) !== false
					|| this.store.isOneTime) {// is one time seems that has a CQL filter

				this.callParent([v]);
			}
		},

		/*
		 * Adds the record when the store is not completely loaded (too many
		 * records)
		 */
		ensureToHaveTheValueInStore: function(value) {

			value = normalizeValue(this, value);

			if (!value || this.store.isLoading()) {
				return true;
			}

			var valueNotInStore = this.store.find(this.valueField, value) == -1;
			if (valueNotInStore) {
				// ask to the server the record to add, return false to
				// not set the value, and set it on success

				var params = Ext.apply({cardId: value}, this.store.baseParams);

				CMDBuild.Ajax.request({
					url: "services/json/management/modcard/getcard",
					params: params,
					method: "GET",
					scope: this,
					success: function(response, options, decoded) {
						var data = adaptResult(decoded);
						if (data) {
							this.addToStoreIfNotInIt(data);
							this.setValue(value);
						} else {
							_debug("The remote reference is not found", params);
						}
					}
				});

				return false;
			}

			return true;
		},

		resolveTemplate : function() {
			var me = this;
			if (me.templateResolver && !me.disabled) {
				if (me.templateResolverBusy) {
					// Don't overlap requests
					me.requireResolveTemplates = true;
					return;
				}
				me.templateResolverBusy = true;
				me.templateResolver.resolveTemplates( {
					attributes : [ FILTER_FIELD ],
					callback : function(out, ctx) {
						me.onTemplateResolved(
							out,
							function afterStoreIsLoaded() {
								me.templateResolverBusy = false;
								if (me.requireResolveTemplates) {
									me.requireResolveTemplates = false;
									me.resolveTemplate();
								}
							}
						);
					}
				});
			}
		},

		onTemplateResolved: function(out, afterStoreIsLoaded) {
			this.filtered = true;
			var store = this.store;
			var callParams = this.templateResolver.buildCQLQueryParameters(out[FILTER_FIELD]);

			if (callParams) {
				// For the popup window! baseParams is not meant to be the old ExtJS 3.x property!
				// Ext.apply(store.baseParams, callParams);
				store.baseParams.filter = Ext.encode({
					CQL: callParams.CQL
				});

				var me = this;
				store.load({
					// params: callParams,
					callback: function() {
						// Fail the validation if the current selection is not in the new filter
						me.validate();
						afterStoreIsLoaded();
					}
				});
			} else {
				var emptyDataSet = {};
				emptyDataSet[store.root] = [];
				emptyDataSet[store.totalProperty] = 0;
				store.loadData(emptyDataSet);
				afterStoreIsLoaded();
			}

			this.addListenerToDeps();
        },

        addListenerToDeps: function() {
			if (this.depsAdded) {
				return;
			}
			this.depsAdded = true;
            // Adding the same listener twice does not double the fired events, that's why it works
            var deps = this.templateResolver.getLocalDepsAsField();
            for (var name in deps) {
                var field = deps[name];
                if (field) {
                    field.mon(field, CHANGE_EVENT, function() {
                    	this.reset();
						this.resolveTemplate();
					}, this);
                }
            }
        },

		isFiltered: function() {
			return (typeof this.templateResolver != "undefined");
		},

		setServerVarsForTemplate: function(vars) {
			if (this.templateResolver) {
				this.templateResolver.serverVars = vars;
			}
		}
    });

    // see SearchableCombo.addToStoreIfNotInIt
    function adaptResult(result) {
    	var data = result.card;
    	if (data) {
    		return {
	    		get: function(key) {
	    			return data[key];
	    		}
	    	};
    	} else {
    		return null;
    	}
    }

	// if set the velue programmatically it could be a integer or a
	// string or null or undefined
	// if the set is raised after a selection on the UI, the value is an array
	// of models
	function normalizeValue(me, v) {
		v = CMDBuild.Utils.getFirstSelection(v);
		if (v && typeof v == "object" && typeof v.get == "function") {
			v = v.get(me.valueField);
		}

		return v;
	}

})();