package org.cmdbuild.workflow.api;

import static java.lang.String.format;

import org.cmdbuild.api.fluent.Card;
import org.cmdbuild.api.fluent.CardDescriptor;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.api.fluent.FluentApiExecutor;
import org.cmdbuild.api.fluent.ws.EntryTypeAttribute;
import org.cmdbuild.common.Constants;
import org.cmdbuild.common.mail.FetchedMail;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.NewMail;
import org.cmdbuild.common.mail.SelectFolder;
import org.cmdbuild.common.mail.SelectMail;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;

public class WorkflowApi extends FluentApi implements SchemaApi, MailApi {

	private final SchemaApi schemaApi;
	private final MailApi mailApi;

	/**
	 * It's really ugly but fortunately all is hidden behind the
	 * {@link SharkWorkflowApiFactory}.
	 */
	public WorkflowApi(final FluentApiExecutor executor, final SchemaApi schemaApi, final MailApi mailApi) {
		super(executor);
		this.schemaApi = schemaApi;
		this.mailApi = mailApi;
	}

	/*
	 * Schema
	 */

	@Override
	public ClassInfo findClass(final String className) {
		return schemaApi.findClass(className);
	}

	@Override
	public ClassInfo findClass(final int classId) {
		return schemaApi.findClass(classId);
	}

	@Override
	public AttributeInfo findAttributeFor(final EntryTypeAttribute entryTypeAttribute) {
		return schemaApi.findAttributeFor(entryTypeAttribute);
	}

	@Override
	public LookupType selectLookupById(final int id) {
		if (id <= 0) {
			return new LookupType();
		} else {
			return schemaApi.selectLookupById(id);
		}
	}

	@Override
	public LookupType selectLookupByCode(final String type, final String code) {
		return schemaApi.selectLookupByCode(type, code);
	}

	@Override
	public LookupType selectLookupByDescription(final String type, final String description) {
		return schemaApi.selectLookupByDescription(type, description);
	}

	/*
	 * Mail
	 */

	@Override
	public NewMail newMail() {
		return mailApi.newMail();
	}

	@Override
	public SelectFolder selectFolder(final String folder) {
		return mailApi.selectFolder(folder);
	}

	@Override
	public SelectMail selectMail(final FetchedMail mail) {
		return mailApi.selectMail(mail);
	}

	/*
	 * Data type conversion
	 */

	public ReferenceType referenceTypeFrom(final Card card) {
		return referenceTypeFrom(card, card.getDescription());
	}

	public ReferenceType referenceTypeFrom(final CardDescriptor cardDescriptor) {
		return referenceTypeFrom(cardDescriptor, null);
	}

	public ReferenceType referenceTypeFrom(final Object idAsObject) {
		final int id = objectToInt(idAsObject);
		if (id <= 0) {
			return new ReferenceType();
		} else {
			final Card referencedCard = existingCard(Constants.BASE_CLASS_NAME, id) //
					.limitAttributes(Constants.DESCRIPTION_ATTRIBUTE) //
					.fetch();
			return referenceTypeFrom(referencedCard);
		}
	}

	private int objectToInt(final Object id) {
		final int idAsInt;
		if (id instanceof String) {
			idAsInt = Integer.parseInt(String.class.cast(id));
		} else if (id instanceof Number) {
			idAsInt = Number.class.cast(id).intValue();
		} else {
			throw new IllegalArgumentException(format("invalid class '%s' for id", id.getClass()));
		}
		return idAsInt;
	}

	private ReferenceType referenceTypeFrom(final CardDescriptor cardDescriptor, final String description) {
		return new ReferenceType( //
				cardDescriptor.getId(), //
				findClass(cardDescriptor.getClassName()).getId(), //
				(description == null) ? descriptionFor(cardDescriptor) : description);
	}

	private String descriptionFor(final CardDescriptor cardDescriptor) {
		return existingCard(cardDescriptor) //
				.limitAttributes(Constants.DESCRIPTION_ATTRIBUTE) //
				.fetch() //
				.getDescription();
	}

	public CardDescriptor cardDescriptorFrom(final ReferenceType referenceType) {
		return new CardDescriptor( //
				findClass(referenceType.getIdClass()).getName(), //
				referenceType.getId());
	}

	public Card cardFrom(final ReferenceType referenceType) {
		return existingCard(cardDescriptorFrom(referenceType)).fetch();
	}

}
