(function() {

	var tr = CMDBuild.Translation.filterWindow;

	Ext.define('CMDBuild.view.administration.tasks.common.emailFilterForm.CMEmailFilterFormWindow', {
		extend: 'Ext.window.Window',

		// Required
		content: undefined,
		delegate: undefined,
		title: undefined,
		type: undefined,
		textareaConcatParameter: undefined,

		autoScroll: true,
		height: 300,
		modal: true,
		width: 400,

		initComponent: function() {
			var me = this;
			var contentItems = null;

			this.delegate = Ext.create('CMDBuild.controller.administration.tasks.common.emailFilterForm.CMEmailFilterFormWindowController', this);
			this.delegate.type = this.type;

			this.tbar = [{
				iconCls: 'add',
				type: 'button',
				text: tr.add,
				handler: function() {
					me.delegate.cmOn('onFilterWindowAdd');
				}
			}];

			if (typeof this.content !== 'undefined') {
				contentItems = this.content.split(this.textareaConcatParameter);
			}

			this.contentComponent = Ext.create('Ext.form.Panel', {
				layout: {
					anchor: '100%'
				},
				items: this.delegate.buildWindowItem(contentItems)
			});

			this.fbar = [
				{
					xtype: 'tbspacer',
					flex: 1
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.buttons.confirm,
					handler: function() {
						me.delegate.cmOn('onFilterWindowConfirm');
					}
				},
				{
					type: 'button',
					text: CMDBuild.Translation.common.buttons.abort,
					handler: function() {
						me.delegate.cmOn('onFilterWindowAbort');
					}
				},
				{
					xtype: 'tbspacer',
					flex: 1
				}
			];

			Ext.apply(this, {
				items: [this.contentComponent]
			});

			this.callParent(arguments);
		}
	});

})();