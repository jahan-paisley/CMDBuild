(function() {
	var DELAY = 500;

	var tr = CMDBuild.Translation.common.splash,
	
		credits = '<ul class="splashScreen_central">'
		+ '<li> <span class="splashBold"> <a href="http://www.tecnoteca.com" target="_blank"> Tecnoteca srl </a></span> '
		+ tr.design + ', '+tr.implementation+', '+ tr.maintainer +'</li>'
		+ '<li> <span class="splashBold"> <a href="http://www.comune.udine.it" target="_blank"> ' + tr.municipality + ' </a> </span> '+ tr.principal+'</li> '
		+ '<li> <span class="splashBold"> <a href="http://www.cogitek.it" target="_blank"> Cogitek srl</a> </span> '+ tr.consultant +' </li>'		
		+ '</ul>',

		splashText = '<div class="splashScreen_central">' + 
			'<div class="spalshMotto">Open Source Configuration and Management Database</div>' +
			'<span class="splashSubTitle copyright">Copyright &copy; Tecnoteca srl</span>' +
		'</div>',

		release = '<div id="splashScreen_version">' + CMDBuild.Translation.release + '</div>';

	Ext.define("CMDBuild.view.CMMainViewport", {
		extend: "Ext.Viewport",
		layout: 'border',
		renderTo: Ext.getBody(),
		cmFirstRender: true,
		cmPanels: [],
		cmAccordions: [],
		hideAccordions: false,
		controllerType: "MainViewportController",
		statics: {
			showSplash: function(forCredit, administration) {
				var txt = forCredit ? credits : splashText;
				var opacity = forCredit ? 0.6: 1;
				var target = Ext.getBody();

				if (target) {
					if (!this.creditWin) {
						this.creditWin = new Ext.window.Window({
							closable: false,
							draggable: false,
							resizable: false
						});
					}

					if (!typeof target.mask == "function") {
						target = target.getEl();
					}

					this.theMask = target.mask();
					if (this.theMask) {
						this.theMask.show();
						this.theMask.fadeIn({
							duration: 0,
							opacity: opacity
						});

						this.theMask.on("click", function() {
							CMDBuild.view.CMMainViewport.hideSplash();
						});
					}

					this.theWin = this.creditWin;

				} else {
					if (!this.splash) {
						this.splash = new Ext.window.Window({
							modal: true,
							closable: false,
							draggable: false,
							resizable: false
						});
					}

					this.theWin = this.splash;
				}

				if (!this.imageCls) {
					this.imageCls = "splashScreen_image" + (administration ? "_administration" : "");
				}

				this.theWin.update('<div class="' + this.imageCls + '">' + txt + release + '</div>');

				this.theWin.show();
				return this;
			},

			hideSplash: function(cb) {

				var delayedCb = null;
				if (cb && typeof cb == "function") {
					delayedCb = Ext.Function.createDelayed(cb, DELAY);
				}

				if (this.theMask) {
					this.theMask.fadeOut({
						duration: DELAY
					});
				}

				if (this.theWin) {
					this.theWin.hide();
				}

				// show the header and the footer, that are initially hidden
				var hiddenCls = "cm_no_display";
				var divs = Ext.DomQuery.select("div[class="+hiddenCls+"]");
				for (var i=0, l=divs.length; i<l; ++i) {
					var e = new Ext.Element(divs[i]);
					e.removeCls(hiddenCls);
				}

				if (delayedCb != null) {
					delayedCb();
				}

				return this;
			},

			showCredits: function() {
				var l = CMDBuild.locale;
				var creditsHtml =	'<div id="cm-credits-content" class="cm-credits-container">' +
										'<div class="cm-credits-logo-container">' +
											'<img src="images/logo.jpg">' +
											'<p class="cm-credits-release-version">' + l.version + ' ' + CMDBuild.Translation.release + '</p>' + 
										'</div>' +
						
										'<div class="cm-credits-links-container">' +
											'<div class="cm-credits-links-left">' +
												'<h1>' + l.needYouHelp + '</h1>' +
												'<ul>' +
													'<li>' + l.lookAtTheManuals + '</li>' +
													'<li>' + l.goToTheForum + '</li>' +
													'<li class="cm-credit-last-link">' + l.requestTecnicalSupport+ '</li>' +
												'</ul>'+
											'</div>' +
											'<div class="cm-credits-links-right">' +
												'<h1>' + l.wouldYouFollowCMDBuild + '</h1>' +
												'<ul>' +
													'<li>' + l.subscribeToNewsLetter + '</li>' +
													'<li>' + l.folowUsOnTweeter + '</li>' +
													'<li class="cm-credit-last-link">' + l.participatesInTheLinkedInGroup + '</li>' +
												'</ul>'+
											'</div>' +
						
											'<div class="cm-credits-producer">' +
												'<h1>Credits</h1>' +
												'<p>' + l.cmdbuildIsASofwareDevelopedByTecnoteca + '</p>' +
												'<p>' + l.cmdbuildIsAtradeMarkRegisterd + '</p>' +
											'</div>' +
						
										'</div>' +
									'</div>';

				new Ext.window.Window({
					modal: true,
					resizable: false,
					contentEl: new Ext.Element({
						html: creditsHtml
					})
				}).show();
			}

		},

		initComponent : function() {
			this.splash = null;
			this.cmAccordions = new Ext.panel.Panel({
				padding: "5 0 5 5",
				margin: this.hideAccordions ? "0 2 0 0" : "0",
				region: 'west',
				split: true,
				collapsible: true, 
				collapseMode: 'mini',
				collapsed: this.hideAccordions,
				preventHeader: true,
				layout: "accordion",
				layoutConfig: {
					animate: false
				},
				items: this.cmAccordions,
				frame: false,
				border: true,
				width: 200
			});

			this.cmPanels = new Ext.panel.Panel({
				padding: "5 5 5 0",
				region: 'center',
				layout: "card",
				items: this.cmPanels,
				frame: false,
				border: false
			});

			this.header = new Ext.panel.Panel({
				border: true,
				region: "north",
				height: 45,
				contentEl: "header"
			});

			this.footer = new Ext.panel.Panel({
				border: true,
				region: "south",
				height: 18,
				contentEl: "footer"
			});

			this.items = [this.cmAccordions,this.cmPanels, this.header, this.footer];
			this.border = false;

			this.callParent(arguments);

			var creditsLink = Ext.get('cmdbuild_credits_link');
			creditsLink.on('click', function(e) {
				CMDBuild.view.CMMainViewport.showCredits();
			}, this);

			if (CMDBuild.Runtime.GroupDescriptions) {
				var defaultGroupTooltip = '<div class="msg-inner-hidden-tooltip"><p><strong>{0}:</strong> {1}</p><p><strong>{2}:</strong> {3}</p></div>';
				Ext.create('Ext.tip.ToolTip', {
					target: 'msg-inner-hidden',
					html: Ext.String.format( //
							defaultGroupTooltip, //
							CMDBuild.Translation.administration.modsecurity.groups, //
							CMDBuild.Runtime.GroupDescriptions, //
							CMDBuild.Translation.administration.modsecurity.user.defaultgroup, //
							CMDBuild.Runtime.DefaultGroupDescription //
					)
				});
			}

		},

		addAccordion: function(a) {
			Ext.suspendLayouts();
			this.cmAccordions.add(a);
			Ext.resumeLayouts();
		},

		addPanel: function(p) {
			Ext.suspendLayouts();
			this.cmPanels.add(p);
			Ext.resumeLayouts();
		},

		/*
		 * Take a function as parameter
		 * iterate over the cmAccordions and call the given
		 * function with the current accordion as parameter
		 */
		foreachAccordion: function(fn, scope) {
			if (typeof fn == "undefined") {
				throw "CMMainViewport.foreachAccordion must have a function as parameter";
			}

			this.cmAccordions.items.each(fn, scope);
		},
		/*
		 * Take a function as parameter
		 * iterate over the cmPanels and call the given
		 * function with the current accordion as parameter
		 */
		foreachPanel: function(fn, scope) {
			if (typeof fn == "undefined") {
				throw "CMMainViewport.foreachPanel must have a function as parameter";
			}
			this.cmPanels.items.each(fn, scope);
		},
		/*
		 * Search in the cmPanels the given name
		 * and bring it to front
		 */
		bringTofrontPanelByCmName: function(cmName, params, silent) {
			var p = this.findModuleByCMName(cmName),
				activatePanel = null;

			if (p) {
				activatePanel = (typeof p.beforeBringToFront != "function" || p.beforeBringToFront(params) !== false);
				if (activatePanel) {
					this.cmPanels.layout.setActiveItem(p.id);
				}
				if (silent !== true) {
					p.fireEvent("CM_iamtofront", params);
				}
			}

			return activatePanel;
		},

		deselectAccordionByName: function(cmName) {
			var a = this.findAccordionByCMName(cmName);

			var sm = a.getSelectionModel();
			sm.deselect(sm.getSelection());
		},

		findAccordionByCMName: function(cmName) {
			return this.cmAccordions.items.findBy(function(accordion) {
				return accordion.cmName == cmName;
			});
		},

		findModuleByCMName: function(cmName) {
			return this.cmPanels.items.findBy(function(panel) {
				return panel.cmName == cmName;
			});
		},

		disableAccordionByName: function(cmName) {
			var a = this.findAccordionByCMName(cmName);
			a.disable();
		},

		enableAccordionByName: function(cmName) {
			var a = this.findAccordionByCMName(cmName);
			a.enable();
		},

		getExpansedAccordion: function() {
			return this.cmAccordions.items.findBy(function(accordion) {
				return (!accordion.collapsed);
			});
		},

		getFirstAccordionWithANodeWithGivenId: function(id) {
			var currentAccordion = this.getExpansedAccordion();
			if (currentAccordion && currentAccordion.getNodeById(id)) {
				return currentAccordion;
			} else {
				var a = null;

				this.foreachAccordion(function(accordion) {
					if (a == null) {
						var node = accordion.getNodeById(id);
						if (node) {
							a = accordion;
						}
					}
				});
	
				return a;
			}
		},

		getFirstAccordionWithASelectableNode: function() {
			var a = null;

			this.foreachAccordion(function(accordion) {
				if (a == null) {
					var firstSelectableNode = accordion.getFirtsSelectableNode();
					if (firstSelectableNode) {
						a = accordion;
					}
				}
			});

			return a;
		}
	});
})();