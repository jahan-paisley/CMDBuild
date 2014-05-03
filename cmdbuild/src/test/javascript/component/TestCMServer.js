(function() {
	TestCase("testCMServer", {

		setUp: function() {
			this.server = new CMDBuild.test.CMServer();
		},

		tearDown: function() {
			this.server.restore();
		},

		"test server return nothing for unknown url": function() {
			assertUndefined(this.server.getCallbackForUrl());
		},

		"test server return the right cb": function() {
			var cb = function() {},
				url = "prettyThere";

			this.server.bindUrl(url, cb);
			assertEquals(this.server.getCallbackForUrl(url), cb);
		},

		"test server return the right cb if url has params": function() {
			var cb = function() {},
				url = "prettyThere",
				urlWithParams = url + "?pippo=true";

			this.server.bindUrl(url, cb);

			assertEquals(this.server.getCallbackForUrl(urlWithParams), cb);
		},

		"test server retrive url if binded with params": function() {
			var cb = function() {},
				url = "prettyThere",
				urlWithParams = url + "?pippo=true";

			this.server.bindUrl(urlWithParams, cb);

			assertEquals(this.server.getCallbackForUrl(url), cb);
		},

		"test hasResponses return false at initialization": function() {
			assertFalse(this.server.hasResponses());
		},

		"test hasResponses return yes if there are requests": function() {
			this.server.respondWith("GET", "/test",
					[200, { "Content-Type": "application/json" },
					'[{ id: 12, comment: "Hey there" }]']);

			assertTrue(this.server.hasResponses());
		}
	});
})();