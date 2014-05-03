(function() {

	describe('CMAttachmentCategoryModel', function() {

		var category;

		beforeEach(function() {
			category = new CMDBuild.model.CMAttachmentCategoryModel();
		});

		afterEach(function() {
			delete category;
		});

		it('is able to manage the name', function() {
			var name = "C1";

			expect(category.getName()).toBe("");
			category.setName(name);
			expect(category.getName()).toBe(name);
		});

		it('is able to manage the description', function() {
			var description = "D1";

			expect(category.getDescription()).toBe("");
			category.setDescription(description);
			expect(category.getDescription()).toBe(description);
		});

		it('is able to manage the metadataGroups', function() {
			var groups = category.getMetadataGroups();
			expect(Ext.isArray(groups)).toBeTruthy();
			expect(groups.length).toBe(0);

			var g = new CMDBuild.model.CMMetadataGroup();
			category.addMetadataGroup(g);
			groups = category.getMetadataGroups();
			expect(groups.length).toBe(1);
			expect(groups[0]).toBe(g);
		});

		it('is able to build an instance from json', function() {
			var j = {
				name: "the name",
				description: "the description",
				metadataGroups: [{},{}]
			};

			var c = CMDBuild.model.CMAttachmentCategoryModel.buildFromJson(j);
			expect(c.getName()).toBe(j.name);
			expect(c.getDescription()).toBe(j.description);
			var groups = c.getMetadataGroups();
			expect(Ext.isArray(groups)).toBeTruthy();
			expect(groups.length).toBe(2);
			var aGroup = groups[0];
			expect(Ext.getClassName(aGroup)).toBe("CMDBuild.model.CMMetadataGroup");
			aGroup = groups[1];
			expect(Ext.getClassName(aGroup)).toBe("CMDBuild.model.CMMetadataGroup");
		});
	});

	describe('CMMetadataGroup', function() {

		var group;

		beforeEach(function() {
			group = new CMDBuild.model.CMMetadataGroup({
				name: "G1",
				metadata: [{},{}]
			});
		});

		afterEach(function() {
			delete group;
		});

		it('is able to manage no data', function() {
			var g = new CMDBuild.model.CMMetadataGroup();
			expect(g.getName()).toBe("");
			var metadata = g.getMetadataDefinitions();
			expect(Ext.isArray(metadata)).toBeTruthy();
			expect(metadata.length).toBe(0);
		});

		it('is able to manage the name', function() {
			var name = "G2";
			expect(group.getName()).toBe("G1");
			group.setName(name);
			expect(group.getName()).toBe(name);
		});

		it('is able to manage the metadataDefinitions', function() {
			var definitions = group.getMetadataDefinitions();
			expect(Ext.isArray(definitions)).toBeTruthy();
			expect(definitions.length).toBe(2);
		});
	});
})();