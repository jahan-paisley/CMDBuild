(function() {
	var STATE = "state",
		STATE_VALUE_OPEN = "open.running",
		STATE_VALUE_SUSPENDED = "open.not_running.suspended",
		STATE_VALUE_COMPLETED = "closed.completed",
		STATE_VALUE_ABORTED = "closed.aborted",
		STATE_VALUE_ALL = "all", // Not existent

		GET_PROCESS_INSTANCE_URL = "services/json/workflow/getprocessinstancelist",

		tr = CMDBuild.Translation.management.modworkflow;

	Ext.define("CMDBuild.view.management.workflow.CMActivityGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmStoreUrl: GET_PROCESS_INSTANCE_URL,

		constructor: function() {

			this.statusCombo = new  Ext.form.field.ComboBox({
				store: buildProcessStateStore(),
				name : "state",
				hiddenName : "state",
				valueField : "code",
				displayField : "description",
				queryMode: "local",
				allowBlank : false,
				editable: false,
				value : STATE_VALUE_OPEN,
				isStateOpen: function() {
					return this.getValue() == STATE_VALUE_OPEN;
				}
			});

			this.addCardButton = new CMDBuild.AddCardMenuButton({
				classId: undefined,
				baseText: tr.add_card,
				textPrefix: tr.add_card
			});

			this.tbar = [this.addCardButton, this.statusCombo];

			this.plugins = [{
				ptype: "activityrowexpander"
			}];

			this.callParent(arguments);
		},

		setStatusToOpen: function() {
			this.setStatus(STATE_VALUE_OPEN);
		},

		setStatus: function(value) {
			this.statusCombo.setValue(value);
			this.updateStatusParamInStoreProxyConfiguration();
		},

		updateStatusParamInStoreProxyConfiguration: function() {
			this.store.proxy.extraParams[STATE] = this.statusCombo.getValue();
		},

		getStoreExtraParams: function() {
			var ep = this.callParent(arguments);
			ep[STATE] = this.statusCombo.getValue();

			return ep;
		},

		// override
		buildStore: function(fields, pageSize) {
			return new Ext.data.Store({
				model: CMDBuild.model.CMProcessInstance,
				pageSize: pageSize,
				remoteSort: true,
				proxy: {
					type: "ajax",
					url: this.cmStoreUrl,
					reader: {
						type: "json",
						root: "response.rows",
						totalProperty: "response.results",
						idProperty: "id"
					},
					extraParams: this.getStoreExtraParams()
				},
				autoLoad: false
			});
		},

		// called by the activityrowexpander plugin
		// when an activity is selected with a mouse click
		onActivitySelected: function(activityInstanceId) {
			_debug("Activity selection", activityInstanceId);
			this.fireEvent("activityInstaceSelect", activityInstanceId);
		},

		// override
		buildClassColumn: function() {
			var cl = this.callParent();
			cl.renderer = function(value, metadata, record, rowIndex, colIndex, store, view) {
				return record.get("classDescription");
			};

			return cl;
		}
	});

	function buildProcessStateStore() {
		var tr = CMDBuild.Translation.management.modworkflow.statuses,
			store = Ext.create('Ext.data.ArrayStore', {
				autoDestroy: true,
				fields: [ "code", "description" ],
				data: [
					[STATE_VALUE_OPEN, tr[STATE_VALUE_OPEN]],
					[STATE_VALUE_SUSPENDED, tr[STATE_VALUE_SUSPENDED]],
					[STATE_VALUE_COMPLETED, tr[STATE_VALUE_COMPLETED]],
					[STATE_VALUE_ABORTED, tr[STATE_VALUE_ABORTED]],
					[STATE_VALUE_ALL, tr[STATE_VALUE_ALL]]
				]
			});

		return store;
	}
})();