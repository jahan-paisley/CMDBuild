(function() {

	var view,
		delegate,
		server,
		getActiveGroupsStore;

	describe('CMDBuild.view.administration.dashboard.CMDashboardPropertiesPanel', function() {

		beforeEach(function() {
			server = CMDBuild.test.CMServer.create();

			getActiveGroupsStore = _CMCache.getActiveGroupsStore;

			_CMCache.getActiveGroupsStore = function() {
				return new Ext.data.Store({
					model: "CMDBuild.cache.CMGroupModel",
					data: [
					       {id: 1, name: "SuperUser", description: "SuperUser"},
					       {id: 2, name: "HelpDesk", description: "HelpDesk"}
					]
				});
			};

			view = new CMDBuild.view.administration.dashboard.CMDashboardPropertiesPanel({
				renderTo: Ext.getBody()
			});

			delegate = new CMDBuild.view.administration.dashboard.CMDashboardPropertiesDelegate();

			this.addMatchers({
				toBeEnabled : function(expected) {
					return !this.actual.disabled;
				}
			});
		});

		afterEach(function() {
			server.restore();
			_CMCache.getActiveGroupsStore = getActiveGroupsStore;
			delete server;
			delete view;
			delete delegate;
		});

		// Toolbar
		it('Start with the toolbar buttons disabled', function() {
			expect(view.modifyButton).not.toBeEnabled();
			expect(view.removeButton).not.toBeEnabled();
		});

		it('Enable the toolbar buttons when enableTBarButtons is called', function() {
			view.enableTBarButtons();

			expect(view.modifyButton).toBeEnabled();
			expect(view.removeButton).toBeEnabled();
		});

		it('Disable the toolbar buttons when disableTBarButtons is called', function() {
			view.enableTBarButtons();
			view.disableTBarButtons();

			expect(view.modifyButton).not.toBeEnabled();
			expect(view.removeButton).not.toBeEnabled();
		});

		// fields
		it('Start with the fields disabled and empty', function() {
			expect(view.nameField).not.toBeEnabled();
			expect(view.descriptionField).not.toBeEnabled();
			expect(view.groupsSelectionList).not.toBeEnabled();

			var data = view.getFieldsValue();
			expect(data.name).toEqual("");
			expect(data.description).toEqual("");
			expect(data.groups).toEqual([]);
		});

		it('Enable right the fields', function() {
			var all = true;
			view.enableFields(all);
			expect(view.nameField).toBeEnabled();
			expect(view.descriptionField).toBeEnabled();
			expect(view.groupsSelectionList).toBeEnabled();

			all = false;
			view.disableFields();
			view.enableFields(all);
			expect(view.nameField).not.toBeEnabled();
			expect(view.descriptionField).toBeEnabled();
			expect(view.groupsSelectionList).toBeEnabled();
		});

		it('Disable the fields when disableFields is called', function() {
			view.enableFields();
			view.disableFields();

			expect(view.nameField).not.toBeEnabled();
			expect(view.descriptionField).not.toBeEnabled();
			expect(view.groupsSelectionList).not.toBeEnabled();
		});

		it('fill well the fields when fillFieldsWith is called', function() {
			waits(function() {
				return !this.store.groupsSelectionList.store.isLoading();
			});

			runs(function() {
				view.fillFieldsWith({
					name: "Foo",
					description: "Bar",
					groups: ["SuperUser"]
				});

				var data = view.getFieldsValue();
				expect(data.name).toEqual("Foo");
				expect(data.description).toEqual("Bar");
				expect(data.groups).toEqual(["SuperUser"]);
			});
		});

		it('clean the fields value when cleanFields is called', function() {
			waits(function() {
				return !this.store.groupsSelectionList.store.isLoading();
			});

			runs(function() {
				view.fillFieldsWith({
					name: "Foo",
					description: "Bar",
					groups: [1]
				});

				view.cleanFields();

				var data = view.getFieldsValue();
				expect(data.name).toEqual("");
				expect(data.description).toEqual("");
				expect(data.groups).toEqual([]);
			});
		});

		it('is able to get the field values', function() {
			waits(function() {
				return !this.store.groupsSelectionList.store.isLoading();
			});

			runs(function() {
				view.fillFieldsWith({
					name: "Foo",
					description: "Bar",
					groups: ["SuperUser"]
				});
	
				var data = view.getFieldsValue();
				expect(data.name).toEqual("Foo");
				expect(data.description).toEqual("Bar");
				expect(data.groups).toEqual(["SuperUser"]);
			});
		});

		// buttons
		it('Start with the buttons disabled', function() {
			expect(view.saveButton).not.toBeEnabled();
			expect(view.abortButton).not.toBeEnabled();
		});

		it('Enable the buttons when enableButtons is calld', function() {
			view.enableButtons();

			expect(view.saveButton).toBeEnabled();
			expect(view.abortButton).toBeEnabled();
		});

		it('Disable the buttons when disableButtons is called', function() {
			view.enableButtons();
			view.disableButtons();

			expect(view.saveButton).not.toBeEnabled();
			expect(view.abortButton).not.toBeEnabled();
		});

		// delegate
		it('Throw exception if pass to setDelegate a non conform object', function() {
			delegate = new Object();
			assertException("The view must throw exception for non conform object on setDelegate",
				function() {
					view.setDelegate(delegate);
				});
		});

		it('Is able to set the delegate', function() {
			expect(view.delegate).toBeUndefined();
			view.setDelegate(delegate);
			expect(view.delegate).toEqual(delegate);
		});

		it('call the delegate onAbortButtonClick method if click on abort button', function() {
			var onAbortButtonClick = spyOn (delegate, "onAbortButtonClick");
			view.setDelegate(delegate);
			view.abortButton.handler.call(view);

			expect(onAbortButtonClick).toHaveBeenCalled();
		});

		it('call the delegate onSaveButtonClick method if click on save button', function() {
			var onSaveButtonClick = spyOn (delegate, "onSaveButtonClick");
			view.setDelegate(delegate);
			view.saveButton.handler.call(view);

			expect(onSaveButtonClick).toHaveBeenCalled();
		});

		it('call the delegate onModifyButtonClick method if click on modify button', function() {
			var onModifyButtonClick = spyOn (delegate, "onModifyButtonClick");
			view.setDelegate(delegate);
			view.modifyButton.handler.call(view);

			expect(onModifyButtonClick).toHaveBeenCalled();
		});

		it('call the delegate onRemoveButtonClick method if click on remove button', function() {
			var onRemoveButtonClick = spyOn (delegate, "onRemoveButtonClick").andReturn(true);
			view.removeButton.enable();
			view.setDelegate(delegate);

			CMDBuild.test.clickButton(view.removeButton);
			CMDBuild.test.clickButton(view.confirm.query("#yes")[0]);
			expect(onRemoveButtonClick).toHaveBeenCalled();

			onRemoveButtonClick.reset();

			CMDBuild.test.clickButton(view.removeButton);
			CMDBuild.test.clickButton(view.confirm.query("#no")[0]);
			expect(onRemoveButtonClick).not.toHaveBeenCalled();
		});
	});
})();