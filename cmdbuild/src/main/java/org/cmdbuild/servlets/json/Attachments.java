package org.cmdbuild.servlets.json;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.ComunicationConstants.CARD_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.dms.DefaultDefinitionsFactory;
import org.cmdbuild.dms.DefinitionsFactory;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataDefinition;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.MetadataGroupDefinition;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.Attachments.JsonAttachmentsContext;
import org.cmdbuild.servlets.json.serializers.Attachments.JsonCategoryDefinition;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class Attachments extends JSONBaseWithSpringContext {

	private static final Predicate<? super Lookup> ACTIVE_ONLY = new Predicate<Lookup>() {
		@Override
		public boolean apply(final Lookup input) {
			return input.active;
		}
	};

	private static final ObjectMapper mapper = new ObjectMapper();

	private final DefinitionsFactory definitionsFactory;

	public Attachments() {
		definitionsFactory = new DefaultDefinitionsFactory();
	}

	@JSONExported
	public JsonResponse getAttachmentsContext() {
		final LookupType lookupType = LookupType.newInstance() //
				.withName(dmsLogic().getCategoryLookupType()) //
				.build();
		final Iterable<Lookup> lookups = lookupStore().listForType(lookupType);
		final List<JsonCategoryDefinition> jsonCategories = Lists.newArrayList();
		for (final Lookup lookup : from(lookups).filter(ACTIVE_ONLY)) {
			final DocumentTypeDefinition categoryDefinition = categoryDefinition(lookup.description);
			jsonCategories.add(JsonCategoryDefinition.from(lookup, categoryDefinition));
		}
		return JsonResponse.success(JsonAttachmentsContext.from(jsonCategories));
	}

	@JSONExported
	public JSONObject getAttachmentList( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(CARD_ID) final Long cardId //
	) throws JSONException, CMDBException {

		final List<StoredDocument> attachments = dmsLogic().search(className, cardId);
		final JSONArray rows = new JSONArray();
		for (final StoredDocument attachment : attachments) {
			rows.put(Serializer.serializeAttachment(attachment));
		}

		final JSONObject out = new JSONObject();
		out.put("rows", rows);
		return out;
	}

	@JSONExported
	public DataHandler downloadAttachment( //
			@Parameter("Filename") final String filename, //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(CARD_ID) final Long cardId //
	) throws JSONException, CMDBException {

		return dmsLogic().download(className, cardId, filename);
	}

	@JSONExported
	public void uploadAttachment( //
			@Parameter("File") final FileItem file, //
			@Parameter("Category") final String category, //
			@Parameter("Description") final String description, //
			@Parameter("Metadata") final String jsonMetadataValues, //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(CARD_ID) final Long cardId //
	) throws JSONException, CMDBException, IOException {

		final Map<String, Map<String, Object>> metadataValues = metadataValuesFromJson(jsonMetadataValues);
		final String username = operationUser().getAuthenticatedUser().getUsername();
		dmsLogic().upload( //
				username, //
				className, //
				cardId, //
				file.getInputStream(), //
				removeFilePath(file.getName()), //
				category, //
				description, //
				metadataGroupsFrom(categoryDefinition(category), metadataValues) //
				);
	}

	/**
	 * Needed by Internet Explorer that uploads the file with full path
	 */
	private String removeFilePath(final String name) {
		final int backslashIndex = name.lastIndexOf("\\");
		final int slashIndex = name.lastIndexOf("/");
		final int fileNameIndex = Math.max(slashIndex, backslashIndex) + 1;
		return name.substring(fileNameIndex);
	}

	@JSONExported
	public void modifyAttachment( //
			@Parameter("Filename") final String filename, //
			@Parameter("Category") final String category, //
			@Parameter("Description") final String description, //
			@Parameter("Metadata") final String jsonMetadataValues, //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(CARD_ID) final Long cardId //
	) throws JSONException, CMDBException, IOException {

		final Map<String, Map<String, Object>> metadataValues = metadataValuesFromJson(jsonMetadataValues);
		dmsLogic().updateDescriptionAndMetadata( //
				className, cardId, //
				filename, //
				description, //
				metadataGroupsFrom(categoryDefinition(category), metadataValues));
	}

	private List<MetadataGroup> metadataGroupsFrom(final DocumentTypeDefinition documentTypeDefinition,
			final Map<String, Map<String, Object>> metadataValues) {
		final List<MetadataGroup> metadataGroups = Lists.newArrayList();
		for (final MetadataGroupDefinition metadataGroupDefinition : documentTypeDefinition
				.getMetadataGroupDefinitions()) {
			final String groupMame = metadataGroupDefinition.getName();
			final Map<String, Object> allMetadataMap = metadataValues.get(groupMame);
			if (allMetadataMap == null) {
				continue;
			}

			metadataGroups.add(new MetadataGroup() {

				@Override
				public String getName() {
					return groupMame;
				}

				@Override
				public Iterable<Metadata> getMetadata() {
					final List<Metadata> metadata = Lists.newArrayList();
					for (final MetadataDefinition metadataDefinition : metadataGroupDefinition.getMetadataDefinitions()) {
						final String metadataName = metadataDefinition.getName();
						final Object rawValue = allMetadataMap.get(metadataName);
						metadata.add(new Metadata() {

							@Override
							public String getName() {
								return metadataName;
							}

							@Override
							public String getValue() {
								return (rawValue == null) ? StringUtils.EMPTY : rawValue.toString();
							}

						});
					}
					return metadata;
				}
			});

		}
		return metadataGroups;
	}

	@JSONExported
	public void deleteAttachment( //
			@Parameter("Filename") final String filename, //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(CARD_ID) final Long cardId //
	) throws JSONException, CMDBException, IOException {

		dmsLogic().delete(className, cardId, filename);
	}

	/*
	 * Utilities
	 */

	/**
	 * At the first level there are the metadataGroups For each metadataGroups,
	 * there is another map with the values for the group
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Map<String, Object>> metadataValuesFromJson(final String jsonMetadataValues)
			throws IOException, JsonParseException, JsonMappingException {
		return mapper.readValue(jsonMetadataValues, Map.class);
	}

	private DocumentTypeDefinition categoryDefinition(final String category) {
		try {
			return dmsLogic().getCategoryDefinition(category);
		} catch (final DmsException e) {
			requestListener();
			RequestListener.getCurrentRequest().pushWarning(e);
			return definitionsFactory.newDocumentTypeDefinitionWithNoMetadata(category);
		}
	}

}
