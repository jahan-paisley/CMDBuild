package org.cmdbuild.servlets;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.cmdbuild.cmdbf.xml.XmlRegistry;
import org.cmdbuild.logic.auth.AuthenticationLogicUtils;
import org.cmdbuild.logic.cache.CachingLogic;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class XsdSchema extends HttpServlet {

	private static final long serialVersionUID = 1L;

	ApplicationContext applicationContext;

	@Override
	public void init() throws ServletException {
		super.init();
		applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
	}

	@Override
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
			ServletException {
		try {
			AuthenticationLogicUtils.assureAdmin(request, AdminAccess.FULL);
			final XmlRegistry xmlRegistry = applicationContext.getBean(XmlRegistry.class);

			final URI baseUri = URI.create(request.getRequestURL().toString());
			final FileItemFactory factory = new DiskFileItemFactory();
			final ServletFileUpload upload = new ServletFileUpload(factory);
			if (ServletFileUpload.isMultipartContent(request)) {
				@SuppressWarnings("unchecked")
				final List<FileItem> items = upload.parseRequest(request);
				for (final FileItem item : items) {
					if (!item.isFormField()) {
						final XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
						final URI uri = baseUri.resolve(item.getName());
						final InputSource source = new InputSource(uri.toString());
						final InputStream filecontent = item.getInputStream();
						source.setByteStream(filecontent);
						final XmlSchema schema = schemaCollection.read(source);
						xmlRegistry.updateSchema(schema);
					}
				}
				cachingLogic().clearCache();
			}

			response.setContentType("text/html");
			final Writer writer = response.getWriter();
			writer.write("<html><head><title>Xml Schema Upload</title></head><body>");
			writer.write("OK");
			writer.write("</body></html>");
			writer.flush();

		} catch (final Exception e) {
			response.setContentType("text/html");
			final Writer writer = response.getWriter();
			writer.write("<html><head><title>Xml Schema Upload</title></head><body>");
			writer.write(e.getMessage());
			writer.write("</body></html>");
			writer.flush();
			throw new ServletException(e.getMessage(), e);
		}
	}

	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
			ServletException {
		String pathInfo = request.getPathInfo();
		if (pathInfo != null && pathInfo.startsWith("/")) {
			try {
				final XmlRegistry xmlRegistry = applicationContext.getBean("xmlRegistry", XmlRegistry.class);
				pathInfo = pathInfo.substring(1);
				if (pathInfo == null || pathInfo.isEmpty()) {
					response.setContentType("text/html");
					final Writer writer = response.getWriter();

					writer.write("<html>\n" + "  <head>\n" + "<title>CMDB Xml Schema</title>\n" + "  </head>\n"
							+ "  <body>\n" + "    <p>\n"
							+ "      <form method='POST' accept-charset='UTF-8' enctype='multipart/form-data'>\n"
							+ "        Xml schema: <input type='file' name='xsd'><br>\n"
							+ "        <input type='submit' value='Upload'>\n" + "      </form>\n" + "    </p>\n"
							+ "    <p>\n" + "    <ul>\n");
					for (final String systemId : xmlRegistry.getSystemIds()) {
						writer.write("<li><a href=\"" + systemId + "\">" + systemId + "</a></li>\n");
					}
					writer.write("    </ul>\n" + "    </p>\n" + "  </body>\n" + "</html>");
					writer.flush();
				} else {
					final XmlSchema schema = xmlRegistry.getSchema(pathInfo);
					if (schema != null) {
						final Document schemaDocument = schema.getSchemaDocument();
						final DOMImplementationLS domImplementation = (DOMImplementationLS) schemaDocument
								.getImplementation();
						final LSSerializer lsSerializer = domImplementation.createLSSerializer();
						final LSOutput destination = domImplementation.createLSOutput();
						destination.setByteStream(response.getOutputStream());
						destination.setEncoding("UTF-8");
						response.setCharacterEncoding(destination.getEncoding());
						response.setContentType("text/xml");
						lsSerializer.write(schemaDocument, destination);
					}
				}
			} catch (final Exception e) {
				throw new ServletException(e.getMessage(), e);
			}
		} else {
			final StringBuffer url = request.getRequestURL();
			url.append('/');
			response.sendRedirect(url.toString());
		}
	}

	private CachingLogic cachingLogic() {
		return applicationContext().getBean(CachingLogic.class);
	}
}