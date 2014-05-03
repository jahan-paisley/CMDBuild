(function() {

	// TODO move in common
	var menuAccordion = new CMDBuild.view.administration.accordion.CMMenuAccordion({
		cmControllerType: CMDBuild.controller.management.menu.CMMenuAccordionController
	});
	// TODO move in common
	var reportAccordion = new CMDBuild.view.common.report.CMReportAccordion();
	// TODO move in common
	var classesAccordion = new CMDBuild.view.common.classes.CMClassAccordion({
		title: CMDBuild.Translation.administration.modClass.tree_title
	});
	// TODO move in common
	var processAccordion = new CMDBuild.view.administration.accordion.CMProcessAccordion({
		rootVisible: true
	});
	// TODO move in common
	var dashboardsAccordion = new CMDBuild.view.administration.accordion.CMDashboardAccordion();
	var dataViewAccordion = new CMDBuild.view.management.dataView.CMDataViewAccordion({
		cmControllerType: CMDBuild.controller.management.common.CMFakeIdAccordionController
	});

	Ext.define("CMDBuild.app.Management", {
		statics: {
			init: function() {

				Ext.tip.QuickTipManager.init();
				// fix a problem of Ext 4.2 tooltips width
				// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
				delete Ext.tip.Tip.prototype.minWidth;

				var me = this,
					cb = function() {
						me.buildComponents();
					};

				CMDBuild.view.CMMainViewport.showSplash();

				// maybe a single request with all the configuration could be better
				CMDBuild.ServiceProxy.group.getUIConfiguration({
					success: function(response, options,decoded) {
						_CMUIConfiguration = new CMDBuild.model.CMUIConfigurationModel(decoded.response);

						CMDBuild.ServiceProxy.configuration.readAll({
							success: function(response, options, decoded) {
								// cmdbuild
								CMDBuild.Config.cmdbuild = decoded.cmdbuild;

								// bim
								CMDBuild.Config.bim = decoded.bim;
								CMDBuild.Config.bim.enabled = ('true' == CMDBuild.Config.bim.enabled);

								// graph
								CMDBuild.Config.graph = decoded.graph;

								// workflow
								CMDBuild.Config.workflow = decoded.workflow;

								// gis
								CMDBuild.Config.gis = decoded.gis;
								CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

								// gis and bim extra configuration
								CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration = {};
								CMDBuild.ServiceProxy.gis.getGisTreeNavigation({
									success: function(operation, config, response) {
										CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration.root = response.root;
										CMDBuild.Config.cmdbuild.cardBrowserByDomainConfiguration.geoServerLayersMapping = response.geoServerLayersMapping;

										if (CMDBuild.Config.bim.enabled) {
											CMDBuild.bim.proxy.rootClassName({
												success: function(operation, config, response) {
													CMDBuild.Config.bim.rootClass = response.root;
												},
												callback: cb
											});
										} else {
											cb();
										}

									} 
								});

							}
						});

					}
				});
			},

			buildComponents: function() {
				/* **********************************************
				 * Suspend here the layouts, and resume after all
				 * the load are end
				 * **********************************************/
				Ext.suspendLayouts();
				/* ***********************************************/

				this.cmAccordions = [
					this.menuAccordion = menuAccordion
				];

				this.cmPanels = [
					new Ext.panel.Panel({}),
					this.cardPanel = new CMDBuild.view.management.classes.CMModCard({
						cmControllerType: CMDBuild.controller.management.classes.CMModCardController
					}),

					this.processPanel = new CMDBuild.view.management.workflow.CMModProcess({
						cmControllerType: CMDBuild.controller.management.workflow.CMModWorkflowController
					}),

					this.reportPanel = new CMDBuild.view.common.report.CMReportGrid({
						cmName: "report",
						cmControllerType: CMDBuild.controller.management.report.CMModReportController
					}),
					this.singleReportPanel = new CMDBuild.view.common.report.CMMainSingleReportPage({
						cmName: "singlereport",
						cmControllerType: CMDBuild.controller.management.report.CMSingleReportPageController
					}),

					this.dashboardPanel = new CMDBuild.view.management.dashboard.CMModDashboard({
						cmControllerType: CMDBuild.controller.management.dashboard.CMModDashboardController
					}),

					this.dataViewPanel = new CMDBuild.view.management.dataView.CMModSQLDataView({
						cmControllerType: CMDBuild.controller.management.dataView.CMModCardController
					})
				];

				if (!_CMUIConfiguration.isModuleDisabled(classesAccordion.cmName)) {
					this.classesAccordion = classesAccordion;
					this.cmAccordions.push(this.classesAccordion);
				}

				if (!_CMUIConfiguration.isModuleDisabled(processAccordion.cmName) && CMDBuild.Config.workflow.enabled == "true") {
					this.processAccordion = processAccordion;
					this.cmAccordions.push(this.processAccordion);
				}
				if (!_CMUIConfiguration.isModuleDisabled(dataViewAccordion.cmName)) {
					this.dataViewAccordion = dataViewAccordion;
					this.cmAccordions.push(this.dataViewAccordion);
				}

				if (!_CMUIConfiguration.isModuleDisabled(dashboardsAccordion.cmName)) {
					this.dashboardsAccordion = dashboardsAccordion;
					this.cmAccordions.push(this.dashboardsAccordion);
				}

				if (!_CMUIConfiguration.isModuleDisabled(reportAccordion.cmName)) {
					this.reportAccordion = reportAccordion;
					this.cmAccordions.push(this.reportAccordion);
				}

				this.utilitiesTree = new CMDBuild.administration.utilities.UtilitiesAccordion({ // TODO move in common
					title: CMDBuild.Translation.management.modutilities.title
				});

				if (this.utilitiesTree.getRootNode().childNodes.length > 0)
					this.cmAccordions.push(this.utilitiesTree);

				for (var moduleName in this.utilitiesTree.submodules) {
					var cmName = this.utilitiesTree.getSubmoduleCMName(moduleName);

					if (!_CMUIConfiguration.isModuleDisabled(cmName))
						addUtilitySubpanel(cmName, this.cmPanels);
				}
				
				this.loadResources();

				if (_CMUIConfiguration.isFullScreenMode())
					_CMUIState.onlyGrid();
			},

			loadResources: function() {
				_CMCache.syncAttachmentCategories();

				var me = this,
					reqBarrier = new CMDBuild.Utils.CMRequestBarrier(function callback() {
						hideIfEmpty(processAccordion);
						hideIfEmpty(reportAccordion);
						hideIfEmpty(menuAccordion);
						hideIfEmpty(classesAccordion);

						_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(
							new CMDBuild.view.CMMainViewport({
								cmAccordions: me.cmAccordions,
								cmPanels: me.cmPanels,
								hideAccordions: _CMUIConfiguration.isHideSidePanel()
							})
						);

						/* *********************************
						 * Resume here the layouts operations
						 */
						Ext.resumeLayouts(true);
						/* *********************************/

						_CMMainViewportController.viewport.doLayout();

						CMDBuild.view.CMMainViewport.hideSplash(function() {
							_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);
							_CMMainViewportController.selectStartingClass();
						});
					});

				CMDBuild.ServiceProxy.classes.read({
					params: {
						active: true
					},
					scope: this,
					success: function(response, options, decoded) {
						_CMCache.addClasses(decoded.classes);
						classesAccordion.updateStore();
						processAccordion.updateStore();

						// Do a separate request for the widgets because, at this time
						// it is not possible serialize them with the classes
						CMDBuild.ServiceProxy.CMWidgetConfiguration.read({
							scope: this,
							success: function(response, options, decoded) {
								// a day I'll can do a request to have only the active, now the cache
								// discards the inactive if the flag onlyActive is true
								_CMCache.addWidgetToEntryTypes(decoded.response, onlyActive = true);
							},
							callback: reqBarrier.getCallback()
						});

						// to fill the menu is needed that the classes are already
						// loaded
						var readMenuParams = {};
						readMenuParams[_CMProxy.parameter.GROUP_NAME] = CMDBuild.Runtime.DefaultGroupName;

						CMDBuild.ServiceProxy.menu.read({
							params: readMenuParams,
							success: function(response, options, decoded) {
								menuAccordion.updateStore(decoded.menu);
							},
							callback: reqBarrier.getCallback()
						});

						_CMProxy.dataView.read({
							success : function(response, options, decoded) {
								dataViewAccordion.updateStore(decoded.views);
							},
							callback: reqBarrier.getCallback()
						});
					},
					failure: function() {
						_CMCache.addClasses([]);
						classesAccordion.updateStore();
						processAccordion.updateStore();
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.configuration.read({
					success: function(response, options,decoded) {
						CMDBuild.Config.dms = decoded.data;
					},
					callback: reqBarrier.getCallback
				},"dms");

				CMDBuild.ServiceProxy.report.getTypesTree({
					scope: this,
					success: function(response, options, reports) {
						_CMCache.addReports(reports);
						reportAccordion.updateStore();
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.administration.domain.list({ //TODO change "administration"
					params: {
						active: true
					},
					success: function(response, options, decoded) {
						_CMCache.addDomains(decoded.domains);
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.Dashboard.fullList({
					success : function(response, options, decoded) {
						_CMCache.addDashboards(decoded.response.dashboards);
						_CMCache.setAvailableDataSources(decoded.response.dataSources);
						dashboardsAccordion.updateStore();
					},
					callback: reqBarrier.getCallback()
				});

				CMDBuild.ServiceProxy.lookup.readAllTypes({
					success : function(response, options, decoded) {
						_CMCache.addLookupTypes(decoded);
					},
					callback: reqBarrier.getCallback()
				});

				reqBarrier.start();
			}
		}
	});

	function hideIfEmpty(a) {
		if (a.isEmpty()) {
			a.disable();
			a.hide();
		}
	}

	function addUtilitySubpanel(cmName, panels) {
		var builders = {
			changepassword : function() {
				return new CMDBuild.view.management.utilities.CMModChangePassword();
			},
			bulkcardupdate : function() {
				return new CMDBuild.view.management.utilites.CMModBulkCardUpdate({
					cmControllerType: CMDBuild.controller.management.utilities.CMModBulkUpdateController
				});
			},
			importcsv : function() {
				return new CMDBuild.view.management.utilities.CMModImportCSV({
					cmControllerType: CMDBuild.controller.management.utilities.CMModImportCSVController
				});
			},
			exportcsv : function() {
				return new CMDBuild.view.management.utilities.CMModExportCSV();
			}
		};

		if (typeof builders[cmName] == "function")
			panels.push(builders[cmName]());
	}

})();