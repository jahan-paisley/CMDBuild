/**
 * @author Jon Ege Ronnenberg
 */
Ext.ns("Ext.ux.form");
Ext.ux.form.XComboBox = function(config){
	Ext.ux.form.XComboBox.superclass.constructor.call(this, config);
};

Ext.define("Ext.ux.form.XComboBox", {
    extend: "Ext.form.field.ComboBox",

    alias: "widget.xcombo",
	growSizeFix : function() {
		if (this.grow && this.rendered) {
			var v = "";
			if (this.store) {
				this.store.each(function(record){
					if (v.length < record.data[this.displayField].length) {
						v = record.data[this.displayField];
					}
				}, this);
			}
			this.recalculateSize(v);
		}
	},
	
	recalculateSize: function(v) {
		var el = this.getEl();
		if (!el || !el.dom) {
			return;
		}
		var metric = Ext.util.TextMetrics.createInstance(el);
		var d = document.createElement('div');
		d.appendChild(document.createTextNode(v));
		v = d.innerHTML;
		d = null;
		v += "&#160;";
		var w = Math.min(this.growMax, Math.max(metric.getWidth(v) + /* add extra padding */ 12, this.growMin));
		if (el.dom.nextSibling) { // why this?
			var numOfChildren = el.dom.nextSibling.childNodes.length;
		} else {
			var numOfChildren = this.triggerConfig.cn.length;
		}
		var newComponentWidth = w + (numOfChildren * 18); //18 is the size of a little button at the combo's right
		this.setWidth(newComponentWidth);
		el.setWidth(w);
		this.wrap.setWidth(newComponentWidth);
		this.bufferSize = newComponentWidth; //take a look at Combo.js:1278 Ext 3.2.0 and have fun
	}
});