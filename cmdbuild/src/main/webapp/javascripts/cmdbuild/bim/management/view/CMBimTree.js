Ext.define('CMDBuild.bim.management.view.CMBimTreeDelegate', {
	onNodeCheckChange: function(node, check) {},
	onOpenCardIconClick: function(classId, cardId) {},
	onNodeSelect: function(node) {}
});

Ext.define('CMDBuild.bim.management.view.CMBimTree', {
	extend: 'Ext.tree.Panel',

	rootVisible: false,
	useArrows: true,
	frame: true,

	// configuration
	delegate: undefined,
	// configuration

	initComponent: function() {

		Ext.apply(this, {
			hideHeaders: true,
			store: new Ext.data.TreeStore(),
			listeners: {
				checkchange: function(node, checked) {
					this.delegate.onNodeCheckChange(node, checked);
				},
				select: function(treePanel, node, index, eOpts) {
					this.delegate.onNodeSelect(node, this.fromViewer);
				},
				beforecellclick: function(treePanel, td, cellIndex, record, tr, rowIndex, e, eOpts) {
					if (cellIndex == 1) {
						var cmData = record.raw.cmdbuild_data;
						this.delegate.onOpenCardIconClick(cmData.classid, cmData.id);
					}
				}
			},
			columns: [{
				xtype: 'treecolumn', //this is so we know which column will show the tree
				header: '&nbsp',
				flex: 2,
				sortable: true,
				dataIndex: 'text'
			}, {
				header: '&nbsp',
				fixed: true,
				width: 30,
				sortable: false, 
				renderer: function(value, metadata, record) {
					if (record.raw.cmdbuild_data.id) {
						return '<img style="cursor:pointer" class="follow-card" src="images/icons/bullet_go.png"/>';
					} else {
						return "";
					}
				},
				align: 'center', 
				tdCls: 'grid-button', 
				dataIndex: 'Fake',
				menuDisabled: true,
				hideable: false
			}]
		});

		this.delegate = this.delegate
				|| new CMDBuild.bim.management.view.CMBimTreeDelegate();
		this.callParent();
	},

	selectNodeByOid: function(oid) {
		var node = this.findNodeByOid(oid);
		var me = this;
		if (node && ! me.inSelection) {
			me.inSelection = true;
			this.expandPreviousNodes(node.parentNode,
				Ext.Function.createDelayed(function() {
					var sm = me.getSelectionModel();
					me.fromViewer = true;
					sm.select([node]);
					me.fromViewer = false;
					me.inSelection = false;
				}, 500)
			);
		}
	},

	expandPreviousNodes: function(node, cb) {
		if (node) {
			var me = this;
			var parent = node.parentNode;

			if (parent) {
				node.expand(false, function() {
					me.expandPreviousNodes(parent, cb);
				});
			} else {
				node.expand(false, cb);
			}
		}
	},

	findNodeByOid: function(oid) {
		var rootNode = this.getRootNode();
		return rootNode.findChildBy(function(aNode) {
			return aNode.raw.oid == oid;
		}, null, true);
	},

	setNodeCheckbox: function(oid, check) {
		var nodeToCheck = this.findNodeByOid(oid);
		if (nodeToCheck) {
			nodeToCheck.set("checked", check);
		}
	},

	checkNode: function(oid) {
		this.setNodeCheckbox(oid, true);
	},

	uncheckNode: function(oid) {
		this.setNodeCheckbox(oid, false);
	}
});