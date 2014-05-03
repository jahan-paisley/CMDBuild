(function() {

	Ext.define("CMDBuild.controller.administration.navigationTrees.CMNavigationTreesFormController", {
		constructor: function(view) {
			this.view = view;
			view.delegate = this;
			this.currentTree = null;
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onNew':
					this.view.onNew();
					break;
				case 'onTreeSelected':
					this.currentTree = param.tree;
					this.view.onTreeSelected(param.tree);
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
		
		getData: function() {
			return this.view.getData();
		}
	});

})();