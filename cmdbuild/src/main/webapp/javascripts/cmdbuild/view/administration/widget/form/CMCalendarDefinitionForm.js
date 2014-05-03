(function() {
	Ext.define("CMDBuild.view.administration.widget.form.CMCalendarDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".Calendar"
		},

		// override
		buildForm: function() {
			var me = this,
				widgetName = this.self.WIDGET_NAME,
				tr = CMDBuild.Translation.administration.modClass.widgets[widgetName];

			this.callParent(arguments);

			this.targetClass = new CMDBuild.field.ErasableCombo({
				fieldLabel : tr.fields.target,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'eventClass',
				valueField : 'name',
				displayField : 'description',
				editable : false,
				store : _CMCache.getClassesAndProcessesStore(),
				queryMode : 'local'
			});

			this.startDate = new CMDBuild.field.ErasableCombo({
				fieldLabel : tr.fields.start,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'startDate',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				store : new Ext.data.Store({
					fields: ["id", "description"],
					data: []
				}),
				queryMode : 'local'
			});

			this.endDate = new CMDBuild.field.ErasableCombo({
				fieldLabel : tr.fields.end,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'endDate',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				store : new Ext.data.Store({
					fields: ["id", "description"],
					data: []
				}),
				queryMode : 'local'
			});

			this.defaultDate = new CMDBuild.field.ErasableCombo({
				fieldLabel : tr.fields.defaultDate,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'defaultDate',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				store : new Ext.data.Store({
					fields: ["id", "description"],
					data: []
				}),
				queryMode : 'local'
			});

			this.eventTitle = new CMDBuild.field.ErasableCombo({
				fieldLabel : tr.fields.title,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : 'eventTitle',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				store : new Ext.data.Store({
					fields: ["id", "description"],
					data: []
				}),
				queryMode : 'local'
			});

			// defaultFields is inherited
			this.defaultFields.add(this.targetClass, this.startDate, this.endDate, this.defaultDate, this.eventTitle);

			this.filter = new Ext.form.field.TextArea({
				fieldLabel: tr.fields.filter,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: "filter",
				flex: 1
			});

			this.filterContainer = new Ext.panel.Panel({
				layout: "hbox",
				frame: true,
				border: true,
				items: [this.filter],
				margin: "0 0 0 3",
				flex: 1
			});

			Ext.apply(this, {
				layout: {
					type: "hbox"
				},
				items: [this.defaultFields, this.filterContainer]
			});
		},

		// override
		fillWithModel: function(model) {
			this.callParent(arguments);
			this.targetClass.setValue(model.get("eventClass"));
			this.startDate.setValue(model.get("startDate"));
			this.endDate.setValue(model.get("endDate"));
			this.eventTitle.setValue(model.get("eventTitle"));
			this.filter.setValue(model.get("filter"));
			this.defaultDate.setValue(model.get("defaultDate"));
		},

		//override
		getWidgetDefinition: function() {
			var me = this;

			return Ext.apply(me.callParent(arguments), {
				eventClass: me.targetClass.getValue(),
				startDate: me.startDate.getValue(),
				endDate: me.endDate.getValue(),
				eventTitle: me.eventTitle.getValue(),
				filter: me.filter.getValue(),
				defaultDate: me.defaultDate.getValue()
			});
		}
	});
})();