(function() {
	var view,
		realDataSourceStoreFunction,
		delegate;

	describe('CMDashboardChartConfigurationForm', function() {

		beforeEach(function() {
			view = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationForm({
				renderTo: Ext.getBody()
			});

			delegate = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationFormDelegate();

			realDataSourceStoreFunction = _CMCache.getAvailableDataSourcesStore;

			_CMCache.getAvailableDataSourcesStore = function() {
				return new Ext.data.SimpleStore({
					model : "CMDBuild.model.CMChartDataSource",
					data : [{
						name: "cm_datasource_1",
						input: [{
							name: "in11",type: "integer"
						},{
							name: "in12",type: "string"
						},{
							name: "in13", type: "date"
						}],
						output: [{
							name: "out11", type: "integer"
						},{
							name: "out12", type: "string"
						},{
							name: "out13", type: "date"
						}]
					}]
				});
			};

			this.addMatchers({
				toBeEnabled : function(expected) {
					return !this.actual.disabled;
				},

				toBeHidden : function(expected) {
					return this.actual.getEl().dom.style.display == "none";
				}
			});
		});

		afterEach(function() {
			delete view;
			delete delegate;

			_CMCache.getAvailableDataSourcesStore = realDataSourceStoreFunction;
		});

		it('starts with all the fields disabled empty and the specific fields hidden', function() {
			expectAllTheFieldsAreEmpty();
			expectAllTheFieldsAreDisabled();
			expectSpecificFieldsHidden();

			expect(view.categoryAxesFieldSet).toBeHidden();
			expect(view.valueAxesFieldSet).toBeHidden();
		});

		it('is able to enable the visible fields', function() {
			view.enableFields();

			// only the fields that are always shown
			expect(view.nameField).toBeEnabled();
			expectAllMutableFieldsAreEnabled();
			expectAllTheOutputFieldsAreNotEnabled();

			// show also some field that are hidden
			view.disableFields();
			view.showFieldsWithName(["maximum", "minimum", "steps"]);
			view.enableFields();
			expectAllMutableFieldsAreEnabled();
			expect(view.maximumField).toBeEnabled();
			expect(view.minimumField).toBeEnabled();
			expect(view.stepsField).toBeEnabled();
			expect(view.fgColorField).not.toBeEnabled();
			expect(view.bgColorField).not.toBeEnabled();
			expect(view.singleSerieField).not.toBeEnabled();
			expect(view.labelField).not.toBeEnabled();
			expect(view.orientationField).not.toBeEnabled();
			expect(view.categoryAxesFieldSet.categoryAxesField).not.toBeEnabled();
			expect(view.categoryAxesFieldSet.categoryAxesLabel).not.toBeEnabled();
			expect(view.valueAxesFieldSet.valueAxesFields).not.toBeEnabled();
			expect(view.valueAxesFieldSet.valueAxesLabel).not.toBeEnabled();

			// show all the remaining fields
			view.disableFields();
			view.showFieldsWithName(["fgcolor", "bgcolor", "singleSeriesField", "labelField", "legend", "categoryAxisField",
				"categoryAxisLabel", "valueAxisFields", "valueAxisLabel", "chartOrientation"]);

			view.enableFields();
			expectAllTheOutputFieldsAreEnabled();
		});

		it('is able to enable only the mutable fields', function() {
			view.enableFields(onlyMutable=true);

			expectAllMutableFieldsAreEnabled();
			expect(view.nameField).not.toBeEnabled();
		});

		it('is able to disable the fields', function() {
			view.enableFields();
			view.disableFields();
	
			expectAllTheFieldsAreDisabled();
		});

		it('disables the fields when hides them', function() {
			view.showFieldsWithName(["maximum", "minimum", "steps", "fgcolor", "bgcolor", "singleSeriesField", "labelField", "legend", "chartOrientation"]);
			view.enableFields();
			view.hideFieldsWithName(["maximum", "minimum", "steps", "fgcolor", "bgcolor", "singleSeriesField", "labelField", "legend", "chartOrientation"]);
			expectAllTheOutputFieldsAreNotEnabled();
		});

		it('enable the fields when shows them only if the form is enabled', function() {
			view.enableFields();
			view.showFieldsWithName(["maximum", "minimum", "steps", "fgcolor",
				"bgcolor", "singleSeriesField", "labelField", "legend",
				"categoryAxisField", "categoryAxisLabel", "valueAxisFields", "valueAxisLabel", "chartOrientation"]);

			expectAllTheOutputFieldsAreEnabled();

			view.hideFieldsWithName(["maximum", "minimum", "steps", "fgcolor",
				"bgcolor", "singleSeriesField", "labelField", "legend",
				"categoryAxisField", "categoryAxisLabel", "valueAxisFields", "valueAxisLabel", "chartOrientation"]);

			view.disableFields();
			view.showFieldsWithName(["maximum", "minimum", "steps", "fgcolor",
				"bgcolor", "singleSerieField", "labelField", "legend",
				"categoryAxisField", "categoryAxisLabel", "valueAxisFields", "valueAxisLabel", "chartOrientation"]);

			expectAllTheOutputFieldsAreNotEnabled();
		});

		it('is able to fill the fields', function() {
			var data = {
				name: "Foo",
				description: "Bar",
				active: true,
				autoLoad: true,
				dataSourceName: "cm_datasource_1",
				type: "PIE",
				maximum: 100,
				minimum: 1,
				steps: 20,
				fgcolor: "#FFFFFF",
				bgcolor: "#FFFFFF"
			};

			view.fillFieldsWith(data);

			expect(view.nameField.getValue()).toEqual(data.name);
			expect(view.descriptionArea.getValue()).toEqual(data.description);
			expect(view.activeCheck.getValue()).toEqual(data.active);
			expect(view.autoLoadCheck.getValue()).toEqual(data.autoLoad);
			expect(view.dataSourcePanel.dataSourceCombo.getValue()).toEqual(data.dataSourceName);
			expect(view.typeField.getValue()).toEqual(data.type);
			expect(view.maximumField.getValue()).toEqual(data.maximum);
			expect(view.minimumField.getValue()).toEqual(data.minimum);
			expect(view.stepsField.getValue()).toEqual(data.steps);
			expect(view.fgColorField.getValue()).toEqual(data.fgcolor);
			expect(view.bgColorField.getValue()).toEqual(data.bgcolor);
		});

		it('is able to take the values from fields', function() {
			var data = {
				name: "Foo",
				description: "Bar",
				active: true,
				autoLoad: true,
				dataSourceName: "cm_datasource_1",
				dataSourceParameters: [],
				type: "pie",
				maximum: 100,
				minimum: 1,
				steps: 20,
				fgcolor: "#FFFFFF",
				bgcolor: "#FFFFFF",
				legend: true,
				singleSeriesField : null,
				labelField: null,
				chartOrientation: "vertical",
				categoryAxisField: null,
				categoryAxisLabel: 'Foo',
				valueAxisFields: [],
				valueAxisLabel: 'Bar'
			};

			view.fillFieldsWith(data);
			var out = view.getFieldsValue();

			expect(out.name).toEqual(data.name);
			expect(out.description).toEqual(data.description);
			expect(out.active).toEqual(data.active);
			expect(out.autoLoad).toEqual(data.autoLoad);
			expect(out.dataSourceName).toEqual(data.dataSourceName);
			expect(out.type).toEqual(data.type);
			expect(out.maximum).toEqual(data.maximum);
			expect(out.minimum).toEqual(data.minimum);
			expect(out.steps).toEqual(data.steps);
			expect(out.fgcolor).toEqual(data.fgcolor);
			expect(out.bgcolor).toEqual(data.bgcolor);
			expect(out.legend).toEqual(data.legend);
			expect(out.singleSeriesField).toEqual(data.singleSeriesField);
			expect(out.labelField).toEqual(data.labelField);
			expect(out.chartOrientation).toEqual(data.chartOrientation);
			expect(out.categoryAxisField).toEqual(data.categoryAxisField);
			expect(out.categoryAxisLabel).toEqual(data.categoryAxisLabel);
			expect(out.valueAxisFields).toEqual(data.valueAxisFields);
			expect(out.valueAxisLabel).toEqual(data.valueAxisLabel);
			expect(out.dataSourceParameters).toEqual(data.dataSourceParameters);
		});

		it('is able to reset the values of fields', function() {
			var data = {
				name: "Foo",
				description: "Bar",
				active: true,
				autoLoad: true,
				dataSourceName: "cm_datasource_1",
				type: "PIE",
				maximum: 100,
				minimum: 1,
				steps: 20,
				fgcolor: "#FFFFFF",
				bgcolor: "#FFFFFF"
			};

			view.fillFieldsWith(data);
			view.cleanFields();

			expectAllTheFieldsAreEmpty();
		});

		it('starts with the type specific fields hidden ', function() {
			expectSpecificFieldsHidden();
			expect(view.categoryAxesFieldSet).toBeHidden();
			expect(view.valueAxesFieldSet).toBeHidden();
		});

		it('is able to hide fields by name', function() {
			expect(view.nameField).not.toBeHidden();
			expect(view.descriptionArea).not.toBeHidden();
			expect(view.activeCheck).not.toBeHidden();
			expect(view.dataSourcePanel.dataSourceCombo).not.toBeHidden();

			view.hideFieldsWithName("name");
			view.hideFieldsWithName(["description", "active"]);

			expect(view.nameField).toBeHidden();
			expect(view.descriptionArea).toBeHidden();
			expect(view.activeCheck).toBeHidden();

			expect(view.dataSourcePanel.dataSourceCombo).not.toBeHidden();
		});

		it ('is able to hide the outPutConfigurationItem', function() {
			view.showFieldsWithName(["minimum", "maximum"]);
			view.hideOutputFields();

			expectSpecificFieldsHidden();
		});

		it ('is able to show fields by name', function () {
			view.hideFieldsWithName(["description", "active", "minimum"]);
			view.showFieldsWithName(["description", "active", "minimum"]);

			expect(view.nameField).not.toBeHidden();
			expect(view.descriptionArea).not.toBeHidden();
			expect(view.activeCheck).not.toBeHidden();
			expect(view.minimumField).not.toBeHidden();
			expect(view.stepsField).toBeHidden();
		});

		it ('is able to load the data for singleSerieField', function() {
			var availableFields = [['foo'],['bar']];
			var reset = spyOn(view.singleSerieField, "reset");

			expect(view.singleSerieField.store.data.length).toBe(0);
			view.setSingleSerieFieldAvailableData(availableFields);
			expect(view.singleSerieField.store.data.length).toBe(2);
			expect(reset).toHaveBeenCalled();
		});

		it ('is able to load the data for labelField', function() {
			var availableFields = [['foo'],['bar']];
			var reset = spyOn(view.labelField, "reset");

			expect(view.labelField.store.data.length).toBe(0);
			view.setLabelFieldAvailableData(availableFields);
			expect(view.labelField.store.data.length).toBe(2);
			expect(reset).toHaveBeenCalled();
		});

		// axes fieldSets

		it ('is able to show the axes fieldsets', function() {
			view.showAxesFieldSets();
			expect(view.categoryAxesFieldSet).not.toBeHidden();
			expect(view.valueAxesFieldSet).not.toBeHidden();
		});

		it ('is able to hide the axes fieldsets', function() {
			view.showAxesFieldSets();
			view.hideAxesFieldSets();

			expect(view.categoryAxesFieldSet).toBeHidden();
			expect(view.valueAxesFieldSet).toBeHidden();
		});

		it ('is able to set the available data for the category axes', function() {
			var field = view.categoryAxesFieldSet.categoryAxesField,
				availableFields = [['foo'],['bar']],
				reset = spyOn(field, "reset");

			expect(field.store.data.length).toBe(0);
			view.setCategoryAxesAvailableData(availableFields);

			expect(field.store.data.length).toBe(2);
			expect(reset).toHaveBeenCalled();
		});

		it ('is able to set the available data for the value axes', function() {
			var field = view.valueAxesFieldSet.valueAxesFields,
				availableFields = [['foo'],['bar']],
				reset = spyOn(field, "reset");

			expect(field.store.data.length).toBe(0);
			view.setValueAxesAvailableData(availableFields);

			expect(field.store.data.length).toBe(2);
			expect(reset).toHaveBeenCalled();
		});

		// delegate

		it('throw exception if pass to setDelegate a non conform object', function() {
			delegate = new Object();
			assertException("The view must throw exception for non conform object on setDelegate",
				function() {
					view.setDelegate(delegate);
				});
		});

		it('is able to set the delegate', function() {
			expect(view.delegate).toBeUndefined();
			view.setDelegate(delegate);
			expect(view.delegate).toEqual(delegate);
		});

		it('call the delegate when change the type of chart', function() {
			var onTypeChanged = spyOn(delegate, "onTypeChanged");
			view.setDelegate(delegate);
			view.fillFieldsWith({
				type: "gauge"
			});

			expect(onTypeChanged).toHaveBeenCalledWith("gauge");
			onTypeChanged.reset();

			// when select a item from the combo
			// it pass as value a model object.
			// I want only the value field
			var record = view.typeField.store.first();
			var value = record.get("value");
			view.typeField.select(record);
			expect(onTypeChanged).toHaveBeenCalledWith(value);

			// call also for undefined
			onTypeChanged.reset();
			view.typeField.setValue(undefined);
			expect(onTypeChanged).toHaveBeenCalledWith(undefined);
		});

		it('call the delegate when change the data source of chart', function() {
			var onDataSourceChanged = spyOn(delegate, "onDataSourceChanged");
			view.setDelegate(delegate);

			view.fillFieldsWith({
				dataSourceName: "cm_datasource_1"
			});

			expect(onDataSourceChanged).toHaveBeenCalled();
			var args = onDataSourceChanged.argsForCall[0];
			expect(args[0]).toBe("cm_datasource_1");
		});
	});

	function expectSpecificFieldsHidden() {
		expect(view.showLegend).toBeHidden();
		expect(view.maximumField).toBeHidden();
		expect(view.minimumField).toBeHidden();
		expect(view.stepsField).toBeHidden();
		expect(view.fgColorField).toBeHidden();
		expect(view.bgColorField).toBeHidden();
		expect(view.singleSerieField).toBeHidden();
		expect(view.labelField).toBeHidden();
		expect(view.orientationField).toBeHidden();

		// category axes
		expect(view.categoryAxesFieldSet.categoryAxesField).toBeHidden();
		expect(view.categoryAxesFieldSet.categoryAxesLabel).toBeHidden();

		// value axes
		expect(view.valueAxesFieldSet.valueAxesFields).toBeHidden();
		expect(view.valueAxesFieldSet.valueAxesLabel).toBeHidden();
	};

	function expectAllMutableFieldsAreEnabled() {
		expect(view.descriptionArea).toBeEnabled();
		expect(view.activeCheck).toBeEnabled();
		expect(view.autoLoadCheck).toBeEnabled();
		expect(view.dataSourcePanel.dataSourceCombo).toBeEnabled();
		expect(view.typeField).toBeEnabled();
	}

	function expectAllTheOutputFieldsAreEnabled() {
		expect(view.showLegend).toBeEnabled();
		expect(view.maximumField).toBeEnabled();
		expect(view.minimumField).toBeEnabled();
		expect(view.stepsField).toBeEnabled();
		expect(view.fgColorField).toBeEnabled();
		expect(view.bgColorField).toBeEnabled();
		expect(view.singleSerieField).toBeEnabled();
		expect(view.labelField).toBeEnabled();
		expect(view.orientationField).toBeEnabled();

		// category axes
		expect(view.categoryAxesFieldSet.categoryAxesField).toBeEnabled();
		expect(view.categoryAxesFieldSet.categoryAxesLabel).toBeEnabled();

		// value axes
		expect(view.valueAxesFieldSet.valueAxesFields).toBeEnabled();
		expect(view.valueAxesFieldSet.valueAxesLabel).toBeEnabled();
	}

	function expectAllTheOutputFieldsAreNotEnabled() {
		expect(view.showLegend).not.toBeEnabled();
		expect(view.maximumField).not.toBeEnabled();
		expect(view.minimumField).not.toBeEnabled();
		expect(view.stepsField).not.toBeEnabled();
		expect(view.fgColorField).not.toBeEnabled();
		expect(view.bgColorField).not.toBeEnabled();
		expect(view.singleSerieField).not.toBeEnabled();
		expect(view.labelField).not.toBeEnabled();
		expect(view.orientationField).not.toBeEnabled();

		// category axes
		expect(view.categoryAxesFieldSet.categoryAxesField).not.toBeEnabled();
		expect(view.categoryAxesFieldSet.categoryAxesLabel).not.toBeEnabled();

		// value axes
		expect(view.valueAxesFieldSet.valueAxesFields).not.toBeEnabled();
		expect(view.valueAxesFieldSet.valueAxesLabel).not.toBeEnabled();
	}

	function expectAllTheFieldsAreDisabled() {
		expect(view.nameField).not.toBeEnabled();
		expect(view.descriptionArea).not.toBeEnabled();
		expect(view.activeCheck).not.toBeEnabled();
		expect(view.autoLoadCheck).not.toBeEnabled();
		expect(view.dataSourcePanel.dataSourceCombo).not.toBeEnabled();
		expect(view.typeField).not.toBeEnabled();
		expectAllTheOutputFieldsAreNotEnabled();
	}

	function expectAllTheFieldsAreEmpty() {
		expect(view.nameField.getValue()).toEqual("");
		expect(view.descriptionArea.getValue()).toEqual("");
		expect(view.activeCheck.getValue()).toEqual(false);
		expect(view.autoLoadCheck.getValue()).toEqual(false);
		expect(view.dataSourcePanel.dataSourceCombo.getValue()).toEqual(null);
		expect(view.typeField.getValue()).toEqual(null);
		expect(view.maximumField.getValue()).toEqual(null);
		expect(view.minimumField.getValue()).toEqual(null);
		expect(view.stepsField.getValue()).toEqual(null);
		expect(view.fgColorField.getValue()).toEqual(undefined);
		expect(view.bgColorField.getValue()).toEqual(undefined);
		expect(view.showLegend.getValue()).toEqual(false);
		expect(view.singleSerieField.getValue()).toEqual(null);
		expect(view.labelField.getValue()).toEqual(null);
		expect(view.orientationField.getValue()).toEqual(null);

		// category axes
		expect(view.categoryAxesFieldSet.categoryAxesField.getValue()).toEqual(null);
		expect(view.categoryAxesFieldSet.categoryAxesLabel.getValue()).toEqual("");

		// value axes
		expect(view.valueAxesFieldSet.valueAxesFields.getValue()).toEqual([]);
		expect(view.valueAxesFieldSet.valueAxesLabel.getValue()).toEqual("");
	}
})();
