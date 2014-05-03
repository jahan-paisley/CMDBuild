package org.cmdbuild.common.collect;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.UnmodifiableIterator;

public class Iterators {

	public static <T1, T2> UnmodifiableIterator<T2> map(final Iterator<T1> source, final Mapper<? super T1, T2> mapper) {
		checkNotNull(source);
		checkNotNull(mapper);
		return new AbstractIterator<T2>() {
			@Override
			protected T2 computeNext() {
				while (source.hasNext()) {
					final T1 element = source.next();
					return mapper.map(element);
				}
				return endOfData();
			}
		};
	}
}
