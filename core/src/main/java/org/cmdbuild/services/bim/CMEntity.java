package org.cmdbuild.services.bim;

import java.util.Map;

import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.dao.entry.CMCard;

public class CMEntity implements Entity {

	private final CMCard card;

	public CMEntity(final CMCard card) {
		this.card = card;
	}

	public CMCard getCard() {
		return card;
	}

	public Long getId() {
		return card.getId();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public Map<String, Attribute> getAttributes() throws BimError {
		throw new BimError("Not implemented");
	}

	@Override
	public Attribute getAttributeByName(String attributeName) {
		throw new BimError("Not implemented");
	}

	@Override
	public String getKey() throws BimError {
		throw new BimError("Not implemented");
	}

	@Override
	public String getTypeName() {
		throw new BimError("Not implemented");
	}
	
	@Override
	public String toString() {
		return card.getClass().getName() + " " + card.getId();
	}

}
