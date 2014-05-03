if (typeof CMDBuild == "undefined") {
	CMDBuild = {};
}

CMDBuild.LoadMask = {
	get: function(text) {
		if (!CMDBuild.LoadMask.instance) {
			CMDBuild.LoadMask.instance = new Ext.LoadMask({
				target: Ext.getBody()
			});
		}
		CMDBuild.LoadMask.instance.msg = text || CMDBuild.Translation.common.wait_title;
		return CMDBuild.LoadMask.instance;
	}
};

/**
 * @class CMDBuild.Ajax
 * @extends Ext.data.Connection
 * Ajax request class that automatically checks for success and implements a
 * default failure method. The success and failure methods are called with an
 * additional parameter representing the decoded response. Example usage:
 * <pre><code>
CMDBuild.Ajax.request({
	important: true, // errors are popups
	url: 'services/json/schema/setup/getconfiguration',
	params: { name: 'cmdbuild' },
	success: function(response, options, decoded) {
		CMDBuild.Config.cmdbuild = decoded.data;
		initLogin();
	}
});
 * @singleton
 */
CMDBuild.Ajax =  new Ext.data.Connection({
	showMaskAndTrapCallbacks: function(object, options) {
		if (options.loadMask) {
			CMDBuild.LoadMask.get().show();
		}
		this.trapCallbacks(object, options);
	},

	trapCallbacks: function(object, options) {
		var failurefn;
		var callbackScope = options.scope || this;
		options.success = Ext.bind(this.unmaskAndCheckSuccess, callbackScope, [options.success], true);
		/**
		 * the error message is not shown if options.failure
		 * is present and returns false
		 */
		if (options.failure) {
			failurefn = Ext.Function.createInterceptor(this.defaultFailure, options.failure, callbackScope);
		} else {
			failurefn = Ext.bind(this.defaultFailure, this);
		}
		options.failure = Ext.bind(this.decodeFailure, this, [failurefn], true);
	},

	unmaskAndCheckSuccess: function(response, options, successfn) {
		if (options.loadMask) {
			CMDBuild.LoadMask.get().hide();
		}
		var decoded = CMDBuild.Ajax.decodeJSONwhenMultipartAlso(response.responseText);
		CMDBuild.Ajax.displayWarnings(decoded);
		if (!decoded || decoded.success !== false) {
			Ext.callback(successfn, this, [response, options, decoded]);
		} else {
			Ext.callback(options.failure, this, [response, options]);
		}
	},

	decodeJSONwhenMultipartAlso: function(jsonResponse) {
		var fixedResponseForMultipartExtBug = jsonResponse;
		if (jsonResponse) {
			fixedResponseForMultipartExtBug = jsonResponse.replace(/<\/\w+>$/,"");
		}
		return Ext.JSON.decode(fixedResponseForMultipartExtBug);
	},

	displayWarnings: function(decoded) {
		if (decoded && decoded.warnings && decoded.warnings.length) {
			for (var i=0; i<decoded.warnings.length; ++i) {
				var w = decoded.warnings[i];
				var errorString = CMDBuild.Ajax.formatError(w.reason, w.reasonParameters);
				if (errorString) {
					CMDBuild.Msg.warn(null, errorString);
				} else {
					CMDBuild.log.warn("Cannot print warning message", w);
				}
			}
		}
	},

	decodeFailure: function(response, options, failurefn) {
		var decoded = CMDBuild.Ajax.decodeJSONwhenMultipartAlso(response.responseText);
		Ext.callback(failurefn, this, [response, options, decoded]);
	},

	defaultFailure: function(response, options, decoded) {
		if (decoded && decoded.errors && decoded.errors.length) {
			for (var i=0; i<decoded.errors.length; ++i) {
				this.showError(response, decoded.errors[i], options);
			}
		} else {
			this.showError(response, null, options);
		}
	},

	showError: function(response, error, options) {
		var tr = CMDBuild.Translation.errors || {
			error_message : "Error",
			unknown_error : "Unknown error",
			server_error_code : "Server error: ",
			server_error : "Server error"
		};
		var errorTitle = null;
		var errorBody = {
				text: tr.unknown_error,
				detail: undefined
		};

		if (error) {
			// if present, add the url that generate the error
			var detail = "";
			if (options && options.url) {
				detail = "Call: " + options.url + "\n";
				var line = "";
				for (var i=0; i<detail.length; ++i) {
					line += "-";
				}

				detail += line + "\n";
			}

			// then add to the details the server stacktrace
			errorBody.detail = detail + "Error: " + error.stacktrace;
			var reason = error.reason;
			if (reason) {
				if (reason == 'AUTH_NOT_LOGGED_IN' || reason == 'AUTH_MULTIPLE_GROUPS') {
					CMDBuild.LoginWindow.addAjaxOptions(options);
					CMDBuild.LoginWindow.setAuthFieldsEnabled(reason == 'AUTH_NOT_LOGGED_IN');
					CMDBuild.LoginWindow.show();
					return;
				}
				var translatedErrorString = CMDBuild.Ajax.formatError(reason, error.reasonParameters);
				if (translatedErrorString) {
					errorBody.text = translatedErrorString;
				}
			}
		} else {
			if (!response || response.status == 200 || response.status == 0) {
				errorTitle = tr.error_message;
				errorBody.text = tr.unknown_error;
			} else if (response.status) {
				errorTitle = tr.error_message;
				errorBody.text = tr.server_error_code+response.status;
			}
		}

		var popup = options.form || options.important;

		CMDBuild.Msg.error(errorTitle, errorBody, popup);
	},

	formatError: function(reasonName, reasonParameters) {
		var tr = CMDBuild.Translation.errors.reasons;

		if (tr && tr[reasonName]) {
			return Ext.String.format.apply(null, [].concat(tr[reasonName]).concat(reasonParameters));
		} else {
			return "";
		}
	},
	
	/*
	 * From Ext.Ajax
	 */
	autoAbort: false,
	serializeForm: function(form) {
		return Ext.lib.Ajax.serializeForm(form);
	}
});

