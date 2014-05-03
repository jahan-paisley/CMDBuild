(function() {
	describe('CMDBuild.LoginPanel', function() {

		beforeEach(function() {
			this.accordion = new CMDBuild.view.administration.accordion.CMDashboardAccordion();
		});

		it('Is empty if no dashboard are loaded', function() {
			expect(this.accordion.getRootNode().childNodes.length).toBe(0);
		});

		it('Has some nodes if some dashboards are loaded', function() {
			var getDashboards = spyOn(_CMCache, 'getDashboards').andReturn([
				buildDashboardModel({
					id: 1,
					name: "Foo",
					description: "Amazing dashboard for amazing people",
					charts: []
				}),
				buildDashboardModel({
					id: 2,
					name: "Bar",
					description: "Cool dashboard for cool people",
					charts: []
				})
			]);

			this.accordion.updateStore();
			expect(getDashboards).toHaveBeenCalled();

			var nodes = this.accordion.getRootNode().childNodes;
			expect(nodes.length).toEqual(2);
		});
	});

	function buildDashboardModel(c) {
		return new CMDBuild.model.CMDashboard(c);
	}

})();