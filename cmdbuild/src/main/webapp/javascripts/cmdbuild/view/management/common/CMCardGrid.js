(function() {

	Ext.define("CMDBuild.view.management.common.CMCardGridDelegate", {
		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		onCMCardGridSelect: function(grid, record) {},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		onCMCardGridDeselect: function(grid, record) {},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridBeforeLoad: function(grid) {},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridLoad: function(grid) {},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridColumnsReconfigured: function(grid) {},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridIconRowClick: function(grid, action, model) {}

	});

	Ext.define("CMDBuild.view.management.common.CMCardGridPagingBar", {
		extend: "Ext.toolbar.Paging",

		// configuration
		grid: undefined,
		// configuration

		// override
		doRefresh: function(value) {
			if (this.grid) {
				var sm = this.grid.getSelectionModel();
				if (sm) {
					sm.deselectAll();
				}
			}
			return this.callOverridden(arguments);
		}
	});

	Ext.define("CMDBuild.view.management.common.CMCardGrid", {
		extend: "Ext.grid.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		CLASS_COLUMN_DATA_INDEX: 'IdClass_value',	// for the header configuration
													// the name is used for the server-side sorting

		// configuration
		columns: [],
		extraParams: undefined, // extra params for the store
		forceSelectionOfFirst: false, // listen load event and select the first row
		skipSelectFirst: false,
		cmStoreUrl: 'services/json/management/modcard/getcardlist',
		cmPaginate: true, // to say if build or not a paging bar, default true
		cmBasicFilter: true, // to add a basic search-field to the paging bar 
		cmAdvancedFilter: true, // to add a button to set an advanced filter
		cmAddGraphColumn: true, // to say if build or not a column to open the mystical graph window, default true
		cmAddPrintButton: true, // to add a button to set an chose the print format
		// configuration

		constructor: function(c) {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.common.CMCardGridDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.loadMask = false;
			this.store = this.getStoreForFields([]);

			if (this.cmPaginate) {
				buildPagingBar(this);
			}

			this.viewConfig = {
				stripeRows: true,
				// Business rule: voluntarily hide the horizontal scroll-bar
				// because probably no one want it
				autoScroll: false,
				overflowX: "hidden",
				overflowY: "auto"
			};

			this.layout = {
				type: "fit",
				reserveScrollbar: true
			};

			this.callParent(arguments);
			this.mon(this, 'beforeitemclick', cellclickHandler, this);

			// register to events for delegates
			this.mon(this, 'select', function(grid, record) {
				this.callDelegates("onCMCardGridSelect", [grid, record]);
			}, this);

			this.mon(this, 'deselect', function(grid, record) {
				this.callDelegates("onCMCardGridDeselect", [grid, record]);
			}, this);
		},

		shouldSelectFirst: function() {
			var out = this.forceSelectionOfFirst && !this.skipSelectFirst;
			this.skipSelectFirst = false;
			return out;
		},

		skipNextSelectFirst: function() {
			this.skipSelectFirst = true;
		},

		updateStoreForClassId: function(classId, o) {

			var me = this;

			function callCbOrLoadFirstPage(me) {
				if (o && o.cb) {
					o.cb.call(o.scope || me);
				} else {
					me.store.loadPage(1);
				}
			}

			if (me.currentClassId == classId) {
				callCbOrLoadFirstPage(me);
			} else {
				me.currentClassId = classId;

				if (this.gridSearchField) {
					this.gridSearchField.setValue(""); // clear only the field without reload the grid
				}

				if (this.filterMenuButton) {
					this.filterMenuButton.reconfigureForEntryType(_CMCache.getEntryTypeById(classId));
				}

				if (me.printGridMenu) {
					me.printGridMenu.setDisabled(!classId);
				}

				me.loadAttributes( //
					classId, //
					function(attributes) { //
						me.setColumnsForClass(attributes);
						me.setGridSorting(attributes);
						callCbOrLoadFirstPage(me);
					} //
				);

			}
		},

		// protected
		loadAttributes: function(classId, cb) {
			_CMCache.getAttributeList(classId, cb);
		},

		loadPage: function(pageNumber, o) {
			o = o || {};
			scope = o.scope || this;
			cb = o.cb || Ext.emptyFn;

			// store.loadPage does not allow the definition of a callBack
			this.mon(this, "load", cb, scope, {single: true});
			this.store.loadPage(Math.floor(pageNumber));
		},

		reload: function(reselect) {
			var cb = Ext.emptyFn;

			if (reselect) {
				var s = this.getSelectionModel().getSelection();
				cb = function() {
					if (s && s.length > 0) {
						var r = this.store.findRecord("Id", s[0].get("Id"));
						if (r) {
							this.getSelectionModel().select(r);
						}
					} else {
						this.getSelectionModel().select(0);
					}
				};
			}

			this.store.load({
				scope: this,
				callback: cb
			});
		},

		getVisibleColumns: function() {
			var columns = this.columns;
			var visibleColumns = [];

			for (var i = 0, len = columns.length ; i<len ; i++) {
				var col = columns[i];
				if (!col.hidden
						&& col.dataIndex // the expander column has no dataIndex
						&& col.dataIndex != "Id") { // The graph column has dataIndex Id

					var columnName = col.dataIndex;
					if (columnName) {
						var index = columnName.lastIndexOf("_value");
						if (index >= 0) {
							columnName = columnName.slice(0,index);
						}
						visibleColumns.push(columnName);
					}
				}
			};

			return visibleColumns;
		},
		
		// protected
		setColumnsForClass: function(classAttributes) {
			var columns = this.buildColumnsForAttributes(classAttributes);
			var s = this.getStoreForFields(columns.fields);

			this.suspendLayouts();
			this.reconfigure(s, columns.headers);
			this.resumeLayouts(true);

			if (this.pagingBar) {
				this.pagingBar.bindStore(s);
			}

			this.callDelegates("onCMCardGridColumnsReconfigured", this);
		},

		// protected
		buildColumnsForAttributes: function(classAttributes) {
			this.classAttributes = classAttributes;
			var headers = [];
			var fields = [];

			if (_CMUtils.isSuperclass(this.currentClassId)) {
				headers.push(this.buildClassColumn());
			}

			for (var i=0; i<classAttributes.length; i++) {
				var attribute = classAttributes[i];
				var header = CMDBuild.Management.FieldManager.getHeaderForAttr(attribute);

				if (header &&
						header.dataIndex != this.CLASS_COLUMN_DATA_INDEX) {

					this.addRendererToHeader(header);
					// There was a day in which I receved the order to skip the Notes attribute.
					// Today, the boss told  me to enable the notes. So, I leave the condition
					// commented to document the that a day the notes were hidden.

					// if (attribute.name != "Notes") {
						headers.push(header);
					// }

					fields.push(header.dataIndex);
				} else if (attribute.name == "Description") {
					// FIXME Always add Description, even if hidden, for the reference popup
					fields.push("Description");
				}
			}

			headers = headers.concat(this.buildExtraColumns());

			if (this.cmAddGraphColumn
					&& CMDBuild.Config.graph
					&& CMDBuild.Config.graph.enabled=="true") {

				buildGraphIconColumn.call(this, headers);
			}

			return {
				headers: headers,
				fields: fields
			};
		},

		// protected
		setGridSorting: function(attributes) {
			if (!this.store.sorters) {
				return;
			}

			this.store.sorters.clear();

			var sorters = [];
			for (var i=0, l=attributes.length; i<l; ++i) {
				var attribute = attributes[i];
				var sorter = {};
				/*
				 * 
				 * After some trouble I understood that
				 * classOrderSign is:
				 * 1 if the direction is ASC
				 * 0 if the attribute is not used for the sorting
				 * -1 if the direction is DESC
				 * 
				 * the absoluteClassOrder is the
				 * index of the sorting criteria
				 */
				var index = attribute.classOrderSign * attribute.absoluteClassOrder;
				if (index != 0) {
					sorter.property = attribute.name;
					if (index > 0) {
						sorter.direction = "ASC";
					} else {
						sorter.direction = "DESC";
						index = -index;
					}

					sorters[index] = sorter;
				}
			}

			for (var i = 0, l = sorters.length; i<l; ++i) {
				var sorter = sorters[i];
				if (sorter) {
					this.store.sorters.add(sorter);
				}
			}

		},

		// protected
		addRendererToHeader: function(h) {
			var me = this;
			h.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
				value = value || record.get(h.dataIndex);
				if (typeof value == "undefined" 
					|| value == null) {

					return "";
				}

				if (typeof value == "object") {
					/*
					 * Some values (like reference or lookup) are
					 * serialized as object {id: "", description:""}.
					 * Here we display the description
					 */
					value = value.description;
				} else if (typeof value == "boolean") {
					/*
					 * Localize the boolean values
					 */
					value = value ? Ext.MessageBox.buttonText.yes : Ext.MessageBox.buttonText.no;
				}

				return value;
			};
		},

		// protected
		getStoreForFields: function(fields) {
			var pageSize = _CMUtils.grid.getPageSize();
			var s = this.buildStore(fields, pageSize);

			this.mon(s, "beforeload", function() {
				this.callDelegates("onCMCardGridBeforeLoad", this);
				this.fireEvent("beforeload", arguments);  // TODO remove?
			}, this);

			this.mon(s, "load", function(store, records) {
				this.callDelegates("onCMCardGridLoad", this);
				this.fireEvent("load", arguments); // TODO remove?

				if (this.shouldSelectFirst() && !this.getSelectionModel().hasSelection()
						&& records && records.length > 0) {

					try {
						this.getSelectionModel().select(0);
					} catch (e) {
						this.fireEvent("cmWrongSelection");
						CMDBuild.log.info("Not selected the first record");
						_trace();
					}
				}

			}, this);

			return s;
		},

		//protected
		buildStore: function(fields, pageSize) {
			fields.push({name: "Id", type: "int"});
			fields.push({name: "IdClass", type: "int"});
			fields.push(this.CLASS_COLUMN_DATA_INDEX);

			return new Ext.data.Store({
				fields: fields,
				pageSize: pageSize,
				remoteSort: true,
				proxy: {
					type: "ajax",
					url: this.cmStoreUrl,
					reader: {
						root: "rows",
						type: "json",
						totalProperty: "results",
						idProperty: "Id"
					},
					extraParams: this.getStoreExtraParams()
				},
				autoLoad: false
			});
		},

		//protected
		getStoreExtraParams: function() {
			var p = {
				className: ""
			};

			if (this.currentClassId) {
				p.className = _CMCache.getEntryTypeNameById(this.currentClassId);
			}

			if (this.CQL) {
				p = Ext.apply(p, this.CQL); // RettoCompatibility
				p.filter = Ext.encode(this.CQL);
			}

			return p;
		},

		//protected
		buildExtraColumns: function() {
			return [];
		},

		// protected
		buildClassColumn: function() {
			return {
				header: CMDBuild.Translation.management.modcard.subclass,
				width: 100,
				sortable: false,
				dataIndex: this.CLASS_COLUMN_DATA_INDEX
			};
		},

		disableFilterMenuButton: function() {
			if (this.cmAdvancedFilter) {
				this.filterMenuButton.disable();
			}
		},

		enableFilterMenuButton: function() {
			if (this.cmAdvancedFilter) {
				this.filterMenuButton.enable();
			}
		},

		applyFilterToStore: function(filter) {
			try {
				var encoded = filter;
				if (typeof encoded != "string") {
					encoded = Ext.encode(filter);
				}

				this.getStore().proxy.extraParams.filter = encoded;
			} catch (e) {
				_debug("I'm not able to set the filter to the store", this, filter);
			}
		}
	});

	function buildPagingBar(me) {
		var items = [];

		if (me.cmBasicFilter) {
			me.gridSearchField = new CMDBuild.field.GridSearchField({grid: me});
			items.push(me.gridSearchField);
		}

		if (me.cmAdvancedFilter) {
			me.filterMenuButton = new CMDBuild.view.management.common.filter.CMFilterMenuButton();
			_CMUtils.forwardMethods(me, me.filterMenuButton, [
				"enableClearFilterButton",
				"disableClearFilterButton",
				"enableSaveFilterButton",
				"disableSaveFilterButton",
				"setFilterButtonLabel",
				"selectAppliedFilter"
			]);
			items.push(me.filterMenuButton);
		}

		if (me.cmAddPrintButton) {
			me.printGridMenu = new CMDBuild.PrintMenuButton({
				callback : function() { me.fireEvent("click"); },
				formatList: ["pdf", "csv"],
				disabled: true
			});
			items.push(me.printGridMenu);
		}

		me.pagingBar = new CMDBuild.view.management.common.CMCardGridPagingBar({
			grid: me,
			store: me.store,
			displayInfo: true,
			displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
			emptyMsg: CMDBuild.Translation.common.display_topic_none,
			items: items
		});

		me.bbar = me.pagingBar;
	}

	function buildGraphIconColumn(headers) {
		 var c = _CMCache.getClassById(this.currentClassId);

		 if (c && c.get("tableType") != "simpletable") {
			var graphHeader = {
					noWrap: true,
				header: '&nbsp', 
				width: 30,
				tdCls: "grid-button",
				fixed: true,
				sortable: false, 
				renderer: renderGraphIcon, 
				align: 'center', 
				dataIndex: 'Id',
				menuDisabled: true,
				hideable: false
			};
			headers.push(graphHeader);
		}
	};

	function renderGraphIcon() {
		return '<img style="cursor:pointer" title="'
			+ CMDBuild.Translation.management.graph.icon_tooltip
			+'" class="action-open-graph" src="images/icons/chart_organisation.png"/>';
	}

	function cellclickHandler(grid, model, htmlelement, rowIndex, event, opt) {
		var action = event.target.className;
		if (action == 'action-open-graph') {
			CMDBuild.Management.showGraphWindow(model.get("IdClass"), model.get("Id"));
		}

		this.callDelegates("onCMCardGridIconRowClick", [grid, action, model]);
	}
})();