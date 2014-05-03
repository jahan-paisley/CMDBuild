(function() {
	describe('CMCardModuleState', function() {
		var state = null;
		var et = null;
		var delegate = null;
		var card = null;

		beforeEach(function() {
			state = new CMDBuild.state.CMCardModuleState();
			delegate = new CMDBuild.state.CMCardModuleStateDelegate();
			card = new CMDBuild.DummyModel({id: "Foo"});
			et = new CMDBuild.cache.CMEntryTypeModel({
				id: "FOO"
			});

			state.addDelegate(delegate);
		});

		afterEach(function() {
			delete state;
			delete et;
			delete delegate;
			delete card;
		});

		it ('start with no data', function() {
			expect(state.entryType).toBeNull();
			expect(state.card).toBeNull();
		});

		it ('set a entry type', function() {
			state.setEntryType(et);

			expect(state.entryType.getId()).toBe(et.getId());
		});

		it ('set a card', function(card) {
			state.setCard(card);

			expect(state.card).toBe(card);
		});

		it ('notify delegates when the entry type change', function() {
			var spy = spyOn(delegate, "onEntryTypeDidChange");
			var danglingCard = null;

			state.setEntryType(et, danglingCard);

			expect(spy).toHaveBeenCalledWith(state, et, danglingCard);

			spy.reset();
			state.setEntryType(et);
			expect(spy).wasNotCalled();

			spy.reset();
			state.setEntryType(null, danglingCard);
			expect(spy).toHaveBeenCalledWith(state, null, danglingCard);

			danglingCard = {Id: "Foo", IdClass: "Bar"};
			state.setEntryType(et, danglingCard);
			expect(spy).toHaveBeenCalledWith(state, et, danglingCard);
		});

		it ('notify delegates when the card change', function() {
			var spy = spyOn(delegate, "onCardDidChange");
			state.setCard(card);

			expect(spy).toHaveBeenCalledWith(state, card);

			spy.reset();
			state.setCard(card);
			expect(spy).wasNotCalled();

			spy.reset();
			state.setCard(null);
			expect(spy).toHaveBeenCalledWith(state, null);
		});

		it ('reset the card when a entryType is setted', function() {
			state.setEntryType(et);
			state.setCard(card);
			var et2 = new CMDBuild.cache.CMEntryTypeModel({
				id: "Bar"
			});

			expect(state.card).toBe(card);
			state.setEntryType(et2);
			expect(state.card).toBe(null);
		});
	});
})();