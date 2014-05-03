/**
 * see http://extjs.com/forum/showthread.php?t=38186
 */

Ext.apply(Ext.StoreMgr, {
  types : Ext.apply({},{
    jsonstore: Ext.data.JsonStore,
    simplestore:Ext.data.SimpleStore
  }),
  
  lookup: function(id) {
    if (typeof id == "object" && !id.load && id.xtype) {
      return new this.types[id.xtype](id);
    }
    return typeof id == "object" ? id : this.get(id);
  }
});

// Support for xtype based store
Ext.grid.Panel.prototype.initComponent = Ext.Function.createInterceptor(
	Ext.grid.Panel.prototype.initComponent,
	function() {
		if (typeof this.store == "object" && !this.store.load && this.store.xtype) {
			this.destroyStore = true;
		}
		this.store = Ext.StoreMgr.lookup(this.store);
	}
);

Ext.grid.Panel.prototype.onDestroy = Ext.Function.createInterceptor(
	Ext.grid.Panel.prototype.onDestroy,
	function() {
		if (this.destroyStore === true && this.store) {
			this.store.destroy();
		}
	}
);

// Support for xtype based store
Ext.form.ComboBox.prototype.initComponent = Ext.Function.createInterceptor(
	Ext.form.ComboBox.prototype.initComponent,
	function() {
		if (typeof this.store == "object" && !this.store.load && this.store.xtype) {
			this.destroyStore = true;
		}
		this.store = Ext.StoreMgr.lookup(this.store);
	}
);

Ext.form.ComboBox.prototype.onDestroy = Ext.Function.createInterceptor(
	Ext.form.ComboBox.prototype.onDestroy,
	function() {
		if (this.destroyStore === true && this.store) {
			this.store.destroy();
		}
	}
);