(function() {

	Ext.define('CMDBuild.model.CMCardBrowserNodeModel', {
		extend : 'Ext.data.Model',
		idProperty: "cardId",
		fields : [ {
			name : 'text',
			type : 'string'
		}, {
			name : 'visible',
			type : 'boolean'
		}, {
			name : 'cardId',
			type : 'string' // cardId or the name of a geoserver layer
		}, {
			name : 'className',
			type : 'string'
		}, {
			name : 'classId',
			type : 'int'
		}, {
			// to identify the exclusive nodes that
			// represent the base for the vertical overlap
			name: 'baseNode',
			type: 'boolean'
		}],

		getCardId: function() {
			return this.get("cardId");
		},

		getCMDBuildClassId: function() {
			return this.get("classId");
		},

		getCMDBuildClassName: function() {
			return this.get("className");
		},

		getExpansibleDomains: function() {
			return this.get("expansibleDomains") || [];
		},

		didChildrenLoaded: function() {
			return this.get("childrenLoaded");
		},

		isBindingCard: function(card) {
			var out = false;
			if (card 
					&& typeof card == "object") {

				out = this.getCardId() == card.get("Id")
					&& this.getCMDBuildClassId() == card.get("IdClass");
			}

			return out;
		},

		setChecked: function(checked) {
			this.set("checked", checked);
		},

		isChecked: function() {
			return this.get("checked");
		},

		isBaseNode: function() {
			return this.get("baseNode");
		},

		checkAncestors: function(cb) {
			var node = this;
			while (node) {
				if (node.isBaseNode()) {
					node.setExclusiveCheck(cb);
				} else {
					node.setChecked(true);
				}
				cb(node);
				node = node.parentNode;
			}
		},

		setExclusiveCheck: function(cb) {
			if (this.parentNode) {
				var siblings = this.parentNode.childNodes || this.parentNode.children || [];
				for (var i=0, l=siblings.length; i<l; ++i) {
					var node = siblings[i];
					node.setChecked(false);
					cb(node);
				}
			}

			this.setChecked(true);
		}
	});

	Ext.define("CMDBuild.view.management.CMCardBrowserTreeDelegate", {
		/**
		 * 
		 * @param {CMDBuild.view.management.CMCardBrowserTree} tree The tree who call the method
		 * @param {Ext.data.NodeInterface} node The node which has changed his check
		 * @param {Boolean} checked the state of the check
		 * @param {Boolean} deeply if propagate the check for all the branch
		 */
		onCardBrowserTreeCheckChange: Ext.emptyFn,

		/**
		 * 
		 * @param {CMDBuild.view.management.CMCardBrowserTree} tree The tree who call the method 
		 * @param {Ext.data.NodeInterface} node The node which was expanded
		 */
		onCardBrowserTreeItemExpand: Ext.emptyFn,

		/**
		 * Called when click over the specific icon
		 * @param {object} cardBaseInfo, an object with Id and IdClass for the selected card
		 */
		onCardBrowserTreeCardSelected: Ext.emptyFn,

		/**
		 * @param {CMDBuild.view.management.CMCardBrowserTree} tree The activated panel
		 * @param {integer} activationCount The number of times the panel was activated
		 */
		onCardBrowserTreeActivate: Ext.empfyFn,

		/**
		 * @param {CMDBuild.view.management.CMCardBrowserTree} tree The activated panel
		 * @param {Ext.data.NodeInterface} node The node that was added 
		 */
		onCardBrowserTreeNodeAppend: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.CMCardBrowserTree", {
		extend: "Ext.tree.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function(ds) {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.CMCardBrowserTreeDelegate");

			this.dataSource = ds || null;
			this.callParent(arguments);
		},

		initComponent: function() {
			this.useArrows = true;
			this.rootVisible = true;
			this.multiSelect = false;
			this.folderSort = false;
			this.frame = false;
			this.border = false;
			this.bodyBorder = false;
			this.hideHeaders = true;

			this.activationCount = 0;
			var me = this;
			var SHOW_ICON = 'images/icons/bullet_go.png';
			var HIDE_ICON = 'images/icons/cancel.png';

			this.tbar = ['->', {
				iconCls: "arrow_refresh",
				handler: function() {
					me.dataSource.refresh();
				}
			}];

			this.columns = [{
				xtype : 'treecolumn',
				flex : 2,
				sortable : false,
				dataIndex : 'text',
				menuDisabled : true
			}, {
				xtype : 'actioncolumn',
				width : 40,
				menuDisabled : true,
				dataIndex : 'visible',
				tooltip : "@@ show/hide this feature only",
				align : 'center',
				sortable : false,
				icon : HIDE_ICON,

				hidden: true,

				handler : function(grid, rowIndex, colIndex,
						actionItem, event, record, row) {

					var value = record.get("visible");
					this.icon = value ? SHOW_ICON : HIDE_ICON;
					record.set("visible", !value);
					record.commit();

					var deeply = false;
					_debug("SINGLE VISIBILITY", value);
					me.callDelegates("onCardBrowserTreeCheckChange", [me, record, value, deeply]);
				}
			}, {
				width : 40,
				menuDisabled : true,
				xtype : 'actioncolumn',
				tooltip : CMDBuild.Translation.management.modcard.open_relation,
				align : 'center',
				sortable : false,
				icon : 'images/icons/bullet_go.png',
				handler : function(grid, rowIndex, colIndex, actionItem,
						event, record, row) {

					// to highlight the node
					me.selectNodeSilently(record);

					me.callDelegates( "onCardBrowserTreeCardSelected", {
						Id : record .get("cardId"),
						IdClass : record .get("classId")
					});
				},
				isDisabled : function(view, rowIdx, colIdx, item, record) {
					return false;
				}
			}];

			this.store = Ext.create('Ext.data.TreeStore', {
				model : "CMDBuild.model.CMCardBrowserNodeModel",
				root : {
					expanded : true,
					checked: true,
					text: me.rootText,
					children : []
				}
			});

			this.callParent(arguments);

			this.mon(this, "afteritemexpand", function(node) {
				this.callDelegates("onCardBrowserTreeItemExpand", [this, node]);
			}, this);

			// Force to not select via UI
			this.mon(this, "beforeselect", function() {
				return false;
			}, this);

			this.mon(this, "checkchange", function(node, checked) {
				Ext.suspendLayouts();
				var deeply = true;
				if (!node.isRoot()) {
					this.callDelegates("onCardBrowserTreeCheckChange", [this, node, checked, deeply]);
				} else {
					var children = node.childNodes || node.childrent || [];
					for (var i=0, l=children.length; i<l; ++i) {
						var c = children[i];
						c.set("checked", checked);
						// the set method does not trigger the checkchange event, so call the delegate by hand
						this.callDelegates("onCardBrowserTreeCheckChange", [this, c, checked, deeply]);
					}
				}
				Ext.resumeLayouts(true);
			}, this);

			this.mon(this, "activate", function(treePanel) {
				this.callDelegates("onCardBrowserTreeActivate", [this, ++this.activationCount]);
			}, this);

			this.mon(this, "itemappend", function(tree, node) {
				// sync the check state firing the checkchange event
				var deeply = false;
				this.callDelegates("onCardBrowserTreeCheckChange", [this, node, node.get("checked"), deeply]);

				// notify the append
				this.callDelegates("onCardBrowserTreeNodeAppend", [this, node]);
			}, this);
		},

		setDataSource: function(ds) {
			this.dataSource = ds;
		},

		selectCardSilently: function(card) {
			deselectAllSilently(this);

			if (!card) {
				return;
			}

			var r = this.getRootNode();
			if (r) {
				var node = r.findChildBy( function(child) {
						return child.isBindingCard(card);
					}, null, true);

				this.selectNodeSilently(node);
			}
		},

		selectNodeSilently: function(node) {
			if (!node) {
				return;
			}

			try {
				var sm = this.getSelectionModel();
				if (sm) {
					sm.suspendEvents();
					sm.select(node);
					sm.resumeEvents();
				}
			} catch (e) {
				_debug("ERROR selecting the CardBrowserTree", e);
			}
		},

		checkCardNodeAncestors: function(card) {
			if (!card) {
				return;
			}

			var r = this.getRootNode();
			var me = this;
			if (r) {
				var node = r.findChildBy(function(child) {
						return child.isBindingCard(card);
					}, null, true);

				if (node) {
					// pass a function called for each node checked
					// that call the delegate method to notify the checked
					// Is necessary because the set of the node does not
					// fire the checkchange event
					node.checkAncestors(function(n) {
						var deeply = n.isBaseNode();
						me.callDelegates("onCardBrowserTreeCheckChange", [me, n, n.isChecked(), deeply]);
					});
				}
			}
		},

		selectCardNodePath: function(card) {
			if (!card) {
				return;
			}

			var r = this.getRootNode();
			var me = this;
			if (r) {
				var node = r.findChildBy(function(child) {
						return child.isBindingCard(card);
					}, null, true);

				var cb = Ext.Function.createDelayed(function() {
					me.selectNodeSilently(node);
				}, 500);

				if (node) {
					me.selectPath(node.getPath(), null, null, cb);
				}
			}
		},

		udpateCheckForLayer: function(layer) {
			var store = this.store,
				scope = null,
				deep = true,
				node = null;

			var root = store.getRootNode();
			if (root) {
				 node = root.findChildBy(function(node) {
					return node.data.cardId == layer.geoAttribute.name;
				}, scope, deep);

				if (node) {
					node.set("checked", layer.getVisibility());
				}
			}
		}
	});

	function deselectAllSilently(me) {
		try {
			var sm = me.getSelectionModel();
			if (sm) {
				var suppressEvent = true;
				sm.deselectAll(suppressEvent);
			}
		} catch (e) {
			_debug("ERROR deselecting the CardBrowserTree", e);
		}
	}
})();