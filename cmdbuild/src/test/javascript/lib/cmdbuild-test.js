(function() {

	// To allow us to test without a real server override
	// the Ext.Ajax.request to use a wrapper of a fake server
	// made available by sinon.js
	var extAjaxRequest = Ext.Ajax.request;

	Ext.Ajax.setCMServer = function(s) {
		if (s) {
			Ext.Ajax.cmServer = s;
		}
	};

	Ext.Ajax.request = function(options) {
		var server = Ext.Ajax.cmServer;
		var url = options.url;
		if (server && url) {
			// set sinon.fakeServer to respond

			var cb = server.getCallbackForUrl(url);

			if (cb) {
				var method = options.method || "GET";
				var params = options.params || {};
				var out = cb.call(this, params);

				if (typeof out == "object") {
					out = Ext.JSON.encode(out);
				}

				// TODO build a object to configure a little more the response
				server.respondWith(method, url,
					["200", { "Content-Type": "application/json" }, out || ""]);
			}
		}

		// call the original function to do the request
		extAjaxRequest.call(this, options);
		server.respond();
	};

	// define an object that wraps the sinon's fakeServer
	// It may be created with the factory method CMDBuild.test.CMServer.create()
	// that set the new server as the server used by Ext.Ajax to do the requests
	// so, a test could looks like this:
	//		"test that the server response well": function() {
	//			var _response = '{"success":true, "I-am-cool": true}';
	//			var spy = sinon.spy();
	//
	//			// say to the server how to respond to a specific url
	//			this.server.bindUrl("/test", function() {
	//				return _response;
	//			});
	//
	// 			// do the request
	//			Ext.Ajax.request({ 
	//				url : '/test',
	//				params : {},
	//				success: function(response, opt) {
	//					spy(response.responseText);
	//				}
	//			});
	//
	//			// assert something about the response
	//			assertTrue(spy.calledWith(_response));
	//		}
	Ext.define("CMDBuild.test.CMServer", {
		statics: {
			create: function() {
				var server = new CMDBuild.test.CMServer();
				Ext.Ajax.setCMServer(server);

				return server;
			}
		},

		constructor: function() {
			this.server = sinon.fakeServer.create();
			this.callbacks = {};
		},

		restore: function() {
			this.server.restore();
			this.callbacks = {};
		},

		respond: function() {
			this.server.respond();
		},

		respondWith: function(method, url, response) {
			url = buildRegExp(removeParamsFromUrl(url));
			// remove previous response configuration with the same url
			var responses = this.server.responses || [];
			var BODY_POSITION = 2;
			for (var i = 0, l = responses.length; i < l; i++) {
				if (responses[i].url.toString() == url.toString()) {
					responses[i].response = response;
					return;
				}
			}

			this.server.respondWith(method, url, response);
		},

		bindUrl: function(url, fn) {
			this.callbacks[removeParamsFromUrl(url)] = fn;
		},

		getCallbackForUrl: function(url) {
			if (url) {
				return this.callbacks[removeParamsFromUrl(url)];
			} else {
				return undefined;
			}
		},

		hasResponses: function() {
			if (!this.server.response && !this.server.responses) {
				return false;
			} else if (this.server.response) {
				return true;
			} else {
				return (this.server.responses && this.server.responses.length > 0); 
			}
		}
	});

	function removeParamsFromUrl(url) {
		return url.split("?")[0] || "";
	}

	function buildRegExp(url) {
		return new RegExp("^" + url);
	}

	CMDBuild.test.clickButton = function(b) {
		b.btnEl.dom.click();
	};

	CMDBuild.test.selectGridRow = function(grid, row) {
		row = row || 0;
		grid.getSelectionModel().select(row);
	};

	/**
	 * return a jasmine spy object with a spy for each
	 * function in the prototype
	 */
	CMDBuild.test.spyObj = function(o, baseName, extraSpy) {
		var funtionsToSpy = extraSpy || [];

		for (var key in o.prototype) {
			if (typeof o.prototype[key] == "function") {
				funtionsToSpy.push(key);
			}
		}

		return jasmine.createSpyObj(baseName, funtionsToSpy);
	};

	// configuration structure
	Ext.define("CMDBuild.Config", {
		statics: {
			cmdbuild: {}
		}
	});


	// So, another episode of "the testing saga":
	// Jasmine's "pretty printer" goes crazy with the Ext's
	// objects. So, override the formatter to have only the class
	// name. If the tests will needs more info abaut an object
	// write here the behaviour to print them.
	var extpp = {
		"Ext.button.Button": function(o) {
			return "(with text " + o.text + ")";
		},

		"Ext.form.field.Text": function(o) {
			return "(with label " + o.fieldLabel+ ")";
		}
	}

	var realPrinter = jasmine.PrettyPrinter.prototype.format;
	jasmine.PrettyPrinter.prototype.format = function(value) {
		var className = Ext.getClassName(value);

		if (className) {
			if (typeof extpp[className] == "function") {
				className += " " + extpp[className](value);
			}

			this.emitScalar(className);
		} else {
			realPrinter.apply(this, arguments);
		}
	};

// /******

	//override describe to allow the run of a set of tests

	var realDescribe = jasmine.Env.prototype.describe;
	jasmine.Env.prototype.describe = function(description, specDefinitions) {

		// specs to run
		var focusSpecs = [ 
			'CMDBuild.LoginPanel',
			'CMDBUild.cache.CMCacheDashboardFunctions',
			'CMDashboardModel',
			'CMDashboardChartConfigurationFormController',
			'GaugeTypeStrategy',
			'CMDashboardChartConfigurationPanel',
			'CMDashboardChartConfigurationPanelController',
			'CMDashboardChartConfigurationGridController',
			'CMDashboardChartConfigurationGridSpec',
			'CMDashboardChartConfigurationDataSourcePanel',
			'_DataSourceInputFildSet',
			'CMDashboardChartConfigurationForm',
			'CMChartBarTypeStrategy',
			'CMChartPieTypeStrategy',
			'CMChartGaugeTypeStrategy',
			'CMDashboardModel',
			'CMWorkflowState',
			'CMAttachmentCategoryModel',
			'CMMetadataGroup',
			'CMCacheAttachmentCategoryFunction',
			'CMEntryTypeModel',
			'CMDelegable',
			'CMCardModuleState'
		];

		if (Ext.Array.contains(focusSpecs, description)) {
			realDescribe.apply(this, arguments);
		}
	};

// *******/

	
})();