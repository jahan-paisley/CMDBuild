package org.cmdbuild.services.meta;

import net.jcip.annotations.NotThreadSafe;

// FIXME: it's not enough to synchronize the update
@NotThreadSafe
public class MetadataService {

	private static final String RUNTIME_PREFIX = "runtime";
	public static final String RUNTIME_PRIVILEGES_KEY = RUNTIME_PREFIX + ".privileges";
	public static final String RUNTIME_USERNAME_KEY = RUNTIME_PREFIX + ".username";
	public static final String RUNTIME_DEFAULTGROUPNAME_KEY = RUNTIME_PREFIX + ".groupname";
	public static final String RUNTIME_PROCESS_ISSTOPPABLE = RUNTIME_PREFIX + ".processstoppable";

	public static final String SYSTEM_PREFIX = "system";
	public static final String SYSTEM_TEMPLATE_PREFIX = SYSTEM_PREFIX + ".template";

	public static final String METADATA_CLASS_NAME = "Metadata";
	public static final String METADATA_FULL_NAME = METADATA_CLASS_NAME;

	private static final String METADATA_SCHEMA_ATTRIBUTE = "Code";
	private static final String METADATA_KEY_ATTRIBUTE = "Description";
	private static final String METADATA_VALUE_ATTRIBUTE = "Notes";

	// public static MetadataService of(final BaseSchema schema) {
	// return new MetadataService(schema);
	// }
	//
	// private final BaseSchema schema;
	//
	// private MetadataService(final BaseSchema schema) {
	// this.schema = schema;
	// }
	//
	// private static final String ALL = EMPTY;
	//
	// public static MetadataMap getAllMetadata() {
	// return getMetadataMap(ALL);
	// }
	//
	// public Object getMetadata(final String name) {
	// return getMetadataMap().get(name);
	// }
	//
	// public MetadataMap getMetadataMap() {
	// final String schemaFullName = fullName(schema);
	// return getMetadataMap(schemaFullName);
	// }
	//
	// private static MetadataMap getMetadataMap(final String schemaFullName) {
	// return loadMetaMap(schemaFullName);
	// }
	//
	// private static MetadataMap loadMetaMap(final String schemaFullName) {
	// final MetadataMap metaMap = new MetadataMap();
	// if (!METADATA_FULL_NAME.equals(schemaFullName)) { // skip metadata class
	// CardQuery cardQuery = metadataClass.cards().list()
	// .attributes(METADATA_KEY_ATTRIBUTE, METADATA_VALUE_ATTRIBUTE);
	// if (isNotBlank(schemaFullName)) {
	// cardQuery = cardQuery.filter(METADATA_SCHEMA_ATTRIBUTE,
	// AttributeFilterType.EQUALS, schemaFullName);
	// }
	// for (final ICard metadataCard : cardQuery) {
	// final String key = (String)
	// metadataCard.getValue(METADATA_KEY_ATTRIBUTE);
	// final String value = (String)
	// metadataCard.getValue(METADATA_VALUE_ATTRIBUTE);
	// metaMap.put(key, value);
	// }
	// }
	// return metaMap;
	// }
	//
	// private static String fullName(final BaseSchema schema) {
	// if (schema instanceof IAttribute) {
	// final IAttribute attr = (IAttribute) schema;
	// return String.format("%s.%s", attr.getSchema().getName(),
	// attr.getName());
	// } else {
	// return schema.getName();
	// }
	// }
	//
	// public synchronized void updateMetadata(final String name, final String
	// newValue) {
	// final MetadataMap metaMap = getMetadataMap();
	// final String oldValue = (String) metaMap.get(name);
	//
	// if (newValue != null) {
	// if (!newValue.equals(oldValue)) {
	// ICard metaCard;
	// if (oldValue == null) {
	// metaCard = createMetaCard(schema, name);
	// } else {
	// metaCard = getMetaCard(schema, name);
	// }
	// metaCard.setValue(METADATA_VALUE_ATTRIBUTE, newValue);
	// metaCard.save();
	// metaMap.put(name, newValue);
	// }
	// } else {
	// if (oldValue != null) {
	// getMetaCard(schema, name).delete();
	// metaMap.remove(name);
	// }
	// }
	// }
	//
	// private static ICard createMetaCard(final BaseSchema schema, final String
	// name) {
	// final String schemaFullName = fullName(schema);
	// final ICard metaCard = metadataClass.cards().create();
	// metaCard.setValue(METADATA_SCHEMA_ATTRIBUTE, schemaFullName);
	// metaCard.setValue(METADATA_KEY_ATTRIBUTE, name);
	// return metaCard;
	// }
	//
	// private static ICard getMetaCard(final BaseSchema schema, final String
	// name) {
	// final String schemaFullName = fullName(schema);
	// final ICard metaCard = metadataClass.cards().list()
	// .filter(METADATA_SCHEMA_ATTRIBUTE, AttributeFilterType.EQUALS,
	// schemaFullName)
	// .filter(METADATA_KEY_ATTRIBUTE, AttributeFilterType.EQUALS,
	// name).get(false);
	// return metaCard;
	// }
	//
	// public void deleteAllMetadata() {
	// deleteMetadata(schema);
	// }
	//
	// private static void deleteMetadata(final BaseSchema schema) {
	// final String schemaFullName = fullName(schema);
	// getDeleteMetaTemplate();
	// metadataClass.cards().list() //
	// .filter(METADATA_SCHEMA_ATTRIBUTE, AttributeFilterType.EQUALS,
	// schemaFullName) //
	// .update(getDeleteMetaTemplate());
	// for (final IAttribute attribute : schema.getAttributes().values()) {
	// deleteMetadata(attribute);
	// }
	// }
	//
	// private static ICard getDeleteMetaTemplate() {
	// final ICard deleteMetaTemplate = metadataClass.cards().create();
	// deleteMetaTemplate.setStatus(ElementStatus.INACTIVE);
	// return deleteMetaTemplate;
	// }
}
