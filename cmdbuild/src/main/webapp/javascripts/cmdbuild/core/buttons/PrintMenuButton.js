(function() {
	Ext.define("CMDBuild.PrintMenuButton",  {
		extend: "Ext.button.Split",
		text: CMDBuild.Translation.common.buttons.print,
		iconCls: 'print',

		/**
		 * when instantiate the button you can chose between these options
		 * */
		formatList: ['pdf', 'csv', 'odt', 'rtf'],
	
		initComponent: function(){
			this.menu = new Ext.menu.Menu();
			this.handler = function() {
				if (!this.disabled) {			
					this.showMenu();
				}
			}
			this.callParent(arguments);
			this.fillMenu();
		},
	
		//private
		fillMenu: function() {
			var formats = this.formatList;
			var menu = this;
			for (var i=0, l=formats.length; i<l; i++) {
				var format = formats[i];
				this.menu.add({
					text: CMDBuild.Translation.common.buttons.as + " " + format.toUpperCase(),
					iconCls: format,
					format: format,
					handler: function() {
						menu.fireEvent("click", this.format);
					}
				});	
			}
		}
	});
})();