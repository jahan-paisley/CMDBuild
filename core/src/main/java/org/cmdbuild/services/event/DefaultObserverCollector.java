package org.cmdbuild.services.event;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultObserverCollector implements ObserverCollector {

	private static final Logger logger = TaskManagerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultObserverCollector.class.getName());

	private final Map<String, Observer> elementsByIdentifier;

	public DefaultObserverCollector() {
		final Map<String, Observer> notSynchronized = Maps.newHashMap();
		elementsByIdentifier = Collections.synchronizedMap(notSynchronized);
	}

	@Override
	public void add(final IdentifiableObserver element) {
		logger.info(marker, "adding element '{}'", element);
		elementsByIdentifier.put(element.getIdentifier(), element);
	}

	@Override
	public void remove(final IdentifiableObserver element) {
		logger.info(marker, "removing element '{}'", element);
		elementsByIdentifier.remove(element.getIdentifier());
	}

	@Override
	public Observer allInOneObserver() {
		logger.info(marker, "getting all-in-one '{}'", Observer.class);
		final Object proxy = Proxy.newProxyInstance( //
				DefaultObserverCollector.class.getClassLoader(), //
				new Class<?>[] { Observer.class }, //
				new InvocationHandler() {

					@Override
					public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
						/*
						 * moves elements from map to another collection so that
						 * other elements can be added/removed to the map while
						 * method has been invoked for all elements (that can be
						 * a quite expensive in terms of time compared to the
						 * simple add/remove operations)
						 */
						final Iterable<Observer> elements = Lists.newArrayList(elementsByIdentifier.values());
						for (final Observer element : elements) {
							try {
								logger.debug(marker, "invoking method '{}' on object '{}'", method, element);
								method.invoke(element, args);
							} catch (final Throwable e) {
								logger.error(marker, "error invoking method '{}' for '{}', skipping", method, element);
								logger.error(marker, "\tcaused by", e);
								throw e;
							}
						}
						return null;
					}

				});
		return Observer.class.cast(proxy);
	}

}
