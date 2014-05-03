package org.cmdbuild.common.collect;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;

import java.util.Iterator;

import com.google.common.base.Predicate;

public class Iterables {

	/**
	 * Returns the elements of {@code source} mapped to a new type. The
	 * resulting iterable's iterator does not support {@code remove()}.
	 */
	public static <T1, T2> Iterable<T2> map(final Iterable<T1> source, final Mapper<? super T1, T2> mapper) {
		checkNotNull(source);
		checkNotNull(mapper);
		return new Iterable<T2>() {
			@Override
			public Iterator<T2> iterator() {
				return Iterators.map(source.iterator(), mapper);
			}
		};
	}

	public static <T> Iterable<T> filterNotNull(final Iterable<T> unfiltered) {
		return filter(unfiltered, new Predicate<T>() {
			@Override
			public boolean apply(final T input) {
				return (input != null);
			}
		});
	}
}
