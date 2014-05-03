var expect = chai.expect;

describe("CMDBuildUtils", function() {
	describe("mergeCardsData", function() {
		it("should not replace existing properties in card1", function() {
			var card1 = {code: "c1", description: "card1"};
			var card2 = {code: "c2", description: "card2" };

			var merge = CMDBuild.Utils.mergeCardsData(card1, card2);
			expect(merge.code).to.be.equal("c1");
			expect(merge.description).to.be.equal("card1");
		});

		it("should add to card 1 the property of card2", function() {
			var card1 = {code: "c1", description: "card1"};
			var card2 = {surface: 3000, owner: "Foo" };

			var merge = CMDBuild.Utils.mergeCardsData(card1, card2);
			expect(merge).to.have.property("surface").and.be.equal(3000);
			expect(merge).to.have.property("owner").and.be.equal("Foo");
		});

		it("should merge recursively the properties", function() {
			var card1 = {code: "c1", store: {code: "s1"}};
			var card2 = {code: "c2", store: {code: "s2", description: "The store 1"}, status: "Up"}

			var merge = CMDBuild.Utils.mergeCardsData(card1, card2);
			expect(merge.store).to.have.property("code").and.be.equal("s1");
			expect(merge.store).to.have.property("description").and.be.equal("The store 1");
		});
	});
}); 