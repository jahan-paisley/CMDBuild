package org.cmdbuild.dms.alfresco.webservice;

import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.types.ResultSetRow;
import org.alfresco.webservice.types.Store;
import org.alfresco.webservice.util.Constants;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.cmdbuild.dms.SingleDocumentSearch;
import org.cmdbuild.dms.exception.DmsError;

class AlfrescoWebserviceClient implements LoggingSupport {

	private static Map<String, AlfrescoWebserviceClient> cache = new WeakHashMap<String, AlfrescoWebserviceClient>();

	private final DmsConfiguration configuration;

	private AlfrescoWebserviceClient(final DmsConfiguration configuration) {
		Validate.notNull(configuration, "null configuration");
		this.configuration = configuration;

		final String address = configuration.getServerURL();
		WebServiceFactory.setEndpointAddress(address);
	}

	public static AlfrescoWebserviceClient getInstance(final DmsConfiguration configuration) {
		Validate.notNull(configuration, "null configuration");
		synchronized (cache) {
			final String address = configuration.getServerURL();
			AlfrescoWebserviceClient client = cache.get(address);
			if (client == null) {
				logger.info(String.format("creating new webservice client for address '%s'", address));
				client = new AlfrescoWebserviceClient(configuration);
				cache.put(address, client);
			}
			return client;
		}
	}

	private void executeWhithinSession(final AlfrescoWebserviceCommand<?> command) {
		final String username = configuration.getAlfrescoUser();
		final String password = configuration.getAlfrescoPassword();
		final AlfrescoSession session = new AlfrescoSession(username, password);
		session.start();
		if (session.isStarted()) {
			logger.debug("executing command '{}'", command.getClass());
			command.execute();
		} else {
			logger.warn("session could not be started");
		}
		session.end();
	}

	private static String baseSearchPath(final DmsConfiguration properties) {
		return new StringBuilder() //
				.append(properties.getRepositoryWSPath()) //
				.append(properties.getRepositoryApp()) //
				.toString();
	}

	public ResultSetRow[] search(final DocumentSearch search) {
		final SearchCommand command = new SearchCommand();
		command.setDocumentSearch(search);
		command.setBaseSearchPath(baseSearchPath(configuration));
		executeWhithinSession(command);
		return command.getResult();
	}

	public ResultSetRow search(final SingleDocumentSearch search) throws DmsError {
		final SingleSearchCommand command = new SingleSearchCommand();
		command.setDocumentSearch(search);
		command.setBaseSearchPath(baseSearchPath(configuration));
		executeWhithinSession(command);
		if (command.isSuccessfull()) {
			return command.getResult();
		}
		throw DmsError.fileNotFound(search.getFileName(), search.getClassName(), search.getCardId());
	}

	public ResultSetRow searchRow(final String uuid) {
		final UuidSearchCommand command = new UuidSearchCommand();
		command.setUuid(uuid);
		executeWhithinSession(command);
		return command.getResult();
	}

	public String searchUuid(final SingleDocumentSearch search) throws DmsError {
		final ResultSetRow resultSetRow = search(search);
		return resultSetRow.getNode().getId();
	}

	public boolean update(final String uuid, final Properties updateProperties,
			final Map<String, Map<String, String>> aspectsProperties) {
		final UpdateCommand command = new UpdateCommand();
		command.setUuid(uuid);
		command.setUpdateProperties(updateProperties);
		command.setAspectsProperties(aspectsProperties);
		executeWhithinSession(command);
		return command.getResult();
	}

	public Reference getCategoryReference(final String category) {
		final GetCategoryCommand command = new GetCategoryCommand();
		command.setCategory(category);
		executeWhithinSession(command);
		return command.getResult();
	}

	public boolean createCategory(final String category) {
		final CreateCategoryCommand command = new CreateCategoryCommand();
		command.setCategoryRoot(configuration.getCmdbuildCategory());
		command.setCategory(category);
		executeWhithinSession(command);
		return command.getResult();
	}

	public boolean applyCategory(final Reference category, final String uuid) {
		final ApplyCategoryCommand command = new ApplyCategoryCommand();
		command.setCategory(category);
		command.setUuid(uuid);
		executeWhithinSession(command);
		return command.getResult();
	}

	public Iterable<DocumentTypeDefinition> getDocumentTypeDefinitions() throws DmsError {
		final GetDocumentTypeDefinitionsCommand command = new GetDocumentTypeDefinitionsCommand();
		command.setUri(configuration.getAlfrescoCustomUri());
		command.setPrefix(configuration.getAlfrescoCustomPrefix());
		command.setCustomModelContent(configuration.getAlfrescoCustomModelFileContent());
		executeWhithinSession(command);
		if (command.isSuccessfull()) {
			return command.getResult().values();
		}
		throw DmsError.wsOperationError("error reading document type definitions");
	}

	public void move(final String uuid, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		final MoveCommand command = new MoveCommand();
		command.setUuid(uuid);
		command.setBaseSearchPath(baseSearchPath(configuration));
		command.setTarget(to);
		executeWhithinSession(command);
		if (!command.isSuccessfull()) {
			throw DmsError.wsOperationError("error moving file");
		}
	}

	public void copy(final String uuid, final DocumentSearch from, final DocumentSearch to) throws DmsError {
		final CopyCommand command = new CopyCommand();
		command.setUuid(uuid);
		command.setBaseSearchPath(baseSearchPath(configuration));
		command.setTarget(to);
		executeWhithinSession(command);
		if (!command.isSuccessfull()) {
			throw DmsError.wsOperationError("error copying file");
		}
	}

	public void delete(final DocumentSearch position) throws DmsError {
		final DeleteCommand command = new DeleteCommand();
		command.setBaseSearchPath(baseSearchPath(configuration));
		command.setTarget(position);
		executeWhithinSession(command);
		if (!command.isSuccessfull()) {
			throw DmsError.wsOperationError("error deleting position");
		}
	}

}

abstract class AlfrescoWebserviceCommand<T> implements LoggingSupport {

	public static final String DEFAULT_STORE_ADDRESS = "SpacesStore";
	public static final Store STORE = new Store(Constants.WORKSPACE_STORE, DEFAULT_STORE_ADDRESS);

	private T result;

	public abstract void execute();

	public abstract boolean isSuccessfull();

	public T getResult() {
		return result;
	}

	protected void setResult(final T result) {
		this.result = result;
	}

	public static String escapeQuery(final String query) {
		return query.replaceAll(" ", "_x0020_");
	}

}
