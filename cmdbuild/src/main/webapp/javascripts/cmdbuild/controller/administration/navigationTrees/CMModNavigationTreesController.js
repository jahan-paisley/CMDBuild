(function() {
	Ext.ns("CMDBuild.administration.navigationTrees");
	var ns = CMDBuild.administration.navigationTrees;
	
	Ext.define("CMDBuild.controller.administration.navigationTrees.CMModNavigationTreesController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		constructor: function() {
			this.callParent(arguments);
			this.tree = null;
			this.view.delegate = this;
			this.formController = new CMDBuild.controller.administration.navigationTrees.CMNavigationTreesFormController(this.view.navigationTreesForm);
			this.formController.parentDelegate = this;
			this.treeController = new CMDBuild.controller.administration.navigationTrees.CMNavigationTreesTreeController(this.view.navigationTreesTree);
			this.treeController.parentDelegate = this;
			this.isNew = false;
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick':
					_CMMainViewportController.deselectAccordionByName("navigationTrees");
					this.view.selectPropertiesTab();
					this.view.navigationTreesTree.disable();
					_CMCache.initModifyingTranslations();
					this.view.navigationTreesForm.enableModify(true);
					this.view.navigationTreesTree.enableModify();
					this.formController.cmOn("onNew");
					this.isNew = true;
					break;
				case 'onModifyButtonClick':
					_CMCache.initModifyingTranslations();
					this.view.navigationTreesForm.enableModify();
					this.view.navigationTreesTree.enableModify();
					this.isNew = false;
					break;
				case 'onAbortButtonClick':
					this.view.navigationTreesTree.enable();
					this.view.navigationTreesForm.disableModify(enableCMTBar = true);
					this.view.navigationTreesTree.disableModify(enableCMTBar = true);
					break;
				case 'onDeleteButtonClick':
					var formData = this.formController.getData();
					if (formData.name) {
						this.remove(formData.name);
					}
					break;
				case 'onSaveButtonClick':
					var formData = this.formController.getData();
					if (formData.error) {
						break;
					}
					this.view.navigationTreesTree.enable();
					this.view.navigationTreesForm.disableModify(enableCMTBar = true);
					this.view.navigationTreesTree.disableModify(enableCMTBar = true);
					if (this.isNew) {
						this.create(formData);
						this.view.navigationTreesTree.disable();
						this.formController.cmOn("onNew");
					}
					else {
						this.save(formData);
					}
					break;
				case 'onTreeSelected':
					this.view.navigationTreesTree.enable();
					this.view.navigationTreesForm.disableModify(enableCMTBar = true);
					this.view.navigationTreesTree.disableModify(enableCMTBar = true);
					this.formController.cmOn("onTreeSelected", param);
					this.treeController.cmOn("onTreeSelected", param);
					this.view.setTitleSuffix(param.tree.type);
					break;

				default: {
					if (
						this.parentDelegate
						&& typeof this.parentDelegate === 'object'
					) {
						return this.parentDelegate.cmOn(name, param, callBack);
					}
				}
			}
			return undefined;
		},
		
		save: function(formData) {
			var treeData = this.treeController.getData();
			var structure = {
					targetClassName:formData.rootName,
					targetClassDescription: formData.description,
					filter: treeData.filter,
					childNodes: treeData.children
			};
			formData.structure = Ext.encode(structure);
			_CMCache.saveNavigationTrees(formData, function() {
			});
		},
		
		create: function(formData) {
			var structure = {
					targetClassName:formData.rootName,
					targetClassDescription: formData.description,
					childNodes: []
			};
			formData.structure = Ext.encode(structure);
			_CMCache.createNavigationTrees(formData, function() {
			});
		},
		
		remove: function(name) {
			if (! name) {
				return;
			}
			Ext.Msg.show({
				title: "Remove tree " + name,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == "yes") {
						_CMCache.removeNavigationTrees(name);
					}
				}
			});
		},

		onViewOnFront: function(selection) {
			if (selection) {
				_CMCache.readNavigationTrees(this, selection.get("id"), selectTree);
			}
		}
		

	});
	function selectTree(me, name) {
		me.cmOn("onTreeSelected", { tree: name });
	}
	
})();