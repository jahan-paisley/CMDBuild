(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMNavigationTreeController", {
		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMNavigationTree.WIDGET_NAME
		},

		constructor: function(view, ownerController, widgetDef, clientForm, card) {
			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);
			this.ownerController = ownerController;
			this.navigationTree = widgetDef.navigationTreeName;
			this.view = view;
			this.view.delegate = this;

		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onTreeSelected':
					this.view.configureForm(this.navigationTree, param.tree);
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
		
		// override
		beforeActiveView: function() {
			var me = this;
			_CMCache.readNavigationTrees(this, this.navigationTree, selectTree);
		},

		// override
		getData: function() {
			var out = null;
			if (!this.readOnly) {
				out = {};
				out["output"] = this.view.getData();
			}

			return out;
		},

		destroy: function() {
			this.callParent(arguments);
		}
	});
	function selectTree(me, name) {
		me.cmOn("onTreeSelected", { tree: name });
	}
})();
