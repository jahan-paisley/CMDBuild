package org.cmdbuild.servlets.json;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils extends JSONBaseWithSpringContext {

	@JSONExported
	@Unauthorized
	public String getTranslationObject() {
		final String lang = languageStore().getLanguage();
		final String transFile = TranslationService.getInstance().getTranslationObject(lang).toString();
		return "CMDBuild.Translation = " + transFile;
	}

	@JSONExported
	@Unauthorized
	public JSONObject listAvailableTranslations(final JSONObject serializer) throws JSONException {

		final Map<String, String> trs = TranslationService.getInstance().getTranslationList();

		for (final String lang : trs.keySet()) {
			final JSONObject j = new JSONObject();
			j.put("name", lang);
			j.put("value", trs.get(lang));
			serializer.append("translations", j);
		}

		return serializer;
	}

	@JSONExported
	@Unauthorized
	public void success() throws JSONException {
	}

	/**
	 * @param exceptionType
	 * @param exceptionCodeString
	 */
	@JSONExported
	@Unauthorized
	public void failure(@Parameter("type") final String exceptionType,
			@Parameter(value = "code", required = false) final String exceptionCodeString) {
		try {
			final Class<? extends CMDBException> classDefinition = Class.forName(
					"org.cmdbuild.exception." + exceptionType).asSubclass(CMDBException.class);
			if (exceptionCodeString == null) {
				final Constructor<? extends CMDBException> constructorDefinition = classDefinition
						.getDeclaredConstructor();
				throw constructorDefinition.newInstance();
			} else {
				for (final Class<?> subClass : classDefinition.getClasses()) {
					if (subClass.isEnum()) {
						for (final Object enumConst : subClass.getEnumConstants()) {
							if (exceptionCodeString.equals(enumConst.toString())) {
								final Constructor<? extends CMDBException> constructorDefinition = classDefinition
										.getDeclaredConstructor(enumConst.getClass());
								throw constructorDefinition.newInstance(enumConst);
							}
						}
					}
				}
			}
		} catch (final CMDBException ex) {
			throw ex;
		} catch (final Exception ex) {
			// Returns success if no error can be instantiated
		}
	}

	@JSONExported
	@Admin
	public void clearCache() {
		cachingLogic().clearCache();
	}

}
