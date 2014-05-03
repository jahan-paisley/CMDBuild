package org.cmdbuild.workflow.service;

import java.lang.reflect.Array;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.encoding.TypeMapping;

import org.apache.axis.client.Stub;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.cmdbuild.workflow.service.RemoteSharkServiceConfiguration.ChangeListener;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;

/**
 * Implementation using remote Shark server
 */
public class RemoteSharkService extends TransactedSharkService implements ChangeListener {

	private static final String CMDBUILD_TYPE_NS = "http://type.workflow.cmdbuild.org";

	/**
	 * Only God knows why it is different.
	 */
	private static final String CMDBUILD_EJB_NS = "http://ebj.workflow.cmdbuild.org";

	private static final String ARRAY_NAME_PREFIX = "ArrayOf_tns1_";

	private final RemoteSharkServiceConfiguration config;

	public RemoteSharkService(final RemoteSharkServiceConfiguration configuration) {
		super(getClientProperties(configuration));
		this.config = configuration;
		this.config.addListener(this);
	}

	@Override
	public void configurationChanged() {
		reconfigure(getClientProperties(config));
	}

	private static Properties getClientProperties(final RemoteSharkServiceConfiguration config) {
		final Properties clientProps = new Properties();
		clientProps.put("ClientType", "WS");
		clientProps.put("SharkWSURLPrefix", config.getServerUrl());
		return clientProps;
	}

	@Override
	protected WMConnectInfo getConnectionInfo() {
		return new WMConnectInfo(config.getUsername(), config.getPassword(), DEFAULT_ENGINE_NAME, DEFAULT_SCOPE);
	}

	/**
	 * The Axis client needs to register our custom types. It would have been so
	 * easy without them!
	 */
	@Override
	protected void configureWAPI(final WAPI wapi) {
		if (wapi instanceof Stub) {
			final Stub axisClientStub = (Stub) wapi;
			registerCustomTypes(axisClientStub);
		}
	}

	private void registerCustomTypes(final Stub axisClientStub) {
		final Service rpcService = axisClientStub._getService();
		final TypeMapping tm = rpcService.getTypeMappingRegistry().getTypeMapping(
				org.apache.axis.Constants.URI_SOAP11_ENC);

		// TODO register if needed, and take care of concurrency
		registerType(tm, org.cmdbuild.workflow.type.LookupType.class);
		registerType(tm, org.cmdbuild.workflow.type.ReferenceType.class);
	}

	/**
	 * Registers a custom type and the array representation.
	 * 
	 * @param tm
	 *            type mapping
	 * @param javaType
	 *            java class to be registered
	 */
	private void registerType(final TypeMapping tm, final Class<?> javaType) {
		final QName typeQname = new QName(CMDBUILD_TYPE_NS, javaType.getSimpleName());
		tm.register(javaType, typeQname, new BeanSerializerFactory(javaType, typeQname), new BeanDeserializerFactory(
				javaType, typeQname));

		final Class<?> javaArrayType = Array.newInstance(javaType, 0).getClass();
		final QName arrayQname = new QName(CMDBUILD_EJB_NS, ARRAY_NAME_PREFIX + javaType.getSimpleName());
		tm.register(javaArrayType, arrayQname, new ArraySerializerFactory(typeQname, null), // why
																							// null?!
				new ArrayDeserializerFactory());
	}
}
