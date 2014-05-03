(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.accordion.CMAccordionTasks', {
		extend: 'CMDBuild.view.common.CMBaseAccordion',

		title: tr.title,
		cmName: 'tasks',

		constructor: function(){
			this.callParent(arguments);
			this.updateStore();
			this.selectFirstSelectableNode();
		},

		updateStore: function() {
			var root = this.store.getRootNode();

			root.appendChild([
				{
					id: 'all',
					cmName: 'tasks',
					leaf: false,
					text: tr.all,
					iconCls: 'cmdbuild-tree-tasks-group-icon',
					children: [
						{
							id: 'connector',
							cmName: 'tasks',
							leaf: true,
							text: tr.tasksTypes.connector,
							iconCls: 'cmdbuild-tree-tasks-icon'
						},
						{
							id: 'email',
							cmName: 'tasks',
							leaf: true,
							text: tr.tasksTypes.email,
							iconCls: 'cmdbuild-tree-tasks-icon'
						},
						{
							id: 'event',
							cmName: 'tasks',
							leaf: false,
							text: tr.tasksTypes.event,
							iconCls: 'cmdbuild-tree-tasks-group-icon',
							children: [
								{
									id: 'event_asynchronous',
									cmName: 'tasks',
									leaf: true,
									text: tr.tasksTypes.eventTypes.asynchronous,
									iconCls: 'cmdbuild-tree-tasks-icon'
								},
								{
									id: 'event_synchronous',
									cmName: 'tasks',
									leaf: true,
									text: tr.tasksTypes.eventTypes.synchronous,
									iconCls: 'cmdbuild-tree-tasks-icon'
								}
							]
						},
						{
							id: 'workflow',
							cmName: 'tasks',
							leaf: true,
							text: tr.tasksTypes.workflow,
							iconCls: 'cmdbuild-tree-tasks-icon'
						}
					]
				}
			]);
		}
	});

})();