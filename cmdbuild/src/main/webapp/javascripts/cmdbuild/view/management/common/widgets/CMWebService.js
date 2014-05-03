(function() {
	var MAGNIFY_CLASS = "action-all-info";

	var CALLBACK_MAPPING = {};
	CALLBACK_MAPPING[MAGNIFY_CLASS] = "onWebServiceWidgetShowAllInfoButtonClick";

	Ext.define("CMDBuild.view.management.common.widgets.CMWebServiceDelegate", {
		/**
		 * 
		 * @param {CMDBuild.view.management.common.widgets.CMWebService} widget
		 * the widget that calls the method 
		 * @param {Ext.data.Model} model
		 * the model of the grid row for which the button was clicked
		 */
		onWebServiceWidgetShowAllInfoButtonClick: function(widget, model) {}
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMWebServiceGrid", {
		extend: "Ext.grid.Panel",

		initComponent: function() {
			this.bbar = [new CMDBuild.field.LocalGridSearchField({
				grid: this
			})];

			this.callParent(arguments);
		}
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMWebService", {
		extend: "Ext.panel.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.common.widgets.CMWebServiceDelegate");
			this.callParent(arguments);
		},

		initComponent: function() {
			this.frame = false;
			this.border = false;
			this.layout = "border";
			this.autoScroll = true;
			this.callParent(arguments);
		},

		statics : {
			WIDGET_NAME: ".WebService"
		},

		getSelectedRecords: function() {
			var selection = [];
			// if the widget is never opened
			// the grid is not configured
			if (this.grid) {
				selection = this.grid.getSelectionModel().getSelection();
			}

			return selection;
		},

		configureGrid: function(store, columns, selectionModel) {
			this.grid = new CMDBuild.view.management.common.widgets.CMWebServiceGrid({
				region: "center",
				border: false,
				columns: addMagnifyButtonColumn(columns),
				selModel: selectionModel,
				store: store
			});

			
			this.add(this.grid);

			this.mon(this.grid, 'beforeitemclick', function(grid, model, htmlelement, rowIndex, event, opt) {
				var className = event.target.className;
				if (typeof CALLBACK_MAPPING[className] == "string") {
					this.callDelegates(CALLBACK_MAPPING[className], [this, model]);
				}
			}, this);
		}
	});

	function addMagnifyButtonColumn(columns) {
		columns.push({
			width: 30,
			sortable: false,
			align: "center",
			hideable: false,
			renderer: function (value, metadata, record) {
				return '<img style="cursor:pointer"'
				+'" class="' + MAGNIFY_CLASS + '" src="images/icons/zoom.png"/>';
			}
		});

		return columns;
	}

	Ext.define("CMDBuild.view.management.common.widgets.CMXMLWindow", {
		extend: "CMDBuild.PopupWindow",

		// configuration
		xmlNode: null,
		// configuration

		initComponent: function() {
			var me = this;

			if (this.xmlNode != null) {
				this.title = this.xmlNode.nodeName;
				this.items = [buildForm(me)];
			}

			this.buttonAlign = "center",
			this.buttons = [{
				text: CMDBuild.Translation.common.buttons.close,
				handler: function() {
					me.destroy();
				}
			}];

			this.bodyStyle = {
				padding: "5px"
			};

			this.callParent(arguments);
		}
	});

	function buildForm(me) {
		var xmlUtility = CMDBuild.core.xml.XMLUtility;
		var children = me.xmlNode.childNodes;
		var fields = [];

		for (var i=0, l=children.length; i<l; ++i) {
			var child = children[i];
			var text = xmlUtility.getNodeText(child);
			var label = child.nodeName;

			fields.push({
				xtype: 'displayfield',
				fieldLabel: label,
				labelAlign: "right",
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.BIG_FIELD_WIDTH,
		        value: text
			});
		}

		return {
			boder: true,
			frame: true,
			items: fields
		};
	}
	
	
	
})();