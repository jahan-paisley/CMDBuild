package unit;

import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static util.Utils.clean;

import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.QuerySpecsImpl;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.from.FunctionFromClause;
import org.junit.Test;

public class SimpleFunctionQueryCreatorTest {

	static private final Long USELESS_FUNCTION_ID = null;

	private static class IdentityAttributeType implements CMAttributeType<Object> {

		@Override
		public Object convertValue(final Object value) {
			return value;
		}

		@Override
		public void accept(final CMAttributeTypeVisitor visitor) {
			throw new UnsupportedOperationException();
		}

	}

	DBFunction setFunc = new DBFunction("func", USELESS_FUNCTION_ID, true);
	Alias f = NameAlias.as("f");

	@Test
	public void withAttributeListAndNoParameters() {
		setFunc.addOutputParameter("o1", UndefinedAttributeType.undefined());
		setFunc.addOutputParameter("o2", UndefinedAttributeType.undefined());
		final QuerySpecsImpl querySpecs = QuerySpecsImpl.newInstance() //
				.fromClause(new FunctionFromClause(call(setFunc), f)) //
				.distinct(false) //
				.build();
		querySpecs.addSelectAttribute(attribute(f, "o2"));
		querySpecs.addSelectAttribute(attribute(f, "o1"));

		final String sql = new QueryCreator(querySpecs).getQuery();
		assertThat(clean(sql), containsString("SELECT f.\"o2\" AS \"f#o2\", f.\"o1\" AS \"f#o1\" FROM func() AS f"));
	}

	@Test
	public void withAttributeListAndParameters() {
		setFunc.addInputParameter("i1", new IdentityAttributeType());
		setFunc.addInputParameter("i2", new IdentityAttributeType());
		setFunc.addInputParameter("i3", new IdentityAttributeType());
		setFunc.addOutputParameter("o", new IdentityAttributeType());
		final QuerySpecsImpl querySpecs = QuerySpecsImpl.newInstance() //
				.fromClause(new FunctionFromClause(call(setFunc, "12", 34, null), f)) //
				.distinct(false) //
				.build();
		querySpecs.addSelectAttribute(attribute(f, "o"));

		final QueryCreator queryCreator = new QueryCreator(querySpecs);
		assertThat(clean(queryCreator.getQuery()), containsString("SELECT f.o AS \"f#o\" FROM func(?,?,?) AS f"));
		assertThat(queryCreator.getParams()[0], is((Object) "12"));
		assertThat(queryCreator.getParams()[1], is((Object) 34));
		assertThat(queryCreator.getParams()[2], is(nullValue()));
	}

}
