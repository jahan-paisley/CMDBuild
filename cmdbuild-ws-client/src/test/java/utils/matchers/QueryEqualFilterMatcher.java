package utils.matchers;

import java.util.List;

import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.Query;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class QueryEqualFilterMatcher extends TypeSafeMatcher<List<Query>> {

	private final String name;
	private final String value;

	public QueryEqualFilterMatcher(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	public void describeTo(final Description description) {
		description //
				.appendText(" contains an EQUALS filter with name ") //
				.appendValue(name) //
				.appendText(" and value ") //
				.appendValue(value);
	}

	@Override
	public boolean matchesSafely(final List<Query> queries) {
		for (final Query query : queries) {
			final Filter filter = query.getFilter();
			final boolean matchesName = name.equals(filter.getName());
			final boolean matchesValue = (value == null) ? true : value.equals(filter.getValue().get(0));
			if (matchesName && matchesValue) {
				return true;
			}
		}
		return false;
	}

	public static Matcher<List<Query>> containsFilter(final String name) {
		return new QueryEqualFilterMatcher(name, null);
	}

	public static Matcher<List<Query>> containsFilter(final String name, final String value) {
		return new QueryEqualFilterMatcher(name, value);
	}

}
