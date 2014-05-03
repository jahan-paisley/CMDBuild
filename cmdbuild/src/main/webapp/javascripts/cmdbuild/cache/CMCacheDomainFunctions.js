(function() {

	var domains = {},
		domainsMapIdAndName = {},
		attributeStore = null,
		ID_CLASS_1 = "idClass1",
		ID_CLASS_2 = "idClass2";

	Ext.define("CMDBUild.cache.CMCacheDomainFunctions", {

		addDomains: function(dd) {
			for (var i=0, l=dd.length; i<l; ++i) {
				this.addDomain(dd[i]);
			}
		},

		addDomain: function(d) {
			var domainModel = Ext.create("CMDBuild.cache.CMDomainModel", {
				active: d.active,
				id: d.idDomain,
				cardinality: d.cardinality,
				nameClass1: d.class1,
				nameClass2: d.class2,
				idClass1: d.class1id,
				idClass2: d.class2id,
				classType: d.classType,
				name: d.name,
				createPrivileges: d.priv_create,
				writePrivileges: d.priv_write,
				isMasterDetail: d.md,
				description: d.description,
				descr_1: d.descrdir,
				descr_2: d.descrinv,
				meta: d.meta,
				attributes: d.attributes,
				md_label: d.md_label
			});

			domainModel.isMany = function(side) {
				var c = this.get("cardinality");
				c = c.split(":");
				if (side == "_1") {
					return c[0] == "N";
				} else {
					return c[1] == "N";
				}
			};

			domains[d.idDomain] = domainModel;
			return domainModel;
		},

		getDomains: function() {
			return domains;
		},

		getDomainById: function(id) {
			return domains[id];
		},

		getDomainByName: function(name) {
			for (var domain in domains) {
				domain = domains[domain];
				if (domain.get("name") == name) {
					return domain;
				}
			}

			_debug("There are no domains with name " + name);
			return null;
		},
		
		getDomainNameById: function(id) {
			if (typeof domainsMapIdAndName[id] == "undefined") {
				var et = this.getDomainById(id);
				if (et) {
					domainsMapIdAndName[id] = et.get("name");
				} else {
					domainsMapIdAndName[id] = "";
				}
			}

			return domainsMapIdAndName[id];
		},

		getDomainAttributesStore: function() {
			if (!attributeStore) {
				var _p = _CMProxy.parameter;
				var ATTR = {
					INDEX: _p.INDEX,
					NAME: _p.NAME,
					DESCRIPTION: _p.DESCRIPTION,
					TYPE: _p.TYPE,
					IS_BASEDSP: _p.DISPLAY_IN_GRID,
					IS_UNIQUE: _p.UNIQUE,
					IS_NOT_NULL: _p.NOT_NULL,
					IS_INHERITED: "inherited",
					IS_ACTIVE: _p.ACTIVE,
					FIELD_MODE: _p.FIELD_MODE,
					GROUP: _p.GROUP,
					ABSOLUTE_CLASS_ORDER: "absoluteClassOrder",
					CLASS_ORDER_SIGN: "classOrderSign",
					EDITOR_TYPE: _p.EDITOR_TYPE
				};

				attributeStore = new Ext.data.Store({
					fields: [
						ATTR.INDEX,
						ATTR.NAME,
						ATTR.DESCRIPTION,
						ATTR.TYPE,
						ATTR.IS_UNIQUE,
						ATTR.IS_BASEDSP,
						ATTR.IS_NOT_NULL,
						ATTR.IS_INHERITED,
						ATTR.FIELD_MODE,
						ATTR.IS_ACTIVE,
						ATTR.GROUP
					],
					autoLoad : false,
					data: [],
					sorters : [ {
						property : 'index',
						direction : "ASC"
					}],
					loadForDomainId: function(domainId) {
						this.lastDomainLoaded = domainId;
						this.removeAll();
						if (domains[domainId]) {
							var rr = domains[domainId].get("attributes") || [];
							if (rr.length > 0) {
								this.loadData(rr);
							}
						}
					},
					reloadForLastDomainId: function() {
						this.loadForDomainId(this.lastDomainLoaded);
					}
				});
			}

			return attributeStore;
		},

		getDirectedDomainForEntryType: function(et, domainName) {
			var domain = _CMCache.getDomainByName(domainName);
			var anchestorsId = _CMUtils.getAncestorsId(et);
			var cid1 = domain.get(ID_CLASS_1);
			var cid2 = domain.get(ID_CLASS_2);

			if (Ext.Array.contains(anchestorsId, cid1)) {
				var et2 = _CMCache.getEntryTypeById(cid2);
				if (et2) {
					return {
						dom_id: domain.get("id"),
						description: domain.get("descr_1") + " (" + et2.get("text") + ")",
						dst_cid: cid2,
						src_cid: cid1,
						src: "_1"
					};
				}
			} else if (Ext.Array.contains(anchestorsId, cid2)) {
				var et1 = _CMCache.getEntryTypeById(cid1);
				if (et1) {
					return {
						dom_id: domain.get("id"),
						description: domain.get("descr_2") + " (" + et1.get("text") + ")",
						dst_cid: cid1,
						src_cid: cid2,
						src: "_2"
					};
				}
			}
		},

		getDirectedDomainsByEntryType: function(et) {
			if (typeof et == "object") {
				et = et.get("id");
			}

			var out = [],
				anchestorsId = _CMUtils.getAncestorsId(et);

			for (var domain in domains) {
				domain = domains[domain];
				var cid1 = domain.get(ID_CLASS_1);
				var cid2 = domain.get(ID_CLASS_2);

				if (Ext.Array.contains(anchestorsId, cid1)) {
					var et2 = _CMCache.getEntryTypeById(cid2);
					if (et2) {
						out.push({
							dom_id: domain.get("id"),
							description: domain.get("descr_1") + " (" + et2.get("text") + ")",
							dst_cid: cid2,
							src_cid: cid1,
							src: "_1"
						});
					}
				}

				if (Ext.Array.contains(anchestorsId, cid2)) {
					var et1 = _CMCache.getEntryTypeById(cid1);
					if (et1) {
						out.push({
							dom_id: domain.get("id"),
							description: domain.get("descr_2") + " (" + et1.get("text") + ")",
							dst_cid: cid1,
							src_cid: cid2,
							src: "_2"
						});
					}
				}
			}

			return out;
		},

		getDomainsBy: function(fn) {
			var out = [];

			for (var domainId in domains) {
				var domain = domains[domainId];
				if (typeof fn == "function") {
					if (fn(domain)) {
						out.push(domain);
					}
				} else {
					out.push(domain);
				}
			}

			return out;
		},

		getMasterDetailsForClassId: function(id) {
			var out = [];
			for (var domain in domains) {
				domain = domains[domain];
				if (domain.get("isMasterDetail")) {
					if (givenClassIdIsTheMasterForDomain(domain, id)) { 
						out.push(Ext.create("CMDBuild.cache.CMDomainModel", domain.data));
					}
				}
			}
			
			return out;
		},

		onDomainSaved: function(domain) {
			var d = this.addDomain(domain);
			this.fireEvent("cm_domain_saved", d);

			return d;
		},

		onDomainDeleted: function(domainId) {
			domains[domainId] = undefined;
			delete domains[domainId];

			this.fireEvent("cm_domain_deleted", domainId);
		},

		onDomainAttributeSaved: function(domainId, attribute) {
			var domainAttributes = domains[domainId].get("attributes") || [];
			eraseAttribute(domainAttributes, attribute); // to manage the modify of an existing attribute

			domainAttributes.push(attribute);

			if (attributeStore) {
				attributeStore.reloadForLastDomainId();
			}
			
		},
		
		onDomainAttributeDelete: function(domainId, attribute) {
			var domainAttributes = domains[domainId].get("attributes") || [];
			eraseAttribute(domainAttributes, attribute);

			if (attributeStore) {
				attributeStore.reloadForLastDomainId();
			}
		}
	});

	function givenClassIdIsTheMasterForDomain(domain, id) {
		var cardinality = domain.get("cardinality"),
			idClass1 = domain.get(ID_CLASS_1),
			idClass2 = domain.get(ID_CLASS_2),
			ancestors = _CMUtils.getAncestorsId(id);

		if (cardinality == "1:N") {
			return Ext.Array.contains(ancestors, idClass1);
		} else if (cardinality == "N:1") {
			return Ext.Array.contains(ancestors, idClass2);
		}
	}

	function eraseAttribute(domainAttributes, attribute) {
		for (var i=0, l=domainAttributes.length; i<l; ++i) {
			if (domainAttributes[i].name == attribute.name) {
				Ext.Array.erase(domainAttributes, i, 1);
				break;
			}
		}
	}

})();