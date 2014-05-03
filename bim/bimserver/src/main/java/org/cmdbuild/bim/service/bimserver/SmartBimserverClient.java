package org.cmdbuild.bim.service.bimserver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SmartBimserverClient extends ForwardingBimServerClient {

	private final BimserverClient delegate;

	public SmartBimserverClient(final BimserverClient delegate) {
		super(proxy(delegate)); // the superclass forwards to the Proxy
		this.delegate = delegate; // this class forwards to the DefaultClient
	}

	private static BimserverClient proxy(final BimserverClient delegate) {
		final BimserverClient proxy = BimserverClient.class.cast(Proxy.newProxyInstance( //
				SmartBimserverClient.class.getClassLoader(), //
				new Class[] { BimserverClient.class }, //
				new InvocationHandler() {

					@Override
					public Object invoke(final Object proxy, final Method method, final Object[] params)
							throws Throwable {
						if (!delegate.isConnected()) {
							delegate.connect();
						}
						if (!delegate.isConnected()) {
							throw new IllegalStateException("connection not estabilished");
						}
						return method.invoke(delegate, params);
					}

				}));
		return proxy;
	}

	@Override
	public void connect() {
		delegate.connect();
	}

	@Override
	public void disconnect() {
		delegate.disconnect();
	}

	@Override
	public boolean isConnected() {
		return delegate.isConnected();
	}
}
