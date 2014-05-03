(function() {

	var FILTER = "FILTER";
	var SQL = "SQL";
	var DATA_VIEW = "dataView";
	var _idCount = 0;

	Ext.define("CMDBuild.view.administration.accordion.CMMenuAccordion", {
		extend: "CMDBuild.view.common.CMBaseAccordion",
		title: CMDBuild.Translation.management.modmenu.menu,
		cmName: "menu",
		buildTreeStructure: function(menu) {
			var out = [];
			if (menu) {
				var tree = adapt(menu);
				if (tree.children) {
					out = tree.children;
				}
			}

			// override to look for the
			// real children nodes
			this.isEmpty = function() {
				return out.length == 0;
			};

			return out;
		},

		// used only to retrieve classes or processes
		getNodeById: function(id) {
			if (! _CMCache.isEntryTypeById(id)) {
				return null;
			}
			var entryType = _CMCache.getEntryTypeById(id);
			if (!entryType) {
				return null;
			}

			var name = entryType.getName();
			var scope = this;
			var deep = true;
			return this.store.getRootNode().findChildBy( //
				function(node) { //
					return node.get("name") == name;
				}, //
				scope, //
				deep //
			);
		}
	});

	function adapt(menu) {
		var out = adaptSingleNode(menu);
		if (menu.children || out.type == "folder") {
			out.leaf = false;
			out.children = [];
			out.expanded = false;

			var children = menu.children || [];
			for (var i=0, l=children.length; i<l; ++i) {
				var child = children[i];
				out.children.push(adapt(child));
			}
		} else {
			out.leaf = true;
		}

		return out;
	}

	/*
	 * a node from the server has this shape:
	 * 
	 * {
	 * 	description: a description
		index: for the sort
		referencedClassName: the class to opent
		referencedElementId: eventually the id
		type: "class | processclass | dashboard | reportcsv | reportpdf | view"
		}
	 */
	function adaptSingleNode(node) {
		var type = node.type;
		var entryType = null;
		var superClass = false;
		var tableType = "";
		var classIdentifier = node.referencedClassName;

		if (type == "class" 
			|| type == "processclass") {

			entryType = _CMCache.getEntryTypeByName(node.referencedClassName);
			if (entryType) {
				superClass = entryType.isSuperClass();
				classIdentifier = entryType.getId();
				tableType = entryType.getTableType();
			}
		}

		var out = {
			id: classIdentifier,
			idClass: classIdentifier,

			name: node.referencedClassName,
			text: node.description,
			tableType: tableType,
			leaf: type != "folder",
			cmName: node.type == "processclass" ? "process" : node.type, //ugly compatibility hack
			iconCls: "cmdbuild-tree-" + (superClass ? "super" : "") + type +"-icon",
			cmIndex: node.index,
			type: node.type
		};

		if (isAReport(node)) {
			addReportStuff(out, node);
		}

		if (isADashboard(node)) {
			out.id = node.referencedElementId;
		}

		if (isView(node)) {
			out.tableType = "standard";
			out.leaf = true;
			out.iconCls = "cmdbuild-tree-class-icon";

			if (node.specificTypeValues.type == FILTER) {
				node.viewType = FILTER;

				var entryTypeName = node.specificTypeValues.sourceClassName;
				var entryType = _CMCache.getEntryTypeByName(entryTypeName);
				
				if (entryType != null) {
					out.id = entryType.getId();
					out.filter = node.specificTypeValues.filter;
					out.cmName = "class"; // To act as a regular class node
				}
				
			} else {
				out.viewType = SQL;
				out.sourceFunction = node.specificTypeValues.sourceFunction;
				out.cmName = DATA_VIEW;
			}
		}

		out.id = addProgressiveNumberToId(out.id);

		return out;
	}

	function addProgressiveNumberToId(cmdbuildId) {
		return _idCount++ + "#" + cmdbuildId;
	}

	function isADashboard(node) {
		return node.type == "dashboard";
	}

	function isAReport(node) {
		return node.type.indexOf("report") > -1;
	}

	function isView(node) {
		return node.type == "view";
	}

	function addReportStuff(n, node) {
		n.cmName = "singlereport";
		n.id = node.referencedElementId;
		n.subtype = "custom";
	}
})();