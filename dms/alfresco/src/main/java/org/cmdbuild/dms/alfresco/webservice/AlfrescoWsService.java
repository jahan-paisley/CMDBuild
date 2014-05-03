package org.cmdbuild.dms.alfresco.webservice;

import static org.alfresco.webservice.util.Constants.ASPECT_VERSIONABLE;
import static org.alfresco.webservice.util.Constants.PROP_DESCRIPTION;
import static org.alfresco.webservice.util.Constants.PROP_TITLE;
import static org.alfresco.webservice.util.Constants.createQNameString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dms.DefaultDefinitionsFactory;
import org.cmdbuild.dms.DefinitionsFactory;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.DocumentUpdate;
import org.cmdbuild.dms.DocumentWithMetadata;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.MetadataType;
import org.cmdbuild.dms.SingleDocumentSearch;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.dms.alfresco.AlfrescoInnerService;
import org.cmdbuild.dms.alfresco.StoredDocumentComparator;
import org.cmdbuild.dms.exception.DmsError;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AlfrescoWsService extends AlfrescoInnerService {

	private static final SimpleDateFormat CMDBUILD_DATETIME_PARSING_FORMAT = new SimpleDateFormat(
			Constants.SOAP_ALL_DATES_PARSING_PATTERN);

	private static final SimpleDateFormat CMDBUILD_DATETIME_PRINTING_FORMAT = new SimpleDateFormat(
			Constants.DATETIME_PRINTING_PATTERN);

	private static final SimpleDateFormat CMDBUILD_DATE_PRINTING_FORMAT = new SimpleDateFormat(
			Constants.DATE_PRINTING_PATTERN);

	private static final DateTimeFormatter ALFRESCO_FORMATTER = ISODateTimeFormat.dateTime();

	private static final Map<String, String> NO_ASPECT_PROPERTIES = Collections.emptyMap();

	private static final List<Metadata> EMPTY_METADATA = Collections.emptyList();

	private final String qnamePrefixForCustomAspects = createQNameString(configuration.getAlfrescoCustomUri(), EMPTY);

	private final DefinitionsFactory definitionsFactory;

	private Iterable<DocumentTypeDefinition> cachedDocumentTypeDefinitions;

	public AlfrescoWsService(final DmsConfiguration configuration) {
		super(configuration);
		definitionsFactory = new DefaultDefinitionsFactory();
	}

	@Override
	public List<StoredDocument> search(final DocumentSearch document) throws DmsError {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final ResultSetRow[] resultSetRows = wsClient.search(document);
		final List<StoredDocument> storedDocuments = Lists.newArrayList();
		for (final ResultSetRow resultSetRow : resultSetRows) {
			storedDocuments.add(storedDocumentFrom(resultSetRow));
		}
		Collections.sort(storedDocuments, StoredDocumentComparator.INSTANCE);
		return storedDocuments;
	}

	private StoredDocument storedDocumentFrom(final ResultSetRow resultSetRow) throws DmsError {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final StoredDocument storedDocument = new StoredDocument();
		final NamedValue[] namedValues = resultSetRow.getColumns();
		/*
		 * We cannot parse aspect properties before having "category" named
		 * value parsed. Since we are not sure of the order of the named values,
		 * we must iterate the first time on well-defined names and a second
		 * time looking for aspect properties.
		 */
		for (final NamedValue namedValue : namedValues) {
			final AlfrescoConstant alfrescoConstant = AlfrescoConstant.from(namedValue);
			alfrescoConstant.setInBean(storedDocument, namedValue, wsClient);
		}
		final String category = storedDocument.getCategory();
		if (isNotBlank(category)) {
			final Map<String, List<Metadata>> metadataListByGroup = guessMetadataGroups(category, namedValues);
			storedDocument.setMetadataGroups(metadataGroupsFrom(metadataListByGroup));
		}

		return storedDocument;
	}

	/**
	 * Needed because actually Alfresco API did not return which aspect a
	 * property ({@link NamedValue}) owns to. So we must guess using property
	 * name. That's why {@link DocumentTypeDefinition}s needs to be cached.
	 * 
	 * @throws DmsError
	 */
	private Map<String, List<Metadata>> guessMetadataGroups(final String category, final NamedValue[] namedValues)
			throws DmsError {
		final DocumentTypeDefinition documentTypeDefinition = documentTypeDefinitionFor(category);
		final Map<String, List<Metadata>> metadataListByGroup = Maps.newHashMap();
		for (final NamedValue namedValue : namedValues) {
			final AlfrescoConstant alfrescoConstant = AlfrescoConstant.from(namedValue);
			if ((alfrescoConstant == AlfrescoConstant.NULL) && canBeAspectProperty(namedValue)) {
				final String propertyName = propertyNameFrom(namedValue);
				for (final MetadataGroupDefinition metadataGroupDefinition : documentTypeDefinition
						.getMetadataGroupDefinitions()) {
					for (final MetadataDefinition metadataDefinition : metadataGroupDefinition.getMetadataDefinitions()) {
						if (metadataDefinition.getName().equals(propertyName)) {
							final String group = metadataGroupDefinition.getName();
							List<Metadata> metadataList = metadataListByGroup.get(group);
							if (metadataList == null) {
								metadataList = Lists.newArrayList();
								metadataListByGroup.put(group, metadataList);
							}
							metadataList.add(metadata(propertyName,
									convertFromAlfrescoValue(metadataDefinition.getType(), namedValue.getValue())));
						}
					}
				}
			}
		}
		return metadataListByGroup;
	}

	private DocumentTypeDefinition documentTypeDefinitionFor(final String type) throws DmsError {
		for (final DocumentTypeDefinition documentTypeDefinition : getDocumentTypeDefinitions()) {
			if (documentTypeDefinition.getName().equals(type)) {
				return documentTypeDefinition;
			}
		}
		return definitionsFactory.newDocumentTypeDefinitionWithNoMetadata(type);
	}

	private boolean canBeAspectProperty(final NamedValue namedValue) {
		return startsWith(namedValue.getName(), qnamePrefixForCustomAspects);
	}

	private String propertyNameFrom(final NamedValue namedValue) {
		return replace(namedValue.getName(), qnamePrefixForCustomAspects, EMPTY);
	}

	private Metadata metadata(final String name, final String value) {
		return new Metadata() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getValue() {
				return value;
			}

		};
	}

	private Iterable<MetadataGroup> metadataGroupsFrom(final Map<String, List<Metadata>> metadataListByGroup) {
		final List<MetadataGroup> metadataGroups = Lists.newArrayList();
		for (final String group : metadataListByGroup.keySet()) {
			final List<Metadata> metadataList = metadataListByGroup.get(group);
			metadataGroups.add(new MetadataGroup() {

				@Override
				public String getName() {
					return group;
				}

				@Override
				public Iterable<Metadata> getMetadata() {
					return (metadataList == null) ? EMPTY_METADATA : metadataList;
				}

			});
		}
		return metadataGroups;
	}

	@Override
	public void updateDescription(final DocumentUpdate document) throws DmsError {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final List<StoredDocument> storedDocuments = search(new DocumentSearch() {

			@Override
			public String getClassName() {
				return document.getClassName();
			}

			@Override
			public String getCardId() {
				return document.getCardId();
			}

			@Override
			public List<String> getPath() {
				return document.getPath();
			}

		});

		for (final StoredDocument storedDocument : storedDocuments) {
			if (storedDocument.getName().equals(document.getFileName())) {
				final String uuid = storedDocument.getUuid();
				final boolean updated = wsClient.update( //
						uuid, //
						updatableProperties(document), //
						additionalAspectProperties(document));
				if (!updated) {
					throw DmsError.wsOperationError("error updating description");
				}
			}
		}
	}

	private static Properties updatableProperties(final DocumentUpdate document) {
		final Properties properties = new Properties();
		properties.setProperty(PROP_DESCRIPTION, document.getDescription());
		return properties;
	}

	@Override
	public void updateProperties(final StorableDocument document) throws DmsError {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final String uuid = getUuid(document);
		final boolean updated = wsClient.update( //
				uuid, //
				updatableProperties(document), //
				aspectsProperties(document));
		if (!updated) {
			final String message = String.format("error updating file '%s' for card '%s' with class '%s'",
					document.getFileName(), document.getCardId(), document.getClassName());
			logger.error(message);
		}
	}

	private static Properties updatableProperties(final StorableDocument document) {
		final Properties properties = new Properties();
		properties.setProperty(PROP_TITLE, document.getFileName());
		properties.setProperty(PROP_DESCRIPTION, document.getDescription());
		properties.setProperty(AlfrescoConstant.AUTHOR.getName(), document.getAuthor());
		return properties;
	}

	private Map<String, Map<String, String>> aspectsProperties(final StorableDocument document) throws DmsError {
		final Map<String, Map<String, String>> aspectsProperties = Maps.newHashMap();
		aspectsProperties.putAll(defaultAspectProperties());
		aspectsProperties.putAll(additionalAspectProperties(document));
		return aspectsProperties;
	}

	private Map<String, Map<String, String>> defaultAspectProperties() {
		final Map<String, Map<String, String>> defaultAspectProperties = Maps.newHashMap();
		defaultAspectProperties.put(ASPECT_VERSIONABLE, NO_ASPECT_PROPERTIES);
		return defaultAspectProperties;
	}

	private Map<String, Map<String, String>> additionalAspectProperties(final DocumentWithMetadata document)
			throws DmsError {
		final Map<String, Map<String, String>> additionalAspectProperties = Maps.newHashMap();
		for (final MetadataGroup metadataGroup : document.getMetadataGroups()) {
			additionalAspectProperties.put(aspectNameFrom(metadataGroup), aspectPropertiesFrom(metadataGroup));
		}
		return additionalAspectProperties;
	}

	private String aspectNameFrom(final MetadataGroup metadataGroup) {
		return createQNameString(configuration.getAlfrescoCustomUri(), metadataGroup.getName());
	}

	private Map<String, String> aspectPropertiesFrom(final MetadataGroup metadataGroup) throws DmsError {
		final Map<String, String> properties = Maps.newHashMap();
		for (final Metadata metadata : metadataGroup.getMetadata()) {
			final MetadataType type = metadataTypeFor(metadataGroup, metadata);
			properties.put(propertyNameFrom(metadata), convertToAlfrescoValue(type, metadata.getValue()));
		}
		return properties;
	}

	private MetadataType metadataTypeFor(final MetadataGroup metadataGroup, final Metadata metadata) throws DmsError {
		for (final DocumentTypeDefinition documentTypeDefinition : getDocumentTypeDefinitions()) {
			for (final MetadataGroupDefinition metadataGroupDefinition : documentTypeDefinition
					.getMetadataGroupDefinitions()) {
				if (metadataGroupDefinition.getName().equals(metadataGroup.getName())) {
					for (final MetadataDefinition metadataDefinition : metadataGroupDefinition.getMetadataDefinitions()) {
						if (metadataDefinition.getName().equals(metadata.getName())) {
							return metadataDefinition.getType();
						}
					}
				}
			}
		}
		return MetadataType.TEXT;
	}

	private String convertToAlfrescoValue(final MetadataType type, final String value) {
		try {
			final String alfrescoValue;
			switch (type) {
			case DATE:
			case DATETIME: {
				final Date date = CMDBUILD_DATETIME_PARSING_FORMAT.parse(value);
				alfrescoValue = ALFRESCO_FORMATTER.print(date.getTime());
				break;
			}
			default: {
				alfrescoValue = value;
			}
			}
			return alfrescoValue;
		} catch (final Exception e) {
			return EMPTY;
		}
	}

	private String convertFromAlfrescoValue(final MetadataType type, final String alfrescoValue) {
		try {
			final String value;
			switch (type) {
			case DATE: {
				final DateTime dateTime = ALFRESCO_FORMATTER.parseDateTime(alfrescoValue);
				value = CMDBUILD_DATE_PRINTING_FORMAT.format(dateTime.toDate());
				break;
			}
			case DATETIME: {
				final DateTime dateTime = ALFRESCO_FORMATTER.parseDateTime(alfrescoValue);
				value = CMDBUILD_DATETIME_PRINTING_FORMAT.format(dateTime.toDate());
				break;
			}
			default: {
				value = alfrescoValue;
			}
			}
			return value;
		} catch (final Exception e) {
			return EMPTY;
		}
	}

	private String propertyNameFrom(final Metadata metadata) {
		return createQNameString(configuration.getAlfrescoCustomUri(), metadata.getName());
	}

	@Override
	public void updateCategory(final StorableDocument document) throws DmsError {
		final AlfrescoWebserviceClient wsClient = wsClient();
		Reference categoryReference = wsClient.getCategoryReference(document.getCategory());
		if (categoryReference == null) {
			final boolean categoryCreated = wsClient.createCategory(document.getCategory());
			if (!categoryCreated) {
				final String message = String.format("error creating category '%s'", document.getCategory());
				throw DmsError.wsOperationError(message);
			}
			categoryReference = wsClient.getCategoryReference(document.getCategory());
			if (categoryReference == null) {
				final String message = String.format("error getting new category '%s'", document.getCategory());
				throw DmsError.wsOperationError(message);
			}
		}
		final String uuid = getUuid(document);
		wsClient.applyCategory(categoryReference, uuid);
	}

	private String getUuid(final StorableDocument document) throws DmsError {
		final AlfrescoWebserviceClient wsClient = wsClient();
		final ResultSetRow resultSetRow = wsClient.search(singleDocumentSearchFrom(document));
		final String uuid = resultSetRow.getNode().getId();
		return uuid;
	}

	private static SingleDocumentSearch singleDocumentSearchFrom(final StorableDocument document) {
		return new SingleDocumentSearch() {

			@Override
			public String getClassName() {
				return document.getClassName();
			}

			@Override
			public String getCardId() {
				return document.getCardId();
			}

			@Override
			public List<String> getPath() {
				return document.getPath();
			}

			@Override
			public String getFileName() {
				return document.getFileName();
			}

		};
	}

	@Override
	public Iterable<DocumentTypeDefinition> getDocumentTypeDefinitions() throws DmsError {
		synchronized (this) {
			if (cachedDocumentTypeDefinitions == null) {
				logger.info("intializing internal cache for document type definitions");
				cachedDocumentTypeDefinitions = wsClient().getDocumentTypeDefinitions();
			}
		}
		return cachedDocumentTypeDefinitions;
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			cachedDocumentTypeDefinitions = null;
		}
	}

	@Override
	public void move(final StoredDocument document, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		final ResultSetRow resultSetRow = wsClient().search(new SingleDocumentSearch() {

			@Override
			public String getClassName() {
				return from.getClassName();
			}

			@Override
			public List<String> getPath() {
				return from.getPath();
			}

			@Override
			public String getCardId() {
				return from.getCardId();
			}

			@Override
			public String getFileName() {
				return document.getName();
			}

		});
		final String uuid = resultSetRow.getNode().getId();
		wsClient().move(uuid, from, to);
	}

	public void copy(final StoredDocument document, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		final ResultSetRow resultSetRow = wsClient().search(new SingleDocumentSearch() {

			@Override
			public String getClassName() {
				return from.getClassName();
			}

			@Override
			public List<String> getPath() {
				return from.getPath();
			}

			@Override
			public String getCardId() {
				return from.getCardId();
			}

			@Override
			public String getFileName() {
				return document.getName();
			}

		});
		final String uuid = resultSetRow.getNode().getId();
		wsClient().copy(uuid, from, to);
	}

	public void delete(final DocumentSearch position) throws DmsError {
		wsClient().delete(position);
	}

	private AlfrescoWebserviceClient wsClient() {
		logger.info("creating ws client");
		return AlfrescoWebserviceClient.getInstance(configuration);
	}

}
