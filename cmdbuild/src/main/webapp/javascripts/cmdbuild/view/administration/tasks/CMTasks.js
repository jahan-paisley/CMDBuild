(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.tasks.CMTasks', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		title: tr.title,
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			this.grid = Ext.create('CMDBuild.view.administration.tasks.CMTasksGrid', {
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.tasks.CMTasksForm', {
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.ServiceProxy.parameter.TOOLBAR_TOP,
						items: [this.addButton]
					}
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * To show correct button in top toolbar
			 */
			show: function(panel, eOpts) {
				this.getDockedComponent(CMDBuild.ServiceProxy.parameter.TOOLBAR_TOP).removeAll();

				if (this.delegate.taskType != 'all') {
					this.getDockedComponent(CMDBuild.ServiceProxy.parameter.TOOLBAR_TOP).add(
						Ext.create('Ext.Button', {
							iconCls: 'add',
							text: tr.add,
							scope: this,
							handler: function() {
								this.delegate.cmOn('onAddButtonClick', { type: this.delegate.taskType });
							}
						})
					);
				} else {
					this.getDockedComponent(CMDBuild.ServiceProxy.parameter.TOOLBAR_TOP).add(
						Ext.create('Ext.button.Split', {
							iconCls: 'add',
							text: tr.add,
							handler: function() {
								this.showMenu();
							},
							menu: Ext.create('Ext.menu.Menu', { // Rendered as dropdown menu on button click
								items: [
									{
										text: tr.tasksTypes.connector,
										scope: this,
										handler: function() {
											this.delegate.cmOn('onAddButtonClick', { type: 'connector' });
										}
									},
									{
										text: tr.tasksTypes.email,
										scope: this,
										handler: function() {
											this.delegate.cmOn('onAddButtonClick', { type: 'email' });
										}
									},
									{
										text: tr.tasksTypes.event,
										menu: [
											{
												text: tr.tasksTypes.eventTypes.asynchronous,
												scope: this,
												handler: function() {
													this.delegate.cmOn('onAddButtonClick', { type: 'event_asynchronous' });
												}
											},
											{
												text: tr.tasksTypes.eventTypes.synchronous,
												scope: this,
												handler: function() {
													this.delegate.cmOn('onAddButtonClick', { type: 'event_synchronous' });
												}
											}
										]
									},
									{
										text: tr.tasksTypes.workflow,
										scope: this,
										handler: function() {
											this.delegate.cmOn('onAddButtonClick', { type: 'workflow' });
										}
									}
								]
							})
						})
					);
				}
			}
		}
	});

})();