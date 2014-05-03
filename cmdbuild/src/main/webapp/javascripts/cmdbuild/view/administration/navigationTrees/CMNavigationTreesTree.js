(function() {
	var DEFAULT_MENU_TEXT = CMDBuild.Translation.trees_navigation; 
	
	Ext.define("CMDBuild.view.administration.navigationTrees.CMNavigationTreesTree",{
		extend: "Ext.panel.Panel",
		treeName : undefined,
		constructor: function() {

			this.callParent(arguments);
		},
		initComponent : function() {

			this.modifyButton = new Ext.button.Button({
				iconCls : 'modify',
				text: CMDBuild.Translation.tree_modify, 
				scope: this,
				handler: function() {
					this.delegate.cmOn("onModifyButtonClick");
				}
			});

			this.deleteButton = new Ext.button.Button({
				iconCls : 'delete',
				text: CMDBuild.Translation.tree_remove, 
				scope: this,
				handler: function() {
					this.delegate.cmOn("onDeleteButtonClick");
				}
			});

			this.saveButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.save,
				scope: this,
				handler: function() {
					this.delegate.cmOn("onSaveButtonClick");
				}
			});

			this.abortButton = new Ext.button.Button( {
				text : CMDBuild.Translation.common.buttons.abort,
				scope: this,
				handler: function() {
					this.delegate.cmOn("onAbortButtonClick");
				}
			});

			this.cmTBar = [this.modifyButton, this.deleteButton];
			this.cmButtons = [this.saveButton, this.abortButton];
			this.tree = new CMDBuild.view.administration.navigationTrees.CMTreePanel({
				frame: false
			});
			this.treePanel = new Ext.panel.Panel( {
				region : "center",
				frame : false,
				border : true,
				autoScroll : true,
				width: "100%",
				height: "100%",
				cls: "x-panel-body-default-framed",
				items : [this.tree]
			});


			Ext.apply(this, {
				tbar: this.cmTBar,
				buttonAlign: "center",
				buttons: this.cmButtons,
				frame: false,
				border: false,
				layout: "border",
				cls: "x-panel-body-default-framed",
				bodyCls: 'cmgraypanel',
				items: [this.treePanel]
			});
			
			this.callParent(arguments);
			this.mon(this.tree, "afteritemexpand", function(node) {
				for (var i = 0; i < node.childNodes.length; i++) {
					expandNode(this, node.childNodes[i]);
				}
			}, this);
		},

		onTreeSelected: function(tree) {
			this.disableModify(enableCMTBar = true);
			var entity = _CMCache.getEntryTypeByName(tree.targetClassName);
			this.setTreeForEntryType(entity);
			this.tree.openTreeForTreeType(tree);
		},
		setTreeForEntryType: function(entryType) {
			if (!entryType) {
				this.resetView();
				return;
			}

			this.tree.updateRootForEntryType(entryType);

			return this.tree.getRootNode();
		},

		enableModify: function() {
			this.treePanel.enable();
			this.saveButton.enable();
			this.abortButton.enable();
			this.modifyButton.disable();
			this.deleteButton.disable();
		},

		disableModify: function() {
			this.treePanel.disable();
			this.saveButton.disable();
			this.abortButton.disable();
			this.modifyButton.enable();
			this.deleteButton.enable();
		},

		addDomainsAsFirstLevelChildren: function(domains) {
			return this.tree.addDomainsAsFirstLevelChildren(domains);
		},

		getData: function() {
			return this.tree.getData();
		},
		
		addDomainsAsNodeChildren: function(domains, node) {
			return this.tree.addDomainsAsNodeChildren(domains, node);
		},

	});	
	Ext.define('CMDBuild.model.NavigationTreeNodeModel', {
		extend: 'Ext.data.Model',
		fields: [{
			name: "text",
			type: "string"
		},{
			name: "domain",
			type: "auto"
		},{
			name: "entryType",
			type: "auto"
		},{
			name: "cqlNode",
			type: "text"
		},{
			name: "destination",
			type: "text"
		}],

		getDomain: function() {
			return this.get("domain");
		},

		getDestination: function() {
			return this.get("destination");
		},

		getEntryType: function() {
			return this.get("entryType");
		},

		setEntryType: function(et) {
			this.set("entryType", et);
		},

		setText: function(text) {
			this.set("text", text);
		},

		getNSideIdInManyRelation: function() {
			var d = this.getDomain();
			if (d) {
				return d.getNSideIdInManyRelation();
			}

			return null;
		},

	});

	var NODE_TEXT_TMP = "{0} ({1} {2})";

	var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
		clicksToEdit: 1
	});
	function createRoot() {
		 var root = Ext.create('Ext.data.TreeStore', {
				model: "CMDBuild.model.NavigationTreeNodeModel",
				root: {
					expanded: false,
					checked: true,
					text: DEFAULT_MENU_TEXT,
					children: []
				}
			});
		return root;
	}
	
	Ext.define("CMDBuild.view.administration.navigationTrees.CMTreePanel", {
		extend: "Ext.tree.Panel",
        selModel: {
            selType: 'cellmodel'
        },
        border: false,
        plugins: [cellEditing],
		initComponent: function() {
			this.store = createRoot();

			this.columns = [{
				xtype: 'treecolumn',
				text: CMDBuild.Translation.tree_navigation, 
				dataIndex: 'text',
				flex: 3,
				sortable: false
			}, {
				dataIndex: 'cqlNode',
				text: CMDBuild.Translation.cql_filter, 
				flex: 2,
				sortable: false,
				field: {
					allowBlank: true,
					enabled: false
				}
			}];

			this.callParent(arguments);
			this.mon(this, "checkchange", function(node, checked) {
				this.onNavigationTreesItemChecked(node, checked);
			}, this);

		},
		
		onNavigationTreesItemChecked: function(node, checked) {
			this.suspendEvents(false);
			if (checked) {
				checkAllParents(node);
			}
			else {
				unCheckAllChildren(node);
			}
			this.resumeEvents();
		},
		
		addDomainsAsFirstLevelChildren: function(domains) {
			var r = this.getStore().getRootNode();
			this.addDomainsAsNodeChildren(domains, r);
		},
		
		openTreeForTreeType: function(tree) {
			this.suspendEvents(false);
			var thereAreChildren = false;
			var r = this.getStore().getRootNode();
			r.set("cqlNode", tree.filter);
			for (var i = 0; i < tree.childNodes.length; i++) {
				if (this.openNodes(tree.childNodes[i], r))
					thereAreChildren = true;
			}
			if (thereAreChildren) {
				r.expand();
			}
			this.resumeEvents();
		},
		
		addDomainsAsNodeChildren: function(domains, node) {
			node.collapse();
			for (var i=0, l=domains.length; i<l; ++i) {
				var d = domains[i];
				var etId = d.destinationClassId;
				var domainDescription = getDomainDescription(d);
				var et = _CMCache.getEntryTypeById(etId);
				node.appendChild({
					text: Ext.String.format(NODE_TEXT_TMP, d.get("description"), domainDescription, et.get("text")),
					checked: false,
					expanded: true,
					domain: d,
					entryType: et,
					destination: et.get("name"),
					children: []
				});
			}
		},

		openNodes: function(nodeSaved, parentComplete) {
			var nodeFound = inChildrenNodes(nodeSaved, parentComplete);
			
			if (nodeFound) {
				expandAllChildrenNodes(this, parentComplete);
				var thereAreChildren = false;
				for (var i = 0; i < nodeSaved.childNodes.length; i++) {
					if(this.openNodes(nodeSaved.childNodes[i], nodeFound)) {
						thereAreChildren = true;
					}
				}
				if (thereAreChildren) {
					nodeFound.expand();
				}
				nodeFound.set("cqlNode", nodeSaved.filter);
				nodeFound.set("checked", true);
				return true;
			}
			return false;
		},

		updateRootForEntryType: function(entryType) {
			this.getSelectionModel().deselectAll();
			this.store.setRootNode({
				expanded: false,
				checked: true,
				text: entryType.get("text"),
				children: []
			});
			var r = this.store.getRootNode();
			r.setText(entryType.get("text"));
			r.setEntryType(entryType);
			r.removeAll(true);
			r.commit(); // to remove the F____ing red triangle to the node
			var domains = retrieveDomainsWithDestinationForEntryType(entryType, undefined);
			this.addDomainsAsFirstLevelChildren(domains);
		},

		getData: function() {
			var node = this.store.getRootNode();
			return {
				filter: node.get("cqlNode"),
				children: getChildren(node)
			};
		}
		
	});

	function retrieveDomainsWithDestinationForEntryType(entryType, domainName, onlyN_1) {
		var ids =  _CMUtils.getAncestorsId(entryType);
		return _CMCache.getDomainsBy(function(domain) {
			if (! onlyN_1) {
				if (domainName && domain.get("name") == domainName)
					return false;
				else if (Ext.Array.contains(ids, domain.getSourceClassId())) {
					domain.destinationClassId = domain.getDestinationClassId();
					return true;
				}
				else if (Ext.Array.contains(ids, domain.getDestinationClassId())) {
					domain.destinationClassId = domain.getSourceClassId();
					return true;
				}
				else {
					return false;
				}
			}
			else {
				var cardinality = domain.get("cardinality");
				if (cardinality == "1:N"
					&& Ext.Array.contains(ids, domain.getSourceClassId())) {
	
					return true;
				}
	
				if (cardinality == "N:1"
					&& Ext.Array.contains(ids, domain.getDestinationClassId())) {
	
					return true;
				}
	
				return false;
			}
		});
	}
	function expandNode(me, node) {
		if (node._alreadyExpanded) {
			return;
		}
		node._alreadyExpanded = true;
		var id = node.getDomain().getDestinationClassId();
		if (id) {
			var domains = retrieveDomainsWithDestinationForEntryType(id, node.getDomain().get("name"), false);
			me.addDomainsAsNodeChildren(domains, node);
		}
		id = node.getDomain().getSourceClassId();
		if (id) {
			var domains = retrieveDomainsWithDestinationForEntryType(id, node.getDomain().get("name"), false);
			me.addDomainsAsNodeChildren(domains, node);
		}
	}
	function expandAllChildrenNodes(me, node) {
		for (var i = 0; i < node.childNodes.length; i++) {
			expandNode(me, node.childNodes[i]);
		}
	}
	function inChildrenNodes(nodeSaved, parentComplete) {
		for (var i = 0; i < parentComplete.childNodes.length; i++) {
			if (isEqual(nodeSaved, parentComplete.childNodes[i])) {
				return parentComplete.childNodes[i];
			}
		}
		return undefined;
	}
	function isEqual(nodeSaved, node) {
		return (nodeSaved.domainName == node.getDomain().get("name") && 
						(nodeSaved.targetClassName == node.getDomain().get("nameClass1") ||
						nodeSaved.targetClassName == node.getDomain().get("nameClass2"))
		);
	}
	function checkAllParents(node) {
		while (node = node.parentNode) {
			node.set("checked", true);
		}
	}
	function unCheckAllChildren(node) {
		for (var i = 0; i < node.childNodes.length; i++) {
			unCheckAllChildren(node.childNodes[i]);
			node.childNodes[i].set("checked", false);
		}
	}
	function getChildren(node) {
		var children = [];
		for (var i = 0; i < node.childNodes.length; i++) {
			var n = node.childNodes[i];
			if (n.get("checked")) {
				children.push(NodeToObject(n));
			}
		}
		return children;
	}
	function NodeToObject(node) {
		var et = _CMCache.getEntryTypeByName(node.getDestination());
		return {
			domainName: node.getDomain().get("name"),
			targetClassName: et.get("name"),
			targetClassDescription: et.get("text"),
			filter: node.get("cqlNode"),
			direct: true,
			BaseNode: false,
			childNodes: getChildren(node)
		};
	}
	function getDomainDescription(domain) {
		return (domain.destinationClassId == domain.get("idClass1")) ?
				domain.get("descr_2") :
				domain.get("descr_1");
	}
})();