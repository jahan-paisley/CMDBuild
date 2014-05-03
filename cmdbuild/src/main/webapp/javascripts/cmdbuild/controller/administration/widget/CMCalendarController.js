(function() {
	Ext.define("CMDBuild.controller.administration.widget.CMCalendarController", {

		extend: "CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController",

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMCalendarDefinitionForm.WIDGET_NAME
		},

		constructor: function() {
			this.callParent(arguments);
			var me = this;
			this.view.targetClass.setValue = Ext.Function.createSequence(this.view.targetClass.setValue,
				function(v) {
					onTargetClassChanged(me, v);
				},
				this.view
			);

			fillDefaultDateStore(me);
		}
	});

	function onTargetClassChanged(me, targetClass) {
		if (Ext.isArray(targetClass)) {
			targetClass = targetClass[0];
			if (targetClass.get) {
				targetClass = targetClass.get("id");
			}
		} else if (typeof targetClass == "string") {
			var tc = _CMCache.getEntryTypeByName(targetClass);
			if (tc) {
				targetClass = tc.get("id");
			}
		}

		if (targetClass) {
			_CMCache.getAttributeList(targetClass, function(attributes) {
				fillAttributeStoresWithData(me, attributes);
			});
		} else {
			fillAttributeStoresWithData(me, []);
		}
	}

	function fillStoreByType(store, attributes, allowedTypes) {
		allowedTypes = allowedTypes || [];
		store.removeAll();
		for (var i=0, l=attributes.length; i<l; ++i) {
			var a = attributes[i],
				type = a.type;

			if (Ext.Array.indexOf(allowedTypes, type) >= 0) {
				store.add({id: a.name, description: a.description});
			}
		}
	}

	function fillAttributeStoresWithData(me, attributes) {
		fillStoreByType(me.view.startDate.store, attributes, ["DATE", "TIMESTAMP"]);
		fillStoreByType(me.view.endDate.store, attributes, ["DATE", "TIMESTAMP"]);
		fillStoreByType(me.view.eventTitle.store, attributes, ["TEXT", "STRING"]);
	}

	function fillDefaultDateStore(me) {
		_CMCache.getAttributeList(me.classId, function(attributes) {
			fillStoreByType(me.view.defaultDate.store, attributes, ["DATE", "TIMESTAMP"]);
		});
	}
})();