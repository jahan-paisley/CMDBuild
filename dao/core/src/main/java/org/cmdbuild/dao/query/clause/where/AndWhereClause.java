package org.cmdbuild.dao.query.clause.where;

import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Arrays.asList;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class AndWhereClause extends CompositeWhereClause {

	private AndWhereClause(final List<? extends WhereClause> clauses) {
		super(clauses);
	}

	@Override
	public void accept(final WhereClauseVisitor visitor) {
		visitor.visit(this);

	}

	public static WhereClause and(final WhereClause first, final WhereClause second, final WhereClause... others) {
		final List<WhereClause> clauses = Lists.newArrayList();
		clauses.add(first);
		clauses.add(second);
		clauses.addAll(asList(others));
		return and(clauses);
	}

	/**
	 * Creates a new {@link AndWhereClause} from the specified
	 * {@link WhereClause}s.<br>
	 * Clause
	 * 
	 * The following considerations are performed:<br>
	 * <ul>
	 * <li>0 where clauses - throws exception</li>
	 * <li>1 where clause - clause</li>
	 * <li>2 or more where clauses - (clause1 AND clause2 AND ...)</li>
	 * </ul>
	 * 
	 * @param whereClauses
	 * 
	 * @return a newly created {@link AndWhereClause}.
	 * 
	 * @throws IllegalArgumentException
	 *             if there are no where clauses.
	 */
	public static WhereClause and(final Iterable<? extends WhereClause> whereClauses) {
		final WhereClause whereClause;
		final Iterator<? extends WhereClause> iterator = filterTrueAndFalseWhereClauses(whereClauses).iterator();
		if (iterator.hasNext()) {
			final WhereClause firstWhereClause = iterator.next();
			if (iterator.hasNext()) {
				final WhereClause secondWhereClause = iterator.next();
				final List<WhereClause> clauses = Lists.newArrayList(firstWhereClause, secondWhereClause);
				while (iterator.hasNext()) {
					clauses.add(iterator.next());
				}
				whereClause = new AndWhereClause(clauses);
			} else {
				whereClause = firstWhereClause;
			}
		} else {
			throw new IllegalArgumentException("there must be at least one where clause");
		}
		return whereClause;
	}

	private static Iterable<WhereClause> filterTrueAndFalseWhereClauses(
			final Iterable<? extends WhereClause> whereClauses) {
		final List<WhereClause> filteredWhereClauses = Lists.newArrayList();
		for (final WhereClause whereClause : whereClauses) {
			if (whereClause instanceof FalseWhereClause) {
				filteredWhereClauses.clear();
				filteredWhereClauses.add(whereClause);
				break;
			} else if (whereClause instanceof TrueWhereClause) {
				continue;
			}
			filteredWhereClauses.add(whereClause);
		}
		/*
		 * if starting collection was populated and filtered collection no,
		 * probably all TrueWhereClauses have been removed, so we must add one
		 * TrueWhereClause
		 */
		if (!isEmpty(whereClauses) && isEmpty(filteredWhereClauses)) {
			filteredWhereClauses.add(trueWhereClause());
		}
		return filteredWhereClauses;
	}

}
