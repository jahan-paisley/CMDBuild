package org.cmdbuild.servlets.utils.transformer;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.utils.MethodParameterResolver;

public class FileItemTransformer extends AbstractTransformer<FileItem> {

	@SuppressWarnings("unchecked")
	public FileItem transform(HttpServletRequest request, Object context,
			String... value) throws Exception {

		String key = (String)context;
		Log.JSONRPC.debug("get FileItem " + key);
		List<FileItem> fileItems = (List<FileItem>)this.request(request, MethodParameterResolver.MultipartRequest);
		for(FileItem file : fileItems){
			Log.JSONRPC.debug("current FileItem: " + file.getFieldName());
			if(file.getFieldName().equals(key)){
				return file;
			}
		}
		return null;
	}

}
