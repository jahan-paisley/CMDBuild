package org.cmdbuild.services.setup;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.config.DefaultProperties;
import org.cmdbuild.logic.setup.SetupLogic.Module;
import org.cmdbuild.logic.setup.SetupLogic.ModulesHandler;
import org.cmdbuild.services.Settings;

import com.google.common.collect.Maps;

public class PropertiesModulesHandler implements ModulesHandler {

	private static class PropertiesModule implements Module {

		private final String name;

		public PropertiesModule(final String name) {
			this.name = name;
		}

		@Override
		public Map<String, String> retrieve() throws Exception {
			final Map<String, String> data = Maps.newHashMap();
			final DefaultProperties module = Settings.getInstance().getModule(name);
			for (final Entry<Object, Object> entry : module.entrySet()) {
				final String key = String.class.cast(entry.getKey());
				data.put(key, entry.getValue().toString());
			}
			return data;
		}

		@Override
		public void store(final Map<String, String> values) throws Exception {
			final DefaultProperties properties = Settings.getInstance().getModule(name);
			for (final Object keyObject : properties.keySet()) {
				final String key = keyObject.toString();
				if (values.containsKey(key)) {
					final String value = values.get(key);
					properties.setProperty(key, defaultString(value));
				}
			}
			properties.store();
		}

	}

	@Override
	public Module get(final String name) {
		return new PropertiesModule(name);
	}

}
