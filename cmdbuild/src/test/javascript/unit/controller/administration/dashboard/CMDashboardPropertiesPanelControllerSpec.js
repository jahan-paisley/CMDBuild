(function() {
	var view,
		controller;

	describe('CMDBuild.controller.administration.dashboard.CMDashboardPropertiesPanelController', function() {

		beforeEach(function() {
			view = jasmine.createSpyObj("CMDBuild.view.administration.dashboard.CMDashboardPropertiesPanelInterface", [
				"disableTBarButtons",
				"enableTBarButtons",
				"disableFields",
				"enableFields",
				"disableButtons",
				"enableButtons",
				"cleanFields",
				"fillFieldsWith",
				"getFieldsValue",
				"setDelegate",
			]);

			controller = new CMDBuild.controller.administration.dashboard.CMDashboardPropertiesPanelController(view);
		});

		afterEach(function() {
			delete view;
			delete controller;
		});

		it('configure well the view and store the dashboard when dashboardWasSelected is called', function() {
			var dashboard = aDashboard();
			expect(controller.dashboard).toBeNull();

			controller.dashboardWasSelected(dashboard);

			expect(controller.dashboard).toEqual(dashboard);
			expect(view.enableTBarButtons).toHaveBeenCalled();
			expect(view.fillFieldsWith).toHaveBeenCalled();
			expect(view.disableFields).toHaveBeenCalled();

			var arg = view.fillFieldsWith.mostRecentCall.args[0];
			expect(arg.name).toEqual("Foo");
			expect(arg.description).toEqual("Cool dashboard for cool people");
			expect(arg.groups).toEqual([1]);
		});

		it('prepare well the view and reset the data when prepareForAdd is called', function() {
			controller.dashboardWasSelected(aDashboard());
			controller.prepareForAdd();

			expect(view.disableTBarButtons).toHaveBeenCalled();
			expect(view.enableFields).toHaveBeenCalledWith(all=true);
			expect(view.enableButtons).toHaveBeenCalled();
			expect(view.cleanFields).toHaveBeenCalled();

			expect(controller.dashboard).toBeNull();
		});

		// CMDashboardPropertiesPanelDelegate
		it('set himself as delegate for his view', function() {
			expect(view.setDelegate).toHaveBeenCalled();
		});

		it('prepare the view to modify a dashboard', function() {
			controller.onModifyButtonClick();

			expect(view.enableFields).toHaveBeenCalledWith(all=false);
			expect(view.enableButtons).toHaveBeenCalled();
			expect(view.disableTBarButtons).toHaveBeenCalled();
		});

		it('aborting the insert of a new dashboard cleans the view', function() {
			controller.onAbortButtonClick();

			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.disableFields).toHaveBeenCalled();
			expect(view.disableTBarButtons).toHaveBeenCalled();
			expect(view.cleanFields).toHaveBeenCalled();
		});

		it('aborting the modification of a dashboard cleans the view', function() {
			var dashboard = aDashboard();
			controller.dashboardWasSelected(dashboard);
			view.fillFieldsWith.reset();

			controller.onAbortButtonClick();

			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.disableFields).toHaveBeenCalled();
			expect(view.enableTBarButtons).toHaveBeenCalled();
			expect(view.fillFieldsWith).toHaveBeenCalled();
			var arg = view.fillFieldsWith.mostRecentCall.args[0];
			expect(arg.name).toEqual("Foo");
			expect(arg.description).toEqual("Cool dashboard for cool people");
		});

		it('call the service proxy to add a dashboard', function() {
			var add = spyOn(CMDBuild.ServiceProxy.Dashboard, "add"),
				fieldsValue = {
					name: "Foo",
					description: "Bar",
					groups: [1]
				};

			view.getFieldsValue.andReturn(fieldsValue);
			controller.onSaveButtonClick();

			expect(view.getFieldsValue).toHaveBeenCalled();
			expect(add).toHaveBeenCalled();
			var arg = add.mostRecentCall.args[0];
			expect(arg).toEqual(fieldsValue);
		});

		it('call the service proxy to modify a dashboard', function() {
			var modify = spyOn(CMDBuild.ServiceProxy.Dashboard, "modify"),
				fieldsValue = {
					name: "Foo",
					description: "Bar",
					groups: [1]
				},
				dashboard = aDashboard();

			view.getFieldsValue.andReturn(fieldsValue);

			controller.dashboardWasSelected(dashboard);
			controller.onSaveButtonClick();

			var id = modify.mostRecentCall.args[0];
			var args = modify.mostRecentCall.args[1];
			expect(view.getFieldsValue).toHaveBeenCalled();
			expect(modify).toHaveBeenCalled();
			expect(id).toEqual(1);
			expect(args.name).toEqual("Foo");
			expect(args.description).toEqual("Bar");
			expect(args.groups).toEqual([1]);
		});

		it('call the service proxy to remove a dashboard', function() {
			var remove = spyOn(CMDBuild.ServiceProxy.Dashboard, "remove"),
				dashboard = aDashboard();

			controller.dashboardWasSelected(dashboard);
			controller.onRemoveButtonClick();

			var arg = remove.mostRecentCall.args[0];
			expect(remove).toHaveBeenCalled();
			arg = 1;
		});

		it('clean the view and the data when remove a dashboard', function() {
			var dashboard = aDashboard();
			var remove = CMDBuild.ServiceProxy.Dashboard.remove();

			CMDBuild.ServiceProxy.Dashboard.remove = function(dashboardId, success, scope) {
				_CMCache.removeDashboardWithId(dashboardId);
				success.apply(scope);
			};

			controller.dashboardWasSelected(dashboard);
			controller.onRemoveButtonClick();

			expect(controller.dashboard).toBeNull();
			expect(view.cleanFields).toHaveBeenCalled();
			expect(view.disableFields).toHaveBeenCalled();
			expect(view.disableButtons).toHaveBeenCalled();
			expect(view.disableTBarButtons).toHaveBeenCalled();

			CMDBuild.ServiceProxy.Dashboard.remove = remove;
		})
	});

	function aDashboard() {
		return new CMDBuild.model.CMDashboard({
			id: 1,
			name: "Foo",
			description: "Cool dashboard for cool people",
			groups: [1]
		});
	}

})();
