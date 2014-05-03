(function() {

	Ext.define("CMDBuild.state.CMWorkflowStateDelegate", {
		onProcessClassRefChange: Ext.emptyFn,
		onProcessInstanceChange: Ext.emptyFn,
		onActivityInstanceChange: Ext.emptyFn
	});

	Ext.define("CMDBuild.model.CMActivityInstance", {
		constructor: function(data) {
			if (data) {
				this.data = data;
			} else {
				this.data = {};
				this.nullObject = true;
			}
		},

		isNew: function() {
			if (this.nullObject) {
				return false;
			} else {
				return (this.data.id  == null || typeof this.data.id == "undefined");
			}
		},

		getId: function() {
			return this.data.id;
		},

		getVariables: function() {
			return this.data.variables || [];
		},

		getPerformerName: function() {
			return this.data.performerName || "";
		},

		getDescription: function() {
			return this.data.description || "";
		},

		getInstructions: function() {
			return this.data.instructions || "";
		},

		getWidgets: function() {
			return this.data.widgets || [];
		},

		isWritable: function() {
			if (this.nullObject) {
				return false;
			}
			return this.data.writable || false;
		},

		isNullObject: function() {
			return this.nullObject;
		}
	});

	Ext.define("CMDBuild.model.CMProcessInstance", {
		extend: "Ext.data.Model",

		fields: [
			"beginDate",
			"beginDateAsLong",
			"classDescription",
			"className",
			"endDate",
			"flowStatus",
			{name: "id", type: "integer"},
			{name: "classId", type: "integer"},
			{name: "values", type: "auto"},
			{name: "activityInstanceInfoList", type: "auto"}
		],

		STATE: {
			OPEN: "OPEN",
			SUSPENDED: "SUSPENDED",
			COMPLETED: "COMPLETED",
			TERMINATED: "TERMINATED",
			ABORTED: "ABORTED",
			UNSUPPORTED: "UNSUPPORTED"
		},

		// override to backward compatibility
		// with the old serialization
		get: function(key) {
			var out;
			if (key == "Id") {
				out = this.getId();
			} else if (key == "IdClass") {
				out = this.getClassId();
			} else if (key == "IdClass_value") {
				out = this.getClassDescription();
			} else {
				out = this.callParent(arguments);
				if (!out) {
					// try in the values
					var values = this.data.values || {};
					out = values[key];
				}
			}

			return out;
		},
 
		getActivityInfoList: function() {
			return this.get("activityInstanceInfoList") || [];
		},

		getId: function() {
			return this.get("id") || null;
		},

		isNew: function() {
			return this.getId() == null;
		},

		getValues: function() {
			return this.get("values") || {};
		},

		getClassId: function() {
			return this.get("classId") || null;
		},

		getClassDescription: function() {
			return this.get("classDescription");
		},

		applyValues: function(values) {
			if (values) {
				this.data.values = Ext.apply(this.getValues(), values);
			}
		},

		getFlowStatus: function() {
			return this.get("flowStatus");
		},

		isStateOpen: function() {
			return this.getFlowStatus() == this.STATE.OPEN;
		},

		isStateCompleted: function() {
			return this.getFlowStatus() == this.STATE.COMPLETED;
		},

		isStateSuspended: function() {
			return this.getFlowStatus() == this.STATE.SUSPENDED;
		},

		setNotes: function(notes) {
			this.data.values.Notes = notes;
		},

		updateBeginDate: function(data) {
			if (data.beginDate && data.beginDateAsLong) {
				this.set("beginDate", data.beginDate);
				this.set("beginDateAsLong", data.beginDateAsLong);
			}
		},

		asDummyModel: function() {
			var values = this.getValues();
			// add the old serialization data that
			// could be called in the template resolver
			values.Id = this.getId();
			values.IdClass = this.getClassId();
			values.IdClass_value = this.getClassDescription();

			return new CMDBuild.DummyModel(values);
		}
	});

	Ext.define("CMDBuild.state.CMWorkflowState", {
		constructor: function() {
			var processClassRef = null,
				activityInstance = null,
				processInstance = null,
				delegates = [];

			Ext.apply(this, {
				setProcessClassRef: function(pcr, danglingCard) {
					if (pcr && (processClassRef !== pcr || danglingCard)) {
						processClassRef = pcr;
						this.notifyToDelegates("onProcessClassRefChange", [pcr, danglingCard]);

						this.setProcessInstance(new CMDBuild.model.CMProcessInstance({
							classId: processClassRef.getId()
						}));
					}
				},

				getProcessClassRef: function() {
					return processClassRef;
				},

				setProcessInstance: function(pi, cb) {
					processInstance = pi;

					var processClassRefIsASuperclass = (processClassRef 
							&& processClassRef.isSuperClass());

					var me = this;

					if (!processInstance.isNew() && processClassRefIsASuperclass) {
						CMDBuild.ServiceProxy.card.get({
							params: {
								Id: processInstance.getId(),
								IdClass: processInstance.getClassId()
							},
							success: function(a,b, response) {
								processInstance.applyValues(response.card);
								onProcessInstanceChange();
							}
						});
					} else {
						onProcessInstanceChange();
					}

					function onProcessInstanceChange() {
						// set the current CMActivityInstance to a empty activity
						me.setActivityInstance(new CMDBuild.model.CMActivityInstance());
						me.notifyToDelegates("onProcessInstanceChange", [processInstance]);
						if (cb) {
							cb();
						}
					}
				},

				getProcessInstance: function() {
					return processInstance;
				},

				setActivityInstance: function(ai) {
					activityInstance = ai;
					this.notifyToDelegates("onActivityInstanceChange", [ai]);
				},

				getActivityInstance: function() {
					return activityInstance;
				},

				addDelegate: function(delegate) {
					CMDBuild.validateInterface(delegate, "CMDBuild.state.CMWorkflowStateDelegate");
					delegates.push(delegate);
				},

				countDelegates: function() { // for test
					return delegates.length;
				},

				notifyToDelegates: function(method, args) {
					for (var i=0, l=delegates.length, d=null; i<l; ++i) {
						d = delegates[i];
						if (d && typeof d[method] == "function") {
							d[method].apply(d, args);
						}
					}
				}
			});
		}
	});

	// Define a global variable of
	_CMWFState = new CMDBuild.state.CMWorkflowState();
})();