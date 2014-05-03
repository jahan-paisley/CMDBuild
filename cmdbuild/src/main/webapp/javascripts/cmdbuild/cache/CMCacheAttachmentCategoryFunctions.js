(function() {
	var categories = {};
	var store = new Ext.data.JsonStore({
		fields: ["name", "description"],
		data: [],
		addCategory: function(c) {
			this.add({
				name: c.getName(),
				description: c.getDescription()
			});
		}
	});

	Ext.define("CMDBUild.cache.CMCacheAttachmentCategoryFunctions", {
		statics: {
			errors: {
				wrongTypeOnAdd: "You are tring to add a wrong object to the attachemnt categories"
			}
		},

		syncAttachmentCategories: function() {
			this.resetAttachmentCategory();
			CMDBuild.ServiceProxy.attachment.getattachmentdefinition({
				scope: this,
				success: function(response, options, decoded) {
					if (decoded 
							&& decoded.response
							&& decoded.response.categories) {
						this.addAttachmentCategoryFromJson(decoded.response.categories);
					}
				}
			});
		},

		getAttachmentCategories: function() {
			return categories;
		},

		addAttachmentCategory: function(c) {
			if (Ext.getClassName(c) != "CMDBuild.model.CMAttachmentCategoryModel") {
				throw CMDBUild.cache.CMCacheAttachmentCategoryFunctions.errors.wrongTypeOnAdd;
			}

			categories[c.getName()] = c;
			store.addCategory(c);
		},

		getAttachmentCategoryWithName: function(name) {
			return categories[name] || null;
		},

		// returns an array of object with name
		// and description of the stored categories
		getAttachmentCategoryBaseInfo: function() {
			var out = [];

			for (var key in categories) {
				var c = categories[key];
				out.push({
					name: c.getName(),
					description: c.getDescription()
				});
			}

			return out;
		},

		getAttechmentCategoryStore: function() {
			return store;
		},

		addAttachmentCategoryFromJson: function(j) {
			if (Ext.isArray(j)) {
				for (var i=0; i<j.length; ++i) {
					addAttachmentCategoryFromJson(this, j[i]);
				}
			} else {
				addAttachmentCategoryFromJson(this, j);
			}
		},

		resetAttachmentCategory: function() {
			for (var key in categories) {
				delete categories[key];
			}

			categories = {};
			store.removeAll();
		}
	});

	function addAttachmentCategoryFromJson(me, j) {
		me.addAttachmentCategory(CMDBuild.model.CMAttachmentCategoryModel.buildFromJson(j));
	}
})();