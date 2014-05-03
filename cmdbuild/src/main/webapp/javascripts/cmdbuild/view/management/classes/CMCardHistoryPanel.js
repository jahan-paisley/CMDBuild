(function() {

	var col_tr = CMDBuild.Translation.management.modcard.history_columns;

	Ext.define("CMDBuild.view.management.classes.CMCardHistoryTab", {
		extend: "Ext.grid.Panel",

		eventtype: 'card',
		eventmastertype: 'class',

		cls: "history_panel",

		constructor: function() {
			this.currentTemplate = null;
			this.autoScroll = true;

			Ext.apply(this, {
				plugins: [{
					ptype: "rowexpander",
					rowBodyTpl: "ROW EXPANDER REQUIRES THIS TO BE DEFINED",
					getRowBodyFeatureData: function(record, idx, rowValues) {
						Ext.grid.plugin.RowExpander.prototype.getRowBodyFeatureData.apply(this, arguments);
						rowValues.rowBody  = genHistoryBody(record);
					},
					expanderWidth: 18
				}],
				columns: this.getGridColumns(),
				store: new Ext.data.JsonStore({
					proxy : {
						type : 'ajax',
						url: 'services/json/management/modcard/getcardhistory',
						reader : {
							type : 'json',
							root : 'rows'
						}
					},
					sorters : [
						{property : 'BeginDate', direction : "DESC"},
						{property : '_EndDate', direction : "DESC"}
					],
					fields: this.getStoreFields(),
					baseParams: {
						IsProcess: (this.eventmastertype == 'processclass')
					}
				})
			});

			this.callParent(arguments);
			this.view.on("expandbody", function() {
				this.doLayout(); // to refresh the scrollbar status
			}, this);
		},

		getGridColumns: function() {
			var c = [
				{header: col_tr.begin_date,  width: 180, fixed: true, sortable: false, dataIndex: 'BeginDate', renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'), flex:1},
				{header: col_tr.end_date,  width: 180, fixed: true, sortable: false, dataIndex: 'EndDate', renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'), flex:1},
				{header: col_tr.user, width: 20, sortable: false, dataIndex: 'User', flex:1}
			];

			if (this.isFullVersion()) {
				c = c.concat([
					{header: col_tr.attributes, width: 60, fixed: true, sortable: false, renderer: tickRenderer, dataIndex: '_AttrHist', align: 'center', tdCls: 'grid-button', flex:1},
					{header: col_tr.relation, width: 60, fixed: true, sortable: false, renderer: tickRenderer, dataIndex: '_RelHist', align: 'center', tdCls: 'grid-button', flex:1},
					{header: col_tr.domain, width: 20, sortable: false, dataIndex: 'DomainDesc', flex:1},
					{header: col_tr.description, width: 40, sortable: false, dataIndex: 'CardDescription', flex:1}
				]);
			};

			return c;
		},

		isFullVersion: function() {
			return !_CMUIConfiguration.isSimpleHistoryModeForCard();
		},

		getStoreFields: function() {
			return [
				{name:'BeginDate', type:'date', dateFormat:'d/m/Y H:i:s'},
				{name:'EndDate', type:'date', dateFormat:'d/m/Y H:i:s'},
				{name:'_EndDate', type:'int'}, // For sorting only
				'User',
				'_AttrHist',
				'_RelHist',
				'DomainDesc',
				'Class',
				'CardCode',
				'CardDescription'
			];
		},

		reset: function() {
			this.getStore().removeAll();
		},

		tabIsActive: tabIsActive,



		// DEPRECATED

		reloadCard: function() { _deprecated();
			this.enable();
			this.loaded = false;
			this.loadCardHistory();
		},

		loadCardHistory: function() { _deprecated();
			if (this.loaded
					|| !this.currentClassId
					|| !this.currentCardId) {
				return;
			}
	
			var params = {};
			params[_CMProxy.parameter.CARD_ID] = this.currentCardId;
			params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.currentClassId);

			this.getStore().load({
				params: params
			});
	
			this.loaded = true;
		},

		onAddCardButtonClick: function() { _deprecated();
			this.disable();
		},

		onClassSelected: function(classId) { _deprecated();
			if (this.currentClassId != classId) {
				this.currentClassId = classId;
				this.disable();
			}
	
		},
	
		onCardSelected: function(card) { _deprecated();
			var et = _CMCache.getEntryTypeById(card.get("IdClass"));
			if (et && et.get("tableType") == CMDBuild.Constants.cachedTableType.simpletable) {
				this.disable();
			} else {
				this.currentCardId = card.raw.Id;
				this.currentClassId = card.raw.IdClass;
	
				this.currentCardPrivileges = {
					create: card.raw.priv_create,
					write: card.raw.priv_write
				};
	
				// FIXME The workflow does not call onAddCardButtonClick()
				var existingCard = (this.currentCardId > 0);
				this.setDisabled(!existingCard);
	
				if (tabIsActive(this)) {
					this.reloadCard();
				} else {
					this.on("activate", this.reloadCard, this);
				}
			}
		}
	});

	function genHistoryBody(record) {
		var body = '';
		if (record.raw['_RelHist']) {
			body += historyAttribute(col_tr.domain, record.raw['DomainDesc'])
				+ historyAttribute(col_tr.destclass, record.raw['Class'])
				+ historyAttribute(col_tr.code, record.raw['CardCode'])
				+ historyAttribute(col_tr.description, record.raw['CardDescription']);
		}

		for (var a = record.raw['Attr'], i=0, l=a.length; i<l ;++i) {
			var ai = a[i];
			var label = ai.d;
			var changed = ai.c;
			var value = ai.v;

			if (typeof value == "undefined" || value == null) {
				value = "";
			}

			body += historyAttribute(label, value, changed);
		}

		return body;
	}

	function historyAttribute(label, value, changed) {
		var cls = changed ? " class=\"changed\"" : "";
		return "<p"+cls+"><b>"+label+"</b>: "+((value || {}).dsc || value)+"</p>";
	};

	function tabIsActive(t) {
		return t.ownerCt.layout.getActiveItem().id == t.id;
	}

	function tickRenderer(value) {
		if (value) {
			return '<img style="cursor:pointer" src="images/icons/tick.png"/>&nbsp;';
		} else {
			return '&nbsp;';
		}
	}
})();
