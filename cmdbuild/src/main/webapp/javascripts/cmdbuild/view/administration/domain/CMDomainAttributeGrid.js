(function() {

	var translation = CMDBuild.Translation.administration.modClass.attributeProperties;
	var ATTRIBUTES = CMDBuild.view.administration.classes.CMAttributeGrid.ATTRIBUTES;

	Ext.define("CMDBuild.view.administration.domain.CMDomainAttributeGrid", {
		extend: "CMDBuild.view.administration.classes.CMAttributeGrid",

		buildColumnConf: function() {
			this.columns = [{
				header: translation.name,
				dataIndex: ATTRIBUTES.NAME,
				flex: 1
			}, {
				header: translation.description,
				dataIndex: ATTRIBUTES.DESCRIPTION,
				flex: 1
			}, {
				header: translation.type,
				dataIndex: ATTRIBUTES.TYPE,
				flex: 1
			},
			new Ext.ux.CheckColumn( {
				header: translation.isbasedsp,
				dataIndex: ATTRIBUTES.IS_BASEDSP,
				cmReadOnly: true
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isunique,
				dataIndex: ATTRIBUTES.IS_UNIQUE,
				cmReadOnly: true
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isnotnull,
				dataIndex: ATTRIBUTES.IS_NOT_NULL,
				cmReadOnly: true
			}),
			new Ext.ux.CheckColumn( {
				header: translation.isactive,
				dataIndex: ATTRIBUTES.IS_ACTIVE,
				cmReadOnly: true
			}), {
				header: translation.field_visibility,
				dataIndex: ATTRIBUTES.FIELD_MODE,
				renderer: renderEditingMode 
			}];
		},

		buildStore: function() {
			this.store = _CMCache.getDomainAttributesStore();
		},

		buildTBar: function() {
			this.tbar = [this.addAttributeButton];
		},
		
		onDomainSelected: function(domain) {
			this.refreshStore(domain, indexAttributeToSelectAfter = null);
		},

		refreshStore: function(domain, indexAttributeToSelectAfter) {
			if (!domain) {
				return;
			}
//			var sm = this.getSelectionModel();
			this.store.loadForDomainId(domain.get("id"));

			if (this.rendered) {
				this.selectRecordAtIndexOrTheFirst(indexAttributeToSelectAfter);
			}
		},
		
		selectAttributeByName: function(name) {
			var sm = this.getSelectionModel();
			var r = this.store.findRecord("name", name);
			if (r) {
				sm.select(r);
			} else if (this.store.count() != 0) {
				sm.select(0);
			}
		}

	});
	
	function renderEditingMode(val) {
		return translation["field_" + val];
	}

})();