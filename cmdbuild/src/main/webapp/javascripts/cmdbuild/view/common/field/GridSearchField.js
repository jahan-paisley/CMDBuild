(function() {
	Ext.define("CMDBuild.field.GridSearchField", {

		extend: "Ext.form.field.Trigger",
		trigger1Cls: Ext.baseCSSPrefix + 'form-search-trigger',
		trigger2Cls: Ext.baseCSSPrefix + 'form-clear-trigger',
		validationEvent:false,
		validateOnBlur:false,
		hideTrigger1 :false,
		hideTrigger2 :false,

		initComponent : function(){
			this.callParent(arguments);
			
			this.on('specialkey', function(f, e){
				if(e.getKey() == e.ENTER){
					this.onTrigger1Click();
				}
			}, this);
		},

		onTrigger1Click : function() {
			var s = this.grid.getStore();
			setQuery(s, this.getRawValue());
		},

		onTrigger2Click: function(e){
			if (!this.disabled) {
				this.reset();
			}
		},

		onUnapplyFilter: function() {this.setValue("");},
		reset: function() {
			this.setValue("");
			var s = this.grid.getStore();
			setQuery(s, this.getRawValue());
		}
	});

	function setQuery(store, query) {
		var filter = null;
		try {
			filter = store.proxy.extraParams.filter;
		} catch (e) {
			// proxy could have no extra parameters
		}

		if (filter) {
			filter = Ext.decode(filter);
		} else {
			filter = {};
		}
		filter.query = query;

		store.proxy.extraParams.filter = Ext.encode(filter);
		store.loadPage(1);
	}

	Ext.define("CMDBuild.field.LocalGridSearchField", {
		extend: "CMDBuild.field.GridSearchField",
	
		// configuration
		grid: null,
		// configuration
	
		/**
		 * Filter the loaded record
		 * comparing every data element
		 * with the content of the field
		 * (case insensitive) 
		 */
		onTrigger1Click: function() {
			var query = this.getValue() || "";
			var s = this.grid.getStore();
			s.clearFilter();
	
			s.filter({
				filterFn: function(item) {
					var data = item.data;
	
					for (var key in data) {
						var value = data[key] || "";
						if ((""+value).toUpperCase()
								.indexOf(query.toUpperCase()) !== -1) {
	
							return true;
						}
					}
	
					return false;
				}
			});
		},
	
		onTrigger2Click: function() {
			var s = this.grid.getStore();
			s.clearFilter();
			this.setValue("");
		},
	
		reset: function() {
			this.onTrigger2Click();
		}
	});
})();