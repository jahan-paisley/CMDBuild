package org.cmdbuild.services.gis.geoserver.commands;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public abstract class AbstractGeoCommand {

	protected static final Map<String, String> atomNS;

	static {
		atomNS = new HashMap<String, String>();
		atomNS.put("atom", "http://www.w3.org/2005/Atom");
	}

	protected final GisConfiguration configuration;

	public AbstractGeoCommand(final GisConfiguration configuration) {
		this.configuration = configuration;
	}

	private final ClientResource createClient(final String url) {
		final ClientResource cr = new ClientResource(url);
		cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, getGeoServerAdminUser(), getGeoServerAdminPassword());
		return cr;
	}

	protected final void put(final InputStream data, final String url, final MediaType mime) {
		final ClientResource cr = createClient(url);
		final Representation input = new InputRepresentation(data, mime);
		Log.REST.debug("PUT REQUEST " + url);
		cr.put(input);
	}

	protected final Document get(final String url) {
		Document response;
		try {
			final ClientResource cr = createClient(url);
			final StringWriter sw = new StringWriter();
			Log.REST.debug("GET REQUEST " + url);
			cr.get(MediaType.TEXT_XML).write(sw);
			response = DocumentHelper.parseText(sw.toString());
		} catch (final Exception e) {
			throw NotFoundExceptionType.SERVICE_UNAVAILABLE.createException();
		}
		return response;
	}

	protected final void delete(final String url) {
		final ClientResource cr = createClient(url);
		Log.REST.debug("DELETE REQUEST " + url);
		cr.delete();
	}

	protected final String getGeoServerURL() {
		return configuration.getGeoServerUrl();
	}

	protected final String getGeoServerWorkspace() {
		return configuration.getGeoServerWorkspace();
	}

	protected final String getGeoServerAdminUser() {
		return configuration.getGeoServerAdminUser();
	}

	protected final String getGeoServerAdminPassword() {
		return configuration.getGeoServerAdminPassword();
	}
}
