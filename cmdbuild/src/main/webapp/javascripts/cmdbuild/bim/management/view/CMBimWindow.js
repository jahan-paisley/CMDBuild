(function() {

	var CANVAS_ID = "scenejsCanvas";

	Ext.define("CMDBuild.bim.management.view.CMBimWindow", {
		extend: "CMDBuild.PopupWindow",
		titleAlign: 'center',
		initComponent: function() {
			this.CANVAS_ID = CANVAS_ID;

			/*
			 * IMPORTANT!!
			 * There are unsolvable problems
			 * trying to destroy the sceneJs.
			 * 
			 * So do not destroy the
			 * window, but only hide, and
			 * reuse the same window.
			 */
			this.closeAction = 'hide';

			this.plain = true;
			this.frame = false;

			this.layout = "border",

			this.controlPanel = new CMDBuild.bim.management.view.CMBimControlPanel({
				delegate: this.delegate
			});

			this.layerPanel = new CMDBuild.bim.management.view.CMBimPlayerLayers({
				title: CMDBuild.Translation.layers,
				delegate: this.delegate,
				border: false
			});

			this.tree = new CMDBuild.bim.management.view.CMBimTree({
				title: CMDBuild.Translation.tree,
				border: false,
				frame: false,
				delegate: this.delegate
			});

			this.items = [{
					layout: "accordion",
					border: false,
					width: "30%",
					split: true,
					region: "west",
					items: [ //
						this.controlPanel, //
						this.layerPanel, //
						this.tree
					]
				},
				{
					border: false,
					frame: false,
					plain: true,
					region: "center",
					html: '<canvas class="bim-canvas" id="' + CANVAS_ID + '"></canvas>'
				}
			];

			this.callParent(arguments);
		},

		loadLayers: function(data) {
			this.layerPanel.loadLayers(data);
		},

		selectLayer: function(layerName) {
			this.layerPanel.selectLayer(layerName);
		},

		resetControls: function() {
			this.controlPanel.reset();
		},

		enableObjectSliders: function() {
			this.controlPanel.enableObjectSliders();
		},

		disableObjectSliders: function() {
			this.controlPanel.disableObjectSliders();
		},

		setTreeRootNode: function(rootNode) {
			var s = this.tree.getStore();
			s.setRootNode(rootNode);
		}
	});

})();