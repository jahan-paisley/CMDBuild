package org.cmdbuild.common.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnsupportedProxyFactory<T> {

	private static final Logger logger = LoggerFactory.getLogger(UnsupportedProxyFactory.class);

	private static Map<Class<?>, Object> cache = new WeakHashMap<Class<?>, Object>();

	private final Class<T> type;

	public UnsupportedProxyFactory(final Class<T> type) {
		this.type = type;
	}

	public T create() {
		synchronized (cache) {
			Object instance = cache.get(type);
			if (instance == null) {
				logger.trace("instance of '{}' not cached, creating new one", type);
				instance = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
						new InvocationHandler() {
							@Override
							public Object invoke(final Object proxy, final Method method, final Object[] args)
									throws Throwable {
								logger.trace("invoking '{}' on '{}'", method.getName(), type);
								throw new UnsupportedOperationException();
							}
						});
				cache.put(type, instance);
			}
			return type.cast(instance);
		}
	}

	public static <T> UnsupportedProxyFactory<T> of(final Class<T> type) {
		return new UnsupportedProxyFactory<T>(type);
	}

}
