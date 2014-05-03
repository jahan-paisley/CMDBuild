(function() {

	Ext.define("CMDBuild.controller.administration.user.CMModUserController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.grid = this.view.userGrid;
			this.form = this.view.userForm;
			this.currentUser = null;
			this.sm = this.grid.getSelectionModel();

			this.view.addUserButton.on("click", onAddUserClick, this);
			this.form.saveButton.on("click", onSaveButtonClick, this);
			this.form.abortButton.on("click", onAbortButtonClick, this);
			this.form.disableUser.on("click", onDisableUserButtonClick, this);
			this.sm.on('selectionchange', onRowSelected , this);
		},

		onViewOnFront: function(selection) {

		}
	});

	function onAddUserClick() {
		this.currentUser = null;
		this.form.onAddUserClick();
		this.sm.deselectAll();
	}

	function onSaveButtonClick() {
		var nonvalid = this.form.getNonValidFields();
		if (nonvalid.length > 0) {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			return;
		}

		var params = this.form.getData();
		//if I'm not passing the "username" and the "description" I'm changing the password
		//and i need to know the value of the "isactive" field to not set it to false anyway

		if (params.description === undefined && params.username === undefined) {
			params['isActive'] = this.currentUser.get("isActive");
		}

		if (this.currentUser != null) {
			params["userid"] = this.currentUser.get("userid");
		} else {
			params["userid"] = -1;
		}

		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modsecurity/saveuser',
			params : params,
			scope : this,
			success : success,
			callback : callback
		});
	}

	function onAbortButtonClick() {
		if (this.currentUser != null) {
			onRowSelected.call(this, null, [this.currentUser]);
		} else {
			this.form.reset();
			this.form.disableModify();
		}
	}

	function onRowSelected(sm, selection) {
		if (selection.length > 0) {
			this.currentUser = selection[0];
			this.form.onUserSelected(this.currentUser);
		}
	}

	function onDisableUserButtonClick() {
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			method : 'POST',
			url : 'services/json/schema/modsecurity/disableuser',
			params: {
				userid: this.currentUser.get("userid"),
				disable: this.currentUser.get("isActive")
			},
			scope: this,
			success : success,
			callback: callback
		});
	}

	function success(result, options, decodedResult) {
		var userid= decodedResult.rows.userid;
		var s = this.grid.store;
		s.load({
			scope: this,
			callback: function(records, operation, success) {
				var rec = s.find("userid", userid);
				this.sm.select(rec);
			}
		});
		this.form.disableModify();
	}

	function callback() {
		CMDBuild.LoadMask.get().hide();
	}
})();