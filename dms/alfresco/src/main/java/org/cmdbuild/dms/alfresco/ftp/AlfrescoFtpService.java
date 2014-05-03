package org.cmdbuild.dms.alfresco.ftp;

import javax.activation.DataHandler;

import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DocumentDelete;
import org.cmdbuild.dms.DocumentDownload;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.StorableDocument;
import org.cmdbuild.dms.alfresco.AlfrescoInnerService;
import org.cmdbuild.dms.exception.DmsError;

public class AlfrescoFtpService extends AlfrescoInnerService {

	public AlfrescoFtpService(final DmsConfiguration configuration) {
		super(configuration);
	}

	@Override
	public void delete(final DocumentDelete document) throws DmsError {
		ftpClient().delete( //
				document.getFileName(), //
				document.getPath() //
				);
	}

	@Override
	public DataHandler download(final DocumentDownload document) throws DmsError {
		return ftpClient().download( //
				document.getFileName(), //
				document.getPath() //
				);
	}

	@Override
	public void upload(final StorableDocument document) throws DmsError {
		ftpClient().upload( //
				document.getFileName(), //
				document.getInputStream(), //
				document.getPath() //
				);
	}

	private AlfrescoFtpClient ftpClient() {
		return new AlfrescoFtpClient(configuration);
	}

	public void create(final DocumentSearch document) throws DmsError {
		ftpClient().mkdir(document.getPath());
	}

}
