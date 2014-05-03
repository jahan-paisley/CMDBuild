(function() {

	Ext.define("CMDBuild.controller.administration.gis.CMModLayerOrderController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			this.view.mon(this.view, "cm-rowmove", onRowMoved, this);
		},

		onViewOnFront: function() {
			this.view.store.load();
		}
	});

	/*
	 *p = {
		node: node,
		data: data,
		dropRec: dropRec,
		dropPosition: dropPosition
	}*/
	function onRowMoved(p) {
		var oldIndex = getOldIndex(p.data);
		var newIndex = getNewIndex(p.dropRec);
		var me = this;

		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.saveLayerOrder({
			oldIndex: oldIndex,
			newIndex: newIndex,
			callback: function() {
				CMDBuild.LoadMask.get().hide();
				_CMCache.onGeoAttributeSaved(); // load always to sync the index
			}
		});
	}

	function getOldIndex(data) {
		var oldIndex = -1;
		try {
			oldIndex = data.records[0].data.index;
		} catch (e) {
			CMDBuild.log.Error("Can not get the old index");
		}

		return oldIndex;
	}

	function getNewIndex(dropRec, dropPosition) {
		return dropRec.data.index;
	}
})();