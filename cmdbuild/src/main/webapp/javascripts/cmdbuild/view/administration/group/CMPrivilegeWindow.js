(function() {

	var NOTE = "Notes";

	Ext.define('CMDBuild.view.administration.group.CMPrivilegeWindowAttributeModel', {
		extend: 'Ext.data.Model',
		fields: [{
			name: 'name',
			type: 'string'
		}, {
			name: 'description',
			type: 'string'
		}, {
			name: 'none',
			type: 'boolean'
		}, {
			name: 'read',
			type: 'boolean'
		}, {
			name: 'write',
			type: 'boolean'
		}],

		getName: function() {
			return this.get("name");
		},

		getPrivilege: function() {
			if (this.get("none")) {
				return "none";
			}

			if (this.get("read")) {
				return "read";
			}

			if (this.get("write")) {
				return "write";
			}

			/*
			 * if no privileges are
			 * set, assume that
			 * the group could have
			 * no privilege on
			 * this attribute
			 */
			return "none";
		},

		setPrivilege: function(privilege) {
			var me = this;
			var setAs = {
				write: function() {
					me.set("none", false);
					me.set("read", false);
					me.set("write", true);
				},
				read: function() {
					me.set("none", false);
					me.set("read", true);
					me.set("write", false);
				},
				none: function() {
					me.set("none", true);
					me.set("read", false);
					me.set("write", false);
				}
			}

			setAs[privilege]();
			me.commit();
		}

	});

	Ext.define("CMDBuild.view.administration.group.CMPrivileWindowAttributeGrid", {
		extend: "Ext.grid.Panel",

		initComponent: function() {

			this.title = CMDBuild.Translation.privileges_on_columns;
			this.border = false;

			var me = this;

			this.columns = [{
				header: CMDBuild.Translation.name,
				dataIndex: "name",
				flex: 1
			}, {
				header: CMDBuild.Translation.description_,
				dataIndex: "description",
				flex: 1
			}, {
				xtype: "checkcolumn",
				header: CMDBuild.Translation.administration.modsecurity.privilege.none_privilege,
				align: "center",
				dataIndex: "none",
				sortable: false,
				width: 70,
				fixed: true,
				listeners: {
					checkchange: {
						fn: me.onCheckChange,
						scope: me
					}
				}
			}, {
				xtype: "checkcolumn",
				header: CMDBuild.Translation.administration.modsecurity.privilege.read_privilege,
				align: "center",
				dataIndex: "read",
				sortable: false,
				width: 70,
				fixed: true,
				listeners: {
					checkchange: {
						fn: me.onCheckChange,
						scope: me
					}
				}
			}, {
				xtype: "checkcolumn",
				header: CMDBuild.Translation.administration.modsecurity.privilege.write_privilege,
				align: "center",
				dataIndex: "write",
				sortable: false,
				width: 70,
				fixed: true,
				listeners: {
					checkchange: {
						fn: me.onCheckChange,
						scope: me
					}
				}
			}];

			this.callParent(arguments);
		},

		onCheckChange: function(column, rowIndex, checked) {
			if (!checked) {
				return;
			}

			var model = this.store.getAt(rowIndex);
			if (model) {
				model.setPrivilege(column.dataIndex);
			}
		}

	});

	Ext.define("CMDBuild.view.administration.group.CMPrivilegeWindow", {
		extend: "CMDBuild.view.common.field.CMFilterChooserWindow",
	
		// configuration
		/**
		 * the model of the group to which
		 * want to set the privileges
		 */
		group: undefined,

		/**
		 * {CMDBuild.model.CMFilterModel}
		 */
		filter: undefined,

		/**
		 * an array of objects that defines the attributes
		 */
		attributes: [],

		/**
		 * the name of the class to which
		 * apply the filter
		 */
		className: "",
		// configuration

		initComponent: function() {
			this.saveButtonText = CMDBuild.Translation.common.buttons.save;
			this.callParent(arguments);
			this.layout = "fit";
		},

		/*
		 * The convention is to send
		 * to server an array of string.
		 * Each string has the template:
		 * 
		 * 	attributeName:mode
		 * 
		 * mode = none | read | write
		 */
		getAttributePrivileges: function() {
			var out = [];
			var store = this.columnPrivilegeGrid.getStore();
			store.each(function(record) {
				out.push(record.getName() + ":" + record.getPrivilege());
			});

			return out;
		},

		// protected
		// override
		setWindowTitle: function() {
			this.title = CMDBuild.Translation.row_and_column_privileges;
		},

		// protected
		// override
		buildItems: function() {
			this.callParent(arguments);

			var data = [];
			var attributePrivileges = extractAttributePrivileges(this.group);

			for (var i=0, l=this.attributes.length; i<l; ++i) {
				var classAttribute = this.attributes[i];
				// As usual, the notes attribute
				// is managed in a special way
				if (classAttribute.name == NOTE) {
					continue;
				}

				var attributeConf = {
					name: classAttribute.name,
					description: classAttribute.description,
					hidden: false,
					read: false,
					write: false
				};

				var privilege = attributePrivileges[classAttribute.name];
				if (privilege) {
					if (privilege == "none") {
						attributeConf.none = true;
					} else if (privilege == "read") {
						attributeConf.read = true;
					} else if (privilege == "write") {
						attributeConf.write = true;
					}
				}

				data.push(attributeConf);
			}

			this.columnPrivilegeGrid = new CMDBuild.view.administration.group.CMPrivileWindowAttributeGrid({
				store: new Ext.data.Store({
					model: "CMDBuild.view.administration.group.CMPrivilegeWindowAttributeModel",
					data: data
				})
			});
	
			var filterChooserWindowItem = this.items;

			this.rowPrivilegePanel = new Ext.panel.Panel({
				title: CMDBuild.Translation.privileges_on_rows,
				layout: "border",
				border: false,
				items: filterChooserWindowItem
			});

			this.items = [{
				xtype: "tabpanel",
				border: false,
				items: [ //
					this.rowPrivilegePanel, //
					this.columnPrivilegeGrid //
				]
			}];

		}
	});

	/*
	 * Convert an array of string with the form:
	 * 	attributeName:privilege
	 * in a map like this:
	 *  {
	 * 		attributeName: privilege,
	 * 		attributeName: privilege,
	 * 		...
	 * 		...
	 * 	}
	 */
	function extractAttributePrivileges(group) {
		var privileges = group.getAttributePrivileges();
		var out = {};
		for (var i=0, l=privileges.length; i<l; ++i) {
			var privilege = privileges[i];
			var parts = privilege.split(":");
			if (parts.length == 2) {
				out[parts[0]] = parts[1];
			}
		}

		return out;
	}
})();