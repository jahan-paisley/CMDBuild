package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.cmdbuild.bim.service.Deserializer;

public class BimserverDeserializer implements Deserializer {

	private final SDeserializerPluginConfiguration deserializerConfiguration;

	protected BimserverDeserializer(
			final SDeserializerPluginConfiguration suggestedDeserializerForExtension) {
		this.deserializerConfiguration = suggestedDeserializerForExtension;
	}

	@Override
	public Long getOid() {
		return deserializerConfiguration.getOid();
	}

}
