(function() {
	var detailURL = "services/json/management/modcard/getdetaillist",
		fkURL =  "services/json/management/modcard/getcardlist";

	Ext.define("CMDBuild.Management.MasterDetailCardGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		// configuration
		cmAddPrintButton: false,
		// configuration

		loadDetails: function(p) {
			var domain = p.detail;
			var parameterNames = CMDBuild.ServiceProxy.parameter;
			var masterCardClassId = p.masterCard.get("IdClass");
			var masterCardClassName = _CMCache.getEntryTypeNameById(masterCardClassId); // needed if is a subclass of the domain master class

			function setExtraParamsAndLoad(me) {
				me.store.proxy.url = detailURL;

				var filter = {
					relation: [{
						domain: domain.getName(),
						type: "oneof",
						destination: domain.getMasterClassName(),
						source: domain.getDetailClassName(),
						direction: domain.getDetailSide(),
						cards:[{
							className: masterCardClassName,
							id: p.masterCard.get("Id")
						}]
					}]
				};

				me.store.proxy.extraParams[parameterNames.FILTER] = Ext.encode(filter);
				me.store.proxy.extraParams[parameterNames.CLASS_NAME] = domain.getDetailClassName();

				me.store.loadPage(1);
			}

			load(this, domain.getDetailClassId(), setExtraParamsAndLoad);
		},

		loadFk: function(p) {
			var idClass = p.detail.idClass,
				fkClass = _CMCache.getEntryTypeById(idClass);

			function setExtraParamsAndLoad(me) {
				me.store.proxy.url = fkURL;
				me.store.proxy.extraParams['IdClass'] = idClass;
				me.store.proxy.extraParams['CQL'] = "from " 
					+ fkClass.get("name") 
					+ " where " + p.detail.name + "=" 
					+ p.masterCard.get("Id");

				me.store.loadPage(1);
			}

			load(this, idClass, setExtraParamsAndLoad);
		},

		updateStoreForClassId: function(classId, cb, scope) {
			this.currentClassId = classId;
			_CMCache.getAttributeList(classId, 
				Ext.bind(function(attributes) {
					this.setColumnsForClass(attributes);
					this.setGridSorting(attributes);
					if (cb) {
						cb(scope);
					}
				}, this)
			);
		},

		reset: function() {
			this.store.removeAll();
			this.currentClassId = null;
			this.reconfigure(null, []);
		},

		// override
		buildExtraColumns: function() {
			return [{
				header : '&nbsp',
				fixed : true,
				sortable : false,
				renderer : imageTagBuilderForIcon,
				align : 'center',
				tdCls : 'grid-button',
				dataIndex : 'Fake',
				menuDisabled : true,
				hideable : false
			}];
		}
	});

	function load(me, idClassToLoad, setExtraParamsAndLoad) {
		if (me.currentClassId != idClassToLoad) {
			me.updateStoreForClassId(idClassToLoad, setExtraParamsAndLoad, me);
		} else {
			setExtraParamsAndLoad(me);
		}
	}

	function getIconsToRender(record) {
		var icons = ["showDetail", "showGraph", "note"];
		var privileges = _CMUtils.getEntryTypePrivilegesByCard(record);
		if (privileges.write) {
			icons = ["editDetail", "deleteDetail", "showGraph", "note"];
		}

		if (CMDBuild.Config.dms.enabled == "true") {
			icons.push("attach");
		}

		return icons;
	}

	function imageTagBuilderForIcon(value, meta, record) {
		var iconsToRender = getIconsToRender(record),
			ICONS_FOLDER = "images/icons/",
			ICONS_EXTENSION = "png",
			EVENT_CLASS_PREFIX = "action-masterdetail-",
			TAG_TEMPLATE = '<img style="cursor:pointer" title="{0}" class="{1}{2}" src="{3}{4}.{5}"/>&nbsp;',
			tag = "",
			icons = {
				showDetail: {
					title: CMDBuild.Translation.management.moddetail.showdetail,
					event: "show",
					icon: "zoom"
				},
				editDetail: {
					title: CMDBuild.Translation.management.moddetail.editdetail,
					event: "edit",
					icon: "modify"
				},
				deleteDetail: {
					title: CMDBuild.Translation.management.moddetail.deletedetail,
					event: "delete",
					icon: "cross"
				},
				showGraph: {
					title: CMDBuild.Translation.management.moddetail.showgraph,
					event: "graph",
					icon: "chart_organisation"
				},		
				note: {
					title: CMDBuild.Translation.management.moddetail.shownotes,
					event: "note",
					icon: "note"
				},
				attach: {
					title: CMDBuild.Translation.management.moddetail.showattach,
					event: "attach",
					icon: "attach"
				}
			};

		function buildTag(iconName) {
			var icon = icons[iconName];
			if (icon) {
				return Ext.String.format(TAG_TEMPLATE, icon.title, EVENT_CLASS_PREFIX, icon.event, ICONS_FOLDER, icon.icon, ICONS_EXTENSION);
			} else {
				return Ext.String.format("<span>{0}</span>", iconName);
			}
		}

		if (Ext.isArray(iconsToRender)) {
			for (var i=0, len=iconsToRender.length; i<len; ++i) {
				tag += buildTag(iconsToRender[i]);
			}
		} else {
			tag = buildTag("");
		}

		return tag;
	}
})();