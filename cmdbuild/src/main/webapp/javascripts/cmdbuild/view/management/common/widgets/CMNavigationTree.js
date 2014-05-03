(function() {
	var DEFAULT_MENU_TEXT = CMDBuild.Translation.tree_navigation; 
	var NODE_TEXT_TMP = "{0} [{1}] {2}";
	var FILTER_FIELD = "_SystemFieldFilter";
	var cacheFields = [];
	Ext.define("CMDBuild.view.management.common.widgets.CMNavigationTree", {
		extend: "Ext.panel.Panel",
		autoScroll: true,	
		statics: {
			WIDGET_NAME: ".NavigationTree"
		},

		initComponent: function() {
			this.WIDGET_NAME = this.self.WIDGET_NAME;
			this.tree = new CMDBuild.view.management.widgets.navigationTree.CMTreePanel({
				widget: this
			});
			this.items = [this.tree];
			this.callParent(arguments);
			this.mon(this.tree, "afteritemexpand", function(node) {
				if (node.loaded)
					return;
				else
					node.loaded = true;
				Ext.suspendLayouts();
				expandNodes(this, node.childNodes);
			}, this);
		},

		configureForm: function(treeName, tree) {
			cacheFields = [];
			this.tree.loadTree(treeName, tree);
		},
		
		getData: function() {
			return this.tree.getData();
		}
		
	});

	Ext.define('CMDBuild.model.widget.NavigationTreeNodeModel', {
		extend: 'Ext.data.Model',
		fields: [{
			name: "text",
			type: "string"
		},{
			name: "cardId",
			type: "string"
		},{
			name: "className",
			type: "string"
		},{
			name: "nodesIn",
			type: "auto"
		},{
			name: "loaded",
			type: "boolean"
		}]

	});

	function createRoot() {
		 var root = Ext.create('Ext.data.TreeStore', {
				model: "CMDBuild.model.widget.NavigationTreeNodeModel",
				root: {
					expanded: false,
					checked: undefined,
					text: DEFAULT_MENU_TEXT,
					children: []
				}
			});
		return root;
	}
	
	Ext.define("CMDBuild.view.management.widgets.navigationTree.CMTreePanel", {
		extend: "Ext.tree.Panel",
		treeName: undefined,
        selModel: {
            selType: 'cellmodel'
        },
        border: false,
		initComponent: function() {
			this.store = createRoot();

			this.columns = [{
				xtype: 'treecolumn',
				text: DEFAULT_MENU_TEXT,
				dataIndex: 'text',
				flex: 3,
				sortable: false
			}];

			this.callParent(arguments);
		},
		loadTree: function(treeName, tree) {
			this.getSelectionModel().deselectAll();
			this.store.setRootNode({
				expanded: false,
				checked: undefined,
				text: DEFAULT_MENU_TEXT + ": " + treeName,
				children: [],
				nodesIn: [tree]
			});
			var r = this.store.getRootNode();
			r.removeAll(true);
			r.commit(); // to remove the F____ing red triangle to the node
			loadChildren(this.widget, r, tree, tree.childNodes);
		},
		
		getData: function() {
			var node = this.store.getRootNode();
			var data = [];
			getNode(data, node);
			return data;
		}
	});
	function getRowsForFilter(widget, filter, className, callBack) {
		var filterTemplateResolver = new CMDBuild.Management.TemplateResolver({
			xaVars: {},
			clientForm: widget.delegate.clientForm,
			serverVars: widget.delegate.getTemplateResolverServerVars()
		});
		filterTemplateResolver.xaVars[FILTER_FIELD] = filter;
		filterTemplateResolver.resolveTemplates({
			attributes: [FILTER_FIELD],
			callback: function(response) {
				var callParams = filterTemplateResolver.buildCQLQueryParameters(response[FILTER_FIELD]);
				var filterEncoded = (! callParams) ? "" : Ext.encode({
					CQL: callParams.CQL
				});
				CMDBuild.ServiceProxy.getCardList({
					params: {
						className: className,
						filter: filterEncoded
					},
					success: function(operation, request, decoded) {
						callBack(operation, request, decoded);
					}
				});
			}
		});
	}
	function loadChildren(widget, node, tree, nodesIn) {
		getRowsForFilter(widget, tree.filter, tree.targetClassName, function(operation, request, decoded) {
			for (var j = 0; j < decoded.rows.length; j++) {
				var row = decoded.rows[j];
				var text = Ext.String.format(NODE_TEXT_TMP, "", row.Code, row.Description);
				appendNode(node, text, row.Id, row.IdClass_value, nodesIn);
			}
		});
	}
	function appendNode(node, text, cardId, className, nodesIn) {
		var iconCls = (nodesIn.length > 0) ? '' : 'cmdbuild-tree-class-icon';
		var n = node.appendChild({
			nodeType: 'node',
			text: text,
			cardId: cardId,
			className: className,
			checked: false,
			expanded: false,
			iconCls: iconCls,
			nodesIn: nodesIn,
			children: []
		});
		n.commit();
	}
	function getDomainDirection(domain, className) {
		var direction = undefined;
		if (className ==  domain.get("nameClass2")) {
			direction = "_1";
		}
		else if (className ==  domain.get("nameClass1")) {
			direction = "_2";
		}
		return direction;
	}
	function getFilterNodes(widget, filter, className, callBack) {
		if (! filter || Ext.String.trim(filter) == "") {
			callBack(undefined);
		}
		else {
			if (! cacheFields[filter + className]) {
				getRowsForFilter(widget, filter, className, function(operation, request, decoded) {
					var arIds = [];
					for (var j = 0; j < decoded.rows.length; j++) {
						var row = decoded.rows[j];
						arIds.push(row.Id);
					}
					cacheFields[filter + className] = arIds;
					callBack(cacheFields[filter + className]);
				});
			}
			else {
				callBack(cacheFields[filter + className]);
			}
		}
	}
	function loadRelations(widget, node, nodeIn, domain, relations, callBack) {
		if (relations.length == 0) {
			callBack();
			return;
		}
		var row = relations[0];
		getFilterNodes(widget, nodeIn.filter, nodeIn.targetClassName, function(idsInFilter) {
			if (! idsInFilter || Ext.Array.contains(idsInFilter, row.dst_id)) {
				var domainDirection = getDomainDirection(domain, nodeIn.targetClassName);
				var domainName = (domainDirection == "_1") ? domain.get("descr_1") : domain.get("descr_2");
				var text = Ext.String.format(NODE_TEXT_TMP, domainName, row.dst_code, row.dst_desc);
				appendNode(node, text, row.dst_id, nodeIn.targetClassName, nodeIn.childNodes);
			}
			var appRelations = relations.slice(1);
			loadRelations(widget, node, nodeIn, domain, appRelations, callBack);
		});
	}
	function loadForDomainChildren(widget, node, nodesIn, callBack) {
		if (nodesIn.length == 0) {
			callBack();
			return;
		}
		var parameterNames = CMDBuild.ServiceProxy.parameter;
		var parameters = {};
		parameters[parameterNames.CARD_ID] = node.get("cardId");
		parameters[parameterNames.CLASS_NAME] = node.get("className");
		var domain = _CMCache.getDomainByName(nodesIn[0].domainName);
		parameters[parameterNames.DOMAIN_ID] = domain.get("id");
		var domainDirection = getDomainDirection(domain, nodesIn[0].targetClassName);
		parameters[parameterNames.DOMAIN_SOURCE] = domainDirection;
		console.log(" loadForDomainChildren " + nodesIn.length + " " + nodesIn[0].filter + " " + nodesIn[0].targetClassName + " " + node.get("className"));
		var appNodesIn = nodesIn.slice(1);
		CMDBuild.ServiceProxy.relations.getList({
			params: parameters,
			scope: this,
			success: function(operation, request, decoded) {
				if (decoded.domains.length > 0) { // searching for id only one domain is possible
					loadRelations(widget, node, nodesIn[0], domain, decoded.domains[0].relations, function() {
						loadForDomainChildren(widget, node, appNodesIn, callBack);
					});
				}
				else {
					loadForDomainChildren(widget, node, appNodesIn, callBack);
				}
			}
		});
	}
	function expandNodes(widget, children) {
		
		if (children.length > 0) {
			var child = children[0];
			loadForDomainChildren(widget, child, child.get("nodesIn"), function() {
				var appChildren = children.slice(1);
				expandNodes(widget, appChildren);
			});
		}
		else {
			Ext.resumeLayouts();
		}
	}
	function getNode(data, node) {
		if (node.get("checked")) {
			data.push(NodeToObject(node));
		}
		for (var i = 0; i < node.childNodes.length; i++) {
			getNode(data, node.childNodes[i]);
		}
	}
	function NodeToObject(node) {
		return {
			cardId: node.get("cardId"),
			className: node.get("className"),
		};
	}
        
})();