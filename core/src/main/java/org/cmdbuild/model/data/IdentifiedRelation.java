package org.cmdbuild.model.data;

import java.util.Map.Entry;

import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.joda.time.DateTime;

/**
 * 
 * This class holds only the information to identify the relation: its Id and
 * the CMDomain that own it
 * 
 * @author tecnoteca
 * 
 */
public class IdentifiedRelation implements CMRelation {

	final CMDomain domain;
	final Long id;
	final String UNSUPPORTED_OPERATION_MESSAGE = "You are tring to use an unsupported operation for class  org.cmdbuild.model.data.IdentifiedRelation";

	public IdentifiedRelation(final CMDomain domain, final Long id) {
		this.id = id;
		this.domain = domain;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public CMDomain getType() {
		return domain;
	}

	@Override
	public String getUser() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public DateTime getBeginDate() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public DateTime getEndDate() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public Iterable<Entry<String, Object>> getAllValues() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public Object get(final String key) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public Long getCard1Id() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public Long getCard2Id() {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}
}
