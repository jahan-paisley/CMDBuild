(function() {
	CMDBuild.ServiceProxy.attachment = {

		download: function(params) {
			var url = 'services/json/attachments/downloadattachment?' + Ext.urlEncode(params);
			window.open(url, "_blank");
		},

		getattachmentdefinition: function(p) {
			p.method = "GET";
			p.url = "services/json/attachments/getattachmentscontext";

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		remove: function(p) {
			p.method = "POST";
			p.url = "services/json/attachments/deleteattachment";

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};
})();