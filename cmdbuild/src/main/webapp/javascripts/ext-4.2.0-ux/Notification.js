/**
 * Ext.ux.Notification
 *
 * @author  Edouard Fattal
 * @date	March 14, 2008
 *
 * @class Ext.ux.Notification
 * @extends Ext.Window
 * 
 * Modified by Tecnoteca
 * 
 *  - Use close instead of hide
 *  - Something else not documented
 */
(function() {
	var BORDER_X_OFFSET = 20,
		BORDER_Y_OFFSET = 40;

Ext.namespace("Ext.ux");

Ext.ux.NotificationMgr = {
	positions: [],

	push: function(item) {
		this.positions.push(item);
	},

	remove: function(item) {
		this.positions = Ext.Array.remove(this.positions, item);
	},

	length: function() {
		return this.positions.length;
	}
};

Ext.define("Ext.ux.Notification", {
	extend: "Ext.Window",
	initComponent: function(){
		Ext.apply(this, {
			iconCls: this.iconCls || 'x-icon-information',
			cls: 'x-notification',
			width: 200,
			plain: false,
			draggable: false,
			bodyStyle: 'text-align:center'
		});

		if(this.autoDestroy) {
			this.task = new Ext.util.DelayedTask(this.close, this);
		} else {
			this.closable = true;
		}

		this.callParent(arguments);
	},

	setMessage: function(msg){
		this.body.update(msg);
	},

	setTitle: function(title, iconCls){
		this.callParent([title, iconCls||this.iconCls]);
	},

	cancelHiding: function(){
		this.addClass('fixed');
		if(this.autoDestroy) {
			this.task.cancel();
		}
	},

	afterShow: function(){
		this.pos = Ext.ux.NotificationMgr.length();
		Ext.ux.NotificationMgr.push(this.pos);
		this.el.alignTo(document, "br-br", [ -BORDER_X_OFFSET, -BORDER_Y_OFFSET-((this.getSize().height+10)*this.pos) ]);

		Ext.fly(this.body.dom).on('click', this.cancelHiding, this);
		if (this.autoDestroy) {
			this.task.delay(this.hideDelay || 5000);
		}
		
		this.toFront();
	},

	afterHide: function() {
		Ext.ux.NotificationMgr.remove(this.pos);
		this.callParent(arguments);
	},

	focus: Ext.emptyFn 

}); 
})();