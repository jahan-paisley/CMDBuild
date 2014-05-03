(function() {

	describe('CMEntryTypeModel', function() {

		beforeEach(function() {});

		afterEach(function() {});

		it('manage no attachment completion rules', function() {
			var et = new CMDBuild.cache.CMEntryTypeModel({
				id: 1,
				meta: {}
			});
			var attachmentAutocompletion = et.getAttachmentAutocompletion();

			expect(attachmentAutocompletion).toBeDefined();
			expect(et.getAttachmentCopletionRuleByGropAndMetadataName("Foo", "Bar")).toBe(null);
			expect(Ext.Object.getSize(attachmentAutocompletion)).toBe(0);
		});

		it('store autocompletion metadata', function() {
			var et = new CMDBuild.cache.CMEntryTypeModel(getEntryTypeConfiguration());
			var attachmentAutocompletion = et.getAttachmentAutocompletion();

			expect(attachmentAutocompletion).toBeDefined();
			expect(et.getAttachmentCopletionRuleByGropAndMetadataName("Foo", "Bar")).toBe(null);
			expect(et.getAttachmentCopletionRuleByGropAndMetadataName("summary", "summary")).toBe("Pipparuolo");
			expect(et.getAttachmentCopletionRuleByGropAndMetadataName("documentStatistics", "characters")).toBe(5000);
		});
	});

	function getEntryTypeConfiguration() {
		return {
			id: 1,
			text: "Foo",
			meta: {
				attachments: {
					autocompletion: {
						summary: {
							summary: "Pipparuolo"
						},
						documentStatistics: {
							characters: 5000
						}
					}
				}
			}
		};
	}

})();