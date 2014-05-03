(function() {

	Ext.define('CMDBuild.core.proxy.CMProxyWorkflow', {
		alternateClassName: 'CMDBuild.ServiceProxy.workflow', // Legacy class name

		statics: {
			getstartactivitytemplate: function(classId, p) {
				CMDBuild.ServiceProxy.core.doRequest(Ext.apply({
					url: CMDBuild.ServiceProxy.url.workflow.getStartActivity,
					method: 'GET',
					params: {
						classId: classId
					}
				}, p));
			},

			getActivityInstance: function(params, conf) {
				conf.url = CMDBuild.ServiceProxy.url.workflow.getActivityInstance,
				conf.method = 'GET';
				conf.params = params;
				conf.important = true;

				if (typeof conf.callback == "undefined") {
					conf.callback = function() {
						CMDBuild.LoadMask.get().hide();
					};
				}

				CMDBuild.ServiceProxy.core.doRequest(conf);
			},

			isPorcessUpdated: function(p) {
				p.url = CMDBuild.ServiceProxy.url.workflow.isProcessUpdated,
				p.method = 'GET';

				CMDBuild.ServiceProxy.core.doRequest(p);
			},

			terminateActivity: function(p) {
				p.url = CMDBuild.ServiceProxy.url.workflow.abortProcess,
				p.method = 'POST';

				CMDBuild.ServiceProxy.core.doRequest(p);
			},

			saveActivity: function(p) {
				p.url = CMDBuild.ServiceProxy.url.workflow.saveActivity,
				p.method = 'POST';

				CMDBuild.ServiceProxy.core.doRequest(p);
			}
		}
	});

})();