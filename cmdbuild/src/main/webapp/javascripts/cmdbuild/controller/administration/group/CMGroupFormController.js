(function() {

	Ext.define("CMDBuild.controller.administration.group.CMGroupFormController", {
		constructor: function(view) {
			this.view = view;
			this.currentGroup = null;

			this.view.saveButton.on("click", onSaveButtonClick, this);
			this.view.abortButton.on("click", onAbortButtonClick, this);
			this.view.enableGroupButton.on("click", onEnableGroupButtonClick, this);
			this.view.modifyButton.on("click", onModifyButtonClick, this);
		},

		onGroupSelected: function(g) {
			this.currentGroup = g;
			this.view.reset();
			if (g == null) {
				this.view.disableModify(enableTBar = false);
			} else {
				this.view.loadGroup(g);

				/*
				 * Business rule
				 * The cloud group could not edit a Full administrator Group
				 */
				var currentGroup = _CMCache.getGroupById(CMDBuild.Runtime.DefaultGroupId);
				var enableTBar = !(currentGroup.isCloudAdmin() && g.isAdmin() && !g.isCloudAdmin());
				this.view.disableModify(enableTBar);
			}
		},

		onAddGroupButtonClick: function() {
			this.currentGroup = null;
			this.view.reset();
			this.view.enableModify(all = true);
			this.view.setDefaults();
		}
	});

	function onSaveButtonClick() {
		var nonValid = this.view.getNonValidFields();
		if (nonValid.length == 0) {
			CMDBuild.ServiceProxy.group.save({
				scope : this,
				params : buildParamsForSave(this),
				success : function(r) {
					var g = Ext.JSON.decode(r.responseText).group;
					_CMCache.onGroupSaved(g);
				},
				failure : onAbortButtonClick
			});
		} else {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
		}
	}

	function onAbortButtonClick() {
		if (this.currentGroup != null) { 
			this.view.loadGroup(this.currentGroup);
		} else {
			this.view.reset();
		}
		this.view.disableModify();
	}

	function onEnableGroupButtonClick() {
		CMDBuild.Ajax.request({
			url : 'services/json/schema/modsecurity/enabledisablegroup',
			params : {
				groupId : this.currentGroup.get("id"),
				isActive : !this.currentGroup.get("isActive")
			},
			waitMsg : CMDBuild.Translation.common.wait_title,
			method : 'POST',
			scope : this,
			success : function(response, options, decoded) {
				var g = decoded.group;
				_CMCache.onGroupSaved(g);
			}
		});
	}

	function onModifyButtonClick() {
		this.view.enableModify();

		/*
		 * Business rule
		 * The cloud group could not edit the type of a group
		 */
		var currentGroup = _CMCache.getGroupById(CMDBuild.Runtime.DefaultGroupId);
		if (currentGroup.isCloudAdmin()) {
			this.view.groupType.disable();
		} else {
			this.view.groupType.enable();
		}
	}

	function buildParamsForSave(me) {
		var params = me.view.getData();

		if (me.currentGroup == null) {
			params.id = -1;
		} else {
			params.id = me.currentGroup.get("id");
		}

		return params;
	}
})();