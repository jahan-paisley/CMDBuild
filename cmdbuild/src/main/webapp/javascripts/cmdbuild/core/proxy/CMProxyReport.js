(function() {

	CMDBuild.ServiceProxy.report = {
		getMenuTree: function(p) {
			p.method = 'GET';
			p.url = "services/json/schema/modreport/menutree",

			CMDBuild.ServiceProxy.core.doRequest(p);
		},

		getTypesTree: function(p) {
			p.method = 'GET';
			p.url = "services/json/management/modreport/getreporttypestree";

			CMDBuild.ServiceProxy.core.doRequest(p);
		}
	};

})();