(function() {
	var tr = CMDBuild.Translation;
	
	function buildLinkedNodes(tableMap, putAllAttributes, sorted) {
		var nodes = buildTreeNodesMap(tableMap, putAllAttributes, sorted);
		for (var id in tableMap) {
			var table = tableMap[id];
			var parentId = table.parent;
			if (parentId) {
				var parent = nodes[parentId];
				if (parent) {
					parent.appendChild(nodes[id]);
				} else {
					//orphan children are removed
					delete nodes[id];
				}
			}
		}
		return nodes;
	};
	
	function buildTreeNodesMap(tableList, putAllAttributes, sorted) {
		var treeNodesMap = {};
		
		for (var i in tableList) {
			var table = tableList[i];
			var node = buildNodeFromTable(table, putAllAttributes, sorted);
			treeNodesMap[table.id] = node;
		}
		return treeNodesMap;
	};

	function buildNodeFromTable(table, putAllAttributes, sorted) {
		var conf = {
		    id: table.id,
		    text: table.text,
		    selectable: table.selectable,
		    iconCls: (function() {
			    if (!table.selectable) {
				    return;
			    }
			    var cls = "cmdbuild-tree-";
			    if (table.superclass) {
				    cls += "super";
			    }
			    cls += table.type + "-icon";
			    return cls;
		    })(table)
		};
		
		if (putAllAttributes) {
			Ext.apply(conf, table);
		}
		
		var node = new Ext.tree.TreeNode(conf);
		if (sorted) {
			new Ext.tree.TreeSorter(node, {
			    folderSort: true,
			    dir: "asc"    
			});
		}
		
		return node;
	};
	
	CMDBuild.TreeUtility = {
		/**	
		 * @param the name of a cached table list
		 * CMDBuild table
		 * @return Ext.tree.TreeNode
		 */
		getTree: function(treeName, rootId, rootText, sorted) {
			var tables = CMDBuild.Cache.getTablesByGroup(treeName);
			
			if (!tables) {
			    return undefined;
		    }
			
		    var tree = this.buildTree(tables, rootId, putAllAttr=false, sorted);
		    if (tree) {
			    if (tr.common.tree_names[treeName]) {
				    tree.attributes["type"] = treeName;
			    }
			    if (rootText) {
			    	tree.setText(rootText);
			    }
		    }
			return	tree;
		},
	    buildTree:  function(tableMap, rootId, putAllAttributes, sorted) {
			var nodes = buildLinkedNodes(tableMap, putAllAttributes, sorted);
			var out = null;
			var roots = [];
			for (var id in nodes) {
				var node = nodes[id];
				if (node.parentNode) {
					continue;
				} else {
					roots.push(node);
				}
			}
			
			if (roots.length == 1 && !rootId) {
				out =  roots[0];
			} else {
				var root = new Ext.tree.TreeNode({
					id: rootId,
					iconCls: "cmdbuild-tree-folder-icon"
				});
				root.appendChild(roots);
				out = root;
			}
			
			if (sorted) {
				new Ext.tree.TreeSorter(out, {
				    folderSort: true,
				    dir: "asc"    
				});
			}
			return out;
		},
		buildNodeFromTable: buildNodeFromTable,
		/**
		 * the parameter p must have the structure
		 * {
		 * 		url: the data url,
		 * 		params: the params for the request,
		 * 		targetTreeRoot: the treeNode that is the root of the tree
		 * }
		 * */
		loadDataFromUrl: function(p){
			CMDBuild.Ajax.request({
				url : p.url,
				params: p.params,
				scope : this,
				success : function(response, options, decoded) {
					p.targetTreeRoot.appendChild(decoded);
				}
			});
		},
		/**
		 * return the first node with type different to "folder" or null
		 * */
		findFirsNodeNonFolder: function(rootNode) {
			if (rootNode) {
				do {
					if (rootNode.attributes.type && rootNode.attributes.type != "folder") { 
						return rootNode;
					} else {
						var oldRoot = rootNode;
						rootNode = rootNode.firstChild;
					}
				} while (oldRoot.hasChildNodes() && rootNode);
			}
			return null;
		},
		
		findFirsSelectableNode: function(rootNode) {
			if (rootNode) {
				do {
					if (rootNode.attributes.selectable) {
						_debug("is selectable", rootNode);
						return rootNode;
					} else {
						var oldRoot = rootNode;
						rootNode = rootNode.firstChild;
					}
				} while (oldRoot.hasChildNodes() && rootNode);
			}
			return null;
		},
		
		/**
		 * search the first node with target the passed attribute
		 * p = {
		 * 	attribute: attributeName
		 *  value: attributeValue
		 *  root: the treeNode to begin the search
		 * }
		 */
		searchNodeByAttribute: function(p) {
			var nodes = [p.root];
			while (nodes.length > 0) {
				var node = nodes.pop();
				if (node.attributes[p.attribute] == p.value) { 
					return node;
				} else {
					if (node.hasChildNodes()) {
						var tmp = nodes;
						nodes = tmp.concat(node.childNodes);
					}
				}
			};
			return null;
		},
		
		isSuperClassNode: function(node) {
			if (!node.attributes.iconCls)
				return false;
			return (node.attributes.iconCls.indexOf("cmdbuild-tree-super") != -1);
		},
		
		cloneNode: function(node) {
			var attr = node.attributes || {};
			var dolly = new Ext.tree.TreeNode(attr);
			var children = node.childNodes;
			
			for (var i=0, len=children.length; i<len; ++i) {
				var node = children[i];
				var cloned = CMDBuild.TreeUtility.cloneNode(node);
				dolly.appendChild(cloned);
			}
			
			return dolly;
		},
		
		/**
		 * convert an array in a map. Assume that every element
		 * of the array have an id attribute to use as pointer in the
		 * new map 
		 **/
		arrayToMap: function (array) {
			var map = {};
			for (var i=0, l=array.length; i<l; i++) {
				var item = array[i];
				if (item.id) {
					map[item.id] = item; 
				} else {
					throw new Error("Array to map: the array item must have an id attribure");
				}
			}
			return map;
		}
	};
})();