package org.cmdbuild.dao.entry;

import java.util.Map;

public class CardValueSet implements CMValueSet {

	private final CMCard card;

	public CardValueSet(final CMCard card) {
		this.card = card;
	}

	@Override
	public Object get(final String key) {
		return card.get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		return card.get(key, requiredType);
	}

	@Override
	public Iterable<Map.Entry<String, Object>> getValues() {
		return card.getValues();
	}

}
