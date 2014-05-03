(function() {

	describe("CMDBUild.cache.CMCacheDashboardFunctions", function() {

		it("Starts with no dashboards", function() {
			var dashboards = _CMCache.getDashboards();
			expect(dashboards).toEqual({});
		});

		it("Return null if has no dashboard for a given id", function() {
			var d = _CMCache.getDashboardById(1);
			expect(d).toBeNull();
		});

		it("Retrieves added dashboard", function() {
			_CMCache.addDashboard(fooDashboardConfig());

			var d = _CMCache.getDashboardById(1);
			expect(d).toBeDefined();
			expect(d.get("name")).toEqual("foo");
		});

		it("Retrieves a dashboard adding an array of dashboards", function() {
			_CMCache.addDashboards({
				1: {
					name: "foo",
					description: "Amazing dashboard for amazing people",
					charts: []
				}, 
				2: {
					name: "bar",
					description: "Cool dashboard for cool people",
					charts: []
				}
			});

			var d = _CMCache.getDashboardById(2);
			expect(d).toBeDefined();
			expect(d.get("name")).toEqual("bar");
		});

		it("Is able to modify a dashboard", function() {
			_CMCache.addDashboards({
				1: {
					name: "foo",
					description: "Amazing dashboard for amazing people",
					charts: []
				}, 
				2: {
					name: "bar",
					description: "Cool dashboard for cool people",
					charts: []
				}
			});

			_CMCache.modifyDashboard({
				id: 1,
				name: "Bar",
				description: "Bar",
				charts: [],
				groups: [0,1]
			});

			var d = _CMCache.getDashboardById(1);
			expect(d).toBeDefined();
			expect(d.getName()).toEqual("Bar");
			expect(d.getDescription()).toEqual("Bar");
			expect(d.getGroups()).toEqual([0,1]);
		});

		it("Fire an event to notify listeners of a new dashboard", function() {
			var callback = jasmine.createSpy();
			var dashboardConfig = fooDashboardConfig();

			_CMCache.on(_CMCache.DASHBOARD_EVENTS.add, callback, this);
			_CMCache.addDashboard(dashboardConfig);

			expect(callback).toHaveBeenCalled();
			var args = callback.argsForCall[0];
			var data = args[0].data;

			expect(dashboardConfig.id).toEqual(data.id);
			expect(dashboardConfig.name).toEqual(data.name);
			expect(dashboardConfig.description).toEqual(data.description);
			expect(dashboardConfig.charts).toEqual(data.charts);
			expect(dashboardConfig.groups).toEqual(data.groups);
			// if no columns build a single column with width 1
			expect(1).toEqual(data.columns[0].width);
			expect([]).toEqual(data.columns[0].charts);
		});

		it("Remove a dashboard of a given id", function() {
			_CMCache.addDashboard(fooDashboardConfig());
			var cachedData = _CMCache.getDashboardById(1);
			expect(cachedData).toBeDefined();

			_CMCache.removeDashboardWithId(1);
			var cachedData = _CMCache.getDashboardById(1);
			expect(cachedData).toBeNull();
		});

		it("Fire an event to notify listeners of a removed dashboard", function() {
			var callback = jasmine.createSpy();

			_CMCache.addDashboard(fooDashboardConfig());
			_CMCache.on(_CMCache.DASHBOARD_EVENTS.remove, callback, this);
			_CMCache.removeDashboardWithId(1)

			expect(callback).toHaveBeenCalled();
			var args = callback.argsForCall[0];
			expect(args[0]).toEqual(1);
		});

		it("Fire an event to notify listeners of a modified dashboard", function() {
			var onModify = jasmine.createSpy("onModify");

			_CMCache.addDashboard(fooDashboardConfig());
			_CMCache.on(_CMCache.DASHBOARD_EVENTS.modify, onModify, this);
			_CMCache.modifyDashboard({
				id: 1,
				name: "Bar",
				description: "Bar"
			});

			expect(onModify).toHaveBeenCalled();
			var arg = onModify.mostRecentCall.args[0];
			expect(arg.getName()).toEqual("Bar");
			expect(arg.getDescription()).toEqual("Bar");
		});

		// datasources

		it ('can get the store with the given data sources', function() {
			_CMCache.setAvailableDataSources(null);
			var dss = _CMCache.getAvailableDataSourcesStore(),
				items = dss.data.items;

			expect(items.length).toBe(0);

			_CMCache.setAvailableDataSources(someDataSources());
			dss = _CMCache.getAvailableDataSourcesStore(),
			items = dss.data.items;

			expect(items.length).toBe(2);
			expect(items[0].get("name")).toBe("cm_datasource_1");
			expect(items[1].get("name")).toBe("cm_datasource_2");

			_CMCache.setAvailableDataSources(null);
			dss = _CMCache.getAvailableDataSourcesStore(),
			items = dss.data.items;

			expect(items.length).toBe(0);
		});

		it ('can get the input configuration of a datasource', function() {
			_CMCache.setAvailableDataSources(someDataSources());
			var input = _CMCache.getDataSourceInput("cm_datasource_1");

			expect(input).toEqual([{
				name: "in11",
				type: "integer"
			},{
				name: "in12",
				type: "string"
			},{
				name: "in13",
				type: "date"
			}]);
		});

		it ('can get the output configuration of a datasource', function() {
			_CMCache.setAvailableDataSources(someDataSources());
			var output = _CMCache.getDataSourceOutput("cm_datasource_1");

			expect(output).toEqual([{
				name: "out11",
				type: "integer"
			},{
				name: "out12",
				type: "string"
			},{
				name: "out13",
				type: "date"
			}]);
		});
	});

	function fooDashboardConfig() {
		return {
			id: 1,
			name: "foo",
			description: "Amazing dashboard for amazing people",
			charts: [],
			groups: [],
			columns: []
		};
	}

	function someDataSources() {
		return [{
			name: "cm_datasource_1",
			input: [{
				name: "in11",
				type: "integer"
			},{
				name: "in12",
				type: "string"
			},{
				name: "in13",
				type: "date"
			}],
			output: [{
				name: "out11",
				type: "integer"
			},{
				name: "out12",
				type: "string"
			},{
				name: "out13",
				type: "date"
			}]
		}, {
			name: "cm_datasource_2",
			input: [{
				name: "in21",
				type: "integer"
			},{
				name: "in22",
				type: "string"
			},{
				name: "in23",
				type: "date"
			}],
			output: [{
				name: "out21",
				type: "integer"
			},{
				name: "out22",
				type: "string"
			},{
				name: "out23",
				type: "date"
			}]
		}]
	}
})();