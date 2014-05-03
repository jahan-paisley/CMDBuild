(function() {
	var realCache;
	var view;
	var controller;

	describe("CMDBuild.controller.accordion.CMDashboardAccordionController", function() {
		beforeEach(function() {

			realCache = _CMCache;
			_CMCache = new Ext.util.Observable();

			view = {
				updateStore: function() {},
				selectNodeById: function() {},
				setDelegate: function() {},
				on: function() {}
			};

			controller = new CMDBuild.controller.accordion.CMDashboardAccordionController(view);
		});

		afterEach(function () {
			_CMCache = realCache;
			delete view;
			delete controller;
		});

		it("Update his view when the cache notifies a new dashboard", function() {
			var updateStore = spyOn(view, 'updateStore'),
				selectNodeById = spyOn(view, 'selectNodeById');

			_CMCache.fireEvent(CMDBuild.cache.CMCacheDashboardFunctions.DASHBOARD_EVENTS.add, {
				getId: function() {return 1;}
			});

			expect(updateStore).toHaveBeenCalled();
			expect(selectNodeById).toHaveBeenCalledWith(1);
		});

		it("Update his view when the cache notifies a removed dashboard", function() {
			var updateStore = spyOn(view, 'updateStore');

			_CMCache.fireEvent(CMDBuild.cache.CMCacheDashboardFunctions.DASHBOARD_EVENTS.remove);
			expect(updateStore).toHaveBeenCalled();
		});

		it("Update his view when the cache notifies that a dasboard was modified", function() {
			var updateStore = spyOn(view, 'updateStore'),
				selectNodeById = spyOn(view, 'selectNodeById');

			_CMCache.fireEvent(CMDBuild.cache.CMCacheDashboardFunctions.DASHBOARD_EVENTS.modify, {
				getId: function() {return 1;}
			});

			expect(updateStore).toHaveBeenCalled();
			expect(selectNodeById).toHaveBeenCalledWith(1);
		});
	});

	function fooDashboardConfig() {
		return {
			id: 1,
			name: "foo",
			description: "Amazing dashboard for amazing people",
			charts: []
		};
	}
})();
