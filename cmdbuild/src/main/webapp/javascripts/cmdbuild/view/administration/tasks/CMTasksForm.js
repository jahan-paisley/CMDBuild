(function() {

	var tr = CMDBuild.Translation.administration.tasks; // Path to translation

	Ext.define('CMDBuild.view.administration.tasks.CMTasksForm', {
		extend: 'Ext.form.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		delegate: undefined,

		autoScroll: false,
		buttonAlign: 'center',
		layout: 'fit',
		split: true,
		frame: false,
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		bodyCls: 'cmgraypanel',

		initComponent: function() {
//			var me = this;

			// Buttons configuration
			this.abortButton = Ext.create('CMDBuild.buttons.AbortButton', {
				scope: this,
				handler: function() {
					this.delegate.cmOn('onAbortButtonClick');
				}
			});

			this.cloneButton = Ext.create('Ext.button.Button', {
				iconCls: 'clone',
				text: tr.clone,
				scope: this,
				handler: function() {
					this.delegate.cmOn('onCloneButtonClick');
				}
			});

			this.modifyButton = Ext.create('Ext.button.Button', {
				iconCls: 'modify',
				text: tr.modify,
				scope: this,
				handler: function() {
					this.delegate.cmOn('onModifyButtonClick');
				}
			});

			this.nextButton = Ext.create('CMDBuild.buttons.NextButton', {
				scope: this,
				handler: function() {
					this.delegate.cmOn('onNextButtonClick');
				}
			});

			this.previousButton = Ext.create('CMDBuild.buttons.PreviousButton', {
				scope: this,
				handler: function() {
					this.delegate.cmOn('onPreviousButtonClick');
				}
			});

			this.removeButton = Ext.create('Ext.button.Button', {
				iconCls: 'delete',
				text: tr.remove,
				scope: this,
				handler: function() {
					this.delegate.cmOn('onRemoveButtonClick');
				}
			});

			this.saveButton = Ext.create('CMDBuild.buttons.SaveButton', {
				scope: this,
				handler: function() {
					this.delegate.cmOn('onSaveButtonClick');
				}
			});
			// END: Buttons configuration

			// Page FieldSets configuration
			this.wizard = Ext.create('CMDBuild.view.administration.tasks.CMTasksWizard', {
				previousButton: this.previousButton,
				nextButton: this.nextButton
			});
			this.cmTBar = [this.modifyButton, this.removeButton, this.cloneButton];
			this.cmButtons = [this.previousButton, this.saveButton, this.abortButton, this.nextButton];

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.ServiceProxy.parameter.TOOLBAR_TOP,
						items: this.cmTBar
					}
				],
				items: [this.wizard],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
		}
	});

})();