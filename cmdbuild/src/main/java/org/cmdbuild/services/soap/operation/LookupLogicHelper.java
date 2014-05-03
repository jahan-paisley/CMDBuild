package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class LookupLogicHelper implements SoapLogicHelper {

	private static final Marker marker = MarkerFactory.getMarker(LookupLogicHelper.class.getName());

	private static interface AttributeChecker {

		boolean check(Lookup input);

	}

	private final LookupLogic logic;

	public LookupLogicHelper(final LookupLogic lookupLogic) {
		this.logic = lookupLogic;
	}

	public int createLookup(final org.cmdbuild.services.soap.types.Lookup lookup) {
		final Lookup lookupDto = transform(lookup);
		return logic.createOrUpdateLookup(lookupDto).intValue();
	}

	public boolean updateLookup(final org.cmdbuild.services.soap.types.Lookup lookup) {
		final Lookup lookupDto = transform(lookup);
		logic.createOrUpdateLookup(lookupDto).intValue();
		return true;
	}

	public boolean disableLookup(final int id) {
		logic.disableLookup(Long.valueOf(id));
		return true;
	}

	public org.cmdbuild.services.soap.types.Lookup getLookupById(final int id) {
		final Lookup lookup = logic.getLookup(Long.valueOf(id));
		return transform(lookup, true);
	}

	public org.cmdbuild.services.soap.types.Lookup[] getLookupListByCode(final String type, final String code,
			final boolean parentList) {
		return getLookupListByAttribute(type, new AttributeChecker() {
			@Override
			public boolean check(final Lookup input) {
				return code.equals(input.code);
			}
		}, parentList);
	}

	public org.cmdbuild.services.soap.types.Lookup[] getLookupListByDescription(final String type,
			final String description, final boolean parentList) {
		return getLookupListByAttribute(type, new AttributeChecker() {
			@Override
			public boolean check(final Lookup input) {
				return (description == null) || description.equals(input.description);
			}
		}, parentList);
	}

	private org.cmdbuild.services.soap.types.Lookup[] getLookupListByAttribute(final String type,
			final AttributeChecker attributeChecker, final boolean parentList) {
		final LookupType lookupType = LookupType.newInstance() //
				.withName(type) //
				.build();
		final Iterable<Lookup> lookupList = logic.getAllLookup(lookupType, true);
		return from(lookupList) //
				.filter(new Predicate<Lookup>() {
					@Override
					public boolean apply(final Lookup input) {
						return attributeChecker.check(input);
					}
				}) //
				.transform(new Function<Lookup, org.cmdbuild.services.soap.types.Lookup>() {
					@Override
					public org.cmdbuild.services.soap.types.Lookup apply(final Lookup input) {
						return transform(input, parentList);
					}
				}) //
				.toArray(org.cmdbuild.services.soap.types.Lookup.class);
	}

	private Lookup transform(final org.cmdbuild.services.soap.types.Lookup from) {
		return Lookup.newInstance() //
				.withType(LookupType.newInstance()//
						.withName(from.getType())) //
				.withCode(defaultIfEmpty(from.getCode(), EMPTY)) //
				.withId(Long.valueOf(from.getId())) //
				.withDescription(from.getDescription()) //
				.withNotes(from.getNotes()) //
				.withParentId(Long.valueOf(from.getParentId())) //
				.withNumber(from.getPosition()) //
				.withActiveStatus(true) //
				.build();
	}

	private org.cmdbuild.services.soap.types.Lookup transform(final Lookup from, final boolean parentList) {
		logger.debug(marker, "serializing lookup '{}'", from);
		final org.cmdbuild.services.soap.types.Lookup to = new org.cmdbuild.services.soap.types.Lookup();
		to.setId(from.getId().intValue());
		to.setCode(from.code);
		to.setDescription(from.description);
		to.setNotes(from.notes);
		to.setType(from.type.name);
		to.setPosition(from.number);
		if (from.parent != null) {
			to.setParentId(from.parentId.intValue());
		}
		if (parentList && from.parent != null) {
			to.setParent(transform(from.parent, true));
		}
		return to;
	}

}
