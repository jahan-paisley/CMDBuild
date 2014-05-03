package org.cmdbuild.dms.alfresco.ftp;

import java.io.InputStream;
import java.util.List;

import javax.activation.DataHandler;

import org.cmdbuild.dms.exception.DmsError;

interface FtpClient {

	void mkdir(List<String> path) throws DmsError;

	void upload(String filename, InputStream is, List<String> path) throws DmsError;

	void delete(String filename, List<String> path) throws DmsError;

	DataHandler download(String filename, List<String> path) throws DmsError;

}
