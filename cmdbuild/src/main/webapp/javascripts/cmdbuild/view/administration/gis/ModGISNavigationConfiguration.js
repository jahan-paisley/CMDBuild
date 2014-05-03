(function() {

	var DEFAULT_MENU_TEXT = CMDBuild.Translation.management.modutilities.csv.selectaclass;

	Ext.define("CMDBuild.view.administration.gis.CMModGISNavigationConfiguration", {
		extend: "Ext.form.Panel",
		cmName: "gis-filter-configuration",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable",
			classesMenuDelegate: "CMDBuild.core.buttons.CMClassesMenuButtonDelegate"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.administration.gis.CMModGISNavigationConfigurationDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.title = CMDBuild.Translation.administration.modcartography.navigationTree.title;
			this.layout = "card";
			this.buttonAlign = "center";
			this.frame = false;
			this.cls = "x-panel-body-default-framed";
			this.bodyCls = "cmgraypanel";
			this.bodyStyle = "padding: 5px";

			var me = this;
			this.classesMenu = new CMDBuild.core.buttons.CMClassesMenuButton({
				text: DEFAULT_MENU_TEXT
			});

			this.classesMenu.addDelegate(this);

			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save,
				disabled: true,
				handler: function() {
					me.callDelegates("onGISNavigationSaveButtonClick", me);
				}
			});

			this.removeButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.remove,
				iconCls: "delete",
				disabled: true,
				handler: function() {
					me.callDelegates("onGISNavigationRemoveButtonClick", me);
				}
			});

			this.tbar = [
				this.classesMenu,
				"-",
				this.removeButton
			];

			this.buttons = [
				this.saveButton
			];

			this.blankPanel = new Ext.panel.Panel({
				cls: "cmdbuild_unconfigured_modpanel",
				html: "<div>" + DEFAULT_MENU_TEXT + "</div>",
				frame: false,
				bodyStyle: {
					padding: "5em 0 0 0"
				}
			});

			this.tree = new CMDBuild.view.administration.gis.CMModGISNavigationConfiguration.TreePanel({
				frame: false
			});

			this.items = [this.blankPanel, this.tree];

			this.callParent(arguments);

			this.mon(this.tree, "checkchange", function(node, checked) {
				this.callDelegates("onGISNavigationTreeItemChecked", [node, checked]);
			}, this);

			this.mon(this.tree, "afteritemexpand", function(node) {
				this.callDelegates("onGISNavigationTreeItemChecked", [node, node.get("checked")]);
			}, this);
		},

		setTreeForEntryType: function(entryType) {
			if (!entryType) {
				this.resetView();
				return;
			}

			this.classesMenu.setText(entryType.get("text"));
			this.saveButton.enable();
			this.removeButton.enable();
			this.getLayout().setActiveItem(this.tree);
			// set the tree as active after modify the root text
			// otherwise ExtJs cries
			this.tree.updateRootForEntryType(entryType);

			return this.tree.getRootNode();
		},

		resetView: function() {
			this.getLayout().setActiveItem(this.blankPanel);
			this.classesMenu.setText(DEFAULT_MENU_TEXT);
			this.classesMenu.showMenu();
			this.saveButton.disable();
			this.removeButton.disable();
		},

		addDomainsAsFirstLevelChildren: function(domains) {
			return this.tree.addDomainsAsFirstLevelChildren(domains);
		},

		addDomainsAsNodeChildren: function(domains, node) {
			return this.tree.addDomainsAsNodeChildren(domains, node);
		},

		getTreeStructure: function() {
			return this.tree.serializeStructure();
		},

		getTreeNodeForConf: function(conf) {
			var root = this.tree.getRootNode();
			var scope = null;
			var deep = true;
			return root.findChildBy(function(node) {
				return node.isMyConf(conf);
			}, scope, deep);
		},

		// as classesMenuDelegate

		onCMClassesMenuButtonItemClick: function(menu, entryType) {
			this.setTreeForEntryType(entryType);
			this.callDelegates("onGISNavigationBaseClassMenuItemSelect", entryType);
		}
	});

	Ext.define('CMDBuild.model.GISNavigationConfigurationTreeNodeModel', {
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
			name: "baseNode",
			type: "boolean"
		}],

		getDomain: function() {
			return this.get("domain");
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

		/**
		 *
		 * @param {Object} conf
		 * has the form:
		 * {
		 * 	childNodes: [],
		 * 	direct: boolean,
		 * 	domainName: string,
		 * 	id: integer,
		 * 	idParent: integer,
		 * 	targetClassDescription: string,
		 * 	targetClassName: string,
		 * 	type: string
		 * }
		 */
		isMyConf: function(conf) {
			var d = this.getDomain();
			if (d && d.get("name") != conf.domainName) {
				return false;
			}
			var et = this.getEntryType();
			return et.get("name") == conf.targetClassName;
		}
	});

	Ext.define("CMDBuild.view.administration.gis.CMModGISNavigationConfigurationDelegate", {
		onGISNavigationSaveButtonClick: function() {},
		onGISNavigationRemoveButtonClick: function() {},
		onGISNavigationBaseClassMenuItemSelect: function() {},
		onGISNavigationTreeItemChecked: function() {}
	});

	var NODE_TEXT_TMP = "{0} ({1})";

	Ext.define("CMDBuild.view.administration.gis.CMModGISNavigationConfiguration.TreePanel", {
		extend: "Ext.tree.Panel",

		initComponent: function() {
			this.store = Ext.create('Ext.data.TreeStore', {
				model: "CMDBuild.model.GISNavigationConfigurationTreeNodeModel",
				root: {
					expanded: true,
					checked: true,
					text: DEFAULT_MENU_TEXT,
					children: []
				}
			});

			this.columns = [{
				xtype: 'checkcolumn',
				dataIndex: 'baseNode',
				text: CMDBuild.Translation.administration.setup.graph.baseLevel,
				width: 100,
				sortable: false,
				cmExclusive: true
			}, {
				xtype: 'treecolumn',
				dataIndex: 'text',
				flex: 5,
				sortable: false
			}];

			this.callParent(arguments);
		},

		addDomainsAsFirstLevelChildren: function(domains) {
			var r = this.getStore().getRootNode();
			this.addDomainsAsNodeChildren(domains, r);
		},

		addDomainsAsNodeChildren: function(domains, node) {
			node.expand();
			for (var i=0, l=domains.length; i<l; ++i) {
				var d = domains[i];
				var etId = d.getNSideIdInManyRelation();
				var et = _CMCache.getEntryTypeById(etId);
				node.appendChild({
					text: Ext.String.format(NODE_TEXT_TMP, d.get("description"), et.get("text")),
					checked: false,
					expanded: false,
					domain: d,
					entryType: et,
					children: []
				});
			}
		},

		updateRootForEntryType: function(entryType) {
			var r = this.store.getRootNode();
			r.setText(entryType.get("text"));
			r.setEntryType(entryType);
			r.commit(); // to remove the F____ing red triangle to the node
			r.removeAll();
		},

		serializeStructure: function() {
			var r = this.store.getRootNode();
			var et = r.getEntryType();
			var structure = {
				targetClassName: et.get("name"),
				targetClassDescription: et.get("text"),
				childNodes: serializeChildNodesForStructure(r.childNodes)
			};

			return structure;
		}
	});

	function serializeChildNodesForStructure(childNodes) {
		var out = [];
		childNodes = childNodes || [];
		for (var i=0, l=childNodes.length; i<l; ++i) {
			var node = childNodes[i];
			var serialized = serializeNodeForStructure(node);
			if (serialized) {
				out.push(serialized);
			}
		}

		return out;
	}

	function serializeNodeForStructure(node) {
		if (!node
				|| !node.get("checked")) {
			return null;
		}

		var et = node.getEntryType();
		var d = node.getDomain();

		return {
			targetClassName: et.get("name"),
			targetClassDescription: et.get("text"),
			domainName: d.get("name"),
			direct: d.get("cardinality") == "1:N",
			baseNode: node.get("baseNode"),
			childNodes: serializeChildNodesForStructure(node.childNodes)
		};
	}
})();