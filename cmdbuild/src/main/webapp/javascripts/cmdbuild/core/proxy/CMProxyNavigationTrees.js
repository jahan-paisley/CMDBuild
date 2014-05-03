(function() {

	_CMProxy.navigationTrees = {
			read: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'GET',
					url: CMDBuild.ServiceProxy.url.navigationTrees.read,
					params: parameters.params,
					success: parameters.success
				});
			},

			create: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.navigationTrees.create,
					params: parameters.params,
					success: parameters.success
				});
			},
			
			save: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.navigationTrees.save,
					params: parameters.params,
					success: parameters.success
				});
			},
			remove: function(parameters, success) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: CMDBuild.ServiceProxy.url.navigationTrees.remove,
					params: parameters.params,
					success: parameters.success
				});
			}
	};

})();