package org.cmdbuild.logic.data.access;

import org.cmdbuild.dao.entry.CMCard;

public class CMCardWithPosition {
	final public Long position;
	final public CMCard card;

	public CMCardWithPosition( //
			final Long position, //
			final CMCard card //
	) {
		this.position = position;
		this.card = card;
	}
}