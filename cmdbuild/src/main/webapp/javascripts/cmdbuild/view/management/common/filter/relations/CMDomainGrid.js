(function() {
	var tr = CMDBuild.Translation.administration.modClass.domainProperties;

	Ext.define("CMDBuild.model.CMDomainGridModel", {
		extend: "Ext.data.Model",
		fields: [{
			name: "domain",
			type: "auto" // CMDBuild.cache.CMDomainModel
		},	{
			name: "destination",
			type: "auto" // "CMDBuild.cache.CMEntryTypeModel"
		}, {
			name: "source",
			type: "auto" // "CMDBuild.cache.CMEntryTypeModel"
		}, {
			/*
			 * "_1" if the source class is the first of the domain configuration,
			 * "_2" if the source class is the second of the domain configuration
			 */
			name: "direction",
			type: "string"
		},{
			name: "orientedDescription",
			type: "string"
		},{
			name: "noone",
			type: "boolean",
			defaultValue: false
		}, {
			name: "any",
			type: "boolean",
			defaultValue: false
		}, {
			name: "oneof",
			type: "boolean",
			defaultValue: false
		}, {
			name: "checkedCards",
			type: "auto" // array of {Id: ..., ClassName: ""}
		}],

		getDomain: function() {
			return this.get("domain");
		},

		getDestination: function() {
			return this.get("destination");
		},

		getSource: function() {
			return this.get("source");
		},

		setDestinationFromId: function(destinationId) {
			this.set("destination", _CMCache.getEntryTypeById(destinationId));
			this.commit();
		},

		getDirection: function() {
			return this.get("direction");
		},

		getCheckedCards: function() {
			var checkedCards = this.get("checkedCards");
			if (!checkedCards) {
				checkedCards = [];
				this.set("checkedCards", checkedCards);
			}

			return  checkedCards;
		},

		setCheckedCards: function(checkedCards) {
			_debug("setCheckedCards");
			if (!checkedCards) {
				checkedCards = [];
			}
			return this.set("checkedCards", checkedCards);
		},

		addCheckedCard: function(cardInfo) {
			_debug("add card info", cardInfo);
			var cardIndex = this.getCheckedCardIndex(cardInfo);
			if (cardIndex == -1) {
				this.getCheckedCards().push(cardInfo);
			}
		},

		removeCheckedCard: function(cardInfo) {
			_debug("remove card info", cardInfo);
			var cardIndex = this.getCheckedCardIndex(cardInfo);
			if (cardIndex >= 0) {
				this.setCheckedCards(Ext.Array.erase(this.getCheckedCards(), cardIndex, 1));
			}
		},

		getCheckedCardIndex: function getCheckedCardIndex(cardInfo) {
			var cc = this.getCheckedCards();
			for (var i=0, l=cc.length; i<l; ++i) {
				var card = cc[i];
				if (card.className == cardInfo.className
						&& card.id == cardInfo.id) {

					return i;
				}
			}

			return -1;
		},

		getType: function() {
			var type = null;

			if (this.get("any")) {
				type = "any";
			} else if (this.get("noone")) {
				type = "noone";
			} else if (this.getCheckedCards().length > 0) {
				type = "oneof";
			}

			return type;
		},

		setType: function(type) {
			var any = type == "any";
			var noone = type == "noone";
			var oneof = type == "oneof";

			this.set("any", any);
			this.set("noone", noone);
			this.set("oneof", oneof);

			this.commit();
		},

		hasName: function(name) {
			return this.getDomain().getName() == name;
		}
	});

	Ext.define("CMDBuild.view.management.common.filter.CMDomainGridDelegate", {
		/**
		 * 
		 * @param {CMDBuild.view.management.common.filter.CMDomainGrid} grid
		 * @param {CMDBuild.model.CMDomainGridModel} record
		 */
		onCMDomainGridSelect: function(grid, record) {},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.filter.CMDomainGrid} grid
		 * @param {Ext.ux.CheckColumn} column
		 * @param {boolean} checked
		 * @param {CMDBuild.model.CMDomainGridModel} record
		 */
		onCMDomainGridCheckedColumn: function(grid, column, checked, record) {},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.filter.CMDomainGrid} grid
		 * @param {string/int} entryTypeId the id of the destination subclass
		 */
		onCMDomainGridDestinationClassChange: function(grid, entryTypeId) {}
	});

	Ext.define("CMDBuild.view.management.common.filter.CMDomainGrid", {
		extend: "Ext.grid.Panel",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		// configuration
		className: undefined,
		// configuration

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
					"CMDBuild.view.management.common.filter.CMDomainGridDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.columns = this.getColumnsConfiguration();
			this.store = new CMDBuild.view.management.common.filter.CMDomainGrid.Store();

			this.plugins = [
				Ext.create('Ext.grid.plugin.CellEditing', {
					clicksToEdit: 2
				})
			],

			this.callParent(arguments);

			this.load();

			this.mon(this, "select", function(grid, record) {
				var destination = record.get("destination");
				if (destination) {
					this.destinationComboStore.fillForEntryTypeId(destination.getId());
				}
				this.callDelegates("onCMDomainGridSelect", [this, record]);
			}, this);

			// when select the a subclass from the
			// destination column combo
			this.mon(this, 'edit', function(editor, e) {
				var c = e.column.getEditor();
				var entryTypeId = c.getValue();
				e.record.setDestinationFromId(entryTypeId);

				this.callDelegates("onCMDomainGridDestinationClassChange", [this, entryTypeId]);
			}, this);

			// Rendering issue --> render the picker of the
			// combo behind the window that own the grid
			this.mon(this, "render", function() {
				this.destinationCombo.ownerCt = this.ownerCt;
			}, this);
		},

		getColumnsConfiguration: function() {
			this.destinationComboStore = new CMDBuild.view.management.common.filter.CMDomainGrid.DestinationComboStore();

			this.destinationCombo = new Ext.form.field.ComboBox({
				store: this.destinationComboStore,
				displayField: 'descr',
				valueField: "id",
				queryMode: "local",
				triggerAction: 'all'
			});

			var me = this;

			return [{
				flex : 1,
				header : tr.domain,
				renderer: function (value, metadata, record) {
					return record.getDomain().getDescription();
				}
			}, {
				dataIndex: "orientedDescription",
				flex: 1,
				header: CMDBuild.Translation.management.findfilter.direction
			}, {
				field: this.destinationCombo,
				flex: 1,
				header: tr.class_destination,
				renderer: function (value, metadata, record) {
					return record.getDestination().getDescription();
				}
			}, {
				header: CMDBuild.Translation.management.findfilter.relations,
				columns: [
					getCheckColumnConfig(me, CMDBuild.Translation.management.findfilter.notinrel, 'noone'),
					getCheckColumnConfig(me, CMDBuild.Translation.management.findfilter.all, 'any'),
					getCheckColumnConfig(me, CMDBuild.Translation.management.findfilter.fromSelection, 'oneof')
				]
			}];
		},

		load: function(cb) {
			this.store.load(this.className);
			if (typeof cb == "function") {
				cb();
			}
		}
	});

	Ext.define("CMDBuild.view.management.common.filter.CMDomainGrid.Store", {
		extend: "Ext.data.Store",
		model: "CMDBuild.model.CMDomainGridModel",

		load: function(className) {
			var me = this;
			var domains = [];

			var targetEntryType = _CMCache.getEntryTypeByName(className);
			if (targetEntryType) {
				domains = _CMCache.getDirectedDomainsByEntryType(targetEntryType);
			}

			me.removeAll();

			for (var i=0, l=domains.length; i<l; ++i) {
				var d = domains[i];

				var domain = _CMCache.getDomainById(d.dom_id);
				var domainGridModel = new CMDBuild.model.CMDomainGridModel({
					domain: domain,
					destination: _CMCache.getEntryTypeById(d.dst_cid),
					source: _CMCache.getEntryTypeById(d.src_cid),
					direction: d.src,
					orientedDescription: d.src == "_1" ? domain.get("descr_1") : domain.get("descr_2")
				});

				me.add(domainGridModel);
			}
		}
	});

	Ext.define("CMDBuild.view.management.common.filter.CMDomainGrid.DestinationComboStore", {
		extend: "Ext.data.Store",
		fields:["id", "descr"],
		fillForEntryTypeId: function(entryTypeId) {
			this.removeAll();

			var ett = _CMCache.getEntryTypes(),
				out = [];

			for (var et in ett) {
				et = ett[et];
				// TODO is right to check only one level of ancestors?
				if (et.get("parent") == entryTypeId || et.get("id") == entryTypeId) {
					out.push({
						id: et.get("id"),
						descr: et.get("text")
					});
				}
			}

			this.add(out);
		}

	});

	function getCheckColumnConfig(me, header, dataIndex) {
		return {
			xtype: 'checkcolumn',
			header: header,
			align: "center",
			dataIndex: dataIndex,
			width: 90,
			fixed: true,
			menuDisabled: true,
			hideable: false,
			listeners: {
				checkchange: function(column, recordIndex, checked) {
					var r = me.store.getAt(recordIndex);
					me.callDelegates("onCMDomainGridCheckedColumn", [me, this, checked, r]);
				}
			}
		};
	}
})();