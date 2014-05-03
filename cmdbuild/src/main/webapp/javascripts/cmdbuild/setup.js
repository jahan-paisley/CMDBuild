(function() {

	CMDBuild.LABEL_WIDTH = 250;

	Ext.define('CMDBuild.app.Setup', {
		statics: {
			init: function() {
				Ext.QuickTips.init();//for the error tips

				this.step1 = new CMDBuild.setup.Step1();
				this.step2 = new CMDBuild.setup.Step2();
				this.step3 = new CMDBuild.setup.Step3();

				this.nextButton = new Ext.button.Button({
					text: CMDBuild.Translation.configure.next
				});

				this.prevButton = new Ext.button.Button({
					text: CMDBuild.Translation.configure.previous,
					disabled: true
				});

				this.finishButton = new Ext.button.Button({
					text: CMDBuild.Translation.configure.finish,
					hidden: true
				});

				this.cardPanel = new Ext.panel.Panel({
					layout: 'card',
					region: 'center',
					border: false,
					frame: true,
					activeItem: 0,
					items: [this.step1, this.step2, this.step3],
					buttons: [this.prevButton, this.nextButton, this.finishButton]
				});

				this.bringToFront = function(panel) {
					this.cardPanel.layout.setActiveItem(panel.id);
				}

				this.showNextButton = function(show) {
					this.finishButton.setVisible(!show);
					this.nextButton.setVisible(show);
				}

				new Ext.Viewport({
					layout:'border',
					frame: false,
					border: false,
					items:[
						this.cardPanel,
						{
							frame: false,
							border: false,
							region:'north',
							id: 'header_panel',
							contentEl: 'header',
							height: 45
						},
						{
							frame: false,
							border: false,
							region: 'south',
							id: 'footer_panel',
							contentEl: 'footer',
							border: false,
							height: 20
						}
					]
				});

				new CMDBuild.setup.CMSetupController(this);
			}
		}
	});

})();