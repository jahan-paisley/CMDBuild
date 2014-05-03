(function() {
	Ext.define("CMDBuild.controller.management.common.CMStandAloneCardGridController", {
		extend: "CMDBuild.controller.management.common.CMCardGridController",

		// override
		buildStateDelegate: function() {
			// Do nothing
			// This kind of cardGrid is not binded to the Card module State
		},

		// override
		onEntryTypeSelected: function(entryType, danglingCard) {
			var me = this;
			if (!entryType) {
				return;
			}

			this.entryType = entryType;
			this.unApplyFilter(this);
			this.view.filterMenuButton.reconfigureForEntryType(entryType);

			me.view.updateStoreForClassId(entryType.get("id"), {
				cb: function cbUpdateStoreForClassId() {
					me.view.loadPage(1);
				}
			});
		},

		// override
		getEntryType: function() {
			return this.entryType;
		}
	});

	Ext.define("CMDBuild.controller.management.utilities.CMModBulkUpdateController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		constructor: function() {
			this.callParent(arguments);

			this.cardGridController = new CMDBuild.controller.management.common.CMStandAloneCardGridController(this.view.cardGrid);
			this.gridSM = this.view.cardGrid.getSelectionModel();
			this.treeSM = this.view.classTree.getSelectionModel();

			this.treeSM.on("selectionchange", onClassTreeSelected, this);

			this.view.saveButton.on("click", onSaveButtonClick, this);
			this.view.abortButton.on("click", onAbortButtonClick, this);

		},

		onViewOnFront: function() {
			var s = this.treeSM.getSelection();
			if (s.length == 0) {
				this.treeSM.select(0);
			}
		}
	});

	function onSaveButtonClick() {
		var cardToModifyMSG = "<p>" + CMDBuild.Translation.management.modutilities.bulkupdate.countMessage + ".</p>",
			me = this;

		CMDBuild.LoadMask.get().show();
		getProxyCall(me)({
			params: builSaveParams(me),
			success: function(response, request, decordedResp) {
				var msg = "";
				if (me.gridSM.cmReverse) {
					// Add the info that will be modified only the card in the filter
					msg += "<p>" + CMDBuild.Translation.warnings.only_filtered + ".</p>";
				}

				msg += Ext.String.format(cardToModifyMSG, decordedResp.count);
				showWarningMsg(me, msg);
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function getProxyCall(me) {
		var proxyCall = _CMProxy.card.bulkUpdate;
		var reverseMode = me.gridSM.cmReverse;
		if (reverseMode) {
			proxyCall = _CMProxy.card.bulkUpdateFromFilter;
		}

		return proxyCall;
	}

	function showWarningMsg(me, msg) {
		Ext.Msg.show({
			title: CMDBuild.Translation.warnings.warning_message,
			msg: msg,
			buttons: Ext.Msg.OKCANCEL,
			icon: Ext.MessageBox.WARNING,
			fn: function(button) {
				if (button == "ok") {
					save(me);
				}
			}
		});
	}

	function save(me) {
		var reverse = me.gridSM.cmReverse;
		var selections = me.gridSM.hasSelection();
		if (!reverse && !selections) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>",
					CMDBuild.Constants.css.error_msg,
					CMDBuild.Translation.errors.no_selections);

			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, msg, false);
			return;
		}

		var params = builSaveParams(me);
		params[_CMProxy.parameter.CONFIRMED] = true;

		CMDBuild.LoadMask.get().show();
		getProxyCall(me)({
			params: params,
			success: function(response, request, decordedResp) {
				me.view.cardGrid.reload(reselect = false);
				onAbortButtonClick.call(me);
			},
			callback: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function onAbortButtonClick() {
		this.gridSM.reset();
		this.view.cardForm.reset();

		this.view.cardGrid.reload(reselect = false);
	}

	function onClassTreeSelected(sm, selection) {
		if (selection.length > 0) {
			var node = selection[0],
				selectable = false;

			try {
				selectable = (node.raw.cmData.type == "class");
			} catch (e) {
				// the folder has not cmData
			}

			if (selectable) {
				this.currentClassId = node.get("id");
				this.view.onClassTreeSelected(this.currentClassId);
				this.cardGridController.onEntryTypeSelected(_CMCache.getEntryTypeById(this.currentClassId));
			}
		}
	}

	function builSaveParams(me) {
		var parameterNames = _CMProxy.parameter;
		var params = me.view.cardForm.getCheckedValues(); // the attributes
		params[parameterNames.CARDS] = formatSelections(me);

		if (me.gridSM.cmReverse) {
			params[parameterNames.FILTER] = me.view.getFilter();
			params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.currentClassId);
		}

		return params;
	}

	function formatSelections(me) {
		var selectedCards = me.gridSM.getSelection();
		var out = [];
		for (var i=0, l=selectedCards.length; i<l; i++) {
			var card = selectedCards[i];
			var outCard = {};
			outCard[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(card.get("IdClass"));
			outCard[_CMProxy.parameter.CARD_ID] = card.get("Id");
			out.push(outCard);
		}

		return Ext.encode(out);
	}
})();