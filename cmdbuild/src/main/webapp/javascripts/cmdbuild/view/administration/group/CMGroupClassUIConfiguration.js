(function() {
	var NO_PERMISSION_TO = CMDBuild.Translation.buttons_to_disable;
	Ext.define('CMDBuild.view.administration.group.CMGroupClassUIConfiguration', {
		extend: 'Ext.window.Window',

		bodyCls: 'cmgraypanel',
		height: 200,
		modal: true,
		title: NO_PERMISSION_TO,
		width: CMDBuild.LABEL_WIDTH + 100,
		
/*	 Create parameters
 * 		delegate: me,
 * 		model: model,
 * 		values: values
 * 
*/

		initComponent: function() {
			var me = this;
			var classId = me.model.get("privilegedObjectId");
			var processes = _CMCache.getProcesses();
			var isProcess = (processes[classId]) ? true : false;
			this.create = Ext.create("Ext.form.field.Checkbox", {
				fieldLabel : (isProcess) ? CMDBuild.Translation.management.modworkflow.add_card : CMDBuild.Translation.common.buttons.add,
				labelWidth: CMDBuild.LABEL_WIDTH,
				checked: this.values.create
				
			});
			this.remove = Ext.create("Ext.form.field.Checkbox", {
				fieldLabel : (isProcess) ? CMDBuild.Translation.management.modworkflow.abort_card : CMDBuild.Translation.common.buttons.remove,
				labelWidth: CMDBuild.LABEL_WIDTH,
				checked: this.values.remove
				
			});
			this.modify = Ext.create("Ext.form.field.Checkbox", {
				fieldLabel : (isProcess) ? CMDBuild.Translation.management.modworkflow.modify_card : CMDBuild.Translation.common.buttons.modify,
				labelWidth: CMDBuild.LABEL_WIDTH,
				checked: this.values.modify
				
			});
			this.clone = Ext.create("Ext.form.field.Checkbox", {
				fieldLabel : CMDBuild.Translation.common.buttons.clone,
				labelWidth: CMDBuild.LABEL_WIDTH,
				checked: this.values.clone, 
				hidden: isProcess // if process does not have clone
			});
			this.form = Ext.create('Ext.form.Panel', {
				bodyCls: 'cmgraypanel',
				height: "100%",
				border: false,
				items: [this.create, this.remove, this.modify, this.clone]
			});
			this.contentComponent = Ext.create('Ext.panel.Panel', {
				layout: {
					anchor: '100%'
				},
				items: [this.form]
			});
			this.fbar = [
				{
					xtype: 'tbspacer',
					flex: 1
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.buttons.save,
					handler: function() {
						me.delegate.cmOn("onSaveClassUIConfiguration", {
							classId: me.model.get("privilegedObjectId"),
							create: me.create.getValue(),
							modify: me.modify.getValue(),
							clone: me.clone.getValue(),
							remove: me.remove.getValue()
						});
					}
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.buttons.abort,
					handler: function() {
						me.delegate.cmOn("onAbortClassUIConfiguration");
					}
				},
				{
					xtype: 'tbspacer',
					flex: 1
				}
			];

			this.items = [this.contentComponent];

			this.callParent(arguments);
		}
	});
})();