(function() {

	var titleTemplate = "{0} - {1} - {2}";

	Ext.define("CMDBuild.view.management.common.filter.CMFilterWindowDelegate", {
		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowApplyButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowSaveAndApplyButtonClick: Ext.emptyFn,

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowAbortButtonClick: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.filter.CMFilterWindow", {
		extend : "CMDBuild.PopupWindow",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		// configuration
		attributes: {},
		className: '',
		filter: undefined,
		/*
		 * In some subclass the relations
		 * panel is used in a tab panel,
		 * so the event to listen for detect the
		 * first time that is shown is different (activate)
		 */
		firstShowDetectEvent: "expand",
		// configuration

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.common.filter.CMFilterWindowDelegate");

			this.callParent(arguments);
		},

		initComponent : function() {
			var me = this;

			this.layout = "accordion";
			this.buttonAlign = "center";

			this.setWindowTitle();
			this.buildItems();
			this.buildButtons();

			this.callParent(arguments);

			me.mon(this, "show", me.onFirstShow, this, {single: true});
		},

		getFilter: function() {
			if (theFilterIsDirty(this)) {
				this.filter.setDirty();
				this.filter.setLocal(true);
				this.filter.setAttributeConfiguration(this.filterAttributesPanel.getData());
				if (!this.filterRelationNeverExpansed) { // the panel was expanded at least once
					this.filter.setRelationConfiguration(this.filterRelationsPanel.getData());
				}
				this.filter.setFunctionConfiguration(this.filterFunctionsPanel.getData());
			}

			return this.filter;
		},

		// protected
		onFirstShow: function() {
			this.filterAttributesPanel.setData(this.filter.getAttributeConfiguration());
			// defer the setting of relations data
			// to the moment in which the panel is expanded
			// If never expanded take the data from the filter
			this.filterRelationNeverExpansed = true;
			this.mon(this.filterRelationsPanel, this.firstShowDetectEvent, function() {
				this.filterRelationsPanel.setData(this.filter.getRelationConfiguration());
				this.filterRelationNeverExpansed = false;
			}, this, {single: true});
			this.filterFunctionsPanel.setData(this.filter.getFunctionConfiguration());
		},

		// protected
		setWindowTitle: function() {
			var prefix = CMDBuild.Translation.management.findfilter.window_title;
			var et = _CMCache.getEntryTypeByName(this.className);
			this.title = Ext.String.format(titleTemplate, prefix, this.filter.getName(), et.getDescription());
		},

		// protected
		buildButtons: function() {
			var me = this;

			this.buttons = [{
				text: CMDBuild.Translation.management.findfilter.apply,
				handler: function() {
					me.callDelegates("onCMFilterWindowApplyButtonClick", [me, me.getFilter()]);
				}
			},{
				text: CMDBuild.Translation.management.findfilter.saveandapply,
				handler: function() {
					me.callDelegates("onCMFilterWindowSaveAndApplyButtonClick", [me, me.getFilter()]);
				}
			},{
				text: CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					me.callDelegates("onCMFilterWindowAbortButtonClick", [me]);
				}
			}];
		},

		// protected
		buildFilterAttributePanel: function() {
			return new CMDBuild.view.management.common.filter.CMFilterAttributes({
				attributes: this.attributes,
				className: this.className
			});
		},

		// protected
		buildItems: function() {
			this.filterAttributesPanel = this.buildFilterAttributePanel();
			this.filterRelationsPanel = new CMDBuild.view.management.common.filter.CMRelations({
				attributes: this.attributes,
				className: this.className
			});
			this.filterFunctionsPanel = new CMDBuild.view.management.common.filter.CMFunctions({
				attributes: this.attributes,
				className: this.className
			});
			this.items = [this.filterAttributesPanel, this.filterRelationsPanel, this.filterFunctionsPanel];
		}
	});

	function theFilterIsDirty(me) {
		var currentFilter = new CMDBuild.model.CMFilterModel();
		currentFilter.setAttributeConfiguration(me.filterAttributesPanel.getData());

		if (me.filterRelationNeverExpansed) {
			currentFilter.setRelationConfiguration(me.filter.getRelationConfiguration());
		} else {
			currentFilter.setRelationConfiguration(me.filterRelationsPanel.getData());
		}
		currentFilter.setFunctionConfiguration(me.filterFunctionsPanel.getData());

		// The string are not equals because serialize the fields of the object not in the same
		// order TODO: impement a comparator of the configuration something like
		// filter.isEquivalent(configuration);
		// return Ext.encode(me.filter.getConfiguration()) != Ext.encode(currentFilter.getConfiguration());
		return true;
	}

	Ext.define("CMDBuild.view.management.common.filter.CMSaveFilterWindowDelegate", {
		/**
		 * @param {CMDBuild.view.management.common.filter.CMSaveFilterWindow} window
		 * the window that calls the delegate
		 * @param {CMDBuild.model.CMFilterModel} filter
		 * the filter to save
		 * @param {String} name
		 * the name set in the form
		 * @param {String} the description set in the form
		 */
		onSaveFilterWindowConfirm: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.common.filter.CMSaveFilterWindow", {
		extend: "Ext.window.Window",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		// configuration
		filter: undefined, // a CMDBuild.model.CMFilterModel,
		referredFilterWindow: undefined, // a CMFilterWindow, used outside to know the referred filter window and close it
		// configuration

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.view.management.common.filter.CMSaveFilterWindowDelegate");

			this.callParent(arguments);
		},

		initComponent: function() {
			this.modal = true;
			this.bodyPadding = "5px 5px 1px 5px";

			var canEditTheName = this.filter.isLocal();
			this.nameField = new Ext.form.field.Text({
				name: 'name',
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.name,
				value: this.filter.getName(),
				disabled: !canEditTheName,
				width: CMDBuild.BIG_FIELD_WIDTH,
				allowBlank: false //requires a non-empty value
			});

			this.descriptionField = new Ext.form.field.TextArea({
				name: 'description',
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.description,
				value: this.filter.getDescription(),
				width: CMDBuild.BIG_FIELD_WIDTH,
				allowBlank: false //requires a non-empty value
			});

			this.items = [this.nameField, this.descriptionField];

			var me = this;
			this.buttonAlign = "center";
			this.buttons = [{
				text: CMDBuild.Translation.common.buttons.save,
				handler: function() {
					var name = me.nameField.getValue();
					var description = me.descriptionField.getValue();

					me.callDelegates("onSaveFilterWindowConfirm", [me, me.filter, name, description]);
				}
			}, {
				text: CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					me.destroy();
				}
			}];

			this.callParent(arguments);
		}
	});
})();