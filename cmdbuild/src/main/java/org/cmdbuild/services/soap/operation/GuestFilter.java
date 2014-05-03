package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.EQUAL;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import org.cmdbuild.auth.AuthenticationStore;
import org.cmdbuild.auth.Login;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.NullOnNotFoundReadStore;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.QueryOptions.QueryOptionsBuilder;
import org.cmdbuild.model.data.Metadata;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.servlets.json.util.JsonFilterHelper;
import org.cmdbuild.servlets.json.util.JsonFilterHelper.FilterElementGetter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

class GuestFilter {

	private static final Logger logger = Log.CMDBUILD;

	private static final String METADATA_PORTLET_USER = "org.cmdbuild.portlet.user.id";
	private static final String CLASS_ATTRIBUTE_SEPARATOR = ".";

	private static final Storable METADATA_PORTLET_USER_STORABLE = new Storable() {

		@Override
		public String getIdentifier() {
			return METADATA_PORTLET_USER;
		}

	};

	private final UserType userType;
	private final Login login;
	private final CMDataView dataView;

	public GuestFilter(final AuthenticationStore authenticationStore, final CMDataView dataView) {
		this.userType = authenticationStore.getType();
		this.login = authenticationStore.getLogin();
		this.dataView = dataView;
	}

	public QueryOptions apply(final CMClass target, final QueryOptions queryOptions) {
		final QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption().clone(queryOptions);
		if (userType == UserType.APPLICATION) {
			logger.warn("cannot apply filter, user is not guest");
		} else {
			final MetadataStoreFactory metadataStoreFactory = applicationContext().getBean(MetadataStoreFactory.class);
			for (final CMAttribute attribute : target.getAttributes()) {
				logger.debug("trying filtering attribute '{}'", attribute.getName());
				attribute.getType().accept(new NullAttributeTypeVisitor() {

					private final Store<Metadata> _store = metadataStoreFactory.storeForAttribute(attribute);
					private final NullOnNotFoundReadStore<Metadata> store = NullOnNotFoundReadStore.of(_store);

					@Override
					public void visit(final ReferenceAttributeType attributeType) {
						/*
						 * absolutely ugly! QueryOptions needs to be refactored
						 * using Java objects instead of JSON
						 */
						try {
							final JSONObject original = queryOptions.getFilter();
							final JSONObject originalWithAddidion = originalWithAddition(original, attribute);
							queryOptionsBuilder.filter(originalWithAddidion);
						} catch (final Exception e) {
							logger.warn("error applying guest filter, leaving original one", e);
						}
					}

					private JSONObject originalWithAddition(final JSONObject original, final CMAttribute attribute)
							throws JSONException {
						final Entry<String, String> classAndAttribute = classAndAttributeOrNull(attribute);
						if (classAndAttribute == null) {
							return original;
						}

						final FilterElementGetter filterElementGetter = new FilterElementGetter() {

							@Override
							public boolean hasElement() {
								return true;
							}

							@Override
							public JSONObject getElement() throws JSONException {
								final JSONArray jsonValues = new JSONArray();
								jsonValues.put(idValue(classAndAttribute));

								final JSONObject jsonObject = new JSONObject();
								jsonObject.put(ATTRIBUTE_KEY, attribute.getName());
								jsonObject.put(OPERATOR_KEY, EQUAL);
								jsonObject.put(VALUE_KEY, jsonValues);

								return jsonObject;
							}

							private Long idValue(final Entry<String, String> classAndAttribute) {
								final CMClass targetClass = dataView.findClass(classAndAttribute.getKey());
								final String attributeName = classAndAttribute.getValue();
								final String attributeValue = login.getValue();
								final Long id = dataView.select(attribute(targetClass, attributeName)) //
										.from(targetClass) //
										.where(condition( //
												attribute(targetClass, attributeName), //
												eq(attributeValue))) //
										.run() //
										.getOnlyRow() //
										.getCard(targetClass) //
										.getId();
								return id;
							}

						};
						return new JsonFilterHelper(original).merge(filterElementGetter);
					}

					private Entry<String, String> classAndAttributeOrNull(final CMAttribute attribute) {
						logger.debug("parsing metadata  for attribute '{}'", attribute.getName());
						final Metadata metadata = store.read(METADATA_PORTLET_USER_STORABLE);
						return classAndAttributeOrNull(metadata);
					}

					private Entry<String, String> classAndAttributeOrNull(final Metadata metadata) {
						logger.debug("parsing metadata '{}'", metadata);
						return (metadata == null) ? null : classAndAttributeOrNull(metadata.value);
					}

					private Entry<String, String> classAndAttributeOrNull(final String metadataValue) {
						logger.debug("parsing metadata value '{}'", metadataValue);
						final Entry<String, String> entry;
						if (isNotBlank(metadataValue) && metadataValue.contains(CLASS_ATTRIBUTE_SEPARATOR)) {
							final String[] tokens = metadataValue.split(quote(CLASS_ATTRIBUTE_SEPARATOR));
							entry = new SimpleImmutableEntry<String, String>(tokens[0], tokens[1]);
							logger.debug(format("extracted attribute name is '%s'", entry));
						} else {
							logger.debug(format("cannot extract attribute name from '%s'", metadataValue));
							entry = null;
						}
						return entry;
					}

				});
			}
		}
		return queryOptionsBuilder.build();
	}

}
