(function() {

	var view,
		delegate;

	describe('CMDashboardChartConfigurationPanel', function() {

		beforeEach(function() {
			view = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanel({
				renderTo: Ext.getBody()
			});

			delegate = new CMDBuild.view.administration.dashboard.CMDashboardChartConfigurationPanelDelegate();
			this.addMatchers({
				toBeEnabled : function(expected) {
					return !this.actual.disabled;
				}
			});
		});

		afterEach(function() {
			delete view;
			delete delegate;
		});

		// toolbar
		it('starts with the toolbar buttons disabled', function() {
			expect(view.addButton).not.toBeEnabled();
			expect(view.modifyButton).not.toBeEnabled();
			expect(view.removeButton).not.toBeEnabled();
		});

		it('is able to enable the toolbar buttons', function() {
			view.enableTBarButtons();

			expect(view.addButton).toBeEnabled();
			expect(view.modifyButton).toBeEnabled();
			expect(view.removeButton).toBeEnabled();

			view.disableTBarButtons();
			view.enableTBarButtons(onlyAdd = true);

			expect(view.addButton).toBeEnabled();
			expect(view.modifyButton).not.toBeEnabled();
			expect(view.removeButton).not.toBeEnabled();
		});

		it('is able to disable the toolbar buttons', function() {
			view.enableTBarButtons();
			view.disableTBarButtons();

			expect(view.addButton).not.toBeEnabled();
			expect(view.modifyButton).not.toBeEnabled();
			expect(view.removeButton).not.toBeEnabled();
		});

		// buttons
		it('starts with the buttons disabled', function() {
			expect(view.saveButton).not.toBeEnabled();
			expect(view.abortButton).not.toBeEnabled();
		});

		it('is able to enable the buttons', function() {
			view.enableButtons();

			expect(view.saveButton).toBeEnabled();
			expect(view.abortButton).toBeEnabled();
		});

		it('is able to disable the buttons', function() {
			view.enableButtons();
			view.disableButtons();

			expect(view.saveButton).not.toBeEnabled();
			expect(view.abortButton).not.toBeEnabled();
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

		it('call the delegate when the modifyButton is clicked', function() {
			var onModifyButtonClick = spyOn (delegate, "onModifyButtonClick")
			view.setDelegate(delegate);
			view.enableTBarButtons();

			CMDBuild.test.clickButton(view.modifyButton);
			expect(onModifyButtonClick).toHaveBeenCalled();
		});

		it('call the delegate when the addButton is clicked', function() {
			var onAddButtonClick = spyOn (delegate, "onAddButtonClick")
			view.setDelegate(delegate);
			view.enableTBarButtons();

			CMDBuild.test.clickButton(view.addButton);
			expect(onAddButtonClick).toHaveBeenCalled();
		});

		it('call the delegate when the removeButton is clicked', function() {
			var onRemoveButtonClick = spyOn (delegate, "onRemoveButtonClick");
			view.enableTBarButtons();
			view.setDelegate(delegate);

			CMDBuild.test.clickButton(view.removeButton);
			CMDBuild.test.clickButton(view.confirm.query("#yes")[0]);
			expect(onRemoveButtonClick).toHaveBeenCalled();

			onRemoveButtonClick.reset();

			CMDBuild.test.clickButton(view.removeButton);
			CMDBuild.test.clickButton(view.confirm.query("#no")[0]);
			expect(onRemoveButtonClick).not.toHaveBeenCalled();
		});

		it('call the delegate when the preview button is clicked', function() {
			var onPreviewButtonClick = spyOn (delegate, "onPreviewButtonClick")
			view.setDelegate(delegate);
			view.enableTBarButtons();

			CMDBuild.test.clickButton(view.previewButton);
			expect(onPreviewButtonClick).toHaveBeenCalled();
		});

		it('call the delegate when the saveButton is clicked', function() {
			var onSaveButtonClick = spyOn (delegate, "onSaveButtonClick")
			view.setDelegate(delegate);
			view.enableButtons();

			CMDBuild.test.clickButton(view.saveButton);
			expect(onSaveButtonClick).toHaveBeenCalled();
		});

		it('call the delegate when the abortClick is clicked', function() {
			var onAbortButtonClick = spyOn (delegate, "onAbortButtonClick")
			view.setDelegate(delegate);
			view.enableButtons();

			CMDBuild.test.clickButton(view.abortButton);
			expect(onAbortButtonClick).toHaveBeenCalled();
		});
	});

})();
