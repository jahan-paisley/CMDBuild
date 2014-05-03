package unit;

import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter;
import org.cmdbuild.dao.driver.postgres.quote.IdentQuoter;
import org.cmdbuild.dao.driver.postgres.quote.ParamAdder;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.function.DBFunction;
import org.junit.Test;

public class QuotingTest {

	static private final Long USELESS_FUNCTION_ID = null;

	@Test
	public void identStringsAreQuoted() {
		assertThat(IdentQuoter.quote("xy"), is("xy"));
		assertThat(IdentQuoter.quote("x1y"), is("\"x1y\""));
		assertThat(IdentQuoter.quote("x+y"), is("\"x+y\""));
		assertThat(IdentQuoter.quote("x'y"), is("\"x'y\""));
		assertThat(IdentQuoter.quote("x\"y"), is("\"x\"\"y\""));
		assertThat(IdentQuoter.quote("XY"), is("\"XY\""));
		assertThat(IdentQuoter.quote("X\"Y"), is("\"X\"\"Y\""));
	}

	@Test
	public void functionCallsAreQuoted() {
		final List<Object> params = new ArrayList<Object>();
		final DBFunction func = new DBFunction("func", USELESS_FUNCTION_ID, true);
		assertThat(EntryTypeQuoter.quote(call(func), new ParamAdder() {

			@Override
			public void add(final Object value) {
				params.add(value);
			}
		}), is("func()"));

		func.addInputParameter("i1", new IntegerAttributeType());
		func.addInputParameter("i2", new StringAttributeType());
		func.addInputParameter("i3", new IntegerAttributeType());
		assertThat(EntryTypeQuoter.quote(call(func, 42, "s", "24"), new ParamAdder() {

			@Override
			public void add(final Object value) {
				params.add(value);
			}
		}), is("func(?,?,?)"));
		assertThat(params.get(0), is((Object) 42));
		assertThat(params.get(1), is((Object) "s"));
		assertThat(params.get(2), is((Object) 24));
	}
}
