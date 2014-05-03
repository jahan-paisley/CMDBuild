(function() {
	Ext.define("CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate", {
		/**
		 * @params {CMDBuild.view.common.field.CMFilterChooserWindow}
		 * filterWindow the window that call the delegate
		 * @params {Ext.data.Model} filter
		 * the selected record
		 */
		onCMFilterChooserWindowRecordSelect: function(filterWindow, filter) {}
	});

	Ext.define("CMDBuild.view.common.field.CMFilterChooserWindow", {
		extend: "CMDBuild.view.management.common.filter.CMFilterWindow",

		// configuration
		className: "",
		firstShowDetectEvent: "activate",
		saveButtonText: CMDBuild.Translation.common.buttons.confirm,
		abortButtonText: CMDBuild.Translation.common.buttons.abort,
		// configuration

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
				"CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate");

			this.callParent(arguments);
		},

		setFilter: function(filter) {
			this.filter = filter;
			this.filterAttributesPanel.removeAllFieldsets();
			this.filterAttributesPanel.setData(this.filter.getAttributeConfiguration());

			this.filterRelationsPanel.setData(this.filter.getRelationConfiguration());

			this.filterFunctionsPanel.setData(this.filter.getFunctionConfiguration());
		},

		// protected
		// override
		buildButtons: function() {
			var me = this;

			this.buttons = [{
				text: me.saveButtonText,
				handler: function() {
					me.callDelegates("onCMFilterWindowSaveButtonClick", [me, me.getFilter()]);
				}
			},{
				text: me.abortButtonText,
				handler: function() {
					me.callDelegates("onCMFilterWindowAbortButtonClick", [me]);
				}
			}];
		},

		// protected
		// override
		buildItems: function() {
			this.callParent(arguments);

			this.layout = "border";
			this.buildGrid();
			this.items = [
				this.grid,
			{
				xtype: "tabpanel",
				region: "center",
				border: false,
				items: [
					this.filterAttributesPanel, // inherited
					this.filterRelationsPanel, // inherited
					this.filterFunctionsPanel // inherited
				]
			}];
		},

		// private
		buildGrid: function() {
			var me = this;
			var store = _CMProxy.Filter.newSystemStore(this.className);
			this.grid = new Ext.grid.Panel({
				autoScroll: true,
				store: store,
				border: false,
				cls: "cmborderbottom",
				frame: false,
				region: "north",
				height: "40%",
				split: true,
				columns: [{
					header: CMDBuild.Translation.name,
					dataIndex: "name",
					flex: 1
				}, {
					header: CMDBuild.Translation.description_,
					dataIndex: "description",
					flex: 1
				}],
				bbar: new Ext.toolbar.Paging({
					store: store,
					displayInfo: true,
					displayMsg: ' {0} - {1} ' + CMDBuild.Translation.common.display_topic_of+' {2}',
					emptyMsg: CMDBuild.Translation.common.display_topic_none
				}),
				listeners: {
					itemclick: function(grid, record, item, index, e, eOpts) {
						me.callDelegates("onCMFilterChooserWindowRecordSelect", [me, record]);
					}
				}
			});
		},

		// protected
		setWindowTitle: function() {
			this.title = CMDBuild.Translation.views + " - " + CMDBuild.Translation.filterView;
		}
	});

	var SET = CMDBuild.Translation.set;
	var UNSET = CMDBuild.Translation.not_set;

	/**
	 * @class CMDBuild.view.common.field.CMFilterChooser
	 */
	Ext.define("CMDBuild.view.common.field.CMFilterChooser", {
		extend: "Ext.form.FieldContainer",

		mixins: {
			filterChooserWindowDelegate: "CMDBuild.delegate.common.field.CMFilterChooserWindowDelegate",
			filterWindow: "CMDBuild.view.management.common.filter.CMFilterWindowDelegate"
		},

		// configuration
		/**
		 * Used to loads the right attributes when click to the
		 * button to add a new filter
		 */
		className: null,

		/**
		 * the filter selected
		 */
		filter: null,

		/**
		 * @see CMDBUild.view.common.CMFormFunctions.enableFields
		 * @see CMDBUild.view.common.CMFormFunctions.disableFields
		 */
		considerAsFieldToDisable: true,
		// configuration

		// override
		initComponent: function() {
			var me = this;

			this.label = new Ext.form.field.Display({
				value: SET,
				disabledCls: "cmdb-displayfield-disabled"
			});

			this.layout = "hbox",

			this.chooseFilterButton = new Ext.button.Button({
				xtype: 'button',
				tooltip: CMDBuild.Translation.setFilter,
				iconCls: "privileges",
				cls: 'cmnoborder',
				style: {
					'margin-left': "5px"
				},
				scope: me,
				handler: function() {
					me.showFilterChooserPicker();
				}
			});

			this.clearFilterButton = new Ext.button.Button({
				xtype: 'button',
				tooltip: CMDBuild.Translation.clearFilter,
				iconCls: "clear_privileges",
				cls: 'cmnoborder',
				scope: me,
				disabled: true,
				handler: function() {
					me.clearSelection();
				}
			});

			this.items = [
				this.label,
				this.chooseFilterButton,
				this.clearFilterButton
			];

			this.callParent(arguments);
		},

		showFilterChooserPicker: function() {
			var me = this;
			var className = this.className;
			var filter = this.filter || new CMDBuild.model.CMFilterModel({
				entryType: className,
				local: true,
				name: CMDBuild.Translation.management.findfilter.newfilter + " " + _CMUtils.nextId()
			});

			var entryType = _CMCache.getEntryTypeByName(className);

			_CMCache.getAttributeList(entryType.getId(), function(attributes) {

				var filterWindow = new CMDBuild.view.common.field.CMFilterChooserWindow({
					filter: filter,
					attributes: attributes,
					className: className
				});

				filterWindow.addDelegate(me);
				filterWindow.show();

			});
		},

		clearSelection: function() {
			this.reset();
		},

		setClassName: function(className) {
			this.className = className;
			var filter = this.getFilter();
			if (filter && filter.getEntryType() != className) {
				this.reset();
			}
		},

		setFilter: function(filter) {
			this.filter = filter;

			if (filter == null) {
				this.label.setValue(UNSET);
				this.clearFilterButton.disable();
			} else {
				this.label.setValue(SET);
				if (!this.label.isDisabled()) {
					this.clearFilterButton.enable();
				}
			}

			this.doLayout();
		},

		reset: function() {
			this.setFilter(null);
		},

		getFilter: function() {
			return this.filter;
		},

		disable: function() {
			this.items.each(function(i) {
				i.disable();
			});
		},

		enable: function() {
			this.items.each(function(i) {
				i.enable();
			});
		},

		// as filterChooserWindowDelegate

		/**
		 * @params {CMDBuild.view.common.field.CMFilterChooserWindow}
		 * filterWindow the window that call the delegate
		 * @params {Ext.data.Model} filter
		 * the selected record
		 */
		// override
		onCMFilterChooserWindowRecordSelect: function(filterWindow, filter) {
			filterWindow.setFilter(filter);
		},

		// as filterWindowDelegate

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 * @params {CMDBuild.model.CMFilterModel} filter
		 * The filter to save
		 */
		onCMFilterWindowSaveButtonClick: function(filterWindow, filter) {
			this.setFilter(filter);
			filterWindow.destroy();
		},

		/**
		 * @params {CMDBuild.view.management.common.filter.CMFilterWindow} filterWindow
		 * The filter window that call the delegate
		 */
		onCMFilterWindowAbortButtonClick: function(filterWindow) {
			filterWindow.destroy();
		}
	});

	function showFilterChooser() {
		var chooserWindow = new CMDBuild.view.common.field.CMFilterChooserWindow({
			store: this.store
		}).show();

		chooserWindow.addDelegate(this);
	}
})();