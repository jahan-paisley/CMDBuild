(function() {

Ext.define("CMDBuild.AddRelationMenuButton", {
	extend: "Ext.button.Split",
	iconCls: 'add',

	//custom fields
	baseText: CMDBuild.Translation.management.modcard.add_relations,
	textPrefix: CMDBuild.Translation.management.modcard.add_relations,
	
	//private
	initComponent: function() {
		Ext.apply(this, {
			text: this.baseText,
			menu : {items :[]},
			handler: onClick,
			scope: this
		});

		this.callParent(arguments);
	},

	setDomainsForEntryType: function(et, singleDomainId) {
		if (!et) {
			return;
		}

		this.menu.removeAll();

		var d,
			domains = _CMCache.getDirectedDomainsByEntryType(et),
			empty = true,
			addAll = (typeof singleDomainId == "undefined");

		for (var i=0, l=domains.length; i<l; ++i) {
			d = domains[i];

			if (d) {
				if (addAll || (d.dom_id == singleDomainId)) {
					var cachedDomain = _CMCache.getDomainById(d.dom_id);
					if (cachedDomain.hasCreatePrivileges()) {
						this.menu.add({
							text: d.description,
							domain: d,
							scope: this,
							handler: function(item, e){
								this.fireEvent("cmClick", item.domain);
							}
						});
						empty = false;
					}
				}
			}
		}

		this.setDisabled(empty);

		return domains.length > 0;
	},

	setTextSuffix: function(suffix) {
		this.setText(this.textPrefix +" "+suffix);
	},

	//private
	isEmpty: function(){
		return (this.menu.items.items.length == 0 );
	},

	//private
	resetText: function() {
		this.setText(this.baseText);
	}

});

	function onClick() {
		//Extjs calls the handler even when disabled
		if (!this.disabled) {
			this.showMenu();
		}
	}

})();