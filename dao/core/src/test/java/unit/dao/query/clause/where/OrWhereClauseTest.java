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

import org.cmdbuild.dao.query.clause.where.FalseWhereClause;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.junit.Test;

public class OrWhereClauseTest {

	private static final Iterable<? extends WhereClause> NO_WHERE_CLAUSES = Collections.emptyList();

	private Iterable<? extends WhereClause> only(final WhereClause whereClause) {
		return Arrays.asList(whereClause);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noWhereClausesThrowsException() {
		// when
		OrWhereClause.or(NO_WHERE_CLAUSES);
	}

	@Test
	public void oneWhereClauseReturnsTheSame() {
		// given
		final WhereClause testWhereClause = mock(WhereClause.class);

		// when
		final WhereClause whereClause = OrWhereClause.or(only(testWhereClause));

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
		final WhereClause whereClause = OrWhereClause.or(testWhereClause1, testWhereClause2, testWhereClause3);

		// then
		assertThat(whereClause, instanceOf(OrWhereClause.class));
		final OrWhereClause orWhereClause = OrWhereClause.class.cast(whereClause);
		assertThat(orWhereClause.getClauses(), hasSize(3));
		assertThat(orWhereClause.getClauses().get(0), is(testWhereClause1));
		assertThat(orWhereClause.getClauses().get(1), is(testWhereClause2));
		assertThat(orWhereClause.getClauses().get(2), is(testWhereClause3));
	}

	@Test
	public void falseWhereClausesAreRemoved() {
		// given
		final WhereClause testWhereClause1 = mock(WhereClause.class);
		final WhereClause testWhereClause2 = mock(WhereClause.class);

		// when
		final WhereClause whereClause = OrWhereClause.or(falseWhereClause(), testWhereClause1, falseWhereClause(),
				testWhereClause2, falseWhereClause());

		// then
		assertThat(whereClause, instanceOf(OrWhereClause.class));
		final OrWhereClause orWhereClause = OrWhereClause.class.cast(whereClause);
		assertThat(orWhereClause.getClauses(), hasSize(2));
		assertThat(orWhereClause.getClauses().get(0), is(testWhereClause1));
		assertThat(orWhereClause.getClauses().get(1), is(testWhereClause2));
	}

	@Test
	public void onlyFalseWhereClausesOneOnlyIsReturned() {
		// when
		final WhereClause whereClause = OrWhereClause.or(falseWhereClause(), falseWhereClause(), falseWhereClause());

		// then
		assertThat(whereClause, instanceOf(FalseWhereClause.class));
	}

	@Test
	public void anyTrueWhereClausesReturnsTrue() {
		// given
		final WhereClause testWhereClause = mock(WhereClause.class);

		// when
		final WhereClause whereClause = OrWhereClause.or(trueWhereClause(), testWhereClause, falseWhereClause());

		// then
		assertThat(whereClause, instanceOf(TrueWhereClause.class));
	}

}
