(function() {
	TestCase("test pimped Ext.Ajax", {

		setUp: function() {
			this.server = CMDBuild.test.CMServer.create();
		},

		tearDown: function() {
			this.server.restore();
		},

		"test doesn't call getCallbackForUrl if CMServer is not setted": function() {
			var anUnsettedServer = new CMDBuild.test.CMServer();
			anUnsettedServer.getCallbackForUrl = sinon.spy();
			anUnsettedServer.bindUrl("/test", function() {});

			Ext.Ajax.request( {
				url : '/test',
				params : {}
			});

			assertFalse(anUnsettedServer.getCallbackForUrl.called);
		},

		"test call getCallbackForUrl if CMServer is setted": function() {
			this.server.bindUrl("/test", function() {});
			this.server.getCallbackForUrl = sinon.spy();

			Ext.Ajax.request({
				url : '/test',
				params : {}
			});

			assertTrue(this.server.getCallbackForUrl.called);
		},

		"test that the fakeResponse is setted": function() {
			this.server.bindUrl("/test", function() {});

			assertFalse(this.server.hasResponses());

			Ext.Ajax.request({
				url : '/test',
				params : {}
			});

			assertTrue(this.server.hasResponses());
		},

		"test that the server response well": function() {
			var _response = '{"success":true, "prettyThere": true}';
			var spy = sinon.spy();

			this.server.bindUrl("/test", function() {
				return _response;
			});

			Ext.Ajax.request({
				url : '/test',
				params : {},
				success: function(response, opt) {
					spy(response.responseText);
				}
			});

			assertTrue(spy.calledWith(_response));
		},

		"test that the server response well analyzing params": function() {
			var _response1 = '{"success":true, "pippo": true}';
			var _response2 = '{"success":true, "pippo": false}';

			var spy = sinon.spy();

			this.server.bindUrl("/test", function(params) {
				if (params.pippo) {
					return _response1;
				} else {
					return _response2;
				}
			});

			Ext.Ajax.request({
				url : '/test',
				method: "GET",
				params : {
					pippo: true
				},
				success: function(response, opt) {
					spy(response.responseText);
				}
			});

			assertTrue(spy.calledWith(_response1));

			Ext.Ajax.request({
				url : '/test',
				method: "GET",
				params : {
					pippo: false
				},
				success: function(response, opt) {
					spy(response.responseText);
				}
			});

			assertTrue(spy.calledWith(_response2));
		}
	});
})();