(function() {
	var view, realCache, controller, treeNode, subcontroller;

	describe("CMDBuild.controller.administration.dashboard.CMModDashboardController", function() {

		beforeEach(function() {
			realCache = _CMCache;

			_CMCache = {
				getDashboardById: function() {
					return new CMDBuild.model.CMDashboard({
						id: 1,
						name: "Foo",
						description: "Cool dashboard for cool people"
					});
				}
			};

			_CMMainViewportController = { // have to be global
				deselectAccordionByName: function() {}
			};

			treeNode = {
				get: function() {return "foo";}
			};

			view = jasmine.createSpyObj("ModDashboard", [
				"setDelegate",
				"setTitleSuffix",
				"on",
				"activateFirstTab"
			]);

			subcontroller = jasmine.createSpyObj("SubController", [
				"dashboardWasSelected",
				"prepareForAdd",
				"setDelegate"
			]);

			controller = new CMDBuild.controller.administration.dashboard.CMModDashboardController(view, subcontroller, subcontroller, subcontroller);
		});

		afterEach(function() {
			_CMCache = realCache;
			delete realCache;
			delete view;
			delete controller;
			delete treeNode;
			delete subcontroller;
			delete _CMMainViewportController;
		});

		it("Take the dashboard from cache when is calld onViewOnFront", function() {
			var getDashboardById = spyOn(_CMCache, "getDashboardById").andCallThrough();

			controller.onViewOnFront(treeNode);
			expect(getDashboardById).toHaveBeenCalled();
			expect(controller.dashboard.get("name")).toEqual("Foo");
		});

		it("Say to the view to set his title when is calld onViewOnFront", function() {
			controller.onViewOnFront(treeNode);
			expect(view.setTitleSuffix).toHaveBeenCalled();
		});

		it("Nofity to the subcontrollers that a dashboard was selected", function() {
			controller.onViewOnFront(treeNode);
			expect(subcontroller.dashboardWasSelected.callCount).toBe(3);

			var args = subcontroller.dashboardWasSelected.argsForCall[0];
			expect(args[0].get("name")).toEqual("Foo");
		});

		it("Prepare the view to add a dashboard", function() {
			var deselectTree = spyOn(_CMMainViewportController, "deselectAccordionByName");

			controller.onAddButtonClick();

			expect(subcontroller.prepareForAdd).toHaveBeenCalled();
			expect(deselectTree).toHaveBeenCalledWith(view.cmName);
		});
	});
})();