(function() {

	var ICON_ACTION = "action-open-bim";
	var MAX_ZOOM = 15;

	Ext.define("CMDBuild.bim.management.CMBimController", {

		mixins: {
			cardGrid: "CMDBuild.view.management.common.CMCardGridDelegate"
		},

		constructor: function(view) {
			// this must be loaded with BIM configuration
			// before to initialize the application
			this.bimConfiguration = CMDBuild.Config.bim;
			this.rootClassName = this.bimConfiguration.rootClass;

			this.view = view;
			this.view.addDelegate(this);

			this.loginProxy = new BIMLoginProxy();

			this.bimWindow = null;
			this.bimSceneManager = null;
			this.viewportEventListener = null;
			this.currentObjectId = null;
			this.roid = null;
			this.basePoid = null;
		},

		/* ******************************************************
		 *  as CMDBuild.view.management.common.CMCardGridDelegate
		 * ******************************************************* */

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridColumnsReconfigured: function(grid) {
			var entryType = _CMCardModuleState.entryType;
			var me = this;
			CMDBuild.bim.proxy.activeForClassName({
				params: {
					className: entryType.getName()
				},
				success: function(operation, options, response) {
					if (response.active) {
						var column = Ext.create('Ext.grid.column.Column', {
							align: 'center',
							dataIndex: 'Id',
							fixed: true,
							header: '&nbsp',
							hideable: false,
							menuDisabled: true,
							renderer: renderBimIcon,
							sortable: false,
							width: 30
						});
						
						grid.headerCt.insert(grid.columns.length - 1, column);
						grid.getView().refresh();
					}
				}
			});

		},

		/**
		 * 
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridIconRowClick: function(grid, action, model) {
			if (action == ICON_ACTION) {
				CMDBuild.LoadMask.get().show();
				var me = this;
				var entryType = _CMCardModuleState.entryType;
				CMDBuild.bim.proxy.roidForCardId({
					params: {
						cardId: model.get("Id"),
						className: entryType.getName(),
						withExport: true
					},
					success: function(operation, options, response) {
						if (response.ROID) {
							startBIMPlayer(me, response.ROID, response.DESCRIPTION, response.BASE_POID);
						} else {
							CMDBuild.Msg.warn(
									CMDBuild.Translation.warnings.warning_message, //
									CMDBuild.Translation.no_bim_project_for_card
							);
						}
						CMDBuild.LoadMask.get().hide();
					},
					failure: function() {
						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		},

		/* **************************************************************
		 * As scene manager delegate
		 *  **************************************************************/

		sceneLoaded: function(sceneManager, scene) {
			if (this.bimWindow != null) {
				var sceneData = scene.data();
				var ifcTypes = sceneData.ifcTypes;
				var data = [];
	
				for (var i=0, l=ifcTypes.length; i<l; ++i) {
					var ifcType = ifcTypes[i];

					data.push({
						description: ifcType.substring(3), // remove the ifc prefix from the label
						id: ifcType,
						checked: false
					});
				}

				this.bimWindow.loadLayers(data);
				this.bimWindow.resetControls();

				var rootNode = convertCMDBuildData(sceneData);
				this.bimWindow.setTreeRootNode(rootNode);
			}
		},

		layerDisplayed: function(sceneManager, layerName) {
			if (this.bimWindow) {
				this.bimWindow.selectLayer(layerName);
			}
		},

		objectSelected: function(sceneManager, objectId) {
			this.currentObjectId = objectId;
			_debug("Object selected", objectId);
			if (this.bimWindow) {
				this.bimWindow.enableObjectSliders();
				this.bimWindow.tree.selectNodeByOid(objectId);
			}
		},

		objectSelectedForLongPressure: function(sceneManager, objectId) {
			var me = this;

			CMDBuild.bim.proxy.fetchCardFromViewewId({
				params: {
					revisionId: me.roid,
					objectId: objectId
				},

				success: function(fp, request, response) {
					if (response.card) {
						openCardDataWindow(me, response.card);
					}
				}
			});
		},

		selectionCleaned: function() {
			this.currentObjectId = null;
			if (this.bimWindow) {
				this.bimWindow.disableObjectSliders();
			}
		},

		bimSceneManagerGeometryAdded: function(sceneManager, oid) {
			this.bimWindow.tree.checkNode(oid);
		},

		/* **************************************************************
		 * As CMBimWindow delegate
		 *  **************************************************************/

		/*
		 * @param {CMDBuild.bim.management.view.CMBimPlayerLayers} bimLayerPanel
		 * the layers panel that call the method
		 * @param {String} ifcLayerName
		 * the name of the layer for which the check is changed
		 * @param {Boolean} checked
		 * the current value of the check
		 */
		onLayerCheckDidChange: function(bimLayerPanel, ifcLayerName, checked) {
			if (checked) {
				this.bimSceneManager.showLayer(ifcLayerName);
			} else {
				this.bimSceneManager.hideLayer(ifcLayerName);
			}
		},

		/* **************************************************************
		 * As CMBimControlPanel delegate
		 * **************************************************************/

		onBimControlPanelResetButtonClick: function() {
			this.bimSceneManager.defaultView();
		},

		onBimControlPanelFrontButtonClick: function() {
			this.bimSceneManager.frontView();
		},

		onBimControlPanelSideButtonClick: function() {
			this.bimSceneManager.sideView();
		},

		onBimControlPanelTopButtonClick: function() {
			this.bimSceneManager.topView();
		},

		onBimControlPanelPanButtonClick: function() {
			this.bimSceneManager.togglePanRotate();
		},

		onBimControlPanelRotateButtonClick: function() {
			this.bimSceneManager.togglePanRotate();
		},

		/**
		 * @param {Number} value
		 * the current value of the slider
		 */
		onBimControlPanelZoomSliderChange: function(value) {
			var zoom = MAX_ZOOM - (value/5);
			this.bimSceneManager.setZoomLevel(zoom);
		},

		/**
		 * @param {Number} value
		 * the current value of the slider
		 */
		onBimControlPanelExposeSliderChange: function(value) {
			this.bimSceneManager.exposeNodeWithItsStorey(this.currentObjectId, (value/2.5));
		},

		/**
		 * @param {Number} value
		 * the current value of the slider
		 */
		onBimControlPanelTransparentSliderChange: function(value) {
			var factor = 100 - value;
			this.bimSceneManager.setNodeTransparentLevel(this.currentObjectId, factor);
		},

		// CMCardDataWinodwDelegate

		/**
		 * @param {CMDBuild.bim.view.CMCardDataWindow} cardDataWindow
		 */
		cardDataWindowOpenCardButtonWasClicked: function(cardDataWindow) {
			var cardData = cardDataWindow.cmCardData;
			cardDataWindow.destroy();
			openCard(this, cardData.IdClass, cardData.Id);
		},
		/**
		 * @param {CMDBuild.bim.view.CMCardDataWindow} cardDataWindow
		 */
		cardDataWindowOkButtonWasClicked: function(cardDataWindow) {
			cardDataWindow.destroy();
		},

		// CMBimTreeDelegate

		onNodeCheckChange: function(node, check) {
			var oid = node.raw.oid;
			if (check) {
				this.bimSceneManager.showObject(oid);
			} else {
				this.bimSceneManager.hideObject(oid);
			}
		},

		onNodeSelect: function(node, fromViewer) {
			this.bimSceneManager.selectObject(node.raw.oid, fromViewer);
		},

		onOpenCardIconClick: function(classId, cardId) {
			openCard(this, classId, cardId);
		},
	});

	function openCard(me, classId, cardId) {
		me.bimWindow.hide();
		_CMMainViewportController.openCard({
			IdClass: classId,
			Id: cardId
		});
	}

	function convertCMDBuildData(data) {
		var properties = data.properties; // map {oid: {}, oid: {}...}
		var relations = data.relationships; // tree

		var project = relations[0];
		return convertNode(project, properties);
	}

	function convertNode(relationshipNode, properties) {
		var out = {};
		var nodeData = properties[relationshipNode.id] || {};
		var cmdbuildData = nodeData.cmdbuild_data || {};
		out.text = cmdbuildData.card_description || nodeData.Name;
		out.leaf = true;
		out.checked = false;
		out.oid = relationshipNode.id;
		out.cmdbuild_data = cmdbuildData;

		if (relationshipNode.contains || relationshipNode.definedBy) {
			out.leaf = false;

			out.children = convertNodes(relationshipNode.decomposedBy, properties)
				.concat(convertNodes(relationshipNode.contains, properties));

			if (relationshipNode.type) {
				out.text += " (" + relationshipNode.type + ")";
			}
		}

		return out;
	}

	function convertNodes(nodes, properties) {
		var out = [];
		var input = nodes || [];
		for (var i=0, l=input.length; i<l; ++i) {
			var node = input[i];
			out.push(convertNode(node, properties));
		}

		return out;
	}

	function doLogin(me, callback) {
		var c = me.bimConfiguration;

		me.loginProxy.login({
			url: c.url,
			username: c.username,
			password: c.password,
			rememberMe: false,
			success: callback,
			failure: function() {
				CMDBuild.Msg.error( //
						CMDBuild.Translation.error, //
						CMDBUild.Translation.error_bimserver_connection, //
						true //
					);
			},
		});

	}

	function startBIMPlayer(me, roid, description, basePoid) {
		// FIXME remove it
		window._BIM_LOGGER = console;

		/*
		 * Reuse the window if already
		 * open to this ROID
		 */
		if (me.roid == roid
				&& me.bimWindow != null) {
			me.bimWindow.show();
			return;
		}

		me.roid = roid;
		me.basePoid = basePoid;

		doLogin(me, function() {
			if (me.bimWindow == null) {
				me.bimWindow = new CMDBuild.bim.management.view.CMBimWindow({
					delegate: me
					
				});
			}
			me.bimWindow.show();
			me.bimWindow.setTitle(description);


			if (me.bimSceneManager == null) {
				me.bimSceneManager = new BIMSceneManager({
					canvasId: me.bimWindow.CANVAS_ID,
					viewportId: me.bimWindow.getId()//,
				});
				me.bimSceneManager.addDelegate(me);

				me.viewportEventListener = new BIMViewportEventListener( //
						me.bimWindow.CANVAS_ID, //
						me.bimSceneManager //
				);
			}

			me.bimSceneManager.visibleLayers = [];
			me.bimWindow.mon(me.bimWindow, "beforehide", function() {
				me.loginProxy.logout();
			});

			me.bimSceneManager.loadProjectWithRoid(me.roid,me.basePoid);
		});
	}

	function openCardDataWindow(me, card) {
		var classId = card.IdClass;

		_CMCache.getAttributeList(classId, function(attributes) {
			var cardWindow = new CMDBuild.bim.view.CMCardDataWindow({
				cmCardData: card,
				attributeConfigurations: attributes,
				delegate: me
			});
			cardWindow.show();

		});

	}

	function renderBimIcon() {
		return '<img style="cursor:pointer"' +
			'" class="' + ICON_ACTION + 
			'" title="' + CMDBuild.Translation.open_3d_viewer + 
			'" src="images/icons/application_home.png"/>';
	}
})();