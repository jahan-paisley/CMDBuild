package org.cmdbuild.bim.service.bimserver;

import javax.activation.DataHandler;

import org.bimserver.interfaces.objects.SDownloadResult;

public class BimserverDownloadResult {

	private final SDownloadResult result;

	protected BimserverDownloadResult(final SDownloadResult result) {
		this.result = result;
	}

	public DataHandler getFile() {
		return result.getFile();
	}

}
