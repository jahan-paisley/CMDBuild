(function() {
	var iconStore = null;
	var urls = {
		list: "services/json/icon/list",
		upload: "services/json/icon/upload",
		update: "services/json/icon/update",
		remove: "services/json/icon/remove"
	};

	CMDBuild.ServiceProxy.Icons = {
		getIconStore: function() {
			if (iconStore == null) {
				iconStore = Ext.create("Ext.data.Store", {
					model: 'IconsModel',
					proxy: {
						type: 'ajax',
						url: urls.list,
						reader: {
							type: 'json',
							root: 'rows'
						}
					},
					autoLoad: true
				});
			}

			return iconStore;
		},

		upload: function(form, config) {
			config.method = 'POST';
			config.url = urls.upload;

			form.submit(config);
		},

		update: function(form, config) {
			config.method = 'POST';
			config.url = urls.update;

			form.submit(config);
		},

		remove: function(config) {
			config.method = 'POST';
			config.url = urls.remove;

			CMDBuild.Ajax.request(config);
		}
	};
})();