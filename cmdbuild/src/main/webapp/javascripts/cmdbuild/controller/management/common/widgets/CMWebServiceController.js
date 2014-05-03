(function() {
	var DOM_NODE = "_domNode";
	var NODE_TYPE = "_nodeType";

	Ext.define("CMDBuild.controller.management.common.widgets.CMWebServiceController", {
		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController",
			webServiceWidgetDelegate: "CMDBuild.view.management.common.widgets.CMWebServiceDelegate"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMWebService.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.wsCallParameters = widgetDef.callParameters;
			this.loaded = false;
			this.store = null;
			this.templateResolver = null;

			this.view.addDelegate(this);
		},

		// override
		beforeActiveView: function() {
			var me = this;

			if (this.loaded) {
				return;
			}

			// build the store only the first
			// time, then reuse it loading only the data
			if (me.store == null) {
				me.store = store(me);
				me.view.configureGrid(me.store, columns(me), selectionModel(me));
			}

			// same for the template resolver
			if (me.templateResolver == null) {
				me.templateResolver = new CMDBuild.Management.TemplateResolver({
					clientForm: me.clientForm,
					xaVars: me.wsCallParameters,
					serverVars: this.getTemplateResolverServerVars()
				});
			}

			resolveTemplate(me);
		},

		// override
		getData: function() {
			var serializedNodes = [];

			if (!this.widgetConf.readOnly) {
				var selectedRecords = this.view.getSelectedRecords();
				for (var i=0, l=selectedRecords.length; i<l; ++i) {
					var xmlNode = selectedRecords[i].get(DOM_NODE);
					serializedNodes.push(CMDBuild.core.xml.XMLUtility.serializeToString(xmlNode));
				}
			}

			return {
				output: serializedNodes
			};
		},

		// override
		isValid: function() {
			if (this.widgetConf.mandatory) {
				var data = this.getData();
				return data.output.length > 0;
			} else {
				return true;
			}
		},

		// override
		destroy: function() {
			this.callParent(arguments);
		},

		// as WebServiceWidgetDelegate
		/**
		 * 
		 * @param {CMDBuild.view.management.common.widgets.CMWebService} widget
		 * the widget that calls the method 
		 * @param {Ext.data.Model} model
		 * the model of the grid row for which the button was clicked
		 */
		onWebServiceWidgetShowAllInfoButtonClick: function(widget, model) {
			new CMDBuild.view.management.common.widgets.CMXMLWindow({
				xmlNode: model.get(DOM_NODE)
			}).show();
		}
	});

	function store(me) {
		var nodesToUseAsColumns = getNodesToUseAsColumns(me);
		nodesToUseAsColumns.push({
			name: DOM_NODE,
			type: "auto" // An XML DOM Node
		}, {
			name: NODE_TYPE,
			type: "string"
		});

		Ext.define("CMWebServiceModel", {
			extend: "Ext.data.Model",
			fields: nodesToUseAsColumns,
			idProperty: "_CM_ID"
		});

		return new Ext.data.Store({
			model: "CMWebServiceModel",
			data: [],
			autoLoad: false
		});
	}

	function columns(me) {
		var nodesToUseAsRows = getNodesToUseAsRows(me);
		var nodesToUseAsColumns = getNodesToUseAsColumns(me);
		var columns = [];

		/*
		 * Add a column with the
		 * element name if there are
		 * more than one nodes to use
		 * as row
		 */
		if (nodesToUseAsRows.length > 1) {
			columns.push({
				header: CMDBuild.Translation.administration.modClass.attributeProperties.type,
				dataIndex: NODE_TYPE,
				flex: 1
			});
		}

		for (var i=0, l=nodesToUseAsColumns.length; i<l; ++i) {
			var nodeName = nodesToUseAsColumns[i];
			columns.push({
				header: nodeName,
				dataIndex: nodeName,
				flex: 1
			});
		}

		return columns;
	}

	function selectionModel(me) {
		var selectionModelConfiguration = {};
		// if readOnly a selection model is
		// not needed
		if (!me.widgetConf.readOnly) {
			selectionModelConfiguration = {
				selType: "checkboxmodel",
				allowDeselect: true,
				mode: me.widgetConf.singleSelect ? "SINGLE" : "MULTI"
			};
		}

		return selectionModelConfiguration;
	}

	function getNodesToUseAsColumns(me) {
		return [].concat(me.widgetConf.nodesToUseAsColumns);
	}

	function getNodesToUseAsRows(me) {
		return [].concat(me.widgetConf.nodesToUseAsRows);
	}

	function resolveTemplate(me) {
		me.templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(me.wsCallParameters),
			callback: function(o) {
				var vars = me.getTemplateResolverServerVars();
				var entryTypeName = _CMCache.getEntryTypeNameById(vars.IdClass);

				var callParameters = {};
				for (var key in me.wsCallParameters) {
					callParameters[key] = o[key];
				}

				var el = me.view.getEl();
				if (el) {
					el.mask(CMDBuild.Translation.common.wait_title);
				}

				CMDBuild.Ajax.request({
					url: "services/json/modwidget/callwidget",
					method: "GET",
					params: {
						className: entryTypeName,
						id: vars.Id,
						activityId: _CMWFState.getActivityInstance().getId(),
						widgetId: me.getWidgetId(),
						params: Ext.encode(callParameters)
					},
					success: function(request, action, response) {
						me.loaded = true;
						var xmlString = response.response || "";

						var xmlUtility = CMDBuild.core.xml.XMLUtility;
						var xml = xmlUtility.xmlDOMFromString(xmlString);
						var data = xmlUtility.fromDOMToArrayOfObjects(xml, //
								getNodesToUseAsRows(me), //
								DOM_NODE, //
								NODE_TYPE);

						me.store.loadRawData(data);

						// when a field of the activity that is used
						// from the template resolver changes, say
						// to the widget that must be reloaded
						me.templateResolver.bindLocalDepsChange(function() {
							me.loaded = false;
						});
					},
					callback: function() {
						if (el) {
							el.unmask();
						}
					}
				});
			}
		});
	}
})();