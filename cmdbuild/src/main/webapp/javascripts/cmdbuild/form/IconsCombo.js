Ext.define("CMDBuild.IconsCombo", {
	extend: "CMDBuild.field.ErasableCombo",
	initComponent: function() {
		this.triggerConfig = {
			tag: 'span',
			cls: 'x-form-twin-triggers',
			cn: [{
				tag: "img",
				src: Ext.BLANK_IMAGE_URL,
				cls: "x-form-trigger " + this.trigger1Class
			}, {
				tag: "img",
				src: Ext.BLANK_IMAGE_URL,
				cls: "x-form-trigger " + this.trigger2Class
			}]
		};

		this.editable = false,
		this.listConfig = {
			itemSelector: ".cm-icon-list-item",
			cls: "cm-icon-list",
			getInnerTpl: function() {
				return '<tpl for=".">' +
					'<span class="cm-icon-list-item">' +
						'<img class="cm-icon-list-item-img" src="{path}" alt="{description}" class="icon-item-image"/>' +
						'<span class="cm-icon-list-item-label">{description}</span>' +
					'</span>' +
				'</tpl>'; 
			}
		}

		this.callParent(arguments);
	},

	getTrigger: Ext.form.TwinTriggerField.prototype.getTrigger,
	initTrigger: Ext.form.TwinTriggerField.prototype.initTrigger,
	trigger1Class: Ext.ux.form.XComboBox.prototype.triggerClass,
	trigger2Class: 'x-form-clear-trigger',
	onTrigger1Click: Ext.ux.form.XComboBox.prototype.onTriggerClick,
	onTrigger2Click: function(e) {
		if (!this.disabled) {
			this.clearValue();
			this.fireEvent('clear', this);
		}
	}
});