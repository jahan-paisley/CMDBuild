(function() {

var configurationURL = "services/json/schema/setup/getconfiguration";
var allConfigurationsURL = "services/json/schema/setup/getconfigurations";

var CMDBUILD = 'cmdbuild';
var WORKFLOW = 'workflow';
var GIS = 'gis';
var BIM = 'bim';
var GRAPH = "graph";

CMDBuild.ServiceProxy.configuration = {
	read: readConf,

	readMainConfiguration: function(p) {
		readConf(p, CMDBUILD);
	},

	readWFConfiguration: function(p) {
		readConf(p, WORKFLOW);
	},

	readGisConfiguration: function(p) {
		readConf(p, GIS);
	},

	readBimConfiguration: function(p) {
		readConf(p, BIM);
	},

	readAll: function(p) {
		p.method = "GET";
		p.url = allConfigurationsURL;
		p.params = { names: Ext.JSON.encode([CMDBUILD, WORKFLOW, GIS, BIM, GRAPH])};

		CMDBuild.ServiceProxy.core.doRequest(p);
	},

	save: function(p, name) {
		p.method = "POST";
		p.url = "services/json/schema/setup/saveconfiguration",
		p.params.name = name;
		CMDBuild.ServiceProxy.core.doRequest(p);
	}
};

function readConf(p, name) {
	p.method = "GET";
	p.url = configurationURL;
	p.params = { name: name };

	CMDBuild.ServiceProxy.core.doRequest(p);
}
})();