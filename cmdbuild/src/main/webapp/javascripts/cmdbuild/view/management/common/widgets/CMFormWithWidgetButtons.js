Ext.define("CMDBuild.view.management.common.CMFormWithWidgetButtons", {
	extend: "Ext.panel.Panel",

	mixins: {
		widgetManagerDelegate: "CMDBuild.view.management.common.widgets.CMWidgetManagerDelegate"
	},

	withToolBar: false,
	withButtons: false,
	initComponent: function() {
		this.form = this.buildForm();

		_CMUtils.forwardMethods(this, this.form, [
			"loadCard",
			"getValues",
			"reset",
			"getInvalidAttributeAsHTML",
			"fillForm",
			"getForm",
			"hasDomainAttributes",
			"ensureEditPanel",
			"isInEditing"
		]);

		this.widgets = new CMDBuild.view.management.common.widget.CMWidgetButtonsPanel({
			region: 'east',
			hideMode: 'offsets',
			cls: "cmborderleft",
			autoScroll: true,
			frame: true,
			border: false,
			items: []
		});

		_CMUtils.forwardMethods(this, this.widgets, ["removeAllButtons", "addWidget"]);
		this.widgets.hide();

		Ext.apply(this, {
			layout: "border",
			items: [this.form, this.widgets]
		});

		this.callParent(arguments);

		this.CMEVENTS = Ext.apply(this.form.CMEVENTS, this.widgets.CMEVENTS);
		this.relayEvents(this.widgets, [this.widgets.CMEVENTS.widgetButtonClick]);

		var ee = this.form.CMEVENTS;
		this.relayEvents(this.form, [
			ee.saveCardButtonClick,
			ee.abortButtonClick,
			ee.removeCardButtonClick,
			ee.modifyCardButtonClick,
			ee.cloneCardButtonClick,
			ee.printCardButtonClick,
			ee.openGraphButtonClick,
			ee.editModeDidAcitvate,
			ee.displayModeDidActivate
		]);

		this.mon(this, "activate", function() {
			this.form.fireEvent("activate");
		}, this);
	},

	buildForm: function() {
		return new CMDBuild.view.management.classes.CMCardForm({
			region: "center",
			cmOwner: this,
			withToolBar: this.withToolBar,
			withButtons: this.withButtons
		});
	},

	displayMode: function(enableCMTbar) {
		this.form.displayMode(enableCMTbar);
		this.widgets.displayMode();
	},

	displayModeForNotEditableCard: function() {
		this.form.displayModeForNotEditableCard();
		this.widgets.displayMode();
	},

	editMode: function() {
		this.form.editMode();
		this.widgets.editMode();
	},

	isTheActivePanel: function() {
		var out = true;
		try {
			out = this.ownerCt.layout.getActiveItem() == this;
		} catch (e) {
			// if fails, the panel is not in a TabPanel, so don't defer the call
		}

		return out;
	},

	formIsVisisble: function() {
		return this.form.isVisible(deep = true);
	},

	// CMWidgetManagerDelegate

	getFormForTemplateResolver: function() {
		return this.form.getForm();
	},

	showCardPanel: Ext.emptyFn,

	getWidgetButtonsPanel: function() {
		return this.widgets;
	}

});