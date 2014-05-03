package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SSerializerPluginConfiguration;
import org.cmdbuild.bim.service.Serializer;

public class BimserverSerializer implements Serializer {

	private final SSerializerPluginConfiguration serializerByContentType;

	protected BimserverSerializer(
			final SSerializerPluginConfiguration serializerByContentType) {
		this.serializerByContentType = serializerByContentType;
	}

	@Override
	public Long getOid() {
		return serializerByContentType.getOid();
	}

}
