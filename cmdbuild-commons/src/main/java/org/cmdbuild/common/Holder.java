package org.cmdbuild.common;

import com.google.common.base.Supplier;

/**
 * @deprecated use {@link Supplier}.
 */
@Deprecated
public interface Holder<T> {

	T get();

}
