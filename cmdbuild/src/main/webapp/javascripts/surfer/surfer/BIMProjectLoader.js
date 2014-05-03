(function() {

	BIMProjectLoader = function(delegate, classNames, progressBar) {
		this.currentAction = {};

		/**
		 * this object must
		 * implements that methods
		 *		projectDidLoad(scene)
		 * 		putGeometriesInScene(data, currentLayerName, currentLayerId);
		 */
		this.delegate = delegate;

		/**
		 *	an array with the name of
		 * 	the IFC class to load for a project
		 */
		this.classNames = classNames || [];

		/**
		 * this array contains the name
		 * of the loaded layers
		 * 
		 * ex: xfcRoof, xfcDoor
		 * 
		 * it's filled incrementally
		 * after each call to load a layer
		 */
		this.loadedTypes = [];

	};

	BIMProjectLoader.prototype.loadFromCmdbuild = function(roid, basePoid) {
		var me = this;
		this.loadedTypes = [];
		this.currentAction = {
			roid: roid,
			poid: basePoid
		};

		CMDBuild.LoadMask.get().show();
		CMDBuild.bim.proxy.fetchJsonForBimViewer({
			params: {
				baseProjectId: basePoid, //
				revisionId: roid
			},
			success: function(fp, request, response) {
				if (me.delegate 
						&& typeof me.delegate.projectDidLoad == "function") {

						me.delegate.projectDidLoad(response);
					}
			},
			failure: function() {
				CMDBuild.LoadMask.get().hide();
			}
		});
	};

	BIMProjectLoader.prototype.loadGeometries = function(ifcTypes) {
		
		var me = this;
		window._BIM_SERVER_API.call( //
			"PluginInterface", //
			"getSerializerByPluginClassName", //
			{
				pluginClassName : "org.bimserver.geometry.json.JsonGeometrySerializerPlugin"
			}, //
			function(serializer) {
				me.typeDownloadQueue = me.classNames;//.slice(0);

				// Remove the types that are not there anyways
				me.typeDownloadQueue.sort();
				ifcTypes.sort();
				me.typeDownloadQueue = intersect_safe(me.typeDownloadQueue, ifcTypes);

				me.loadGeometry(me.currentAction.roid, serializer.oid);
			}, //
			function() {
				window._BIM_LOGGER.log("GET_SERIALIZER_BY_PLUGIN_CLASSNAME: fail");
			}
		);
	};

	BIMProjectLoader.prototype.loadGeometry = function(roid, serializerOid) {
		if (this.typeDownloadQueue.length == 0) {
			CMDBuild.LoadMask.get().hide();
			return;
		}
		var className = this.typeDownloadQueue[0];
		this.typeDownloadQueue = this.typeDownloadQueue.slice(1);
		
		var me = this;

		window._BIM_SERVER_API.call( //
			"Bimsie1ServiceInterface", //
			"downloadByTypes", //
			{
				roids: [roid],
				classNames: [className],
				serializerOid: serializerOid,
				includeAllSubtypes: false,
				useObjectIDM: false,
				sync: false,
				deep: true
			}, //
			function(laid) {
				me.mode = "loading";
				me.currentAction.serializerOid = serializerOid;
				me.currentAction.laid = laid;
				me.currentAction.roid = roid;
				me.currentAction.className = className;
				
				window._BIM_SERVER_API.call( //
					"Bimsie1NotificationRegistryInterface", //
					"getProgress", //
					{
						topicId: laid
					}, //
					function(state) {
						window._BIM_LOGGER.log("STATE:", state);
						me.drawGeometries(laid, state);
					}, //
					function() {
						window._BIM_LOGGER.log("GET_PROGRESS_FAIL");
					}
				);
	
			} //
		);
	};

	BIMProjectLoader.prototype.drawGeometries = function(topicId, state) {
		var me = this;
		var url = window._BIM_SERVER_API.generateRevisionDownloadUrl({
			serializerOid : me.currentAction.serializerOid,
			laid : me.currentAction.laid
		});
			
		
		$.getJSON(url, function(data) {
			var currentLayerName = me.currentAction.className;
			var currentLayerId = currentLayerName.toLowerCase();
	
			me.loadedTypes.push(currentLayerName);
			me.loadGeometry(me.currentAction.roid, me.currentAction.serializerOid);
			
			if (me.delegate
				&& typeof me.delegate.putGeometriesInScene == "function") {
				
				me.delegate.putGeometriesInScene(data, currentLayerName, currentLayerId);
			}
	
		});
	};


	BIMProjectLoader.prototype.loadGeometryForType = function(typeName) {
		this.typeDownloadQueue = [typeName];
		var me = this;
		CMDBuild.LoadMask.get().show();
		window._BIM_SERVER_API.call("PluginInterface", "getSerializerByPluginClassName", {
			pluginClassName : "org.bimserver.geometry.json.JsonGeometrySerializerPlugin"
		}, function(serializer) {
			me.loadGeometry(me.currentAction.roid, serializer.oid);
		});
	};

	BIMProjectLoader.prototype.isTypeLoaded = function(typeName) {
		return this.loadedTypes.indexOf(typeName) > -1;
	};

	// http://stackoverflow.com/questions/1885557/simplest-code-for-array-intersection-in-javascript	
	function intersect_safe(a, b) {
		var ai=0, bi=0;
		var result = new Array();

		while(ai < a.length && bi < b.length) {
			if (a[ai] < b[bi]){
				ai++;
			} else if (a[ai] > b[bi]) {
				bi++;
			} else { // are equals
				result.push(a[ai]);
				ai++;
				bi++;
			}
		}

		return result;
	}

})();