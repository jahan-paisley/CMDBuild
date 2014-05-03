package org.cmdbuild.services;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.logger.Log;
import org.cmdbuild.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class TranslationService {

	private static TranslationService instance;

	private Map<String, JSONObject> map;

	private TranslationService() {
		init();
	}

	public void reload() {
		init();
	}

	private void init() {
		map = new HashMap<String, JSONObject>();
	}

	public static TranslationService getInstance() {
		if (instance == null)
			instance = new TranslationService();

		return instance;
	}

	public void loadTraslation(String lang) {
		String path = Settings.getInstance().getRootPath();
		String file = path + "translations" + File.separator + lang + ".json";
		try {
			JSONObject tr = new JSONObject(FileUtils.getContents(file));
			map.put(lang, tr);
		} catch (JSONException ex) {
			Log.CMDBUILD.error("Can't read translation", ex);
			map.put(lang, new JSONObject());
		}
	}

	public Map<String, String> getTranslationList() {
		String path = Settings.getInstance().getRootPath();
		File dir = new File(path + "translations");

		File[] files = dir.listFiles();
		Map<String, String> list = new Hashtable<String, String>();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isFile()) {
				int pos = f.getName().indexOf(".json");
				String lang = f.getName().substring(0, pos);
				String description = getTranslation(lang, "description");
				list.put(lang, description);
			}
		}
		return list;
	}

	public JSONObject getTranslationObject(String lang) {
		if (!map.containsKey(lang))
			loadTraslation(lang);
		return map.get(lang);
	}

	public String getTranslation(String key) {
		final LanguageStore languageStore = applicationContext().getBean(LanguageStore.class);
		return getTranslation(languageStore.getLanguage(), key);
	}

	public String getTranslation(String lang, String key) {
		try {
			JSONObject json = getTranslationObject(lang);
			StringTokenizer tokenizer = new StringTokenizer(key, ".");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (tokenizer.hasMoreTokens()) {
					json = json.getJSONObject(token);
				} else {
					return json.getString(token);
				}
			}
		} catch (Exception e) {
			Log.CMDBUILD.debug("Error translating: " + key, e);
		}
		// translation not found
		return "[" + lang + "." + key + "]";
	}
}
