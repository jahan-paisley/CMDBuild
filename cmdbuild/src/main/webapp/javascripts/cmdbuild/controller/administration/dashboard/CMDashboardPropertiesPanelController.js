(function() {

	Ext.define("CMDBuild.controller.administration.dashboard.CMDashboardPropertiesPanelController", {

		alias: "controller.cmdashboardproperties",

		mixins: {
			viewDelegate: "CMDBuild.view.administration.dashboard.CMDashboardPropertiesDelegate"
		},

		constructor : function(view) {
			this.callParent(arguments);

			this.view = view;
			this.view.setDelegate(this);

			this.dashboard = null;
		},

		dashboardWasSelected: function(dashboard) {
			this.dashboard = dashboard;
			this.onAbortButtonClick();
		},

		prepareForAdd: function() {
			this.dashboard = null;

			this.view.disableTBarButtons();
			this.view.enableButtons();
			this.view.enableFields(all=true);
			this.view.cleanFields();
			_CMCache.initAddingTranslations();
			this.view.descriptionField.translationsKeyName = "";
		},

		// CMDashboardPropertiesDelegate

		onModifyButtonClick: function() {
			this.view.enableFields(all=false);
			this.view.enableButtons();
			this.view.disableTBarButtons();
			_CMCache.initModifyingTranslations();
			this.view.descriptionField.translationsKeyName = this.dashboard.get("name");
		},

		onAbortButtonClick: function() {
			this.view.disableFields();
			this.view.disableButtons();

			if (this.dashboard) {
				fillFormWithDashboardData(this);
				this.view.enableTBarButtons();
			} else {
				this.view.cleanFields();
				this.view.disableTBarButtons();
			}
		},

		onSaveButtonClick: function() {
			var data = this.view.getFieldsValue();
			if (!data.name || !data.description) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

			if (this.dashboard) {
				CMDBuild.ServiceProxy.Dashboard.modify(this.dashboard.getId(), data, proxySuccess, this);
			} else {
				_CMCache.flushTranslationsToSave(data.name);
				CMDBuild.ServiceProxy.Dashboard.add(data, proxySuccess, this);
			}
		},

		onRemoveButtonClick: function() {
			var id = this.dashboard.getId();
			CMDBuild.ServiceProxy.Dashboard.remove(id, proxySuccess, this);
		}
	});

	function fillFormWithDashboardData(me) {
		me.view.fillFieldsWith({
			name: me.dashboard.getName(),
			description: me.dashboard.getDescription(),
			groups: me.dashboard.getGroups()
		});
	}

	function proxySuccess() {
		this.dashboard = null;
		this.onAbortButtonClick();
	}
})();
