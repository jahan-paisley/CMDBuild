package org.cmdbuild.logic.dms;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collection;
import java.util.Collections;

import javax.activation.DataHandler;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.logic.Action;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class StoreDocument implements Action {

	private static final Marker marker = MarkerFactory.getMarker(StoreDocument.class.getName());

	public static interface Document {

		String getName();

		DataHandler getDataHandler();

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<StoreDocument> {

		private static final Collection<Document> NO_DOCUMENTS = Collections.emptyList();

		private DmsLogic dmsLogic;
		private String classname;
		private Long id;
		private String category;
		private final Collection<Document> documents = newArrayList();

		private Builder() {
			// use factory method
		}

		@Override
		public StoreDocument build() {
			validate();
			return new StoreDocument(this);
		}

		private void validate() {
			Validate.notNull(dmsLogic, "missing workflow logic");
			Validate.notBlank(classname, "missing classname");
			Validate.notNull(id, "missing id");
			Validate.notBlank(category, "missing category");
		}

		public Builder withDmsLogic(final DmsLogic dmsLogic) {
			this.dmsLogic = dmsLogic;
			return this;
		}

		public Builder withClassName(final String classname) {
			this.classname = classname;
			return this;
		}

		public Builder withCardId(final long id) {
			this.id = id;
			return this;
		}

		public Builder withCategory(final String category) {
			this.category = category;
			return this;
		}

		public Builder withDocument(final Document document) {
			if (document != null) {
				this.documents.add(document);
			}
			return this;
		}

		public Builder withDocuments(final Iterable<? extends Document> documents) {
			addAll(this.documents, defaultIfNull(documents, NO_DOCUMENTS));
			return this;
		}

	}

	private static final String AUTHOR = "system";
	private static final String NO_DESCRIPTION = EMPTY;
	private static final Iterable<MetadataGroup> metadataGroups = Collections.emptyList();

	public static Builder newInstance() {
		return new Builder();
	}

	private final DmsLogic dmsLogic;
	private final String classname;
	private final long id;
	private final String category;
	private final Collection<Document> documents;

	private StoreDocument(final Builder builder) {
		this.dmsLogic = builder.dmsLogic;
		this.classname = builder.classname;
		this.id = builder.id;
		this.category = builder.category;
		this.documents = builder.documents;
	}

	@Override
	public void execute() {
		for (final Document document : documents) {
			try {
				dmsLogic.upload( //
						AUTHOR, //
						classname, //
						id, //
						document.getDataHandler().getInputStream(), //
						document.getName(), //
						category, //
						NO_DESCRIPTION, //
						metadataGroups);
			} catch (final Exception e) {
				logger.error(marker, "error storing document", e);
			}
		}
	}

}
