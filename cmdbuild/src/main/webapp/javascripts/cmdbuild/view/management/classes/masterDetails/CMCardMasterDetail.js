(function() {
	
	var MD = "detail";
	var FK = "foreignkey";

	Ext.define("CMDBuild.view.management.classes.masterDetails.CMCardMasterDetail", {
		extend: "Ext.panel.Panel",

		editable: true,
		eventType: 'card',
		eventmastertype: 'class',

		constructor: function() {
			this.addDetailButton = new CMDBuild.AddCardMenuButton({
				classId: undefined,
				baseText: CMDBuild.Translation.management.moddetail.adddetail,
				textPrefix: CMDBuild.Translation.management.moddetail.adddetail
			});

			this.detailGrid = new CMDBuild.Management.MasterDetailCardGrid({
				editable: this.editable,
				cls: "cmborderright",
				border: false,
				region: "center",
				columns: [],
				loadMask: false,
				cmAdvancedFilter: false,
				cmAddGraphColumn: false
			});

			this.tabs = new CMDBuild.view.common.CMSideTabPanel({
				region: "east"
			});

			Ext.apply(this, {
				border: false,
				frame: false
			});
			this.callParent(arguments);
		},

		initComponent: function() {

			Ext.apply(this, {
				layout: "border",
				tbar: [ this.addDetailButton ],
				items: [ this.detailGrid, this.tabs]
			});

			this.callParent(arguments);
		},

		loadDetailsAndFKThenBuildSideTabs: function(classId) {
			// for blocking the onTabClick events that comes during the building
			// anyway the onTabClick is called at end of buildTabs
			this.buildingTabsDetails = true;
			this.addDetailButton.disable();
			var domainList = _CMCache.getMasterDetailsForClassId(classId),
				me = this;

			this.disable();
			this.empty = true;
			this.details = {};
			this.details[MD] = {};
			this.details[FK] = {};

			for (var i = 0, len = domainList.length; i < len; i++) {
				var domain = domainList[i];
				domain['directedDomain'] = setDirectedDomain(domain);
				this.details[MD][getId(domain)] = domain;
			}

			var params = {};
			params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(classId);
			CMDBuild.ServiceProxy.getFKTargetingClass( {
				params: params,
				scope: me,
				success: takeFkAttributesAndBuildTabs
			});

			function takeFkAttributesAndBuildTabs(response, options, attributes) {
				this.details[FK] = {};
				this.tabs.removeAll();

				for (var i=0, l = attributes.length; i < l; ++i) {
					var attr = attributes[i];
					this.details[FK][getId(attr)] = attr;
				}
				this.buildingTabsDetails = false;
				if (CMDBuild.Utils.isEmpty(this.details[FK]) 
						&& CMDBuild.Utils.isEmpty(this.details[MD])) {

					this.fireEvent("empty");
				} else {
					this.empty = false;
					this.enable();
					buildTabs.call(this);
				}
			}
		},

		selectDetail: function(detail) {
			var et = _CMCache.getEntryTypeById(getDetailClass(detail));

			if (et) {
				this.addDetailButton.updateForEntry(et);
			}

		},

		selectForeignKey: function(fkAttribute) {
			var et = _CMCache.getEntryTypeById(fkAttribute.idClass);

			if (et) {
				this.addDetailButton.updateForEntry(et);
			}
		},

		resetDetailGrid: function() {
			this.detailGrid.reset();
		},

		activateFirstTab: function() {
			this.tabs.activateFirst();
		},

		updateGrid: function(type, p) {
			if (type == MD) {
				this.detailGrid.loadDetails(p);
			} else {
				this.detailGrid.loadFk(p);
			}
		},

		loadDetailCardList: function(attributeList, cardId, classId, idDomain, superclass, classType) {
			this.actualAttributeList = attributeList;
			this.idDomain = idDomain;
			this.detailGrid.loadDetailCardList( {
				directedDomain: idDomain,
				cardId: cardId,
				classId: classId,
				classAttributes: attributeList,
				className: this.currentDetail.name,
				superclass: superclass,
				classType: classType
			});
		},

		loadFKCardList: function(attributes, fkClass, fkAttribute, idCard) {
			this.detailGrid.loadFKCardList(attributes, fkClass, fkAttribute, idCard);
			this.isLoaded = true;
		},
		
		reload: function() {
			this.detailGrid.reload();
		},

		// DEPRECATED

		onAddCardButtonClick: function() { _deprecated();
			this.disable();
		},

		onClassSelected: function() {_deprecated();
			// Something is done on the controller, something else on the view: this is a mess!
			this.disable();
		}
	});

	function getId(tab) {
		if (typeof tab.get == "undefined") {
			// is a fk
			return tab.idClass + "_" + tab.name;
		} else {
			// is a md
			return tab.get("name");
		}
	}

	function setDirectedDomain(domain) {
		var cardinality = domain.get("cardinality"),
			idDomain = domain.get("id");
		
		if (cardinality == "1:N") {
			return idDomain + "_D";
		} else if (cardinality == "N:1") {
			return idDomain + "_I";
		} else {
			CMDBuild.log.error('Wrong cardinality');
		}
	}

	function buildTabs() {
		var details = this.details;
		function build() {
			this.tabs.removeAll(true);
			var tabs = Ext.apply(details[MD], details[FK]),
				detailLabel="",
				detailId="",
				type="",
				t;

			var sortedKeys = sortKeys(tabs);
			for (var i = 0, l=sortedKeys.length; i<l; i++) {
				var detailId = sortedKeys[i];
				t = tabs[detailId];

				if (typeof t.get == "undefined") {
					// there is a FK and t is the server serialization of the fk-attribute
					type = FK;
					detailLabel = t.description;
				} else {
					// there is a MD and t is the Ext model of the domain
					type = MD;
					detailLabel = t.get("md_label") || t.get("description");
				}

				this.tabs.addTabFor({
					title: detailLabel,
					tabLabel: detailLabel,
					detailType: type,
					detailId: detailId,
					on: function() {}
				}, type);
			}

			this.mon(this.tabs, "afterlayout", function() {
				this.tabs.activateFirst();
			}, this, {single: true});

			this.doLayout();
		}

		if (this.isVisible()) {
			build.call(this);
		} else {
			this.on("show", build, this, {single: true});
		}

	}

	function sortKeys(tabs) {
		var keys = [];
		for (var key in tabs) {
			keys.push(key);
		}

		return Ext.Array.sort(keys, function sortingFunction(key1, key2) {
			var obj1 = tabs[key1],
				obj2 = tabs[key2],
				data1 = obj1.data || obj1,
				data2 = obj2.data || obj2,
				string1 = data1.md_label || data1.description || key1,
				string2 = data2.md_label || data2.description || key2;

			return string1.toUpperCase() > string2.toUpperCase();
		});
	};

	function getDetailClass(detail) {
		var cardinality = detail.get("cardinality");
		if (cardinality == "1:N") {
			return detail.get("idClass2");
		} else if (cardinality == "N:1") {
			return detail.get("idClass1");
		}
	}

	function showAddDetailWindow(attributes, detail) {
		var idDomain;
		if (this.currentDetail) {
			idDomain = this.currentDetail.directedDomain;
		} else {
			idDomain = this.currentForeignKey.id;
		}

		var win = new CMDBuild.Management.AddDetailWindow( {
			titlePortion: "",
			detail: detail,
			classAttributes: attributes,
			fkAttribute: this.currentforeignKeyAttribute,
			masterData: this.actualMasterData,
			idDomain: idDomain,
			classId: detail.classId,
			className: detail.className
		});
		win.show();
	}
})();