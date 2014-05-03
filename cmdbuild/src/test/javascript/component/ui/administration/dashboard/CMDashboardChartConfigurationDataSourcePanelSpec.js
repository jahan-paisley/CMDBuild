(function() {
	var view,
		realDataSourceStoreFunction;

	describe('CMDashboardChartConfigurationDataSourcePanel', function() {

		beforeEach(function() {
			view = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationDataSourcePanel({
				renderTo: Ext.getBody(),
				afterComboValueChanged: jasmine.createSpy("afterComboValueChanged")
			});

			realDataSourceStoreFunction = _CMCache.getAvailableDataSourcesStore;

			_CMCache.getAvailableDataSourcesStore = function() {
				return new Ext.data.SimpleStore({
					model : "CMDBuild.model.CMChartDataSource",
					data : [{
						name: "cm_datasource_1",
						input: [{
							name: "in11",type: "STRING"
						},{
							name: "in12",type: "INTEGER"
						},{
							name: "in13", type: "DATE"
						}],
						output: [{
							name: "out11", type: "INTEGER"
						},{
							name: "out12", type: "STRING"
						},{
							name: "out13", type: "DATE"
						}]
					}]
				});
			};
		});

		afterEach(function() {
			delete view;
			_CMCache.getAvailableDataSourcesStore = realDataSourceStoreFunction;
		});

		it ('call the afterComboValueChanged when the value of the dataSourceCombo change', function() {
			view.dataSourceCombo.setValue("cm_datasource_1");
			expect(view.afterComboValueChanged).toHaveBeenCalled();
		});

	});

	/*
	 * CMDBuild.view.administration.dashboard._DataSourceInputFildSet
	 */

	var dsFieldSet;

	describe('_DataSourceInputFildSet', function() {
		beforeEach(function() {
			dsFieldSet = new CMDBuild.view.administration.dashboard._DataSourceInputFildSet({
				input: {
					name: "Foo",
					type: "INTEGER"
				},
				typeComboIsdisabled: Ext.emptyFn,
				fieldTypeStore: getFakeStore(),
				afterInputFieldTypeChanged: Ext.emptyFn
			});
		});

		afterEach(function() {
			delete dsFieldSet;
		});

		it ('could add a combo af classes for the default', function() {
			var realGetClassesAndProcessesStore = _CMCache.getClassesAndProcessesStore;
			_CMCache.getClassesAndProcessesStore = getFakeStore;

			expect(dsFieldSet.defaultField).not.toBeDefined();
			dsFieldSet.addClassesFieldForDefault();
			expect(Ext.getClassName(dsFieldSet.defaultField)).toBe("CMDBuild.field.ErasableCombo");
			expect(dsFieldSet.defaultField.store.cmType).toBe("fake_store");

			_CMCache.getClassesAndProcessesStore = realGetClassesAndProcessesStore;
		});

		it ('could add a combo to choose a lookup type', function() {
			var realGetLookupTypeAsStore = _CMCache.getLookupTypeAsStore;
			_CMCache.getLookupTypeAsStore = getFakeStore;

			expect(dsFieldSet.lookupTypeField).not.toBeDefined();
			dsFieldSet.addLookupTypesField();
			expect(Ext.getClassName(dsFieldSet.lookupTypeField)).toBe("CMDBuild.field.ErasableCombo");
			expect(dsFieldSet.lookupTypeField.store.cmType).toBe("fake_store");

			_CMCache.getLookupTypeAsStore = realGetLookupTypeAsStore;
		});

		it ('is able to set and get the data', function() {
			dsFieldSet.addDefaultFieldFromFieldManager();
			dsFieldSet.setData({
				fieldType: "free",
				defaultValue: "234"
			});

			var data = dsFieldSet.getData();
			expect(data.name).toBe("Foo");
			expect(data.type).toBe("INTEGER");
			expect(data.fieldType).toBe("free");
			expect(data.defaultValue).toBe("234");
		});
	});

	function getFakeStore() {
		return new Ext.data.SimpleStore({
			cmType : "fake_store"
		});
	}

	function getDSParameters() {
		return [{
			name: "in1",
			type: "string"
		}, {
			name: "in2",
			type: "integer"
		}, {
			name: "in3",
			type: "date"
		}];
	}

})();