(function() {
	Ext.define("CMDBuild.view.management.CMMiniCardGridWindowDelegate", {
		/**
		 * @param {CMDBuild.view.management.CMMiniCardGridWindow} window this window
		 */
		miniCardGridWindowDidShown: Ext.emptyFn
	});

	var LI_TAG_FORMAT = '<li><span class="cm-bold">{0}:</span> {1}</li>';

	Ext.define("CMDBuild.view.management.CMMiniCardGridWindow", {
		extend: "Ext.window.Window",

		mixins: {
			delegable: "CMDBuild.core.CMDelegable"
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this,
					"CMDBuild.view.management.CMMiniCardGridWindowDelegate");

			this.callParent(arguments);
		},

		withDetailExpander: true,

		initComponent: function() {

			if (!this.dataSource) {
				throw "A data source is needed";
				return;
			}

			this.ghost = false;
			this.layout = "border",

			this.miniCardGrid = new CMDBuild.view.management.CMMiniCardGrid({
				frame: false,
				border: false,
				withPagingBar: false,
				denySelection: false,
				dataSource: this.dataSource,
				region: "center",
				columns: [{
					text: CMDBuild.Translation.administration.modsecurity.privilege.classname,
					dataIndex: 'ClassName',
					flex: 1,
					sortable: true
				}]
			});

			if (this.withDetailExpander) {
				this.detailsPanel = new Ext.panel.Panel({
					height: "50%",
					split: true,
					region: "south",
					border: false,
					frame: false,
					autoScroll: true,
					collapsed: true,
					collapseMode: "mini"
				});

				this.items = [this.miniCardGrid, this.detailsPanel];
			} else {
				this.items = [this.miniCardGrid];
			}

			this.mon(this, "show", function() {
				this.callDelegates("miniCardGridWindowDidShown", this);
			}, this);

			this.callParent(arguments);
		},

		getMiniCardGrid: function() {
			return this.miniCardGrid;
		},

		clearDetailsPanel: function() {
			if (this.detailsPanel) {
				this.detailsPanel.removeAll();
			}
		},

		showDetailsForCard: function(card) {
			this.clearDetailsPanel();
			if (!card) {
				return;
			}

			var me = this;
			_CMCache.getAttributeList(card.get("IdClass"), function(attributes) {

				if (me.detailsPanel) {
	
					me.detailsPanel.expand();
	
					var details = card.getDetails();
					var html = '<ul class="cm_detailed_mini_card_grid_window_detail">';
	
					for (var i=0, a=null, detail=null; i<attributes.length; ++i) {
						a = attributes[i];
						detail = details[a.name];
	
						if (detail) {
							if (typeof detail == "object") {
								detail = detail.description
							}
							html += Ext.String.format(LI_TAG_FORMAT, a.name, detail);
						}
					}
	
					html += "</ul>";
	
					me.detailsPanel.add({
						html: html,
						frame: false,
						border: false
					});
				}
			})


		}
	});
})();