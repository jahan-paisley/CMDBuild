(function() {

	Ext.override('Ext.form.field.Base', {

		disable: function(silent) {
			var me = this;

			if (me.rendered) {
				me.bodyEl.addCls(me.disabledCls); // to disable the field only
				me.el.dom.disabled = true;
				me.onDisable();
			}

			me.disabled = true;

			if (silent !== true)
				me.fireEvent('disable', me);

			return me;
		},

		enable: function(silent) {
			var me = this;

			if (me.rendered) {
				me.bodyEl.removeCls(me.disabledCls); // to enable the field only
				me.el.dom.disabled = false;
				me.onEnable();
			}

			me.disabled = false;

			if (silent !== true)
				me.fireEvent('enable', me);

			return me;
		}
	});

})();