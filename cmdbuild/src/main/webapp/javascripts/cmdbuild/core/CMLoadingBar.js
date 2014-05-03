Ext.define("CMDBuild.common.CMLoadingBar", {
	extend: "Ext.window.Window",

	initComponent: function() {
		this.progressBar = Ext.create('Ext.ProgressBar', {
			renderTo: Ext.getBody(),
			width: 300
		});

		this.minHeight = 30;
		this.header = false;
		this.border = false;
		this.frame = false;
		this.modal = true;
		this.resizable = false;

		this.items = [{
			boder: false,
			frame: false,
			items: [this.progressBar]
		}];

		this.callParent(arguments);
	},

	/**
	 * Change the displayed text inside the progress bar
	 * 
	 * @param(String) text the text to display
	 */
	setText: function(text) {
		this.progressBar.updateText(text);
	},

	/**
	 * Update the status bar filling
	 * the available space in relation
	 * at the given number
	 * 
	 * @param {Number} progress
	 * a number between 0 and 100
	 */
	setProgress: function(progress) {
		var progressForExt = progress / 100;
		this.progressBar.updateProgress(progressForExt);
	},

	/**
	 * this must indicate the end of
	 * loading and begin of processing
	 * data. Do something like pulse...
	 */
	beginProcessing: function() {
		// Style only, not implemented yet
	},

	/**
	 * Clean the progress indicator
	 * and remove the displayed text
	 */
	reset: function() {
		this.setProgress(0);
		this.setText();
	}

});