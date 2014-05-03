(function() {

var tr = CMDBuild.Translation.administration.modsecurity;

Ext.define("CMDBuild.view.administration.group.CMGroupUsers", {
	extend: "Ext.panel.Panel",
	alias: "userpergroup",

	initComponent: function() {

		this.availableUsersStore = CMDBuild.ServiceProxy.group.getUserPerGroupStoreForGrid();
		this.assignedUsersStore = CMDBuild.ServiceProxy.group.getUserPerGroupStoreForGrid();

		var availableUsersGrid =  new Ext.grid.Panel({
			store: this.availableUsersStore,
			columns: [{
				header: tr.user.username,
				dataIndex: 'username',
				flex: 1
			},{
				header: tr.user.description,
				dataIndex: 'description',
				flex: 1
			}],
			viewConfig: {
				plugins: {
					ptype: 'gridviewdragdrop',
					dragGroup: 'firstGridDDGroup',
					dropGroup: 'secondGridDDGroup'
				},
				listeners: {
					drop: function(node, data, dropRec, dropPosition) {
						var dropOn = dropRec ? ' ' + dropPosition + ' ' + dropRec.get('name') : ' on empty view';
						_debug("Drag from right to left", 'Dropped ' + data.records[0].get('name') + dropOn);
					}
				}
			},
			title: tr.group.availableusers,
			flex: 1
		});

		var assignedUsersGrid =  new Ext.grid.Panel({
			margins: "0 0 0 5",
			store: this.assignedUsersStore,
			viewConfig: {
				plugins: {
					ptype: 'gridviewdragdrop',
					dragGroup: 'secondGridDDGroup',
					dropGroup: 'firstGridDDGroup'
				},
				listeners: {
					drop: function(node, data, dropRec, dropPosition) {
						var dropOn = dropRec ? ' ' + dropPosition + ' ' + dropRec.get('name') : ' on empty view';
						_debug("Drag from left to right", 'Dropped ' + data.records[0].get('name') + dropOn);
					}
				}
			},
			columns: [{
				header: tr.user.username,
				dataIndex: 'username',
				flex: 1
			},{
				header: tr.user.description,
				dataIndex: 'description',
				flex: 1
			}],
			title: tr.group.groupchoice,
			flex: 1
		});

		var _this = this;

		var concatUsersIdOfAssignedUsers = function() {
			var out = "";
			var users = _this.assignedUsersStore.getRange();
			for (var i=0, len=users.length; i<len ; ++i) {
				if (i > 0) {
					out = out.concat(",");
				}
				var u = users[i];
				out = out.concat(u.get("userid"));
			}
			return out;
		};

		var saveGroupUsers = function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.Ajax.request({
				url : 'services/json/schema/modsecurity/savegroupuserlist',
				params:{
					groupId: this.groupId,
					users: concatUsersIdOfAssignedUsers()
				},
				method : 'POST',
				scope : this,
				success : function(response, options, decoded) {
					CMDBuild.LoadMask.get().hide();
					CMDBuild.Msg.success(arguments);
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		};

		Ext.apply(this, {
			frame: false,
			border: false,
			bodyCls: "x-panel-body-default-framed",
			cls: "x-panel-body-default-framed",
			padding: "0 0 0 0",
			layout: {
				type: 'hbox',
				padding:'5',
				align:'stretch'
			},
			items: [availableUsersGrid,assignedUsersGrid],
			buttonAlign: "center",
			buttons: [{
				text: CMDBuild.Translation.common.buttons.confirm,
				scope: this,
				handler: saveGroupUsers
			}]
		});
		this.callParent(arguments);
	},

	onGroupSelected: function(group) {
		if (group) {
			this.groupId = group.get("id");
		} else {
			this.groupId=-1;
		}

		if (this.groupId > 0) {
			this.enable();
			this.availableUsersStore.load({
				params:{
					groupId:this.groupId,
					alreadyAssociated: false
				}
			});
			this.assignedUsersStore.load({
				params:{
					groupId:this.groupId,
					alreadyAssociated: true
				}
			});
		}
	}
});

})();