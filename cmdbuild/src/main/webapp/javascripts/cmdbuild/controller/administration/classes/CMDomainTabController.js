(function() {
	Ext.define("CMDBuild.controller.administration.classes.CMDomainTabController", {
		constructor: function(view) {
			this.view = view;
			this.selection = null;
			
			this.view.on("itemdblclick", onItemDoubleClick, this);
			this.view.getSelectionModel().on("selectionchange", onSelectionChange, this);
			this.view.addDomainButton.on("click", onAddDomainButton, this);
			this.view.modifyButton.on("click", onModifyDomainButton, this);
			this.view.deleteButton.on("click", onDeleteDomainButton, this);
		},

		onClassSelected: function(classId) {
			this.selection = classId;
			var entryTypeData = _CMCache.getEntryTypeById(classId).data;
			if (entryTypeData.tableType == "simpletable") {
				this.view.disable();
				return;
			}

			var view = this.view;
			var params = {};
			params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(classId);

			CMDBuild.LoadMask.get().show();
			view.store.load({
				params: params,
				callback: function() {
					CMDBuild.LoadMask.get().hide();
					view.filterInherited(view.filtering);
				}
			});

			view.enable();
			view.modifyButton.disable();
			view.deleteButton.disable();
		},

		onAddClassButtonClick: function() {
			this.selection = null;
			this.view.disable();
		}

	});
	
	function onSelectionChange(sm, selection) {
		if (selection.length > 0) {
			this.currentDomain = selection[0];
			this.view.modifyButton.enable();
			this.view.deleteButton.enable();
		}
	}

	function onItemDoubleClick(grid, record) {
		var domainAccordion = _CMMainViewportController.findAccordionByCMName("domain");
		domainAccordion.expand();
		Ext.Function.createDelayed(function() {
			domainAccordion.selectNodeById(record.get("idDomain"));
		}, 100)();
		
	}

	function onModifyDomainButton() {
		if (this.currentDomain) {
			onItemDoubleClick(this.view, this.currentDomain);
			Ext.Function.createDelayed(function() {
				_CMMainViewportController.panelControllers["domain"].view.domainForm.enableModify();
			}, 500)();
		}
	}
	
	function onDeleteDomainButton() {
		Ext.Msg.show({
			title: CMDBuild.Translation.administration.modClass.domainProperties.delete_domain,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					deleteDomain.call(this);
				}
			}
		});
	}

	function deleteDomain() {
		if (this.currentDomain == null) {
			// nothing to delete
			return;
		}

		var me = this;
		var params = {};
		params[_CMProxy.parameter.DOMAIN_NAME] = this.currentDomain.get("name");

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.administration.domain.remove({
			params: params,
			success : function(form, action) {
				me.onClassSelected(me.selection);
				_CMCache.onDomainDeleted(me.currentDomain.get("idDomain"));
				me.currentDomain = null;
			},
			callback : function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}
	
	function onAddDomainButton() {
		var domainAccordion = _CMMainViewportController.accordionControllers["domain"];
		if (domainAccordion) {
			domainAccordion.expandForAdd();
		}
	}
})();