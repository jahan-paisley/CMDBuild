package utils;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import org.alfresco.webservice.test.BaseWebServiceSystemTest;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.dms.DefaultDocumentCreator;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreator;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.AlfrescoDmsService;
import org.cmdbuild.dms.exception.DmsError;
import org.junit.rules.ExternalResource;

/**
 * Component for all tests which needs a running instance of Alfresco.
 * 
 * The file {@literal cmdbuildCustomModel.xml} (within resources) must be copied
 * in the extensions directory of Alfresco before starting it.
 * 
 * @see {@link TestConfiguration} for an overview of the settings of the
 *      Alfresco instance.
 * @see {@link BaseWebServiceSystemTest} for tests that use an embedded instance
 *      of Alfresco (we cannot use it because we need to set a custom model).
 */
public class DmsServiceResource extends ExternalResource {

	public static class Builder implements org.cmdbuild.common.Builder<DmsServiceResource> {

		private static final List<String> DEFAULT_PATH = asList("path", "of", "test", "documents");
		private static final String DEFAULT_TARGET_CLASS = "class";

		private DmsConfiguration configuration;
		private List<String> path;
		private String targetClass;

		private Builder() {
			// prevents instantiation
		}

		@Override
		public DmsServiceResource build() {
			validate();
			return new DmsServiceResource(this);
		}

		private void validate() {
			Validate.notNull(configuration, "invalid configuratin");

			path = (path == null) ? DEFAULT_PATH : path;
			targetClass = (targetClass == null) ? DEFAULT_TARGET_CLASS : targetClass;
		}

