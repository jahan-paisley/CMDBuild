package org.cmdbuild.servlets.json.management.dataimport;

import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_1N;
import static org.cmdbuild.dao.constants.Cardinality.CARDINALITY_N1;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.json.JSONException;
import org.json.JSONObject;

public class CardFiller {

	final private CMClass destinationClass;
	final private CMDataView view;
	final private LookupStore lookupStore;

	public CardFiller( //
			final CMClass destinationClass, //
			final CMDataView view, //
			final LookupStore lookupStore //
	) {
		this.destinationClass = destinationClass;
		this.view = view;
		this.lookupStore = lookupStore;
	}

	@SuppressWarnings("serial")
	public static class CardFillerException extends Exception {
		public final String attributeName;
		public final Object attributeValue;

		public CardFillerException(final String attributeName, final Object value) {
			this.attributeName = attributeName;
			this.attributeValue = value;
		}
	}

	/**
	 * Try to set the given value to the attribute with the given name for the
	 * diven card
	 * 
	 * If the value is a JSONObject try to build a CardReference and set it to
	 * the attribute: so the value must have "id" and "description" attributes
	 * 
	 * Otherwise, check the type of the attribute, - if it is a
	 * ReferenceAttributeType try to retrieve the referenced card comparing the
	 * given value with the "code" attribute and build a CardReference. - if it
	 * is a LookupAttributeType try to retrieve the lookup with the given
	 * description and build a CardReference
	 */
	public void fillCardAttributeWithValue( //
			final DBCard card, //
			final String attributeName, //
			final Object value //
	) throws CardFillerException, JSONException {

		// if the attribute has no value do nothing
		if (value == null || "".equals(value)) {

			return;
		}

		final CMAttribute attribute = destinationClass.getAttribute(attributeName);

		if (attribute == null) {
			throw NotFoundExceptionType.ATTRIBUTE_NOTFOUND.createException(destinationClass.getDescription(),
					attributeName);
		}

		if (value instanceof JSONObject) {
			final JSONObject jsonValue = (JSONObject) value;
			final IdAndDescription cardReference = new IdAndDescription( //
					(Long) jsonValue.getLong("id"), //
					(String) jsonValue.get("description") //
			);

			card.set(attributeName, cardReference);

		} else {
			if (attribute.getType() instanceof ReferenceAttributeType) {

				// Use the Code attribute of the referenced card
				manageReferenceAttribute(card, attributeName, value, attribute);

			} else if (attribute.getType() instanceof LookupAttributeType) {

				// For the lookup use the Description
				manageLookupAttribute(card, attributeName, value, attribute);

			} else {
				/*
				 * Business rule: 16 July 2013 Do not manage the ForeignKey no
				 * one has asked to do that
				 */
				try {
					card.set(attributeName, value);
				} catch (final Exception ex) {
					throw new CardFillerException(attributeName, value);
				}
			}
		}
	}

	private void manageLookupAttribute( //
			final DBCard mutableCard, //
			final String attributeName, //
			final Object value, //
			final CMAttribute attribute //
	) throws CardFillerException {

		final LookupAttributeType type = (LookupAttributeType) attribute.getType();
		final String lookupTypeName = type.getLookupTypeName();
		final LookupType lookupType = LookupType.newInstance().withName(lookupTypeName).build();

		boolean set = false;
		for (final Lookup lookup : lookupStore.listForType(lookupType)) {
			if (value.equals(lookup.description)) {
				mutableCard.set( //
						attributeName, //
						new LookupValue( //
								lookup.getId(), //
								lookup.description, lookupTypeName //
						) //
						);

				set = true;
				break;
			}
		}

		if (!set) {
			throw new CardFillerException(attributeName, value);
		}
	}

	private void manageReferenceAttribute( //
			final DBCard mutableCard, //
			final String attributeName, //
			final Object value, //
			final CMAttribute attribute //
	) throws CardFillerException {

		final ReferenceAttributeType type = (ReferenceAttributeType) attribute.getType();
		final String domainName = type.getDomainName();
		final CMDomain domain = view.findDomain(domainName);
		if (domain != null) {

			// retrieve the destination
			final String cardinality = domain.getCardinality();
			CMClass destination = null;
			if (CARDINALITY_1N.value().equals(cardinality)) {
				destination = domain.getClass1();
			} else if (CARDINALITY_N1.value().equals(cardinality)) {
				destination = domain.getClass2();
			}

			if (destination != null) {
				final CMQueryResult queryResult = view.select(anyAttribute(destination)) //
						.from(destination) //
						.where(condition(attribute(destination, CODE_ATTRIBUTE), eq(value))) //
						.run();

				if (!queryResult.isEmpty()) {
					final CMQueryRow row = queryResult.iterator().next();
					final CMCard referredCard = row.getCard(destination);
					mutableCard.set(attributeName, buildCardReference(referredCard));
				} else {
					throw new CardFillerException(attributeName, value);
				}
			} else {
				throw new CardFillerException(attributeName, value);
			}
		}
	}

	private IdAndDescription buildCardReference(final CMCard referredCard) {
		IdAndDescription cardReference;
		final Object description = referredCard.getDescription();
		if (description == null) {
			cardReference = new IdAndDescription(referredCard.getId(), "");
		} else {
			cardReference = new IdAndDescription(referredCard.getId(), (String) referredCard.getDescription());
		}
		return cardReference;
	}
}