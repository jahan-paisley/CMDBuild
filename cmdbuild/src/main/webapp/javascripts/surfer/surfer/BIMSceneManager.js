(function () {

	var ZOOMSPEEDFACTOR = 0.05;
	var HIGHLIGHT_MATERIAL = {
		type : 'material',
		id : 'highlight',
		emit : 0.0,
		baseColor : {
			r : 0.0,
			g : 0.5,
			b : 0.5
		}
	};

	// setView constants
	var RESET = 0;
	var FRONT = 1;
	var SIDE = 2;
	var TOP = 3;

/**
 * 	To avoid framework specific event managements
 * 	this object implements a delegation system.
 * 
 * 	use addDelegate(xxx) to add a delegate to this object
 * 
 * 	the delegates could  implements the following methods:
 * 
 * 		sceneLoaded(sceneManager, scene)
 * 		objectSelectedForLongPressure(sceneManager, objectId)
 * 		objectSelected(sceneManager, objectId)
 * 		selectionCleaned(sceneManager)
 */

	BIMSceneManager = function(config) {

		this.scene = null;

		this.canvas = document.getElementById(config.canvasId);

		this.mode = "none";

		/**
		 * this array contains the
		 * names of the visible layers
		 * 
		 * after load a layer is set as visible
		 * by default
		 * 
		 * use the methods showLayer(layerName)
		 * and hideLayer(layerName)
		 * 
		 * to change the visibility of a layer
		 */
		this.visibleLayers = [];

		// TODO: I think is not needed in this class
		this.viewport = {
			domElement : document.getElementById(config.viewportId),
			selectedIfcObject : null,
			mouse : {
				last : [ 0, 0 ],
				leftDown : false,
				middleDown : false,
				leftDragDistance : 0,
				middleDragDistance : 0,
				pickRecord : null
			}
		};
	
		this.queryArgs = {}; // TODO: ???
	
		this.camera = {
			distanceLimits: [0.0, 0.0]
		};

		// Variables for BPI.MOST Features (TU Vienna)
		this.propertyValues = {
			scalefactor : 0,
			viewfactor : 0,
			selectedObj : 'emtpy Selection',
			mouseRotate : 0,
			oldZoom : 15,
			boundfactor : 0,
			autoLoadPath : ""
		};
	
		this.lookAt = {
			defaultParameters : {
				look : {
					x : 0.0,
					y : 0.0,
					z : 0.0
				},
				eye : {
					x : 10.0,
					y : 10.0,
					z : 10.0
				},
				up : {
					x : 0.0,
					y : 0.0,
					z : 1.0
				}
			}
		};

		this.classNames = config.classNames || [ 
			"IfcColumn", 
			"IfcStair", 
			"IfcSlab", 
			"IfcWindow", 
			"IfcDoor", 
			//"IfcBuildingElementProxy",
			"IfcWallStandardCase", 
			"IfcWall", 
			"IfcBeam", 
			"IfcRailing",
			"IfcProxy",
			"IfcRoof",
			"IfcFurnishingElement",
			"IfcFlowSegment"
		];

		/**
		 * This component is able to communicate with
		 * BIMServer via bimServerApi
		 */
		this.bimProjectLoader = new BIMProjectLoader(this, this.classNames, config.progressBar);

		/*
		 * Delegate management
		 */

		var delegates = [];
		this.addDelegate = function(d) {
			if (d) {
				delegates.push(d);
			}
		};

		this.callDelegates = function(methodName, args) {
			for (var i=0, l=delegates.length; i<l; i++) {
				var d = delegates[i];
				if (typeof d[methodName] == "function") {
					d[methodName].apply(d, args);
				}
			}
		};
	};

	// ##################################################
	// Init scene and project loading
	// ##################################################

	BIMSceneManager.prototype.loadProjectWithRoid = function(roid, poid) {
		this.bimProjectLoader.loadFromCmdbuild(roid,poid);
//		this.bimProjectLoader.load(roid);
	};

	// TODO: check what destroy here
	BIMSceneManager.prototype.destroy = function() {
		if (this.scene) {
			this.scene.stop();
			this.scene.destroy();
		}
	};

	/**
	 * called from BIMProjectLoader when the loading is end
	 */
	function addLight(obj) {
		if (obj.nodes) {
			for (o in obj.nodes) {
				o = obj.nodes[o];
				if (o.type == "light") {
					var newLight = {
							"specular": true, 
							"id": "sun-light2", 
							"dir": {"x" : -o.dir.x, "y" : -o.dir.y, "z" : -o.dir.z}, 
							"color": {"r" : o.color.r/1.2, "g" : o.color.g/1.2, "b" : o.color.b/1.2}, 
							"diffuse": true,
							"type" : "light",
							"mode" : "dir"
					};
					obj.nodes.push(newLight);
				}
				else
					addLight(o);
			}
		}
	};
	BIMSceneManager.prototype.projectDidLoad = function(sceneData) {
		addLight(sceneData);

		if (this.scene != null) {
			this.scene.destroy();
			this.scene = null;
			SceneJS._scenes = {};
			SceneJS._scene = null;


		    /* Stack to track nodes during scene traversal.
		     */
			SceneJS._nodeStack = [];
			SceneJS._stackLen = 0;
		}
	
		try {
			this.scene = SceneJS.createScene(sceneData);
			// othis.viewportInit();
			if (this.scene != null) {
				this.sceneInit();
				this.scene.start({
					idleFunc : SceneJS.FX.idle
				});

				// Calculate Scalefactor
				var ref, len, i, unit, sizingFactor;
	
				unit = this.scene.data().unit;
				window._BIM_LOGGER.log("Unit: " + unit);
				ref = this.scene.data().bounds;
				for (i = 0, len = ref.length; i < len; i++) { // iterate all
					// bounds
					window._BIM_LOGGER.log("Bound" + i + ": " + ref[i]);
				}
				// to decide which side is used to set view in setViewToObject
				if (ref[0] > ref[1]) {
					this.propertyValues.boundfactor = 1; // for setView to
					// decide the main
					// viewside
				}
				this.propertyValues.scalefactor = parseFloat(unit);
	
				// setting viewfactor for different views
				this.propertyValues.viewfactor = SceneJS_math_lenVec3(this.scene.data().bounds);
	
				// set Navigation Mode to rotate
				this.setNavigationMode(0);

				var ifcTypes = sceneData.data.ifcTypes;
				this.bimProjectLoader.loadGeometries(ifcTypes);

				this.callDelegates("sceneLoaded", [this, this.scene]);

				return this.scene;
			}
		} catch (error) {
			window._BIM_LOGGER.log(error);
			window._BIM_LOGGER.log('...Errors occured');
			CMDBuild.LoadMask.get().hide();
		}

//		this.helpShortcuts('standard');
		return null;
	};

	BIMSceneManager.prototype.sceneInit = function() {
		var lookAtNode, sceneDiameter, tag, tags, me;
		modifySubAttr( //
			this.scene.findNode('main-camera'), //
			'optics', //
			'aspect', //
			this.canvas.width / this.canvas.height //
		);

		sceneDiameter = SceneJS_math_lenVec3(this.scene.data().bounds);

		this.camera.distanceLimits = [sceneDiameter * 0.1, sceneDiameter * 2.0];

		me = this;

		tags = (function() {
			var _i, _len, _ref, _results;
			_ref = me.scene.data().ifcTypes;
			_results = [];
			for (_i = 0, _len = _ref.length; _i < _len; _i++) {
				tag = _ref[_i];
				_results.push(tag.toLowerCase());
			}
			return _results;
		})();

		this.scene.set('tagMask', '^(' + (tags.join('|')) + ')$');
		lookAtNode = this.scene.findNode('main-lookAt');
		this.lookAt.defaultParameters.eye = lookAtNode.get('eye');
		this.lookAt.defaultParameters.look = lookAtNode.get('look');
		return this.lookAt.defaultParameters.up = lookAtNode.get('up');
	};

	function modifySubAttr(node, attr, subAttr, value) {
		var attrRecord;
		attrRecord = node.get(attr);
		attrRecord[subAttr] = value;
		return node.set(attr, attrRecord);
	};

	BIMSceneManager.prototype.putGeometriesInScene = function(data, currentLayerName, currentLayerId) {
		
		var me = this;
		var library = me.scene.findNode("library");
		var bounds = me.scene.data().bounds2;
		var typeNode = {
			type : "tag",
			tag : currentLayerId,
			id : currentLayerId,
			nodes : []
		};

		me.visibleLayers.push(currentLayerName);

		data.geometry.forEach(function(geometry) {
			var material = {
				type : "material",
				coreId : geometry.material + "Material",
				nodes : [ {
					id : geometry.coreId,
					type : "name",
					nodes : []
				}]
			};

			if (geometry.nodes != null) {
				geometry.nodes.forEach(function(node){
					if (node.positions != null) {
						for (var i = 0; i < node.positions.length; i += 3) {
							node.positions[i] = node.positions[i] - bounds[0];
							node.positions[i + 1] = node.positions[i + 1] - bounds[1];
							node.positions[i + 2] = node.positions[i + 2] - bounds[2];
						}
						node.indices = [];
						for (var i = 0; i < node.nrindices; i++) {
							node.indices.push(i);
						}
						library.add("node", node);
						me.callDelegates("bimSceneManagerGeometryAdded", [me, node.coreId]);
						material.nodes[0].nodes.push({
							type: "geometry",
							coreId: node.coreId
						});

					}
				});
			} else {
				if (geometry.positions != null) {
					for (var i = 0; i < geometry.positions.length; i += 3) {
						geometry.positions[i] = geometry.positions[i] - bounds[0];
						geometry.positions[i + 1] = geometry.positions[i + 1] - bounds[1];
						geometry.positions[i + 2] = geometry.positions[i + 2] - bounds[2];
					}
					geometry.indices = [];
					for (var i = 0; i < geometry.nrindices; i++) {
						geometry.indices.push(i);
					}
					library.add("node", geometry);
					me.callDelegates("bimSceneManagerGeometryAdded", [me, geometry.coreId]);
					material.nodes[0].nodes.push({
						type: "geometry",
						coreId: geometry.coreId
					});
				}
			}

			if (geometry.material == "IfcWindow") {
				var flags = {
					type : "flags",
					flags : {
						transparent : true
					},
					nodes : [ material ]
				};
				typeNode.nodes.push(flags);
			} else {
				typeNode.nodes.push(material);
			}
		});

		me.scene.findNode("main-renderer").add("node", typeNode);

		showVisibleLayers(me);

		me.callDelegates("layerDisplayed", [me, currentLayerName]);
	};

	// ##################################################
	// Managing viewpoint 
	// ##################################################

	BIMSceneManager.prototype.setNavigationMode = function(_mouseRotate) {
		this.propertyValues.mouseRotate = _mouseRotate;
	};

	/**
	 * Get Navigation Mode (0 for Rotate and 1 for Pan)
	 */
	BIMSceneManager.prototype.getNavigationMode = function(event) {
		return this.propertyValues.mouseRotate;
	};

	BIMSceneManager.prototype.orbitLookAtNode = function(node, dAngles, orbitUp) {
		return node.set(orbitLookAt(dAngles, orbitUp, {
			eye : node.get('eye'),
			look : node.get('look'),
			up : node.get('up')
		}));
	};

	function orbitLookAt(dAngles, orbitUp, lookAt) {
		if (dAngles[0] === 0.0 && dAngles[1] === 0.0) {
			return {
				eye : lookAt.eye,
				look : lookAt.look,
				up : lookAt.up
			};
		}

		var eye0 = recordToVec3(lookAt.eye);
		var up0 = recordToVec3(lookAt.up);
		var look = recordToVec3(lookAt.look);
		var axes = [ //
			[ 0.0, 0.0, 0.0 ], //
			[ 0.0, 0.0, 0.0 ], //
			[ 0.0, 0.0, 0.0 ] //
		];
		var axesNorm = [ //
			[ 0.0, 0.0, 0.0 ], //
			[ 0.0, 0.0, 0.0 ], //
			[ 0.0, 0.0, 0.0 ] //
		];
		SceneJS_math_subVec3(eye0, look, axes[2]);
		SceneJS_math_cross3Vec3(up0, axes[2], axes[0]);
		SceneJS_math_normalizeVec3(axes[0], axesNorm[0]);
		SceneJS_math_normalizeVec3(axes[2], axesNorm[2]);
		SceneJS_math_cross3Vec3(axesNorm[2], axesNorm[0], axesNorm[1]);

		var rotAxis = [ //
			axesNorm[0][0] * -dAngles[1] + axesNorm[1][0] * -dAngles[0], //
			axesNorm[0][1] * -dAngles[1] + axesNorm[1][1] * -dAngles[0], //
			axesNorm[0][2] * -dAngles[1] + axesNorm[1][2] * -dAngles[0]
		];
		var dAngle = SceneJS_math_lenVec2(dAngles);
		var rotMat = SceneJS_math_rotationMat4v(dAngle, rotAxis);
		var transformedX = SceneJS_math_transformVector3(rotMat, axesNorm[0]);
		var transformedZ = SceneJS_math_transformVector3(rotMat, axes[2]);
		var eye1 = [ 0.0, 0.0, 0.0 ];
		SceneJS_math_addVec3(look, transformedZ, eye1);

		var tangent1 = transformedX;
		var tangentError = [ 0.0, 0.0, 0.0 ];
		SceneJS_math_mulVec3(tangent1, orbitUp, tangentError);
		SceneJS_math_subVec3(tangent1, tangentError);

		var up1 = [ 0.0, 0.0, 0.0 ];
		SceneJS_math_cross3Vec3(transformedZ, tangent1, up1);

		return {
			eye : vec3ToRecord(eye1),
			look : lookAt.look,
			up : vec3ToRecord(up1)
		};
	};

	BIMSceneManager.prototype.lookAtNodePanRelative = function(node, dPosition) {
		return node.set(lookAtPanRelative(dPosition, {
			eye : node.get('eye'),
			look : node.get('look'),
			up : node.get('up')
		}));
	};

	function lookAtPanRelative(dPosition, lookAt) {

		if (dPosition[0] === 0.0 && dPosition[1] === 0.0) {
			return {
				eye : lookAt.eye,
				look : lookAt.look,
				up : lookAt.up
			};
		}

		var eye = recordToVec3(lookAt.eye);
		var look = recordToVec3(lookAt.look);
		var up = recordToVec3(lookAt.up);
		var axes = [ [ 0.0, 0.0, 0.0 ], [ 0.0, 0.0, 0.0 ], [ 0.0, 0.0, 0.0 ] ];
		SceneJS_math_subVec3(eye, look, axes[2]);
		SceneJS_math_cross3Vec3(up, axes[2], axes[0]);
		SceneJS_math_normalizeVec3(axes[0]);
		SceneJS_math_cross3Vec3(axes[2], axes[0], axes[1]);
		SceneJS_math_normalizeVec3(axes[1]);
		SceneJS_math_mulVec3Scalar(axes[0], dPosition[0]);
		SceneJS_math_mulVec3Scalar(axes[1], dPosition[1]);
		var dPositionProj = [ 0.0, 0.0, 0.0 ];
		SceneJS_math_addVec3(axes[0], axes[1], dPositionProj);
		return {
			eye : vec3ToRecord(SceneJS_math_addVec3(eye, dPositionProj)),
			look : vec3ToRecord(SceneJS_math_addVec3(look, dPositionProj)),
			up : lookAt.up
		};
	};

	/**
	 * Set View to start view
	 */
	BIMSceneManager.prototype.defaultView = function(event) {
		this.setView(0);
	};

	/**
	 * Set View to front view
	 */
	BIMSceneManager.prototype.frontView = function(event) {
		this.setView(1);
	};

	/**
	 * Set View to side view
	 */
	BIMSceneManager.prototype.sideView = function(event) {
		this.setView(2);
	};

	/**
	 * Set View to top view
	 */
	BIMSceneManager.prototype.topView = function(event) {
		this.setView(3);
	};

	/**
	 * Switch between rotate and pan navigation mode
	 */
	BIMSceneManager.prototype.togglePanRotate = function(event) {
		if (this.getNavigationMode() == 1) {
			this.setNavigationMode(0);
		} else {
			this.setNavigationMode(1);
		}
	};

	BIMSceneManager.prototype.setView = function(view) {
		if (this.scene == null) {
			return 0;
		}

		resetView(this);

		var lookAtNode;
		var origin = {
			x : 0,
			y : 0,
			z : 0
		};

		switch (view) {

			case (RESET):
				this.propertyValues.oldZoom = 15;
				break;

			case (FRONT):
				lookAtNode = this.scene.findNode('main-lookAt');
				lookAtNode.set('eye', {
					x : this.propertyValues.viewfactor,
					y : 0,
					z : 0
				});
				lookAtNode.set('look', origin);
				this.propertyValues.oldZoom = 10;
				break;

			case (SIDE):
				lookAtNode = this.scene.findNode('main-lookAt');
				lookAtNode.set('eye', {
					x : 0,
					y : this.propertyValues.viewfactor,
					z : 0
				});
				lookAtNode.set('look', origin);
				this.propertyValues.oldZoom = 10;
				break;

			case (TOP):
				lookAtNode = this.scene.findNode('main-lookAt');
				lookAtNode.set('up', {
					x : 0,
					y : 1,
					z : 0
				});
				lookAtNode.set('eye', {
					x : 0,
					y : 0,
					z : this.propertyValues.viewfactor
				});
				lookAtNode.set('look', origin);
				this.propertyValues.oldZoom = 10;
				break;
		}
	};

	function resetView(me) {
		var lookAtNode;
		lookAtNode = me.scene.findNode('main-lookAt');
		lookAtNode.set('eye', me.lookAt.defaultParameters.eye);
		lookAtNode.set('look', me.lookAt.defaultParameters.look);
		return lookAtNode.set('up', me.lookAt.defaultParameters.up);
	};

	BIMSceneManager.prototype.updateCameraRatio = function(canvas) {
		if (this.scene != null) {
			var cameraNode = this.scene.findNode('main-camera');
			var cameraOptics = cameraNode.get('optics');
			cameraOptics.aspect = canvas.width / canvas.height;

			cameraNode.set('optics', cameraOptics);
		}
	};

	// ##################################################
	// transparent, zoom and expose nodes
	// ##################################################

	BIMSceneManager.prototype.setNodeTransparentLevel = function(nodeId, factor) {
		if (this.scene == null) {
			return;
		}

		var node = this.scene.findNode(nodeId);

		var transparentMaterial = {
			type : 'material',
			id : nodeId + '-' + 'transparent-walls',
			alpha : factor / 100.0,

			nodes : [ {
				type : 'flags',
				coreid : 'flagsTransparent',
				flags : {
					picking : false, // Picking enabled
					transparent : true,
				}
			} ]
		};

		var insertedMaterial = this.scene.findNode(transparentMaterial.id);
		if (insertedMaterial == null) {
			insertedMaterial = node.insert('node', transparentMaterial);
		} else {

			// there is already a transparent node inserted, update the
			// transparentNode to the new factor

			if (factor == 100) {
				// factor is 100, remove transparency material and set
				// elements pickable again
				insertedMaterial.node(0).splice();
				insertedMaterial.splice();
			} else {
				insertedMaterial.set('alpha', factor / 100.0);
				insertedMaterial.node(0).set('flags', {
					picking : false,
					transparent : true
				});
			}

		}

	};

	BIMSceneManager.prototype.setZoomLevel = function(zoomVal) {
		if (this.scene == null) {
			return;
		}

		var zoomDistance;

		if ((zoomVal >= 0) && (zoomVal <= 20)) {
			var zoomSteps = zoomVal - this.propertyValues.oldZoom;

			zoomDistance = this.camera.distanceLimits[1] * ZOOMSPEEDFACTOR;
			zoomDistance = zoomDistance * zoomSteps;
			this.zoomLookAtNode( //
				this.scene.findNode('main-lookAt'), //
				zoomDistance, //
				this.camera.distanceLimits
			);

			this.propertyValues.oldZoom = zoomVal;
		}

		return 0;
	};

	BIMSceneManager.prototype.zoomLookAtNode = function(node, distance, limits) {
		return node.set( //
			zoomLookAt( //
				distance, //
				limits, //
				{ //
					eye : node.get('eye'),
					look : node.get('look'),
					up : node.get('up')
				}//
			) //
		);
	};

	function zoomLookAt(distance, limits, lookAt) {
		var eye0, eye0len, eye1, eye1len, look;

		eye0 = recordToVec3(lookAt.eye);
		look = recordToVec3(lookAt.look);
		eye0len = SceneJS_math_lenVec3(eye0);
		eye1len = Math.clamp(eye0len + distance, limits[0], limits[1]);
		eye1 = [ 0.0, 0.0, 0.0 ];
		SceneJS_math_mulVec3Scalar(eye0, eye1len / eye0len, eye1);
		return {
			eye : vec3ToRecord(eye1),
			look : lookAt.look,
			up : lookAt.up
		};
	};

	BIMSceneManager.prototype.hideLayer = function(nameOfLayerToHide) {
		var indexLayerToHide = this.visibleLayers.indexOf(nameOfLayerToHide);
		if (indexLayerToHide == -1) {
			// already hidden
		} else {
			this.visibleLayers.splice(indexLayerToHide, 1);
		}

		showVisibleLayers(this);
	};

	BIMSceneManager.prototype.showLayer = function(nameOfLayerToShow) {
		if (this.visibleLayers.indexOf(nameOfLayerToShow) == -1) { // is not already shown
			if (!this.bimProjectLoader.isTypeLoaded(nameOfLayerToShow)) {
				this.bimProjectLoader.loadGeometryForType(nameOfLayerToShow);
			} else {
				this.visibleLayers.push(nameOfLayerToShow);
				showVisibleLayers(this);
			}

		}
	};

	function showVisibleLayers(me) {
		var layersToShow = [];
		for (var i=0, l=me.visibleLayers.length; i<l; ++i) {
			var layerName = me.visibleLayers[i];
			layersToShow.push(layerName.toLowerCase());
		}

		return me.scene.set('tagMask', '^(' + (layersToShow.join('|')) + ')$');
	};

	BIMSceneManager.prototype.showObject = function(objectId) {
		var node = this.scene.findNode('disable-' + objectId);
		if (node != null) {
			node.splice();
		}
	};

	BIMSceneManager.prototype.hideObject = function(objectId) {
		var disableTagJson = {
			type: 'tag',
			tag: 'disable-' + objectId,
			id: 'disable-' + objectId
		};

		var node = this.scene.findNode(objectId);
		if (node != null) {
			node.insert("node", disableTagJson);
		}
	};

	BIMSceneManager.prototype.getBuildingStoreyOfObject  = function(objectId) {
		var storey = null;
		var relationships = this.scene.data().relationships;
		var fakeRoot = {
			contains: relationships,
			id: "fakeRootId",
			type: "FAKE ROOT"
		};

		var node = findNode(objectId, fakeRoot);
		if (node) {
			storey = node.storey;
		}

		return storey;
	};

	function findNode(objectId, root, _currentStorey) {
		var currentStorey = _currentStorey;

		if (root.id == objectId) {
			return {
				node: root,
				storey: currentStorey
			};
		} else {

			if (root.type = "BuildingStorey") {
				currentStorey = root;
			}

			var children = getChildren(root);
			var searchedNode = null;
			for (var i=0, l=children.length; i<l; ++i) {
				var child = children[i]; 
				searchedNode = findNode(objectId, child, currentStorey);
				if (searchedNode) {
					return searchedNode;
				}
			}
		}
	}

	BIMSceneManager.prototype.expose  = function(nodeId, distance) {
		var node = this.scene.findNode(nodeId);
		if (node) {
			expose(this, node, distance);
		}
	};

	function expose(me, node, distance) {
		// create a new translate Node; hierarchies with this node
		// attached
		// get translated
		var translateNodeJson = {
			type : 'translate',
			id : 'translateZone-' + node.get("id"),
			x : 0,
			y : distance,
			z : 0
		};

		if (node.get('type') === 'name') {
			var insertedTranslateNode = me.scene.findNode(translateNodeJson.id);
			if (insertedTranslateNode == null) {
				insertedTranslateNode = node.insert("node", translateNodeJson);
			} else {
				insertedTranslateNode.set('y', distance);
			}
		}
	}

	BIMSceneManager.prototype.exposeNodeWithItsStorey = function(nodeId, distance) {
		var me = this;

		var buildingStorey = this.getBuildingStoreyOfObject(nodeId);
		if (buildingStorey) {
			exposeRelationshipNodeWithAllBranch(me, buildingStorey, distance);
		}

	};

	function exposeRelationshipNodeWithAllBranch(me, relationshipNode, distance) {
		me.expose(relationshipNode.id, distance);

		var children = getChildren(relationshipNode);
		for (var i=0, l=children.length; i<l; ++i) {
			var child = children[i];
			exposeRelationshipNodeWithAllBranch(me, child, distance);
		}
	}

	/*
	 * return a merge of the
	 * contains, decomposedBy, definedBy
	 * arraies of an object of
	 * scene.data().relationships
	 */
	function getChildren(node) {
		var children = [];

		if (node.contains) {
			children = children.concat(node.contains);
		}

		if (node.decomposedBy) {
			children = children.concat(node.decomposedBy);
		}

		if (node.definedBy) {
			children = children.concat(node.definedBy);
		}

		return children;
	}

	// ##################################################
	// Object selection 
	// ##################################################

	BIMSceneManager.prototype.selectObject = function(objectId, fromViewer) {
		this.clearSelection();
		if (objectId) {

			selectSceneObject(this, objectId, fromViewer);
			this.callDelegates("objectSelected", [this, objectId], fromViewer);
		}
	};

	BIMSceneManager.prototype.selectObjectForLongPressure = function(objectId) {
		this.clearSelection();
		if (objectId) {

			selectSceneObject(this, objectId, true);
			this.callDelegates("objectSelectedForLongPressure", [this, objectId], true);
		}
	};

	function selectSceneObject(me, objectId, fromViewer) {
		_BIM_LOGGER.log("Selecting object", objectId);

		me.currentSelectedObjectId = objectId;
		var node = me.scene.findNode(objectId);
		if (node != null) {
			if (node._targetNode && ! fromViewer) {
				var lookAtNode = me.scene.findNode('main-lookAt');
				var nodeCube = getNodeCube(node);
				var nodeCenter = getCubeCenter(nodeCube);
				var nodeView = getCubeView(nodeCube);
				lookAtNode.set('look', nodeCenter);
				lookAtNode.set('eye', nodeView);
				lookAtNode.set('up', {x: 0, y: 0, z: 1});
			}
			changeHighlightMaterial(node);
			node.insert('node', HIGHLIGHT_MATERIAL);
		}
	}

	BIMSceneManager.prototype.clearSelection = function() {
		var oldHighlight = this.scene.findNode(HIGHLIGHT_MATERIAL.id);
		if (oldHighlight != null) {
			// set transparency to 100 otherwise the node
			// could not be selected yet
			// this.setNodeTransparentLevel(this.currentSelectedObjectId, 100);

			this.currentSelectedObjectId = null;
			oldHighlight.splice();

			this.callDelegates("selectionCleaned", [this]);
		}
	};
	function getCubeView(cube) {
		var maxDistance = Math.max(cube.max.x - cube.min.x, cube.max.z - cube.min.z) * 4;
		var view = {
				x : cube.min.x + (cube.max.x - cube.min.x) / 2,	
				y : cube.max.y + maxDistance,	
				z : cube.max.z// + (cube.max.z - cube.min.z) / 2,	
		};
		return view;
	}
	function getCubeCenter(cube) {
		var center = {
				x : cube.min.x + (cube.max.x - cube.min.x) / 2,	
				y : cube.min.y + (cube.max.y - cube.min.y) / 2,	
				z : cube.min.z + (cube.max.z - cube.min.z) / 2,	
		};
		return center;
	}
	function getNodeCube(node) {
		var minx = Number.MAX_VALUE;
		var maxx = -Number.MAX_VALUE;
		var miny = Number.MAX_VALUE;
		var maxy = -Number.MAX_VALUE;
		var minz = Number.MAX_VALUE;
		var maxz = -Number.MAX_VALUE;
		var geometries = [];
		getGeometriesByType(node._targetNode, geometries, "geometry");
		for (var i = 0; i < geometries.length; i++) {
			var positions = geometries[i].core.arrays.positions;
			for (var j = 0; j < positions.length; j += 3) {
				minx = Math.min(minx, positions[j]);
				maxx = Math.max(maxx, positions[j]);
				miny = Math.min(miny, positions[j + 1]);
				maxy = Math.max(maxy, positions[j + 1]);
				minz = Math.min(minz, positions[j + 2]);
				maxz = Math.max(maxz, positions[j + 2]);
			}
		}
		var cube = {
			"min": {"x" : minx, "y" : miny, "z" : minz}, 
			"max": {"x" : maxx, "y" : maxy, "z" : maxz}
		};
		return cube;
	}
	function getGeometriesByType(node, geometries, name) {
		if (node.attr && node.attr.type == name) {
			geometries.push(node);
		}
		if (! node.children) {
			return;
		}
		for (var i = 0; i < node.children.length; i++) {
			getGeometriesByType(node.children[i], geometries, name);
		}
	}
	function getParentByName(node, name) {
		if (! (node && node._targetNode)) {
			return null;
		}
		node = node._targetNode.parent;
		if (node.attr && node.attr.type == name) {
			return node;
		}
		return getParentByName(node, name);
	}
	function changeHighlightMaterial(node) {
		var colorNode = getParentByName(node, "material");
		if (colorNode) {
			var c = colorNode.core.baseColor;
			if (c[0] >= c[1] && c[0] >= c[2] && ! (c[0] == c[1] && c[0] == c[2])) {
				HIGHLIGHT_MATERIAL.baseColor.r = 0;
				HIGHLIGHT_MATERIAL.baseColor.g = 0;
				HIGHLIGHT_MATERIAL.baseColor.b = 1;
			}
			else {
				HIGHLIGHT_MATERIAL.baseColor.r = 1;
				HIGHLIGHT_MATERIAL.baseColor.g = 0;
				HIGHLIGHT_MATERIAL.baseColor.b = 0;
			}
		}
	}

})();