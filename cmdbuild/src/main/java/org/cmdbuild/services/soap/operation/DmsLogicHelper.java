package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.services.soap.types.Attachment;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DmsLogicHelper implements SoapLogicHelper {

	private static final Marker marker = MarkerFactory.getMarker(DmsLogicHelper.class.getName());

	private static final List<MetadataGroup> METADATA_NOT_SUPPORTED = Collections.emptyList();

	private final OperationUser operationUser;
	private final DmsLogic dmsLogic;

	public DmsLogicHelper(final OperationUser operationUser, final DmsLogic dmsLogic) {
		this.operationUser = operationUser;
		this.dmsLogic = dmsLogic;
	}

	public Attachment[] getAttachmentList(final String className, final Long cardId) {
		final List<StoredDocument> storedDocuments = dmsLogic.search(className, cardId);
		final List<Attachment> attachments = newArrayList();
		for (final StoredDocument storedDocument : storedDocuments) {
			final Attachment attachment = new Attachment(storedDocument);
			attachments.add(attachment);
		}
		return attachments.toArray(new Attachment[attachments.size()]);
	}

	public boolean uploadAttachment(final String className, final Long cardId, final DataHandler file,
			final String filename, final String category, final String description) {
		try {
			dmsLogic.upload( //
					operationUser.getAuthenticatedUser().getUsername(), //
					className, //
					cardId, //
					file.getInputStream(), //
					filename, //
					category, //
					description, //
					METADATA_NOT_SUPPORTED);
			return true;
		} catch (final Exception e) {
			final String message = String.format("error uploading file '%s' in '%s'", filename, className);
			logger.error(marker, message, e);
		}
		return false;
	}

	public DataHandler download(final String className, final Long cardId, final String filename) {
		return dmsLogic.download( //
				className, //
				cardId, //
				filename);
	}

	public boolean delete(final String className, final Long cardId, final String filename) {
		dmsLogic.delete( //
				className, //
				cardId, //
				filename);
		return true;
	}

	public boolean updateDescription(final String className, final Long cardId, final String filename,
			final String description) {
		try {
			dmsLogic.updateDescriptionAndMetadata( //
					className, //
					cardId, //
					filename, //
					description, //
					METADATA_NOT_SUPPORTED);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

}
