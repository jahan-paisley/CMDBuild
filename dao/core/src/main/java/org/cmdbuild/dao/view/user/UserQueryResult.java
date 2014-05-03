package org.cmdbuild.dao.view.user;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterators.toArray;
import static java.util.Arrays.asList;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.jcip.annotations.NotThreadSafe;

import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/*
 * Mutable classes used by the driver implementations
 */
@NotThreadSafe
public class UserQueryResult implements CMQueryResult {

	private static Predicate<UserQueryRow> NOT_NULL = new Predicate<UserQueryRow>() {
		@Override
		public boolean apply(final UserQueryRow input) {
			return input != null;
		}
	};

	private static Function<UserQueryRow, CMQueryRow> TO_PARENT_TYPE = new Function<UserQueryRow, CMQueryRow>() {
		@Override
		public CMQueryRow apply(final UserQueryRow input) {
			return CMQueryRow.class.cast(input);
		}
	};

	private final UserDataView view;
	private final CMQueryResult inner;

	static UserQueryResult newInstance(final UserDataView view, final CMQueryResult inner) {
		return new UserQueryResult(view, inner);
	}

	private UserQueryResult(final UserDataView view, final CMQueryResult inner) {
		this.view = view;
		this.inner = inner;
	}

	@Override
	public Iterator<CMQueryRow> iterator() {
		final Iterable<CMQueryRow> rows = asList(toArray(inner.iterator(), CMQueryRow.class));
		return from(rows) //
				.transform(toUserQueryRow()) //
				.filter(NOT_NULL) //
				.transform(TO_PARENT_TYPE) //
				.iterator();
	}

	private Function<CMQueryRow, UserQueryRow> toUserQueryRow() {
		return new Function<CMQueryRow, UserQueryRow>() {
			@Override
			public UserQueryRow apply(final CMQueryRow input) {
				return UserQueryRow.newInstance(view, input);
			}
		};
	}

	@Override
	public int size() {
		return inner.size();
	}

	@Override
	public boolean isEmpty() {
		return inner.isEmpty();
	}

	@Override
	public int totalSize() {
		return inner.totalSize();
	}

	@Override
	public CMQueryRow getOnlyRow() throws NoSuchElementException {
		return UserQueryRow.newInstance(view, inner.getOnlyRow());
	}

}
