(function() {

	Ext.define('CMDBuild.controller.management.common.filter.CMRelationsController', {
		extend: 'CMDBuild.controller.management.common.CMCardGridController',

		mixins: {
			cardGridDelegate: 'CMDBuild.view.management.common.CMCardGridDelegate'
		},

		constructor: function() {
			this.callParent(arguments);
			this.view.addDelegate(this);
		},

		// override
		buildStateDelegate: function() {
			// This subclass does not refer to the global state
		},

		onCardSelected: function(sm, selection) {
			// Do nothig
		},

		// override
		getEntryType: function() {
			var entryTypeId = this.supercontroller.currentDomain.getDestination().getId();

			return _CMCache.getEntryTypeById(entryTypeId);
		},

		// cardGridDelegate

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		// override
		onCMCardGridSelect: function(grid, record) {
			this.supercontroller.currentDomain.addCheckedCard(getCardInfoFromRecord(record));
		},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		// override
		onCMCardGridDeselect: function(grid, record) {
			if (this.supercontroller.ignoreDeselect) {
				return;
			} else {
				this.supercontroller.currentDomain.removeCheckedCard(getCardInfoFromRecord(record));
			}
		},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		// override
		onCMCardGridBeforeLoad: function(grid) {
			this.supercontroller.ignoreDeselect = true;
		},

		/**
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		// override
		onCMCardGridLoad: function(grid) {
			this.supercontroller.ignoreDeselect = false;

			var keepExistingSelection = true;
			var checkedCards = this.supercontroller.currentDomain.getCheckedCards();

			for (var i = 0, l = checkedCards.length; i < l; ++i) {
				var cardInfo = checkedCards[i];
				var recordIndex = grid.store.findBy(function(record) {
					return cardInfo.className == _CMCache.getEntryTypeNameById(record.get('IdClass')) && cardInfo.id == record.get('Id');
				});

				if (recordIndex >= 0)
					grid.getSelectionModel().select(recordIndex, keepExistingSelection);
			}
		}
	});

	Ext.define('CMDBuild.view.management.common.filter.CMRelations', {
		extend: 'Ext.panel.Panel',

		title: CMDBuild.Translation.management.findfilter.relations,

		mixins: {
			domainGridDelegate: 'CMDBuild.view.management.common.filter.CMDomainGridDelegate'
		},

		// configuration
			className: undefined,
		// configuration

		initComponent: function() {
			/*
			 * A flag needed because the ExtJs grid fires the 'deselect' event before to load the store. We want capture only
			 * the user generated events. I haven't found a different solution
			 */
			this.ignoreDeselect = false;

			this.domainGrid = Ext.create('CMDBuild.view.management.common.filter.CMDomainGrid', {
				border: false,
				className: this.className,
				cls: 'cmborderbottom',
				region: 'center'
			});
			this.domainGrid.addDelegate(this);

			this.cardGrid = Ext.create('CMDBuild.view.management.common.CMCardGrid', {
				border: false,
				split: true,
				height: '70%',
				cls: 'cmbordertop',
				frame: false,
				multiSelect: true,
				selType: 'checkboxmodel',
				region: 'south',
				disabled: true
			});

			new CMDBuild.controller.management.common.filter.CMRelationsController(this.cardGrid, this);

			this.layout = 'border';
			this.items = [this.domainGrid, this.cardGrid];

			this.callParent(arguments);
		},

		getData: function() {
			var data = [];

			this.domainGrid.store.each(function(domain) {
				var type = domain.getType();

				if (type != null) {
					var domainFilterConfiguration = {
						domain: domain.getDomain().getName(),
						type: type,
						destination: domain.getDestination().getName(),
						source: domain.getSource().getName(),
						direction: domain.getDirection()
					};

					if (type == 'oneof') {
						domainFilterConfiguration.cards = domain.getCheckedCards();
					}

					data.push(domainFilterConfiguration);
				}
			});

			return data;
		},

		/**
		 * @param {array of object} data
		 * data -> [{
		 * 		domain: {string} name of the domain
		 * 		source: {string} name of entryType source of the domain
		 * 		destination: {string} name of the entryType destinatin of the domain
		 * 		type: {string} any | noone | oneof,
		 * 		cards: {array} array of objects {Id: id of a card, ClassName: name of the card's class}
		 * }]
		 */
		setData: function(data) {
			var me = this;
			var domains = data || [];

			this.domainGrid.load(function() {
				for (var i = 0, l = domains.length; i < l; ++i) {
					var domainRecord = null;
					var domain = domains[i];
					var recordIndex = me.domainGrid.store.findBy(function(record) {
						return record.hasName(domain.domain) && record.getDirection() == domain.direction;
					});

					if (recordIndex >= 0)
						domainRecord = me.domainGrid.store.getAt(recordIndex);

					if (domainRecord) {
						domainRecord.setType(domain.type);
						domainRecord.setCheckedCards(domain.cards);
					}
				}
			});
		},

		// as domainGridDelegate

		/**
		 * @param {CMDBuild.view.management.common.filter.CMDomainGrid} grid
		 * @param {CMDBuild.model.CMDomainGridModel} record
		 */
		onCMDomainGridSelect: function(grid, record) {
			this.currentDomain = record;
			loadRelationGrid(this, this.currentDomain.getDestination().getId());
		},


		/**
		 * @param {CMDBuild.view.management.common.filter.CMDomainGrid} grid
		 * @param {Ext.ux.CheckColumn} column
		 * @param {boolean} checked
		 * @param {CMDBuild.model.CMDomainGridModel} record
		 */
		onCMDomainGridCheckedColumn: function(grid, column, checked, record) {
			// the fields 'any' and 'noone' and 'oneof' are mutual exclusive
			if (checked)
				record.setType(column.dataIndex);

			// The grid must be enabled only if the current domain has oneof as type
			if (this.currentDomain) {
				var currentDomain = this.currentDomain.getDomain();
				var recordDomain = record.getDomain();
				var theChangeHappensOnCurrentDomain = currentDomain.getName() == recordDomain.getName();

				if (theChangeHappensOnCurrentDomain) {
					var mustBeEnabled = column.dataIndex == 'oneof' && checked;

					this.cardGrid.setDisabled(!mustBeEnabled);
				}
			}
		},

		/**
		 * @param {CMDBuild.view.management.common.filter.CMDomainGrid} grid
		 * @param {string/int} entryTypeId the id of the destination subclass
		 */
		onCMDomainGridDestinationClassChange: function(grid, entryTypeId) {
			loadRelationGrid(this, entryTypeId);
		}

	});

	function loadRelationGrid(me, entryTypeId) {
		var sm = me.cardGrid.getSelectionModel();

		if (sm)
			sm.clearSelections();

		me.cardGrid.updateStoreForClassId(entryTypeId);
		var oneof = me.currentDomain.get('oneof');
		me.cardGrid.setDisabled(!oneof);

		if (typeof me.currentDomain.cards == 'undefined')
			me.currentDomain.cards = {};
	}

	function getCardInfoFromRecord(record) {
		return {
			className: _CMCache.getEntryTypeNameById(record.get('IdClass')),
			id: record.get('Id')
		};
	}

})();