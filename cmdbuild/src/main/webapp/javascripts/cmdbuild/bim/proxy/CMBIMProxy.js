(function() {

	var url = {
		create: "services/json/bim/create",
		read: "services/json/bim/read",
		update: "services/json/bim/update",

		enable: "services/json/bim/enableproject",
		disable: "services/json/bim/disableproject",

		readLayer: "services/json/bim/readbimlayer",
		saveLayer: "services/json/bim/savebimlayer",
		rootLayer: "services/json/bim/rootclassname",

		roidForCardId: "services/json/bim/getroidforcardid",

		activeForClassName: "services/json/bim/getactiveforclassname",

		fetchCardFromViewewId: "services/json/bim/fetchcardfromviewewid",
		fetchJsonForBimViewer: "services/json/bim/fetchjsonforbimviewer"
	};

	CMDBuild.bim.proxy = {

		enable: function(config) {
			config.method = "POST";
			config.url = url.enable;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		disable: function(config) {
			config.method = "POST";
			config.url = url.disable;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		create: function(form, params, success, failure) {
			submit(url.create, form, params, success, failure);
		},

		update: function(form, params, success, failure) {
			submit(url.update, form, params, success, failure);
		},

		store: function() {

			var store = Ext.create("Ext.data.Store", {
				model: "CMDBuild.bim.data.CMBIMProjectModel",
				// fields: ["name", "description", "active"],
				proxy: {
					type: 'ajax',
					url: url.read,
					actionMethods: "GET",
					reader: {
						type: 'json',
						root: 'bimProjects'
					}
				},
				autoLoad: false,

				sorters: [{
					property: "description",
					direction: "ASC"
				}],

				// Disable paging
				defaultPageSize: 0,
				pageSize: 0
			});

			return store;
		},

		layerStore: function() {
			return Ext.create('Ext.data.Store', {
				model: 'CMDBuild.bim.data.CMBimLayerModel',
				proxy: {
					type: 'ajax',
					url: url.readLayer,
					reader: {
						type: 'json',
						root: 'bimLayer'
					}
				},

				autoLoad: false
			});
		},

		saveLayer: function(config) {
			config.method = "POST";
			config.url = url.saveLayer;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		getAllLayers: function(config) {
			config.method = "GET";
			config.url = url.readLayer;
			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		rootClassName: function(config) {
			config.method = "GET";
			config.url = url.rootLayer;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		roidForCardId: function(config) {
			config.method = "GET";
			config.url = url.roidForCardId;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		/**
		 * @param {Object} config
		 * @param {String} config.className
		 */
		activeForClassName: function(config) {
			config.method = "GET";
			config.url = url.activeForClassName;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		/**
		 * @param {Object} config
		 * @param {Object} config.params
		 * @param {String} config.params.objectId
		 * the id of the geometry, given from the
		 * viewer
		 * @param {String} config.params.revisionId
		 * the ROID of the project
		 */
		fetchCardFromViewewId: function(config) {
			config.method = "GET";
			config.url = url.fetchCardFromViewewId;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

		/**
		 * @param {Object} config
		 * @param {Object} config.params
		 * @param {String} config.params.revisionId
		 * the ROID of the project to download
		 */
		fetchJsonForBimViewer: function(config) {
			config.method = "GET";
			config.url = url.fetchJsonForBimViewer;

			CMDBuild.ServiceProxy.core.doRequest(config);
		},

	};

	function submit(url, form, params, success, failure) {
		form.submit({
			url: url,
			params: params,
			fileUpload: true,
			success: success,
			failure: failure
		});
	}
})();