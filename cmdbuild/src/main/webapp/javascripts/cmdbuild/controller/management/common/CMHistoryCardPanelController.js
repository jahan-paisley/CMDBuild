(function() {
	Ext.define("CMDBuild.controller.management.classes.CMCardHistoryPanelController", {
		extend: "CMDBuild.controller.management.classes.CMModCardSubController",

		onEntryTypeSelected: function(entryType) {
			this.callParent(arguments);
			this.view.disable();
		},

		onCardSelected: function(card) {
			this.callParent(arguments);

			if (card) {
				if (this.entryType.get("tableType") != CMDBuild.Constants.cachedTableType.simpletable) {
					var existingCard = (!!this.card);
					this.view.setDisabled(!existingCard);

					if (this.view.tabIsActive(this.view)) {
						this.load();
					} else {
						this.mon(this.view, "activate", this.load, this, {single: true});
					}
				} else {
					this.view.disable();
				}
			}
		},

		onAddCardButtonClick: function() {
			this.view.disable();
		},

		load: function() {
			var me = this;
			var params = {};
			params[_CMProxy.parameter.CARD_ID] = me.card.get("Id");
			params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.card.get("IdClass"));

			this.view.getStore().load({
				params: params
			});
		}
	});

	Ext.define("CMDBuild.controller.management.workflow.CMWorkflowHistoryPanelController", {

		extend: "CMDBuild.controller.management.classes.CMCardHistoryPanelController",

		mixins: {
			wfStateDelegate: "CMDBuild.state.CMWorkflowStateDelegate"
		},

		constructor: function() {
			this.callParent(arguments);
			_CMWFState.addDelegate(this);

			this.mon(this.view, "activate", function() {
				if (!this._loaded) {
					this.load();
				}
			}, this);
		},

		// wfStateDelegate
		onProcessInstanceChange: function(processInstance) {
			this._loaded = false;

			if (processInstance.isNew()) {
				this.view.disable();
			} else {
				this.view.enable();
				if (this.view.isVisible()) {
					this.load();
				}
			}

		},

		// override
		load: function() {
			this._loaded = true;
			var processInstance = _CMWFState.getProcessInstance();
			if (processInstance) {
				var params = {};
				params[_CMProxy.parameter.CARD_ID] = processInstance.get("id");
				params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(processInstance.get("classId"));

				this.view.getStore().load({
					params: params
				});
			}
		},

		// override
		buildCardModuleStateDelegate: Ext.emptyFn,
		onEntryTypeSelected: Ext.emptyFn,
		onCardSelected: Ext.emptyFn
	});
})();