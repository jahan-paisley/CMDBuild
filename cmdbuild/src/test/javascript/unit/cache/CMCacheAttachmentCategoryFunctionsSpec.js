(function() {

	describe("CMCacheAttachmentCategoryFunction", function() {

		afterEach(function() {
			_CMCache.resetAttachmentCategory();
		});

		it("Starts with no category", function() {
			var categories = _CMCache.getAttachmentCategories();
			expect(Ext.Object.getSize(categories)).toBe(0);
		});

		it("Does not add a category if is not of the right class", function() {
			expect(function() {
				_CMCache.addAttachmentCategory({});
			}).toThrow(CMDBUild.cache.CMCacheAttachmentCategoryFunctions.errors.wrongTypeOnAdd);
		});

		it("Retrieves added category", function() {
			var c = new CMDBuild.model.CMAttachmentCategoryModel({
				name: "C1"
			});

			_CMCache.addAttachmentCategory(c);

			expect(Ext.Object.getSize(_CMCache.getAttachmentCategories())).toBe(1);
			expect(_CMCache.getAttachmentCategoryWithName(c.getName())).toBe(c);
		});

		it("Return null if has no category for a given name", function() {
			expect(_CMCache.getAttachmentCategoryWithName("Janky")).toBeNull();
		});

		it("Add attachment category from json", function() {
			var j = {
				name: "From Json",
				description: "",
				metadataGroups: []
			};

			_CMCache.addAttachmentCategoryFromJson(j);
			var c = _CMCache.getAttachmentCategoryWithName(j.name);
			expect(Ext.getClassName(c)).toBe("CMDBuild.model.CMAttachmentCategoryModel");
		});

		it("Add more category from json", function() {
			var j1 = {
				name: "From Json 1",
				description: "",
				metadataGroups: []
			}, j2 = {
				name: "From Json 2",
				description: "",
				metadataGroups: []
			};

			_CMCache.addAttachmentCategoryFromJson([j1, j2]);
			expect(Ext.Object.getSize(_CMCache.getAttachmentCategories())).toBe(2);
			var c = _CMCache.getAttachmentCategoryWithName(j1.name);
			expect(Ext.getClassName(c)).toBe("CMDBuild.model.CMAttachmentCategoryModel");
			c = _CMCache.getAttachmentCategoryWithName(j2.name);
			expect(Ext.getClassName(c)).toBe("CMDBuild.model.CMAttachmentCategoryModel");
		});

		it("Returns the categories base info", function() {
			var j1 = {
				name: "From Json 1",
				description: "d1",
				metadataGroups: []
			}, j2 = {
				name: "From Json 2",
				description: "d2",
				metadataGroups: []
			};

			_CMCache.addAttachmentCategoryFromJson([j1, j2]);
			var baseInfo = _CMCache.getAttachmentCategoryBaseInfo();
			expect(Ext.isArray(baseInfo)).toBeTruthy();
			expect(baseInfo.length).toBe(2);
		});

		it("Returns a syncronized json store with base info", function() {
			var s = _CMCache.getAttechmentCategoryStore();
			expect(Ext.getClassName(s)).toBe("Ext.data.JsonStore");
			expect(s.count()).toBe(0);

			_CMCache.addAttachmentCategoryFromJson([{
				name: "From Json 1",
				description: "d1",
				metadataGroups: []
			}]);

			expect(s.count()).toBe(1);
		});
	});

})();