		public Builder withConfiguration(final DmsConfiguration configuration) {
			this.configuration = configuration;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static class StorableDocumentBuilder implements org.cmdbuild.common.Builder<StorableDocument> {

		private final DocumentCreator documentFactory;

		private String targetClass;
		private String id;
		private File file;
		private String category;
		private Iterable<MetadataGroup> metadataGroups;

		private StorableDocumentBuilder(final DocumentCreator documentFactory) {
			this.documentFactory = documentFactory;
		}

		@Override
		public StorableDocument build() {
			try {
				return documentFactory.createStorableDocument( //
						AUTHOR, //
						targetClass, //
						id, //
						(file == null) ? null : new FileInputStream(file), //
						(file == null) ? null : file.getName(), //
						category, //
						DESCRIPTION, //
						metadataGroups);
			} catch (final FileNotFoundException e) {
				throw new RuntimeException("should never come here", e);
			}
		}

		public StorableDocumentBuilder withTargetClass(final String targetClass) {
			this.targetClass = targetClass;
			return this;
		}

		public StorableDocumentBuilder withId(final Long id) {
			return withId(id.toString());
		}

		public StorableDocumentBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public StorableDocumentBuilder withFile(final File file) {
			this.file = file;
			return this;
		}

		public StorableDocumentBuilder withCategory(final String category) {
			this.category = category;
			return this;
		}

		public StorableDocumentBuilder withMetadataGroups(final Iterable<MetadataGroup> metadataGroups) {
			this.metadataGroups = metadataGroups;
			return this;
		}

	}

	public static class DocumentRepository {

		private final DmsService dmsService;
		private final DocumentCreator documentFactory;
		private final String targetClass;
		private final Long id;

		private DocumentRepository(final DmsServiceResource dmsServiceResource, final Long id) {
			this.dmsService = dmsServiceResource.dmsService;
			this.documentFactory = dmsServiceResource.documentFactory;
			this.targetClass = dmsServiceResource.targetClass;
			this.id = id;
		}

		public void create() throws DmsError {
			dmsService.create(currentPosition());
		}

		public List<StoredDocument> listDocuments() throws DmsError {
			return dmsService.search(currentPosition());
		}

		public void upload(final File file) throws DmsError, FileNotFoundException {
			dmsService.upload(storableDocumentFrom(file));
		}

		public void upload(final File file, final String category) throws DmsError, FileNotFoundException {
			dmsService.upload(storableDocumentFrom(file, category));
		}

		public void upload(final File file, final List<MetadataGroup> metadataGroups) throws DmsError,
				FileNotFoundException {
			dmsService.upload(storableDocumentFrom(file, metadataGroups));
		}

		public void delete(final String name) throws DmsError {
			dmsService.delete(documentDeleteFrom(name));
		}

		public void clear() throws DmsError {
			for (final StoredDocument document : listDocuments()) {
				delete(document.getName());
			}
		}

		public void copy(final String name, final Long target) throws DmsError {
			for (final StoredDocument storedDocument : dmsService.search(currentPosition())) {
				if (storedDocument.getName().equals(name)) {
					dmsService.copy(storedDocument, from(currentPosition()), to(positionAt(target)));
				}
			}
		}

		public void move(final String name, final Long target) throws DmsError {
			for (final StoredDocument storedDocument : dmsService.search(currentPosition())) {
				if (storedDocument.getName().equals(name)) {
					dmsService.move(storedDocument, from(currentPosition()), to(positionAt(target)));
				}
			}
		}

		public void delete() throws DmsError {
			dmsService.delete(from(currentPosition()));
		}

		/*
		 * Utilities
		 */

		private DocumentSearch currentPosition() {
			return positionAt(id);
		}

		private DocumentSearch positionAt(final Long id) {
			return documentFactory.createDocumentSearch(targetClass, id.toString());
		}

		private StorableDocument storableDocumentFrom(final File file) throws FileNotFoundException {
			return storableDocumentFrom(file, EMPTY_METADATA_GROUP);
		}

		private StorableDocument storableDocumentFrom(final File file, final String category)
				throws FileNotFoundException {
			return storableDocumentFrom(file, category, EMPTY_METADATA_GROUP);
		}

		private StorableDocument storableDocumentFrom(final File file, final Iterable<MetadataGroup> metadataGroups)
				throws FileNotFoundException {
			return storableDocumentFrom(file, CATEGORY, metadataGroups);
		}

		private StorableDocument storableDocumentFrom(final File file, final String category,
				final Iterable<MetadataGroup> metadataGroups) throws FileNotFoundException {
			return storableDocumentFrom(id, file, category, metadataGroups);
		}

		private StorableDocument storableDocumentFrom(final Long id, final File file, final String category,
				final Iterable<MetadataGroup> metadataGroups) throws FileNotFoundException {
			return a(storableDocument() //
					.withTargetClass(targetClass) //
					.withId(id) //
					.withFile(file) //
					.withCategory(category) //
					.withMetadataGroups(metadataGroups));
		}

		private StorableDocumentBuilder storableDocument() {
			return new StorableDocumentBuilder(documentFactory);
		}

		private DocumentDelete documentDeleteFrom(final String name) {
			return documentFactory.createDocumentDelete(targetClass, id.toString(), name);
		}

		private static <T> T from(final T t) {
			return t;
		}

		private static <T> T to(final T t) {
			return t;
		}

		private static <T> T a(final org.cmdbuild.common.Builder<T> builder) {
			return builder.build();
		}

	}

	public static final String DESCRIPTION = "a brief description for this document";
	public static final String CATEGORY = "Document";
	public static final String AUTHOR = "The Author";

	private static final List<MetadataGroup> EMPTY_METADATA_GROUP = Collections.emptyList();

	private final DmsService dmsService;
	private final DocumentCreator documentFactory;

	private final String targetClass;

	private DmsServiceResource(final Builder builder) {
		dmsService = new AlfrescoDmsService();
		dmsService.setConfiguration(builder.configuration);
		documentFactory = new DefaultDocumentCreator(builder.path);
		targetClass = builder.targetClass;
	}

	public DocumentRepository at(final Long id) {
		return new DocumentRepository(this, id);
	}

}
