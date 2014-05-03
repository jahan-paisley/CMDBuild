(function() {

	GET_PROCESS_INSTANCE_URL = "services/json/workflow/getprocessinstancelist";

	var queryTypology = Ext.create('Ext.data.Store', {
	    fields: ['name', 'description'],
	    data : [
	        {"name":"name", "description": CMDBuild.Translation.workflow_by_name},
	        {"name":"cql", "description": CMDBuild.Translation.workflow_by_cql}
	    ]
	});
	
	Ext.define("CMDBuild.view.administration.widget.form.CMWorkflowDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".Workflow"
		},

		initComponent: function() {
			this.callParent(arguments);

			this.addEvents(
				/* fired when is set the workflow in the combo-box*/
				"cm-selected-workflow"
			);

			var me = this;
			this.mon(this.workflowId, "select", function(field, records) {
				me.fireEvent("cm-selected-workflow", records);
			}, this.workflowId);

		},

		// override
		buildForm: function() {
			var me = this;
			var workflowsStore = buildWorkflowsStore();
			this.callParent(arguments);
			
			this.queryTypology = Ext.create('Ext.form.ComboBox', {
			    fieldLabel: CMDBuild.Translation.workflow_query_tipology,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
			    store: queryTypology,
			    queryMode: 'local',
			    displayField: 'description',
			    valueField: 'name',
			    autoSelect: true,
			    name: 'filterType',
			    listeners:{
			         scope: me,
			         'select': function(item, param) {
			        	 switch (param[0].get("name")) {
			        	 case "cql" :
			        		 setFilterFields(this);
			        		 break;
			        	 case "name" :
			        		 setNameFields(this);
			        		 break;
			        	 }
			         }
			    }
			});
			this.queryTypology.select("name");
			this.workflowFilter = new Ext.form.field.TextArea({
				name: "filter",
				fieldLabel: CMDBuild.Translation.workflow_cql_filter,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.CFG_BIG_FIELD_WIDTH,
				hidden: true
			});

			this.workflowId = new Ext.form.field.ComboBox({
				name: "code",
				fieldLabel: CMDBuild.Translation.workflow,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField: 'id',
				displayField: 'description',
				store: workflowsStore
			});


			this.presetGrid = new CMDBuild.view.administration.common.CMKeyValueGrid({
				title: CMDBuild.Translation.workflow_attributes,
				keyLabel: CMDBuild.Translation.attribute,
				valueLabel: CMDBuild.Translation.value,
				margin: "0 0 0 3"
			});

			// defaultFields is inherited
			this.defaultFields.add(this.queryTypology, this.workflowFilter, this.workflowId);

			Ext.apply(this, {
				layout: {
					type: "hbox"
				},
				items: [this.defaultFields, this.presetGrid]
			});
		},

		fillPresetWithData: function(data) {
			this.presetGrid.fillWithData(data);
		},

		// override
		fillWithModel: function(model) {
			this.callParent(arguments);
			var name = model.get("workflowName");
			var filter = model.get("filter");
			if (name) {
				this.queryTypology.select("name");
				setNameFields(this)
				var card = _CMCache.getEntryTypeByName(name);
				if ( card && card.data) {
					this.workflowId.setValue(parseInt(card.data.id));
				}
	
				this.fillPresetWithData(model.get("preset"));
			}
			else if (filter) {
				setFilterFields(this)
				this.queryTypology.select("cql");
				this.workflowFilter.setValue(filter);
			}
		},

		// override
		disableNonFieldElements: function() {
			this.presetGrid.disable();
		},

		// override
		enableNonFieldElements: function() {
			this.presetGrid.enable();
		},

		// override
		getWidgetDefinition: function() {
			var me = this;
			var queryType = this.queryTypology.getValue();	
			switch (queryType) {
				case "cql":
					return Ext.apply(me.callParent(arguments), {
						filter: me.workflowFilter.getValue(),
						filterType: queryType
					});
				default:
					return Ext.apply(me.callParent(arguments), {
						workflowName: _CMCache.getEntryTypeNameById(me.workflowId.getValue()),
						preset: me.presetGrid.getData()
					});
			}
		}
	});
	function buildWorkflowsStore() {
		var processes = _CMCache.getProcesses();
		var data = [];
		for (var key in processes) {
		   var obj = processes[key];
		   if (obj.raw.superclass)
			   	continue;
		   data.push({
			   id: obj.raw.id,
			   description: obj.raw.text
		   });
		}
		var workflows = Ext.create('Ext.data.Store', {
		    fields: ['id', 'description'],
		    data : data,
		    autoLoad: true
		});
		return workflows;
	}
	function setFilterFields(me) {
		me.workflowFilter.show();
		me.workflowId.hide();
		me.presetGrid.hide();
		me.workflowFilter.enable();
	}
	function setNameFields(me) {
		me.workflowFilter.hide();
		me.workflowId.show();
		me.presetGrid.show();
		me.workflowId.enable();
		me.presetGrid.enable();
	}
})();