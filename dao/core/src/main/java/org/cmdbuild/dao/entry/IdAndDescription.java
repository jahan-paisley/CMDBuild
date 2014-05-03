package org.cmdbuild.dao.entry;

import java.util.HashMap;
import java.util.Map;

public class IdAndDescription {

	private final Long id;
	private final String description;

	public IdAndDescription(final Long referencedCardId, final String referencedCardDescription) {
		this.id = referencedCardId;
		this.description = referencedCardDescription;
	}

	/**
	 * 
	 * @return the id of the referenced card
	 */
	public Long getId() {
		return id;
	}

	/**
	 * 
	 * @return the description of the referenced card
	 */
	public String getDescription() {
		return description;
	}

	public Map<String, Object> asMap() {
		final Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", getId());
		map.put("description", getDescription());

		return map;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return id == null;
		}

		if (o instanceof IdAndDescription) {
			final Long otherId = ((IdAndDescription) o).getId();
			if (id == null) {
				return otherId == null;
			} else {
				return id.equals(otherId);
			}
		}

		return false;
	}
}
