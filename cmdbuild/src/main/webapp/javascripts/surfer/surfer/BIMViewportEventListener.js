(function() {
	var PICKDRAGTHRESHOLD = 10;
	var ORBITSPEEDFACTOR = 0.05;
	var MAXORBITSPEED = Math.PI * 0.1;
	var PANSPEEDFACTOR = 0.6;
	var ZOOMSPEEDFACTOR = 0.05;
	var LONG_PRESS_TRESHOLD = 600;

	var startingPoint = {x:0, y:0};

	BIMViewportEventListener = function(viewportId, bimSceneManager) {

		this.sceneManager = bimSceneManager;
		this.viewport = {
			domElement : viewportDOMElement,
			selectedIfcObject : null,
			mouse: {
				last: [0, 0],
				leftDown: false,
				middleDown: false,
				leftDragDistance: 0,
				middleDragDistance: 0,
				pickRecord: null
			}
		};

		var viewportDOMElement = document.getElementById(viewportId);
		var me = this;
		viewportDOMElement.onmousedown = //
				function(event) {
					mouseDown(event, me);
				}; //

			viewportDOMElement.onmouseup = //
				function(event) {
					mouseUp(event, me);
				};

			viewportDOMElement.onmousemove =//
				function(event) {
					mouseMove(event, me);
				};


		viewportDOMElement.addEventListener( //
			'mousewheel', //
			function(event) {
				mouseWheel(event, me);
			}, //
			false //
		);

	};

	function mouseDown(event, me) {
		coords = mouseCoordsWithinElement(event);
		if (! event.ctrlKey) {
			manageLongPress(me, event);
		}

		var coords, picknode;
		if (! me.sceneManager.scene) {
			return;
		}

		me.viewport.mouse.lastEvent = event;

		switch (event.which) {
		case 1:
			me.viewport.mouse.leftDown = true;
			break;
		case 2:
			me.viewport.mouse.middleDown = true;
		}

		if (event.which === 1) {
			coords = mouseCoordsWithinElement(event);
			// check if selected Object is a special object
			me.viewport.mouse.pickRecord = me.sceneManager.scene.pick(coords[0], coords[1]);

			return 0;
		}
	};

	function manageLongPress(me, event) {
		me._mouseDown = true;
		me._longPressure = false;

		window.setTimeout(
				function() {
					if (me._mouseDown && !isMouseMoved(me)) {
						me._mouseDown = false;
						me._longPressure = true;
						selectSceneObject(me, event, true);
						clearSelectionMovement(me, event);
					}
				},

				LONG_PRESS_TRESHOLD
		);
	}
	
	function isMouseMoved(me) {
		return me.viewport.mouse.leftDragDistance >= PICKDRAGTHRESHOLD
			|| me.viewport.mouse.middleDragDistance >= PICKDRAGTHRESHOLD;
	}

	function selectSceneObject(me, event, forLongPressure) {
		if (me.viewport.mouse.pickRecord) {
			if (forLongPressure) {
				me.sceneManager.selectObjectForLongPressure(me.viewport.mouse.pickRecord.name);
			} else {
				me.sceneManager.selectObject(me.viewport.mouse.pickRecord.name, true);
			}
		} else {
			me.sceneManager.clearSelection();
		}

		me.viewport.mouse.pickRecord = null;
	}

	function clearSelectionMovement(me, event) {
		// switch between Navigation Mode (pan/rotate)
		var navigationMode = me.sceneManager.getNavigationMode();
		switch (navigationMode) {
		case 0:
			me.viewport.mouse.leftDragDistance = 0;
			break;
		case 1:
			me.viewport.mouse.middleDragDistance = 0;
			break;
		}

		switch (event.which) {
		case 1:
			me.viewport.mouse.leftDown = false;
			return me.viewport.mouse.leftDragDistance = 0;
		case 2:
			me.viewport.mouse.middleDown = false;
			return me.viewport.mouse.middleDragDistance = 0;
		}
	}

	function mouseUp(event, me) {
		if (event.ctrlKey) {
			clearSelectionMovement(me, event);
			return;
		}
			
		if (me._longPressure) {
			return;
		}

		me._mouseDown = false;
		if (! me.sceneManager.scene) {
			return;
		}

		if (event.which === 1
				&& !isMouseMoved(me)) {

			selectSceneObject(me, event, false);
		}

		clearSelectionMovement(me, event);
	};
	function mouseMoveObject(event, me) {
		oidOggettoDaSpostare = me.viewport.mouse.pickRecord.name.toString();
		objectHasMoved = true;
		if (! me.sceneManager.scene.findNode("obj-translate" + me.viewport.mouse.pickRecord.name.toString())) {
			var moveNode = me.sceneManager.scene.findNode(me.viewport.mouse.pickRecord.name);
			moveNode.insert("node", {
				type : "translate",
				id : "obj-translate" + me.viewport.mouse.pickRecord.name.toString(),
				x : 0.0,
				y : 0.0,
				z : 0.0
			});
		}
		var objTranslate = me.sceneManager.scene.findNode("obj-translate" + me.viewport.mouse.pickRecord.name.toString());
		var coords = mouseCoordsWithinElement(me.viewport.mouse.lastEvent);
		var hitLast = me.sceneManager.scene.pick(coords[0], coords[1], { rayPick: true });
		var coords = mouseCoordsWithinElement(event);
		var hitFirst = me.sceneManager.scene.pick(coords[0], coords[1], { rayPick: true });

		if (hitFirst && hitLast) {
			// Check if object still inside a room (TODO a real check funtion and error handling)
			var worldPosFirst = hitFirst.worldPos;
			var worldPosLast = hitLast.worldPos;
			var dx = ( startingPoint.x + worldPosFirst[0] - worldPosLast[0]);
            var dy = ( startingPoint.y + worldPosFirst[1] - worldPosLast[1]);
            dx += objTranslate.get("x");
            dy += objTranslate.get("y");
			objTranslate.set({
				x : dx,
				y : dy
			});
			
		}
	
	}
	function mouseMove(event, me) {
		var delta, deltaLength, orbitAngles, panVector;
		if (! me.viewport.mouse.lastEvent) {
			me.viewport.mouse.lastEvent = event;
			return;
		}
		delta = [ //
			event.clientX - me.viewport.mouse.lastEvent.clientX, //
			event.clientY - me.viewport.mouse.lastEvent.clientY //
		];

		if (delta[0] == 0 && delta[1] == 0) {
			me.viewport.mouse.lastEvent = event;
			return;  // avoids disappearing
		}
		if (me.viewport.mouse.pickRecord && event.ctrlKey && me.viewport.mouse.leftDown) {
			mouseMoveObject(event, me);
			me.viewport.mouse.lastEvent = event;
			return;
		}

		// object
		deltaLength = SceneJS_math_lenVec2(delta);
		if (me.viewport.mouse.leftDown) {
			// check which navigation mode is activated
			if (me.sceneManager.getNavigationMode() == 0) {
				me.viewport.mouse.leftDragDistance += deltaLength;
			} else {
				me.viewport.mouse.middleDragDistance += deltaLength;
			}
		}

		if (me.viewport.mouse.middleDown) {
			me.viewport.mouse.middleDragDistance += deltaLength;
		}

		if (me.viewport.mouse.leftDown && event.which === 1) {

			if (me.sceneManager.getNavigationMode() == 0) {
				orbitAngles = [ 0.0, 0.0 ];
				SceneJS_math_mulVec2Scalar(delta, ORBITSPEEDFACTOR / deltaLength, orbitAngles);
				orbitAngles = [ //
					Math.clamp(orbitAngles[0], -MAXORBITSPEED, MAXORBITSPEED), //
					Math.clamp(orbitAngles[1], -MAXORBITSPEED, MAXORBITSPEED) //
				];

				if ((isNaN(orbitAngles[0])) || (Math.abs(orbitAngles[0])) === Infinity) {
					orbitAngles[0] = 0.0;
				}

				if ((isNaN(orbitAngles[1])) || (Math.abs(orbitAngles[1])) === Infinity) {
					orbitAngles[1] = 0.0;
				}

				me.sceneManager.orbitLookAtNode( //
					me.sceneManager.scene.findNode('main-lookAt'), //
					orbitAngles, [ 0.0, 0.0, 1.0 ] //
				);

			} else {
				panVector = [ 0.0, 0.0 ];
				SceneJS_math_mulVec2Scalar( //
					[-delta[0], delta[1]], //
					PANSPEEDFACTOR * 1 / me.sceneManager.propertyValues.scalefactor / deltaLength, //
					panVector //
				);

				me.sceneManager.lookAtNodePanRelative( //
					me.sceneManager.scene.findNode('main-lookAt'), //
					panVector //
				);
			}

		} else if (me.viewport.mouse.middleDown && event.which === 2) {
			panVector = [0.0, 0.0];
			SceneJS_math_mulVec2Scalar( //
				[-delta[0], delta[1]], //
				PANSPEEDFACTOR * 1 / me.sceneManager.propertyValues.scalefactor / deltaLength, //
				panVector //
			);

			me.sceneManager.lookAtNodePanRelative( //
				me.sceneManager.scene.findNode('main-lookAt'), //
				panVector //
			);
		}

		return me.viewport.mouse.lastEvent = event;
	};

	function mouseWheel(event, me) {
		var delta, zoomDistance;
		if (me.sceneManager.scene == null) {
			return;
		}

		delta = event.wheelDelta != null ? event.wheelDelta / -120.0 : Math.clamp(event.detail, -1.0, 1.0);

		me.sceneManager.propertyValues.oldZoom = Math.clamp(me.sceneManager.propertyValues.oldZoom + delta, 0, 20);

		//GWT: window.callbackZoomLevelAbsolute(othis.propertyValues.oldZoom);
		zoomDistance = delta * me.sceneManager.camera.distanceLimits[1] * ZOOMSPEEDFACTOR;

		return me.sceneManager.zoomLookAtNode( //
			me.sceneManager.scene.findNode('main-lookAt'), //
			zoomDistance, //
			me.sceneManager.camera.distanceLimits //
		);
	};

	function mouseCoordsWithinElement(event) {
		var coords, element, totalOffsetLeft, totalOffsetTop;
		coords = [ 0, 0 ];
		if (!event) {
			event = window.event;
			coords = [ event.x, event.y ];
		} else {
			element = event.target;
			totalOffsetLeft = 0;
			totalOffsetTop = 0;
			while (element.offsetParent) {
				totalOffsetLeft += element.offsetLeft;
				totalOffsetTop += element.offsetTop;
				element = element.offsetParent;
			}
			coords = [ event.pageX - totalOffsetLeft, event.pageY - totalOffsetTop ];
		}
		return coords;
	};
})();