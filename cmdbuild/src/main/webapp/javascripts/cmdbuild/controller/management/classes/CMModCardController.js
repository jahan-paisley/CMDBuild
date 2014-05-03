(function() {
	Ext.define("CMDBuild.controller.management.common.CMModController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		mixins: {
			commonFunctions: "CMDBuild.controller.management.common.CMModClassAndWFCommons",
			observable: "Ext.util.Observable"
		},

		constructor: function() {
			this.callParent(arguments);
			this.buildSubControllers();
		},

		onViewOnFront: function(entryType) {
			if (entryType) {
				var currentEntryType = _CMCardModuleState.entryType;
				var newEntryId = entryType.get("id");
				var filter = entryType.get(_CMProxy.parameter.FILTER);
				var dc = _CMMainViewportController.getDanglingCard();
				var entryIdChanged = currentEntryType ? (currentEntryType.get("id") != newEntryId) : true;

				// if there is a danglingCard do the same things that happen
				// when select a new entryType, the cardGridController is able to
				// manage the dc and open it.
//				if (entryIdChanged || dc || filter) {
					this.setEntryType(newEntryId, dc, filter);
//				}
			}
		},

		onCardSelected: function onCardSelected(card) {
			this.setCard(card);
		},

		setEntryType: function(entryTypeId, dc, filter) {
			this.entryType = _CMCache.getEntryTypeById(entryTypeId);
			this.setCard(null);
			this.callForSubControllers("onEntryTypeSelected", [this.entryType, dc, filter]);

			if (dc != null) {
				if (dc.activateFirstTab) {
					this.view.activateFirstTab();
				}
			}
		},

		getEntryType: function() {
			return this.entryType || null;
		},

		getEntryTypeId: function() {
			var id = null;
			if (this.entryType) {
				id = this.entryType.get("id");
			}

			return id;
		},

		setCard: function(card) {
			this.card = card;
			this.onCardChanged(card);
		},

		getCard: function() {
			return this.card;
		},

		// private, called from setCard. Implement different
		// behaviours in subclasses
		onCardChanged: function(card) {
			this.callForSubControllers("onCardSelected", this.card);
		},

		// private, call a given function for all the subcontrolles, and
		// pass the arguments to them.
		callForSubControllers: function(fnName, params) {
			for (var i=0, l = this.subControllers.length, ct=null; i<l; ++i) {
				ct = this.subControllers[i];
				if (typeof fnName == "string" 
					&& typeof ct[fnName] == "function") {

					params = Ext.isArray(params) ? params : [params];
					ct[fnName].apply(ct, params);
				}
			}
		},

		// protected
		buildSubControllers: function() {}
	});

	Ext.define("CMDBuild.controller.management.classes.CMModCardController", {
		extend: "CMDBuild.controller.management.common.CMModController",

		constructor: function() {
			this.callParent(arguments);
			this.mon(this.view, this.view.CMEVENTS.addButtonClick, onAddCardButtonClick, this);
		},

		// override
		buildSubControllers: function() {
			var me = this;
			me.subControllers = [];
			Ext.suspendLayouts();
			buildCardPanelController(me, me.view.getCardPanel());
			buildGridController(me, me.view.getGrid());
			buildRelationsController(me, me.view.getRelationsPanel());
			buildMapController(me);
			buildMDController(me, me.view.getMDPanel());
			buildNoteController(me, me.view.getNotePanel());
			buildAttachmentsController(me, me.view.getAttachmentsPanel());
			buildHistoryController(me, me.view.getHistoryPanel());
			buildBimController(me, me.view.getGrid());
			Ext.resumeLayouts();
		},

		// override: bind the CMCardModuleState
		setEntryType: function(entryTypeId, dc, filter) {
			var entryType = _CMCache.getEntryTypeById(entryTypeId);

			this.view.addCardButton.updateForEntry(entryType);
			this.view.mapAddCardButton.updateForEntry(entryType);
			this.view.updateTitleForEntry(entryType);

			if (dc != null) {
				if (dc.activateFirstTab) {
					this.view.activateFirstTab();
				}
			}

			_CMCardModuleState.setEntryType(entryType, dc, filter);
			_CMUIState.onlyGridIfFullScreen();
			this.changeClassUIConfigurationForGroup(entryTypeId);
		},

		changeClassUIConfigurationForGroup: function(classId) {
			var me = this;
			CMDBuild.ServiceProxy.group.loadClassUiConfiguration({
				params: {
					groupId: "",
					classId: classId
				},
				success: function(operation, config, response) {
					var disabledForGroupButtons = Ext.JSON.decode(response.response);
					me.view.addCardButton.disabledForGroup = disabledForGroupButtons.create;
					if (me.view.addCardButton.disabledForGroup)
						me.view.addCardButton.disable();
					else
						me.view.addCardButton.enable();
					me.cardPanelController.changeClassUIConfigurationForGroup(disabledForGroupButtons);
				}
			});
		},
		
		onGridVisible: function onCardGridVisible(visible, selection) {
			if (visible 
					&& this.entryType
					&& this.card) {

				if (selection 
					&& selection[0] && selection[0].get("Id") != this.card.get("Id")) {
						this.gridController.openCard({
							IdClass: this.entryType.get("id"),
							Id: this.card.get("Id")
						}, retryWithoutFilter = true);
				}
			}
		},

		onGridLoad: function(args) {
			// TODO notify to sub-controllers ?
			// args[1] is the array with the loaded records
			// so, if there are no records clear the view
			if (args[1] && args[1].length == 0) {
				this.view.getCardPanel().displayMode();
			}
		}
	});

	function buildCardPanelController(me, cardPanel) {
		var widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerController(me.view.getWidgetManager());
		if (cardPanel) {
			me.cardPanelController = new CMDBuild.controller.management.classes.CMCardPanelController(cardPanel, me, widgetControllerManager);

			me.mon(me.cardPanelController, me.cardPanelController.CMEVENTS.cardRemoved,
				function(idCard, idClass) {
					var et = _CMCardModuleState.entryType;
					me.gridController.onCardDeleted();
					me.view.reset(et.get("id")); // TODO change to notify the sub-controllers
					_CMCache.onClassContentChanged(idClass);
				});

			me.mon(me.cardPanelController, me.cardPanelController.CMEVENTS.cardSaved,
				function(cardData) {
					var et = _CMCardModuleState.entryType;
					me.gridController.onCardSaved(cardData);
					me.mapController.onCardSaved(cardData);
					_CMCache.onClassContentChanged(et.get("id"));
			});

			me.mon(me.cardPanelController, me.cardPanelController.CMEVENTS.editModeDidAcitvate, function() {
				me.mapController.editMode();
			}, me);

			me.mon(me.cardPanelController, me.cardPanelController.CMEVENTS.displayModeDidActivate, function() {
				me.mapController.displayMode();
			}, me);

			me.mon(me.cardPanelController, me.cardPanelController.CMEVENTS.cloneCard, onCloneCard, me);

			me.subControllers.push(me.cardPanelController);
		}
	}

	function buildGridController(me, grid) {
		if (grid) {
			me.gridController = new CMDBuild.controller.management.common.CMCardGridController(grid);
			me.mon(me.gridController, me.gridController.CMEVENTS.cardSelected, me.onCardSelected, me);
			me.mon(me.gridController, me.gridController.CMEVENTS.wrongSelection, onSelectionWentWrong, me);
			me.mon(me.gridController, me.gridController.CMEVENTS.gridVisible, me.onGridVisible, me);
			me.mon(me.gridController, me.gridController.CMEVENTS.load, me.onGridLoad, me);
			me.mon(me.gridController, me.gridController.CMEVENTS.itemdblclick, function() {
				me.cardPanelController.onModifyCardClick();
				_CMUIState.onlyFormIfFullScreen();
			}, me);

			me.subControllers.push(me.gridController);
		}
	}

	function buildRelationsController(me, view) {

		if (view == null) {return;}

		me.relationsController = new CMDBuild.controller.management.classes.CMCardRelationsController(view, me);
		me.mon(me.relationsController, me.relationsController.CMEVENTS.serverOperationSuccess, function() {
			me.gridController.reload(reselect=true);
		});

		me.subControllers.push(me.relationsController);
	}

	function buildMDController(me, view) {

		if (view == null) {return;}

		me.mdController = new CMDBuild.controller.management.classes.masterDetails.CMMasterDetailsController(view, me);
		me.mon(me.mdController, "empty", function(isVisible) {
			if (isVisible) {
				me.view.cardTabPanel.activateFirstTab();
			}
		});
		me.subControllers.push(me.mdController);
	}

	function buildMapController(me) {
		if (typeof me.view.getMapPanel == "function") {
			me.mapController = new CMDBuild.controller.management.classes.CMMapController(me.view.getMapPanel(), me);
		} else {
			me.mapController = {
				onEntryTypeSelected: Ext.emptyFn,
				onAddCardButtonClick: Ext.emptyFn,
				onCardSaved: Ext.emptyFn,
				getValues: function() {return false;},
				refresh: Ext.emptyFn,
				editMode: Ext.emptyFn,
				displayMode: Ext.emptyFn
			};
		}

		me.subControllers.push(me.mapController);
		me.cardPanelController.addCardDataProviders(me.mapController);
	}

	function buildNoteController(me, view) {

		if (view == null) {return;}

		me.noteController = new CMDBuild.controller.management.classes.CMNoteController(view);
		me.mon(me.noteController, me.noteController.CMEVENTS.noteWasSaved, function(card) {
			if (me.cardHistoryPanelController) {
				me.cardHistoryPanelController.onCardSelected(card);
			}
		}, me);
		me.subControllers.push(me.noteController);
	}

	function buildAttachmentsController(me, view) {

		if (view == null) {return;}

		me.attachmentsController = new CMDBuild.controller.management.classes.attachments.CMCardAttachmentsController(view, me);
		me.subControllers.push(me.attachmentsController);
	}

	function buildHistoryController(me, view) {

		if (view == null) {return;}

		me.cardHistoryPanelController = new CMDBuild.controller.management.classes.CMCardHistoryPanelController(view);
		me.subControllers.push(me.cardHistoryPanelController);
	}

	function buildBimController(me, view) {
		if (view == null) {return;}

		if (CMDBuild.Config.bim.enabled) {
			new CMDBuild.bim.management.CMBimController(view);
		}
	}

	function onSelectionWentWrong() {
		this.view.cardTabPanel.reset(_CMCardModuleState.entryType.get("id"));
	}

	function onAddCardButtonClick(p) {
		this.setCard(null);
		this.callForSubControllers("onAddCardButtonClick", p.classId);
		this.view.activateFirstTab();

		_CMUIState.onlyFormIfFullScreen();
	}

	function onCloneCard() {
		this.callForSubControllers("onCloneCard");
	}
})();