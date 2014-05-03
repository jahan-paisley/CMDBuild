(function() {

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep2Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,
		className: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		drawFilterTabs: function() {
			var me = this;

			if (this.className) {
				_CMCache.getAttributeList(_CMCache.getEntryTypeByName(this.className).getId(), function(attributes) {
					me.view.filterTabPanel.removeAll();

					// Filter tabs
					me.view.filterAttributesTab = Ext.create('CMDBuild.view.management.common.filter.CMFilterAttributes', {
						attributes: attributes
					});
					me.view.filterRelationsTab = Ext.create('CMDBuild.view.management.common.filter.CMRelations', {
						className: me.className,
						height: '100%'
					});
					me.view.filterFunctionsTab = Ext.create('CMDBuild.view.management.common.filter.CMFunctions', {
						className: me.className
					});

					me.view.filterTabPanel.add([me.view.filterAttributesTab, me.view.filterRelationsTab, me.view.filterFunctionsTab]);
					me.view.filterTabPanel.doLayout();
				});
			}
		},

		/**
		 * Function to get filter's datas
		 *
		 * @return (Object) filter's tab datas
		 */
		getDataFilters: function() {
			return {
				attributes: this.view.filterAttributesTab.getData(),
				relations: this.view.filterRelationsTab.getData(),
				functions: this.view.filterFunctionsTab.getData()
			};
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.event.synchronous.CMStep2', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'event_synchronous',

		border: false,
		overflowY: 'auto',
		layout: 'fit',

		initComponent: function() {
			this.delegate = Ext.create('CMDBuild.view.administration.tasks.event.synchronous.CMStep2Delegate', this);

			this.filterTabPanel = Ext.create('Ext.tab.Panel', {
				border: false
			});

			Ext.apply(this, {
				items: [this.filterTabPanel]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * Draw tabs on show
			 */
			show: function(panel, eOpts) {
				this.delegate.drawFilterTabs();
			}
		}
	});

})();
