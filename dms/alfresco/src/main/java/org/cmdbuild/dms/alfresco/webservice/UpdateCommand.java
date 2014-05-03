package org.cmdbuild.dms.alfresco.webservice;

import static org.alfresco.webservice.util.Utils.createNamedValue;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.webservice.repository.RepositoryServiceSoapPort;
import org.alfresco.webservice.types.CML;
import org.alfresco.webservice.types.CMLAddAspect;
import org.alfresco.webservice.types.CMLUpdate;
import org.alfresco.webservice.types.NamedValue;
import org.alfresco.webservice.types.Predicate;
import org.alfresco.webservice.types.Reference;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;

class UpdateCommand extends AlfrescoWebserviceCommand<Boolean> {

	private String uuid;
	private Properties updateProperties;
	private Map<String, Map<String, String>> aspectsProperties;

	public void setUuid(final String uuid) {
		this.uuid = uuid;
	}

	public void setUpdateProperties(final Properties update) {
		this.updateProperties = update;
	}

	public void setAspectsProperties(final Map<String, Map<String, String>> aspectsProperties) {
		this.aspectsProperties = aspectsProperties;
	}

	@Override
	public boolean isSuccessfull() {
		final Boolean result = getResult();
		return (result == null) ? false : result.booleanValue();
	}

	@Override
	public void execute() {
		Validate.isTrue(StringUtils.isNotBlank(uuid), String.format("invalid uuid '%s'", uuid));
		Validate.notNull(updateProperties, "null properties");

		final Reference reference = new Reference(STORE, uuid, null);

		final Predicate predicate = new Predicate();
		predicate.setStore(STORE);
		predicate.setNodes(new Reference[] { reference });

		final CML cml = new CML();

		final CMLUpdate update = cmlUpdate(predicate, updateProperties);
		cml.setUpdate(new CMLUpdate[] { update });

		final CMLAddAspect[] aspects = aspects(predicate, aspectsProperties);
		cml.setAddAspect(aspects);

		try {
			final RepositoryServiceSoapPort repository = WebServiceFactory.getRepositoryService();
			repository.update(cml);
			setResult(true);
		} catch (final Exception e) {
			final String message = String.format("error updating element '%s'", uuid);
			logger.error(message, e);
			setResult(false);
		}
	}

	private static CMLUpdate cmlUpdate(final Predicate predicate, final Properties properties) {
		final CMLUpdate update = new CMLUpdate();
		update.setWhere(predicate);
		final List<NamedValue> namedValues = new ArrayList<NamedValue>();
		for (final String name : properties.stringPropertyNames()) {
			final String value = properties.getProperty(name, EMPTY);
			namedValues.add(createNamedValue(name, value));
		}
		update.setProperty(namedValues.toArray(new NamedValue[namedValues.size()]));
		return update;
	}

	private static CMLAddAspect[] aspects(final Predicate predicate,
			final Map<String, Map<String, String>> aspectsProperties) {
		final List<CMLAddAspect> aspects = new ArrayList<CMLAddAspect>();
		for (final String name : aspectsProperties.keySet()) {
			final Map<String, String> properties = aspectsProperties.get(name);
			final CMLAddAspect aspect = aspectFrom(name, properties);
			aspect.setWhere(predicate);
			aspects.add(aspect);
		}
		return aspects.toArray(new CMLAddAspect[aspects.size()]);
	}

	private static CMLAddAspect aspectFrom(final String aspect, final Map<String, String> properties) {
		final CMLAddAspect cmlAddAspect = new CMLAddAspect();
		cmlAddAspect.setAspect(aspect);
		cmlAddAspect.setProperty(namedValuesFrom(properties));
		return cmlAddAspect;
	}

	private static NamedValue[] namedValuesFrom(final Map<String, String> properties) {
		final List<NamedValue> namedValues = Lists.newArrayList();
		for (final String name : properties.keySet()) {
			final String value = properties.get(name);
			namedValues.add(createNamedValue(name, value));
		}
		return namedValues.toArray(new NamedValue[namedValues.size()]);
	}

}
