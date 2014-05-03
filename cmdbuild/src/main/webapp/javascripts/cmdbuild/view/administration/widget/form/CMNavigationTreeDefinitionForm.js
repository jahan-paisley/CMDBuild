(function() {

	Ext.define("CMDBuild.view.administration.widget.form.CMNavigationTreeDefinitionForm", {
		extend: "CMDBuild.view.administration.widget.form.CMBaseWidgetDefinitionForm",

		statics: {
			WIDGET_NAME: ".NavigationTree"
		},

		// override
		buildForm: function() {
			var navigationTreesStore = buildNavigationTreesStore();
			this.callParent(arguments);

			this.navigationTreeName = new Ext.form.field.ComboBox({
				name: "navigationTreeName",
				fieldLabel: CMDBuild.Translation.tree_navigation, 
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField: 'name',
				displayField: 'description',
				store: navigationTreesStore
			});


/*			this.presetGrid = new CMDBuild.view.administration.common.CMKeyValueGrid({
				title: "@@ Navigation tree attributes",
				keyLabel: CMDBuild.Translation.attribute,
				valueLabel: CMDBuild.Translation.value,
				margin: "0 0 0 3"
			});
*/
			// defaultFields is inherited
			this.defaultFields.add(this.navigationTreeName);

			Ext.apply(this, {
				layout: {
					type: "hbox"
				},
				items: [this.defaultFields]
			});
		},

/*		fillPresetWithData: function(data) {
			this.presetGrid.fillWithData(data);
		},

*/		// override
		fillWithModel: function(model) {
			this.callParent(arguments);
			var name = model.get("navigationTreeName");
			this.navigationTreeName.setValue(name);
		},

/*		// override
		disableNonFieldElements: function() {
			this.presetGrid.disable();
		},

*//*		// override
		enableNonFieldElements: function() {
			this.presetGrid.enable();
		},

*/		// override
		getWidgetDefinition: function() {
			var me = this;
			return Ext.apply(me.callParent(arguments), {
				navigationTreeName: this.navigationTreeName.getValue()
			});
		}
	});
	function buildNavigationTreesStore() {
		var navigationTrees = _CMCache.getNavigationTrees();
		var data = [];

		for (var i = 0; i < navigationTrees.data.length; i++) {
			var obj = navigationTrees.data[i];
			data.push({
				name: obj.name,
				description: obj.description
			});
		}
		var navigationTrees = Ext.create('Ext.data.Store', {
		    fields: ['name', 'description'],
		    data : data,
		    autoLoad: true,
		    sorters: [{
		        sorterFn: function(o1, o2){
		            return o1.get('description') <  o2.get('description') ? -1 : 1;
		        }
		    }],
		});
		return navigationTrees;
	}
})();