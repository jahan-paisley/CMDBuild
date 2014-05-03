package org.cmdbuild.servlets.json.management;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.logic.email.EmailLogic.Copy.newCopy;
import static org.cmdbuild.logic.email.EmailLogic.Delete.newDelete;
import static org.cmdbuild.logic.email.EmailLogic.Upload.newUpload;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTACHMENTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.PROCESS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.SUCCESS;
import static org.cmdbuild.servlets.json.ComunicationConstants.TEMPORARY_ID;

import java.io.IOException;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.email.EmailLogic.CopiableAttachment;
import org.cmdbuild.logic.email.EmailLogic.Copy;
import org.cmdbuild.logic.email.EmailLogic.EmailWithAttachmentNames;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.JsonWorkflowDTOs.JsonEmail;
import org.cmdbuild.servlets.utils.FileItemDataSource;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class Email extends JSONBaseWithSpringContext {

	private static class JsonAttachment {

		public String className;
		public Long cardId;
		public String fileName;

	}

	private static final Function<EmailWithAttachmentNames, JsonEmail> TO_JSON_EMAIL = new Function<EmailWithAttachmentNames, JsonEmail>() {

		@Override
		public JsonEmail apply(final EmailWithAttachmentNames input) {
			return new JsonEmail(input);
		}

	};

	private static final Function<JsonAttachment, CopiableAttachment> TO_COPIABLE_ATTACHMENTS = new Function<JsonAttachment, EmailLogic.CopiableAttachment>() {

		@Override
		public CopiableAttachment apply(final JsonAttachment input) {
			// TODO Auto-generated method stub
			return CopiableAttachment.newCopy() //
					.withClassName(input.className) //
					.withCardId(input.cardId) //
					.withFileName(input.fileName) //
					.build();
		}

	};

	@JSONExported
	public JsonResponse getEmailList( //
			@Parameter(PROCESS_ID) final Long processCardId //
	) {
		final Iterable<EmailWithAttachmentNames> emails = emailLogic().getEmails(processCardId);
		return JsonResponse.success(Iterators.transform(emails.iterator(), TO_JSON_EMAIL));
	}

	@JSONExported
	public JSONObject uploadAttachmentFromExistingEmail( //
			@Parameter(EMAIL_ID) final Long emailId, //
			@Parameter(FILE) final FileItem file //
	) throws JSONException, IOException {
		emailLogic().uploadAttachment(newUpload() //
				.withIdentifier(emailId.toString()) //
				.withDataHandler(new DataHandler(FileItemDataSource.of(file))));

		final JSONObject out = new JSONObject();
		out.put(SUCCESS, true);
		out.put(FILE_NAME, file.getName());
		return out;
	}

	@JSONExported
	public JSONObject uploadAttachmentFromNewEmail( //
			@Parameter(value = TEMPORARY_ID, required = false) final String temporaryId, //
			@Parameter(FILE) final FileItem file //
	) throws JSONException, IOException {
		final String returnedIdentifier = emailLogic().uploadAttachment(newUpload() //
				.withIdentifier(temporaryId) //
				.withDataHandler(new DataHandler(FileItemDataSource.of(file))) //
				.withTemporaryStatus(true));

		final JSONObject out = new JSONObject();
		out.put(SUCCESS, true);
		out.put(FILE_NAME, file.getName());
		out.put(TEMPORARY_ID, returnedIdentifier);
		return out;
	}

	@JSONExported
	public JSONObject copyAttachmentsFromCardForNewEmail(
			@Parameter(value = TEMPORARY_ID, required = false) final String temporaryId,
			@Parameter(ATTACHMENTS) final String jsonAttachments //
	) throws JSONException, JsonParseException, JsonMappingException, IOException {
		final Iterable<JsonAttachment> attachments = mapJsonAttachmentsFromJsonArray(jsonAttachments);
		final Copy copied = emailLogic().copyAttachments(newCopy() //
				.withIdentifier(temporaryId) //
				.withTemporaryStatus(true) //
				.withAllAttachments(from(attachments) //
						.transform(TO_COPIABLE_ATTACHMENTS)));

		final JSONObject out = new JSONObject();
		extractFileNames(new JSONArray(jsonAttachments), out);
		out.put(TEMPORARY_ID, copied.identifier);
		out.put(SUCCESS, true);
		return out;
	}

	@JSONExported
	public JSONObject copyAttachmentsFromCardForExistingEmail(
			@Parameter(value = EMAIL_ID, required = false) final Long emailId,
			@Parameter(ATTACHMENTS) final String jsonAttachments //
	) throws JSONException, JsonParseException, JsonMappingException, IOException {
		final Iterable<JsonAttachment> attachments = mapJsonAttachmentsFromJsonArray(jsonAttachments);
		emailLogic().copyAttachments(newCopy() //
				.withIdentifier(emailId.toString()) //
				.withAllAttachments(from(attachments) //
						.transform(TO_COPIABLE_ATTACHMENTS)));

		final JSONObject out = new JSONObject();
		extractFileNames(new JSONArray(jsonAttachments), out);
		out.put(SUCCESS, true);
		return out;
	}

	private Iterable<JsonAttachment> mapJsonAttachmentsFromJsonArray(final String content) throws JsonParseException,
			JsonMappingException, IOException {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue( //
				content, //
				mapper.getTypeFactory() //
						.constructCollectionType(Set.class, JsonAttachment.class));
	}

	@JSONExported
	public JsonResponse deleteAttachmentFromExistingEmail( //
			@Parameter(EMAIL_ID) final Long emailId, //
			@Parameter(FILE_NAME) final String fileName //
	) {
		emailLogic().deleteAttachment(newDelete() //
				.withIdentifier(emailId.toString()) //
				.withFileName(fileName));

		return JsonResponse.success(null);
	}

	@JSONExported
	public JsonResponse deleteAttachmentFromNewEmail( //
			@Parameter(TEMPORARY_ID) final String temporaryId, //
			@Parameter(FILE_NAME) final String fileName //
	) {
		emailLogic().deleteAttachment(newDelete() //
				.withIdentifier(temporaryId) //
				.withFileName(fileName) //
				.withTemporaryStatus(true));

		return JsonResponse.success(null);
	}

	/**
	 * @param attachments
	 *            an array of object like that {className: "...", cardId: "...",
	 *            fileName: "..."}
	 * @param out
	 * @throws JSONException
	 */
	private void extractFileNames(final JSONArray attachments, final JSONObject out) throws JSONException {
		final JSONArray fileNames = new JSONArray();
		for (int i = 0; i < attachments.length(); i++) {
			final JSONObject attachmentConf = attachments.getJSONObject(i);
			fileNames.put(attachmentConf.get(FILE_NAME));
		}
		out.put(ATTACHMENTS, fileNames);
	}

};
