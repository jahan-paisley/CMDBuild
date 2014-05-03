(function() {

	Ext.define("CMDBuild.controller.administration.group.CMGroupUIConfigurationController", {
		constructor: function(view) {
			this.view = view;
			this.currentGroup = null;

			this.view.saveButton.on("click", onSaveButtonClick, this);
			this.view.abortButton.on("click", onAbortButtonClick, this);
		},

		onGroupSelected: function(g) {
			this.currentGroup = g;
			this.currentUIConfiguration = null;

			// The CloudAdministrator could not change the UIConfiguration of
			// full administrator groups
			var currentGroup = _CMCache.getGroupById(CMDBuild.Runtime.DefaultGroupId);
			if (g == null || currentGroup.isCloudAdmin()
				&& g.isAdmin() && !g.isCloudAdmin()) {

				this.view.disable();
			} else {
				this.view.enable();
				if (this.view.isVisible()) {
					readConfiguration(this);
				} else {
					if (!this.view.hasListener("show")) {
						this.view.mon(this.view, "show", function() {
							readConfiguration(this);
						}, this);
					}
				}
			}
		}
	});

	function readConfiguration(me) {
		if (!me.currentGroup) {
			return;
		}

		if (me.currentUIConfiguration != null) {
			me.view.loadGroupConfiguration(me.currentUIConfiguration);
		} else {
			var gId = me.currentGroup.getId();
			CMDBuild.ServiceProxy.group.getGroupUIConfiguration(gId, {
				success: function(operatio, request, response) {
					me.currentUIConfiguration = new CMDBuild.model.CMUIConfigurationModel(response.response);
					me.view.loadGroupConfiguration(me.currentUIConfiguration);
				}
			});
		}
	}

	function onSaveButtonClick() {
		var uiConfiguration = this.view.getUIConfiguration().toString();
		var gId = this.currentGroup.getId();

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.group.saveUIConfiguration(gId, uiConfiguration, {
			callback: function(operatio, request, response) {
				CMDBuild.LoadMask.get().hide();
			}
		});
	}

	function onAbortButtonClick() {
		if (this.currentUIConfiguration != null) {
			this.view.loadGroupConfiguration(this.currentUIConfiguration);
		} else {
			this.view.reset();
		}
	}
})();