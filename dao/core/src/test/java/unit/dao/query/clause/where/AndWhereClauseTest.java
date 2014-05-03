package unit.dao.query.clause.where;

import static org.cmdbuild.dao.query.clause.where.FalseWhereClause.falseWhereClause;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;

import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.FalseWhereClause;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.junit.Test;

public class AndWhereClauseTest {

	private static final Iterable<? extends WhereClause> NO_WHERE_CLAUSES = Collections.emptyList();

	private Iterable<? extends WhereClause> only(final WhereClause whereClause) {
		return Arrays.asList(whereClause);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noWhereClausesThrowsException() {
		// when
		AndWhereClause.and(NO_WHERE_CLAUSES);
	}

	@Test
	public void oneWhereClauseReturnsTheSame() {
		// given
		final WhereClause testWhereClause = mock(WhereClause.class);

		// when
		final WhereClause whereClause = AndWhereClause.and(only(testWhereClause));

		// then
		assertThat(whereClause, is(testWhereClause));
	}

	@Test
	public void threeWhereClausesAreCombined() {
		// given
		final WhereClause testWhereClause1 = mock(WhereClause.class);
		final WhereClause testWhereClause2 = mock(WhereClause.class);
		final WhereClause testWhereClause3 = mock(WhereClause.class);

		// when
		final WhereClause whereClause = AndWhereClause.and(testWhereClause1, testWhereClause2, testWhereClause3);

		// then
		assertThat(whereClause, instanceOf(AndWhereClause.class));
		final AndWhereClause andWhereClause = AndWhereClause.class.cast(whereClause);
		assertThat(andWhereClause.getClauses(), hasSize(3));
		assertThat(andWhereClause.getClauses().get(0), is(testWhereClause1));
		assertThat(andWhereClause.getClauses().get(1), is(testWhereClause2));
		assertThat(andWhereClause.getClauses().get(2), is(testWhereClause3));
	}

	@Test
	public void trueWhereClausesAreRemoved() {
		// given
		final WhereClause testWhereClause1 = mock(WhereClause.class);
		final WhereClause testWhereClause2 = mock(WhereClause.class);

		// when
		final WhereClause whereClause = AndWhereClause.and(trueWhereClause(), testWhereClause1, trueWhereClause(),
				testWhereClause2, trueWhereClause());

		// then
		assertThat(whereClause, instanceOf(AndWhereClause.class));
		final AndWhereClause andWhereClause = AndWhereClause.class.cast(whereClause);
		assertThat(andWhereClause.getClauses(), hasSize(2));
		assertThat(andWhereClause.getClauses().get(0), is(testWhereClause1));
		assertThat(andWhereClause.getClauses().get(1), is(testWhereClause2));
	}

	@Test
	public void onlyTrueWhereClausesOneOnlyIsReturned() {
		// when
		final WhereClause whereClause = AndWhereClause.and(trueWhereClause(), trueWhereClause(), trueWhereClause());

		// then
		assertThat(whereClause, instanceOf(TrueWhereClause.class));
	}

	@Test
	public void anyFalseWhereClausesReturnsFalse() {
		// given
		final WhereClause testWhereClause = mock(WhereClause.class);

		// when
		final WhereClause whereClause = AndWhereClause.and(trueWhereClause(), testWhereClause, falseWhereClause());

		// then
		assertThat(whereClause, instanceOf(FalseWhereClause.class));
	}

}
