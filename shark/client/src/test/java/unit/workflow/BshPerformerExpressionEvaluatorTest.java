package unit.workflow;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.cmdbuild.workflow.BshActivityPerformerExpressionEvaluator;
import org.junit.Test;

public class BshPerformerExpressionEvaluatorTest {

	private static final String BLANK = " \t";

	@Test
	public void nullEmptyOrBlankExpressionsReturnsNoValues() {
		assertTrue(namesFor(null).isEmpty());
		assertTrue(namesFor(EMPTY).isEmpty());
		assertTrue(namesFor(BLANK).isEmpty());
	}

	@Test
	public void expressionWithoutDelimitersMeansSingleValue() {
		final String s = "foo bar baz";
		assertThat(namesFor(expression(s)).size(), equalTo(1));
		assertThat(namesFor(expression(s)), hasItems(s));
	}

	@Test
	public void expressionWithSingleOrMultipleDelimitersReturnsNoValues() {
		assertTrue(namesFor(expression(",")).isEmpty());
		assertTrue(namesFor(expression("  , ,,  ,\t,,   ")).isEmpty());
	}

	@Test
	public void delimitedValuesAreAlsoTrimmed() {
		final String s = "\t foo, bar    ,baz  ";
		assertThat(namesFor(expression(s)).size(), equalTo(3));
		assertThat(namesFor(expression(s)), hasItems("foo", "bar", "baz"));
	}

	/*
	 * Utils
	 */

	private String expression(final String s) {
		return format("\"%s\"", s);
	}

	private Set<String> namesFor(final String expression) {
		return new BshActivityPerformerExpressionEvaluator(expression).getNames();
	}

}
