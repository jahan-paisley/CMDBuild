Ext.define("CMDBuild.ReadOnlyField", {
	extend: "Ext.form.TextField",
	alias: "readonlyfield",
	// private
    onRender : function(ct, position){
        if (!this.el) {
            this.defaultAutoCreate = {
                tag: "p",
                style:"width:100px;height:60px;",
                autocomplete: "off"
            };
        }
        CMDBuild.ReadOnlyField.superclass.onRender.call(this, ct, position);
        if (this.grow) {
            this.textSizeEl = Ext.DomHelper.append(document.body, {
                tag: "pre", cls: "x-form-grow-sizer"
            });
            if (this.preventScrollbars) {
                this.el.setStyle("overflow", "hidden");
            }
            this.el.setHeight(this.growMin);
        }
    }
});