CMDBuild.Ajax.on('beforerequest', CMDBuild.Ajax.showMaskAndTrapCallbacks);
Ext.Ajax = CMDBuild.Ajax;

/**
 * @class CMDBuild.ChainedAjax
 * Executes a series of CMDBuild.Ajax.request one after the other. When it has
 * finished, it executes the fn function with the specified scope or this.
 * Example usage:
 * <pre><code>
	CMDBuild.ChainedAjax.execute({
		loadMask: true,
		requests: [{
			url: 'services/json/utils/success',
			success: function(response, options, decoded) {
				alert('First');
			}
		},{
			url: 'services/json/utils/success',
			success: function(response, options, decoded) {
				alert('Second');
			}
		}],
		fn: function() {
			alert('Done');
		}
	});
	</code></pre>
 * @singleton
 */
CMDBuild.ChainedAjax = {
    execute: function(o) {
		this.executeNextAndWait(o, 0);
    },

    // private
    executeNextAndWait: function(o, index) {
    	if (index < o.requests.length) {
    		this.showMask(o, index);
	    	var requestObject = Ext.apply(o.requests[index]);
	    	var execNext = Ext.bind(this.executeNextAndWait,this, [o, index+1]);
			if (requestObject.success)
		    	requestObject.success = Ext.Function.createSequence(requestObject.success, execNext);
		    else
		    	requestObject.success = execNext;
			requestObject.loadMask = requestObject.loadMask && !o.loadMask;
	    	CMDBuild.Ajax.request(requestObject);
    	} else {
    		if (o.loadMask) {
    			CMDBuild.LoadMask.get().hide();
    		}
    		Ext.callback(o.fn, o.scope || this);
    	}
    },
    
    //private
    showMask: function(o, index) {
    	if (o.loadMask) {
			if (o.requests[index].maskMsg) {
				var m = CMDBuild.LoadMask.get(o.requests[index].maskMsg);
				m.show();
			}
		}
    }
};

CMDBuild.ConcurrentAjax = {
	execute: function(o) {
		var counter = o.requests.length;
		for (var i=0, l=o.requests.length; i<l; ++i) {
			var requestConfig = Ext.apply(o.requests[i]);
			this.showMask(o, i);
			requestConfig.callback = function() {
				if (--counter == 0) {
					CMDBuild.LoadMask.get().hide();
					o.fn.call(o.scope || this);
				}
			}
			CMDBuild.Ajax.request(requestConfig);
		}
	},
	//private
	showMask: function(o, index) {
		if (o.loadMask) {
			if (o.requests[index].maskMsg) {
				var m = CMDBuild.LoadMask.get(o.requests[index].maskMsg);
				m.show();
			}
		}
	}
};