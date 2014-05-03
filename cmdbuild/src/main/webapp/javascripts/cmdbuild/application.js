(function() {

	Ext.ns("CMDBuild");

	// global constants
	CMDBuild.LABEL_WIDTH = 150;

	CMDBuild.BIG_FIELD_ONLY_WIDTH = 475;
	CMDBuild.MEDIUM_FIELD_ONLY_WIDTH = 150;
	CMDBuild.SMALL_FIELD_ONLY_WIDTH = 100;
	CMDBuild.BIG_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.BIG_FIELD_ONLY_WIDTH;
	CMDBuild.MEDIUM_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.SMALL_FIELD_ONLY_WIDTH;
	CMDBuild.SMALL_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + CMDBuild.SMALL_FIELD_ONLY_WIDTH;

	CMDBuild.ADM_BIG_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 250;
	CMDBuild.ADM_MEDIUM_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 150;
	CMDBuild.ADM_SMALL_FIELD_WIDTH = CMDBuild.LABEL_WIDTH + 80;

	CMDBuild.CFG_LABEL_WIDTH = 300;
	CMDBuild.CFG_BIG_FIELD_WIDTH = CMDBuild.CFG_LABEL_WIDTH + 450;
	CMDBuild.CFG_MEDIUM_FIELD_WIDTH = CMDBuild.CFG_LABEL_WIDTH + 150;

	// global object with runtime configuration
	CMDBuild.Config = {};

	CMDBuild.log = log4javascript.getLogger();
	CMDBuild.log.addAppender(new log4javascript.BrowserConsoleAppender());

	// convenience methods to debug
	_debug = function() {
		var prefix = "DEBUG";

		if (typeof arguments[0] == "string")
			arguments[0] = prefix + ": " + arguments[0];

		CMDBuild.log.debug.apply(CMDBuild.log, arguments);
	};

	_warning = function() {
		var prefix = "WARNING";

		if (typeof arguments[0] == "string")
			arguments[0] = prefix + ": " + arguments[0];

		CMDBuild.log.warn.apply(CMDBuild.log, arguments);
	};

	_trace = function() {
		_debug("TRACE", arguments);

		if (console && typeof console.trace == "function")
			console.trace();
	};

	_deprecated = function() {
		var name = "";

		try {
			name  = arguments.callee.caller.name;
		} catch (e) {
			_debug("DEPRECATED", _trace());
		}

		_debug("DEPRECATED: " + name, _trace());
	};

	// TODO: Read from real configuration
	CMDBuild.Config.defaultTimeout = 90;

	Ext.override('Ext.data.Connection', {
		timeout: CMDBuild.Config.defaultTimeout * 1000
	});

	Ext.override('Ext.data.proxy.Ajax', {
		timeout: CMDBuild.Config.defaultTimeout * 1000
	});

	Ext.override('Ext.form.BasicForm', {
		timeout: CMDBuild.Config.defaultTimeout
	});

	// Component masks are shown at 20000 z-index. This oddly fixes
	// the problem of masks appearing on top of new windows.
	// Ext.WindowMgr.zseed = 30000;

	Ext.WindowManager.getNextZSeed();	// to increase the default zseed. Is needed for the combo on windoows
										// probably it fix also the prev problem
	Ext.enableFx = false;

})();