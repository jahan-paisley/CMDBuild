(function() {

	var navigationTrees = null;
	Ext.define("CMDBUild.cache.CMCacheNavigationTreesFunctions", {
		
		observers: [],
		lastEntry: "",
		listNavigationTrees: function(p) {
			p.method = "GET";
			var appSuccess = p.success;
			p.success = function(response, options, decoded) {
				navigationTrees = decoded.response;
				appSuccess(response, options, decoded);
			};
			p.url = CMDBuild.ServiceProxy.url.navigationTrees.get;
			CMDBuild.ServiceProxy.core.doRequest(p);
		},
		
		registerOnNavigationTrees: function(observer) {
			this.observers.push(observer);
		},

		//private
		refreshObserversNavigationTrees: function() {
			for (var i = 0; i < this.observers.length; i++) {
				this.observers[i].refresh();
			}
		},
		
		getNavigationTrees: function() {
			return {
				data: navigationTrees,
				lastEntry: this.lastEntry
			};
		},
			
		saveNavigationTrees: function(formData, success) {
			_CMProxy.navigationTrees.save({
				params: formData,
				success: function(operation, request, decoded) {
					success(operation, request, decoded);
				}
			});
		},
		
		createNavigationTrees: function(formData, success) {
			var me = this;
			this.lastEntry = formData.name;
			_CMProxy.navigationTrees.create({
				params: formData,
				success: function(operation, request, decoded) {
					me.listNavigationTrees({
						success: function() {
							me.refreshObserversNavigationTrees();
							success(operation, request, decoded);
						},
						callback:  Ext.emptyFn
					});
				}
			});
		},
		
		readNavigationTrees: function(me, name, success) {
			_CMProxy.navigationTrees.read({
				params: {
					name: name
				},
				success: function(operation, request, decoded) {
					me.tree = Ext.JSON.decode(decoded.response);
					success(me, me.tree);
				}
			});
		},

		removeNavigationTrees: function(name, success) {
			var me = this;
			this.lastEntry = "";
			_CMProxy.navigationTrees.remove({
				params: {
					name: name
				},
				success: function(operation, request, decoded) {
					me.listNavigationTrees({
						success: function() {
							me.refreshObserversNavigationTrees();
						},
						callback:  Ext.emptyFn
					});
				}
			});
		},
		
	});

})();