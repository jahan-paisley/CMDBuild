(function() {

	var CM_INDEX = 'cmIndex',
		TREE_NODE_NAME_ATTRIBUTE = 'cmName',
		TREE_FOLDER_NODE_NAME = 'folder';

	Ext.define('CMDBuild.view.common.CMAccordionStoreModel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: TREE_NODE_NAME_ATTRIBUTE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.TEXT, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.PARENT, type: 'string' },
			{ name: CM_INDEX, type: 'ingeter' },
			{ name: CMDBuild.ServiceProxy.parameter.NAME, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.TYPE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.FILTER, type: 'auto' },
			{ name: 'sourceFunction', type: 'auto' },
			{ name: 'viewType', type: 'string' }
		]
	});

	Ext.define('CMDBuild.view.common.CMBaseAccordionStore', {
		extend: 'Ext.data.TreeStore',

		model: 'CMDBuild.view.common.CMAccordionStoreModel',
		root: {
			expanded : true,
			children : []
		},
		sorters: [
			{
				property : CM_INDEX,
				direction: 'ASC'
			},
			{
				property : CMDBuild.ServiceProxy.parameter.TEXT,
				direction: 'ASC'
			}
		]
	});

	/*
	 * this class can not be instantiated,
	 * it is a template for the accordions
	 *
	 * It is a panel with as item a TreePanel,
	 * it may be directly a TreePanel but there are
	 * problemps with the accordion layout
	 * */
	Ext.define('CMDBuild.view.common.CMBaseAccordion', {
		extend: 'Ext.tree.Panel',

		rootVisible: false,

		initComponent: function() {
			this.store = new CMDBuild.view.common.CMBaseAccordionStore();
			this.layout = 'border';
			this.border = true;
			this.autoRender = true;
			this.animCollapse = false;
			this.floatable = false;
			this.bodyStyle = {
				background: '#FFFFFF'
			};

			this.callParent(arguments);
		},

		updateStore: function(items) {
			var root = this.store.getRootNode(),
				treeStructure = this.buildTreeStructure(items);

			if (Ext.isArray(treeStructure) && treeStructure.length == 0) {
				treeStructure = [{}];
			}

			root.removeAll();
			root.appendChild(treeStructure);
			this.store.sort();
			this.afterUpdateStore();
		},

		selectNodeById: function(node) {
			var sm = this.getSelectionModel();

			if (typeof node != 'object') {
				node = this.getNodeById(node);
			}

			if (node) {
				// the expand fail if the accordion is not really
				// visible to the user. But I can not know when
				// a parent of the accordion will be visible, so
				// skip only the expand to avoid the fail
				if (this.isVisible(deep = true)) {
					node.bubble(function() {
						this.expand();
					});
				}

				sm.select(node);
			} else {
				_debug('I have not found a node with id ' + node);
			}
		},

		selectNodeByIdSilentry: function(nodeId) {
			this.getSelectionModel().suspendEvents();
			this.selectNodeById(nodeId);
			this.getSelectionModel().resumeEvents();
		},

		expandSilently: function() {
			this.cmSilent = true;
			Ext.panel.Panel.prototype.expand.call(this);
		},

		expand: function() {
			this.cmSilent = false;
			this.callParent(arguments);
		},

		removeNodeById: function(nodeId) {
			var node = this.store.getNodeById(nodeId);

			if (node) {
				try {
					node.remove();
				} catch (e) {
					// Rendering issues
				}
			} else {
				_debug('I have not find a node with id ' + nodeId);
			}
		},

		deselect: function() {
			this.getSelectionModel().deselectAll();
		},

		getNodeById: function(id) {
			return this.store.getRootNode().findChild('id', id, deep=true);
		},

		getAncestorsAsArray: function(nodeId) {
			var out = [],
				node = this.store.getRootNode().findChild('id', nodeId, deep=true);

			if (node) {
				out.push(node);

				while (node.parentNode != null) {
					out.push(node.parentNode);
					node = node.parentNode;
				}
			}

			return out;
		},

		isEmpty: function() {
			return !(this.store.getRootNode().hasChildNodes());
		},

		getFirtsSelectableNode: function() {
			if (this.disabled) {
				return null;
			}

			var l = this.getRootNode(),
				out = null;

			while (l) {
				if (this.nodeIsSelectable(l)) {
					out = l;
					break;
				} else {
					l = l.firstChild;
				}
			}

			return out;
		},

		nodeIsSelectable: function(node) {
			var name = node.get(TREE_NODE_NAME_ATTRIBUTE),
				isFolder = (!name || name == TREE_FOLDER_NODE_NAME),
				isRootNode = (this.getRootNode() == node),
				isHidden = isRootNode && !this.rootVisible;

			return !isFolder && !isHidden;
		},

		selectFirstSelectableNode: function() {
			var l = this.getFirtsSelectableNode();

			if (l) {
				this.expand();
				// Defer the call because Ext.selection.RowModel
				// for me.views.lenght says 'can not refer to length of undefined'
				Ext.Function.createDelayed(function() {
					this.selectNodeById(l);
				}, 100, this)();
			}
		},

		buildTreeStructure: function() {
			_debug('CMBaseAccordion.buildTreeStructure: buildTreeStructure() unimplemented method');
		},

		afterUpdateStore: function() {}
	});

})();