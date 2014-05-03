(function() {

	var theMod, delegateMock;

	describe('CMDBuild.view.administration.dashboard.CMModDashboard', function() {

		beforeEach(function() {
			theMod = new CMDBuild.view.administration.dashboard.CMModDashboard();
			delegateMock = new CMDBuild.view.administration.dashboard.CMModDashboardDelegate();

			theMod.setDelegate(delegateMock);
		});

		afterEach(function() {
			delete theMod;
			delete delegateMock;
		});

		it('Change the title when calld setTitleSuffix', function() {
			var startingTitle = theMod.title,
				suffix = "Foo";

			theMod.setTitleSuffix(suffix);
			expect(theMod.title).toEqual(startingTitle + " - " + suffix);
		});

		it("Call the delegate when the add button is clicked", function() {
			var onAddButtonClick = spyOn(delegateMock, "onAddButtonClick");
			theMod.addButton.fireHandler();

			expect(onAddButtonClick).toHaveBeenCalled();
		})
	});

})();