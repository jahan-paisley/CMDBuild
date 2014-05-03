package org.cmdbuild.dms.alfresco.webservice;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.webservice.dictionary.DictionaryFault;
import org.alfresco.webservice.dictionary.DictionaryServiceSoapPort;
import org.alfresco.webservice.types.ClassDefinition;
import org.alfresco.webservice.types.PropertyDefinition;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.MetadataType;
import org.cmdbuild.dms.alfresco.utils.CustomModelParser;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class GetDocumentTypeDefinitionsCommand extends AlfrescoWebserviceCommand<Map<String, DocumentTypeDefinition>> {

	private static class ClassDefinitionWithUriInName implements Predicate<ClassDefinition> {

		private final String uri;

		private ClassDefinitionWithUriInName(final String uri) {
			this.uri = uri;
		}

		@Override
		public boolean apply(final ClassDefinition classDefinition) {
			final String name = classDefinition.getName();
			return startsWith(name, uriInCurlyBrackets());
		}

		private String uriInCurlyBrackets() {
			return Constants.createQNameString(uri, EMPTY);
		}

		public static Predicate<ClassDefinition> of(final String uri) {
			return new ClassDefinitionWithUriInName(uri);
		}

	}

	private static class AspectProperty implements MetadataDefinition {

		private static final String TEXT_ID = "text";
		private static final String INT_ID = "int";
		private static final String LONG_ID = "long";
		private static final String FLOAT_ID = "float";
		private static final String DATE_ID = "date";
		private static final String DATETIME_ID = "datetime";
		private static final String BOOLEAN_ID = "boolean";

		private static enum AlfrescoMetadataType {

			TEXT(TEXT_ID, MetadataType.TEXT), //
			INT(INT_ID, MetadataType.INTEGER), //
			LONG(LONG_ID, MetadataType.INTEGER), //
			FLOAT(FLOAT_ID, MetadataType.FLOAT), //
			DATE(DATE_ID, MetadataType.DATE), //
			DATETIME(DATETIME_ID, MetadataType.DATETIME), //
			BOOLEAN(BOOLEAN_ID, MetadataType.BOOLEAN);

			private final String id;
			private final MetadataType metadataType;

			private AlfrescoMetadataType(final String alfrescoId, final MetadataType metadataType) {
				this.id = alfrescoId;
				this.metadataType = metadataType;
			}

			public MetadataType getMetadataType() {
				return metadataType;
			}

			public static AlfrescoMetadataType of(final String alfrescoId) {
				for (final AlfrescoMetadataType element : values()) {
					if (element.id.equals(alfrescoId)) {
						return element;
					}
				}
				return TEXT;
			}

		}

		private final PropertyDefinition propertyDefinition;
		private final List<String> constraints;

		private AspectProperty(final PropertyDefinition propertyDefinition, final List<String> constraints) {
			this.propertyDefinition = propertyDefinition;
			this.constraints = constraints;
		}

		@Override
		public String getName() {
			return removeNamespace(propertyDefinition.getName());
		}

		@Override
		public String getDescription() {
			return propertyDefinition.getTitle();
		}

		@Override
		public MetadataType getType() {
			if (isList()) {
				return MetadataType.LIST;
			} else {
				final String alfrescoId = removeNamespace(propertyDefinition.getDataType());
				return AlfrescoMetadataType.of(alfrescoId).getMetadataType();
			}
		}

		@Override
		public boolean isMandatory() {
			return propertyDefinition.isMandatory();
		}

		@Override
		public boolean isList() {
			return !constraints.isEmpty();
		}

		@Override
		public Iterable<String> getListValues() {
			return constraints;
		}

		public static MetadataDefinition of(final PropertyDefinition propertyDefinitions, final List<String> constraints) {
			return new AspectProperty(propertyDefinitions, constraints);
		}

		@Override
		public boolean equals(final Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof AspectProperty)) {
				return false;
			}
			final AspectProperty metadataDefinition = AspectProperty.class.cast(object);
			return getName().equals(metadataDefinition.getName());
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getName()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this) //
					.append("name", getName()) //
					.append("type", getType()) //
					.toString();
		}

		private String removeNamespace(final String name) {
			return name.replaceAll(ANYTHING_BETWEEN_CURLY_BRACERS, EMPTY);
		}

	}

	private static class AspectDefinition implements MetadataGroupDefinition {

		private final ClassDefinition classDefinition;
		private final Map<String, List<String>> constraintsByMetadata;

		private AspectDefinition(final ClassDefinition classDefinition,
				final Map<String, List<String>> constraintsByMetadata) {
			this.classDefinition = classDefinition;
			this.constraintsByMetadata = constraintsByMetadata;
		}

		@Override
		public String getName() {
			return strip(classDefinition.getName());
		}

		@Override
		public Iterable<MetadataDefinition> getMetadataDefinitions() {
			final List<MetadataDefinition> metadataDefinitions = Lists.newArrayList();
			for (final PropertyDefinition propertyDefinition : allPropertyDefinitions()) {
				metadataDefinitions.add(AspectProperty.of(propertyDefinition, constraintsOf(propertyDefinition)));
			}
			return metadataDefinitions;
		}

		/**
		 * Null-safe
		 */
		private List<PropertyDefinition> allPropertyDefinitions() {
			final PropertyDefinition[] propertyDefinitions = classDefinition.getProperties();
			return (propertyDefinitions == null) ? Collections.<PropertyDefinition> emptyList()
					: asList(propertyDefinitions);
		}

		private List<String> constraintsOf(final PropertyDefinition propertyDefinition) {
			final List<String> values = constraintsByMetadata.get(strip(propertyDefinition.getName()));
			return (values == null) ? Collections.<String> emptyList() : values;
		}

		private String strip(final String name) {
			return name.replaceAll(ANYTHING_BETWEEN_CURLY_BRACERS, EMPTY);
		}

		@Override
		public boolean equals(final Object object) {
			if (object == this) {
				return true;
			}
			if (!(object instanceof AspectDefinition)) {
				return false;
			}
			final AspectDefinition metadataGroupDefinition = AspectDefinition.class.cast(object);
			return getName().equals(metadataGroupDefinition.getName());
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(getName()) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this) //
					.append("name", getName()) //
					.toString();
		}

		public static MetadataGroupDefinition of(final ClassDefinition classDefinition,
				final Map<String, List<String>> constraintsByMetadata) {
			return new AspectDefinition(classDefinition, constraintsByMetadata);
		}

	}

	private static class DocumentTypeDefinitionBuilder {

		private String name;
		private final List<MetadataGroupDefinition> metadataGroupDefinitions = Lists.newArrayList();

		public DocumentTypeDefinition build() {
			return new DocumentTypeDefinition() {

				@Override
				public String getName() {
					return name;
				}

				@Override
				public Iterable<MetadataGroupDefinition> getMetadataGroupDefinitions() {
					return metadataGroupDefinitions;
				}

			};
		}

		public void setName(final String name) {
			this.name = name;
		}

		public void add(final MetadataGroupDefinition metadataGroupDefinition) {
			metadataGroupDefinitions.add(metadataGroupDefinition);
		}

	}

	private static final String ANYTHING_BETWEEN_CURLY_BRACERS = "\\{.*\\}";

	private final DictionaryServiceSoapPort dictionaryService;

	private String uri;
	private String prefix;
	private String content;

	private CustomModelParser customModelParser;

	public GetDocumentTypeDefinitionsCommand() {
		dictionaryService = WebServiceFactory.getDictionaryService();
	}

	public void setUri(final String uri) {
		this.uri = uri;
	}

	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	public void setCustomModelContent(final String content) {
		this.content = content;
	}

	@Override
	public boolean isSuccessfull() {
		return getResult() != null;
	}

	@Override
	public void execute() {
		Validate.isTrue(StringUtils.isNotBlank(uri), "invalid uri root '%s'", uri);
		Validate.isTrue(StringUtils.isNotBlank(prefix), "invalid prefix '%s'", prefix);
		try {
			final Map<String, DocumentTypeDefinition> result = Maps.newLinkedHashMap();

			final Map<String, MetadataGroupDefinition> aspectDefinitions = aspectDefinitions();
			final Map<String, List<String>> aspectNamesByType = aspectNamesByType();
			for (final String type : aspectNamesByType.keySet()) {
				final DocumentTypeDefinitionBuilder builder = new DocumentTypeDefinitionBuilder();
				builder.setName(type);
				for (final String aspectName : aspectNamesByType.get(type)) {
					final MetadataGroupDefinition aspectDefinition = aspectDefinitions.get(aspectName);
					if (aspectDefinition == null) {
						logger.warn("no aspect definition for expected name '{}'", aspectName);
					} else {
						builder.add(aspectDefinition);
					}
				}
				final DocumentTypeDefinition typeDefinition = builder.build();
				result.put(type, typeDefinition);
			}

			setResult(result);
		} catch (final Exception e) {
			logger.error("error getting document type definitions", e);
		}
	}

	private Map<String, MetadataGroupDefinition> aspectDefinitions() throws RemoteException, DictionaryFault {
		final Map<String, MetadataGroupDefinition> aspectDefinitions = Maps.newHashMap();
		final Map<String, List<String>> constraintsByMetadata = constraintsByMetadata();
		for (final ClassDefinition classDefinition : cmdbuildClassDefinitions()) {
			if (classDefinition.isIsAspect()) {
				final MetadataGroupDefinition aspectDefinition = AspectDefinition.of(classDefinition,
						constraintsByMetadata);
				aspectDefinitions.put(aspectDefinition.getName(), aspectDefinition);
			} else /* is custom content type */{
				// we don't need this kind of informations
			}
		}
		return aspectDefinitions;
	}

	private Collection<ClassDefinition> cmdbuildClassDefinitions() throws RemoteException, DictionaryFault {
		final List<ClassDefinition> allClassDefinitions = allClassDefinitions();
		final Collection<ClassDefinition> filteredClassDefinitions = filter(allClassDefinitions,
				ClassDefinitionWithUriInName.of(uri));
		return filteredClassDefinitions;
	}

	/**
	 * null-safe
	 */
	private List<ClassDefinition> allClassDefinitions() throws RemoteException, DictionaryFault {
		final ClassDefinition[] classDefinitions = dictionaryService.getClasses(null, null);
		return (classDefinitions == null) ? Collections.<ClassDefinition> emptyList() : asList(classDefinitions);
	}

	private Map<String, List<String>> aspectNamesByType() {
		final Map<String, List<String>> aspectNamesByType;
		if (isNotBlank(content)) {
			aspectNamesByType = customModelParser().getAspectsByType();
		} else {
			aspectNamesByType = Collections.emptyMap();
		}
		return aspectNamesByType;
	}

	private Map<String, List<String>> constraintsByMetadata() {
		final Map<String, List<String>> constraintsByMetadata;
		if (isNotBlank(content)) {
			constraintsByMetadata = customModelParser().getConstraintsByMetadata();
		} else {
			constraintsByMetadata = Collections.emptyMap();
		}
		return constraintsByMetadata;
	}

	private CustomModelParser customModelParser() {
		if (customModelParser == null) {
			customModelParser = new CustomModelParser(content, prefix);
		}
		return customModelParser;
	}

}
