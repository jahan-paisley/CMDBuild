(function() {

	var controllerNS = CMDBuild.controller,
		bimAccordion = null,
		classesAccordion = null,
		dashboardsAccordion = null,
		dataViewAccordion = null,
		domainAccordion = null,
		gisAccordion = null,
		groupsAccordion = null,
		lookupAccordion = null,
		menuAccordion = null,
		processAccordion = null,
		reportAccordion = null,
		navigationTreesAccordion = null;

	Ext.define("CMDBuild.app.Administration", {
		statics: {
			init: function() {
				var me = this;
				var administration = true;
				var forCredits = false;

				Ext.tip.QuickTipManager.init();
				// fix a problem of Ext 4.2 tooltips width
				// see http://www.sencha.com/forum/showthread.php?260106-Tooltips-on-forms-and-grid-are-not-resizing-to-the-size-of-the-text/page3#24
				delete Ext.tip.Tip.prototype.minWidth;

				CMDBuild.view.CMMainViewport.showSplash(forCredits, administration);
				_CMCache.resetMultiLanguages();

				// maybe a single request with all the configuration could be better
				CMDBuild.ServiceProxy.group.getUIConfiguration({
					success: function(response, options,decoded) {
						_CMUIConfiguration = new CMDBuild.model.CMUIConfigurationModel(decoded.response);

						CMDBuild.ServiceProxy.configuration.readMainConfiguration({
							success: function(response, options, decoded) {
								CMDBuild.Config.cmdbuild = decoded.data;

								/* **********************************************
								 * Suspend here the layouts, and resume after all
								 * the load are end
								 * **********************************************/
								Ext.suspendLayouts();
								/* ***********************************************/

								var panels = [
									new Ext.Panel({
										cls: 'empty_panel x-panel-body'
									}),
									new CMDBuild.view.administration.filter.CMGroupFilterPanel({
										cmControllerType: controllerNS.administration.filter.CMGroupFilterPanelController,
										cmName: "groupfilter"
									}),
									new CMDBuild.view.administration.bim.CMBIMPanel({
										cmControllerType: CMDBuild.controller.administration.filter.CMBIMPanelController,
										cmName: 'bim-project'
									}),
									new CMDBuild.bim.administration.view.CMBimLayers({
										cmControllerType: CMDBuild.controller.administration.filter.CMBimLayerController,
										cmName: 'bim-layers'
									}),
									new CMDBuild.view.administration.configuration.CMModConfigurationGenericOption({
										cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
										cmName: "modsetupcmdbuild"
									}),
									new CMDBuild.view.common.CMUnconfiguredModPanel({
										cmControllerType: controllerNS.common.CMUnconfiguredModPanelController,
										cmName: "notconfiguredpanel"
									})
								];

								if (!_CMUIConfiguration.isCloudAdmin()) {
									dataViewAccordion = new CMDBuild.view.administration.accordion.CMDataViewAccordion();

									panels = panels.concat([
										new CMDBuild.view.administration.configuration.CMModConfigurationBIM({
											cmControllerType : controllerNS.administration.configuration.CMModConfigurationController,
											cmName : "modsetupbim"
										}),
										new CMDBuild.view.administration.dataview.CMSqlDataView({
											cmControllerType: controllerNS.administration.dataview.CMSqlDataViewController,
											cmName: "sqldataview"
										}),
										new CMDBuild.view.administration.dataview.CMFilterDataView({
											cmControllerType: controllerNS.administration.dataview.CMFilerDataViewController,
											cmName: "filterdataview"
										}),
										new CMDBuild.view.administration.configuration.CMModConfigurationGis({
											cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
											cmName: "modsetupgis"
										}),
										new CMDBuild.view.administration.configuration.CMModConfigurationGraph({
											cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
											cmName: "modsetupgraph"
										}),
										new CMDBuild.view.administration.configuration.CMModConfigurationAlfresco({
											cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
											cmName: "modsetupalfresco"
										}),
										new CMDBuild.view.administration.configuration.CMModConfigurationWorkflow({
											cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
											cmName: "modsetupworkflow"
										}),
										new CMDBuild.view.administration.configuration.CMModConfigurationServer({
											cmControllerType: controllerNS.administration.configuration.CMModConfigurationServerController,
											cmName: "modsetupserver"
										}),
										new CMDBuild.view.administration.configuration.CMModConfigurationTranslations({
											cmControllerType: controllerNS.administration.configuration.CMModConfigurationTranslationsController,
											cmName: "modsetuptranslations"
										}),
										Ext.create('CMDBuild.view.administration.email.CMEmailAccounts', {
											cmControllerType: CMDBuild.controller.administration.email.CMEmailAccountsController,
											cmName: 'emailAccounts'
										}),
										Ext.create('CMDBuild.view.administration.email.CMEmailTemplates', {
											cmControllerType: CMDBuild.controller.administration.email.CMEmailTemplatesController,
											cmName: 'emailTemplates'
										}),
										Ext.create('CMDBuild.view.administration.tasks.CMTasks', {
											cmControllerType: CMDBuild.controller.administration.tasks.CMTasksController,
											cmName: 'tasks'
										}),
										new CMDBuild.view.administration.configuration.CMModConfigurationBIM({
											cmControllerType: controllerNS.administration.configuration.CMModConfigurationController,
											cmName: 'modsetupbim'
										})
									]);
								}

								_CMMainViewportController = new CMDBuild.controller.CMMainViewportController(new CMDBuild.view.CMMainViewport({
									cmAccordions: [],
									cmPanels: panels
								}));

								me.loadResources();
							}
						});
					}
				});
			},

			loadResources: function() {
				var reqBarrier = new CMDBuild.Utils.CMRequestBarrier(
					function callback() {

						_CMMainViewportController.addAccordion([
							classesAccordion,
							processAccordion,
							domainAccordion,
							dataViewAccordion,
							Ext.create('CMDBuild.view.administration.accordion.CMFilterAccordion'),
							navigationTreesAccordion,
							lookupAccordion,
							dashboardsAccordion,
							reportAccordion,
							menuAccordion,
							groupsAccordion,
							Ext.create('CMDBuild.view.administration.accordion.CMAccordionTasks'),
							Ext.create('CMDBuild.view.administration.accordion.CMAccordionEmail'),
							gisAccordion,
							bimAccordion,
							Ext.create('CMDBuild.view.administration.accordion.CMConfigurationAccordion')
						]);

						// Resume here the layouts operations
						Ext.resumeLayouts(true);

						_CMMainViewportController.viewport.doLayout();

						CMDBuild.view.CMMainViewport.hideSplash(function() {
							_CMMainViewportController.setInstanceName(CMDBuild.Config.cmdbuild.instance_name);
							_CMMainViewportController.selectFirstSelectableLeafOfOpenedAccordion();
						});

					}
				);

				/*
				 * BIM Configuration
				 * */
				CMDBuild.ServiceProxy.configuration.readBimConfiguration({
					success: function(response, option, decoded) {
						var disabled = decoded.data.enabled == "false";
						bimAccordion = new CMDBuild.view.administration.accordion.CMBIMAccordion({
							disabled: disabled
						});
					}
				});

				/*
				 * Classes and process
				 */
				CMDBuild.ServiceProxy.classes.read({
					params: {
						active: false
					},
					success: function(response, options, decoded) {
						_CMCache.addClasses(decoded.classes);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							classesAccordion = new CMDBuild.view.administration.accordion.CMClassAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMClassAccordionController
							});
							classesAccordion.updateStore();

							processAccordion = new CMDBuild.view.administration.accordion.CMProcessAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMAccordionProcessController,
								disabled: !CMDBuild.Config.workflow.enabled
							});
							processAccordion.updateStore();

							_CMMainViewportController.addPanel([
								new CMDBuild.view.administration.classes.CMModClass({
									cmControllerType: controllerNS.administration.classes.CMModClassController
								}),
								new CMDBuild.view.administration.workflow.CMProcess({
									cmControllerType: controllerNS.administration.workflow.CMProcessController
								})
							]);
						}

						// Do a separate request for the widgets because, at this time
						// it is not possible serialize them with the classes
						CMDBuild.ServiceProxy.CMWidgetConfiguration.read({
							scope: this,
							callback: reqBarrier.getCallback(),
							success: function(response, options, decoded) {
								_CMCache.addWidgetToEntryTypes(decoded.response);
							}
						});
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * Workflow configuration
				 */
				CMDBuild.ServiceProxy.configuration.readWFConfiguration({
					success: function(response, options, decoded) {
						CMDBuild.Config.workflow = decoded.data;
						CMDBuild.Config.workflow.enabled = ('true' == CMDBuild.Config.workflow.enabled);
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * GIS configuration
				 */
				CMDBuild.ServiceProxy.configuration.readGisConfiguration({
					success: function(response, options, decoded) {
						CMDBuild.Config.gis = decoded.data;
						CMDBuild.Config.gis.enabled = ('true' == CMDBuild.Config.gis.enabled);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							gisAccordion = new CMDBuild.view.administration.accordion.CMGISAccordion({
								disabled: !CMDBuild.Config.gis.enabled
							});

							_CMMainViewportController.addPanel([
								new CMDBuild.Administration.ModIcons(),
								new CMDBuild.view.administration.gis.CMModGISNavigationConfiguration({
									cmControllerType: controllerNS.administration.gis.CMModGISNavigationConfigurationController
								}),
								new CMDBuild.Administration.ModExternalServices(),
								new CMDBuild.view.administration.gis.CMModGeoServer({
									cmControllerType: controllerNS.administration.gis.CMModGeoServerController
								}),
								new CMDBuild.Administration.ModLayerOrder({
									cmControllerType: controllerNS.administration.gis.CMModLayerOrderController
								})
							]);
						}
					},

					callback: reqBarrier.getCallback()
				});

				/*
				 * Lookups
				 */
				CMDBuild.ServiceProxy.lookup.readAllTypes({
					success: function(response, options, decoded) {
						_CMCache.addLookupTypes(decoded);
						lookupAccordion = new CMDBuild.view.administration.accordion.CMLookupAccordion({
							cmControllerType: CMDBuild.controller.accordion.CMLookupAccordionController
						});
						lookupAccordion.updateStore();

						_CMMainViewportController.addPanel(
							new CMDBuild.Administration.ModLookup({
								cmControllerType: controllerNS.administration.lookup.CMModLookupController
							})
						);
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * Groups
				 */
				CMDBuild.ServiceProxy.group.read({
					success : function(response, options, decoded) {
						_CMCache.addGroups(decoded.groups);

						groupsAccordion = new CMDBuild.view.administration.accordion.CMGroupsAccordion({
							cmControllerType: CMDBuild.controller.accordion.CMGroupAccordionController
						});
						groupsAccordion.updateStore();

						menuAccordion = new CMDBuild.view.administration.accordion.CMMenuAccordion({
							cmControllerType: CMDBuild.controller.accordion.CMMenuAccordionController
						});
						menuAccordion.updateStore();

						_CMMainViewportController.addPanel([
							new CMDBuild.Administration.ModMenu({
								cmControllerType: controllerNS.administration.menu.CMModMenuController
							}),
							new CMDBuild.view.administration.group.CMModGroup({
								cmControllerType: controllerNS.administration.group.CMModGroupsController
							}),
							new CMDBuild.view.administration.user.CMModUser({
								cmControllerType: controllerNS.administration.user.CMModUserController
							})
						]);
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * Report
				 */
				CMDBuild.ServiceProxy.report.getMenuTree({
					success: function(response, options, reports) {
						_CMCache.addReports(reports);

						reportAccordion = new CMDBuild.view.common.report.CMReportAccordion();
						reportAccordion.updateStore();

						_CMMainViewportController.addPanel(
							new CMDBuild.view.administration.report.CMModReport({
								cmControllerType: controllerNS.administration.report.CMModReportController
							})
						);
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * Domains
				 */
				CMDBuild.ServiceProxy.administration.domain.list({
					success: function(response, options, decoded) {
						_CMCache.addDomains(decoded.domains);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							domainAccordion = new CMDBuild.view.administration.accordion.CMDomainAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMDomainAccordionController
							});
							domainAccordion.updateStore();

							_CMMainViewportController.addPanel(
								new CMDBuild.view.administration.domain.CMModDomain({
									cmControllerType: controllerNS.administration.domain.CMModDomainController
								})
							);
						}
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * Navigation trees
				 */
				_CMCache.listNavigationTrees({
					success: function(response, options, decoded) {

						if (!_CMUIConfiguration.isCloudAdmin()) {
							navigationTreesAccordion = new CMDBuild.view.administration.accordion.CMNavigationTreesAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMNavigationTreesAccordionController
							});
							navigationTreesAccordion.updateStore();

							_CMMainViewportController.addPanel(
								new CMDBuild.view.administration.navigationTrees.CMModNavigationTrees({
									cmControllerType: controllerNS.administration.navigationTrees.CMModNavigationTreesController
								})
							);
						}
					},
					callback: reqBarrier.getCallback()
				});

				/*
				 * Dashboards
				 */
				CMDBuild.ServiceProxy.Dashboard.fullList({
					success: function(response, options, decoded) {
						_CMCache.addDashboards(decoded.response.dashboards);
						_CMCache.setAvailableDataSources(decoded.response.dataSources);

						if (!_CMUIConfiguration.isCloudAdmin()) {
							dashboardsAccordion = new CMDBuild.view.administration.accordion.CMDashboardAccordion({
								cmControllerType: CMDBuild.controller.accordion.CMDashboardAccordionController
							});
							dashboardsAccordion.updateStore();

							_CMMainViewportController.addPanel(
								new CMDBuild.view.administration.dashboard.CMModDashboard({
									cmControllerType: controllerNS.administration.dashboard.CMModDashboardController
								})
							);
						}
					},
					callback: reqBarrier.getCallback()
				});

				reqBarrier.start();
			}
		}
	});

})();