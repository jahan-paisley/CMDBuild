package org.cmdbuild.dao.view.user;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EmptyArrayOperatorAndValue.emptyArray;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.StringArrayOverlapOperatorAndValue.stringArrayOverlap;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.NullEntryTypeVisitor;
import org.cmdbuild.dao.query.ForwardingQuerySpecs;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.FunctionWhereClause;
import org.cmdbuild.dao.query.clause.where.NullWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * {@link QuerySpecs} with user-specific behavior.
 */
public class UserQuerySpecs extends ForwardingQuerySpecs {

	static UserQuerySpecs newInstance(final UserDataView dataView, final QuerySpecs querySpecs,
			final OperationUser operationUser, final RowAndColumnPrivilegeFetcher rowAndColumnPrivilegeFetcher) {
		return new UserQuerySpecs(dataView, querySpecs, operationUser, rowAndColumnPrivilegeFetcher);
	}

	private static final String GROUPS_SEPARATOR = ",";
	private static final String ID = Constants.ID_ATTRIBUTE;
	private static final String ID_CLASS = Constants.CLASS_ID_ATTRIBUTE;
	private static final String PREV_EXECUTORS = "PrevExecutors";

	private final UserDataView dataView;
	private final QuerySpecs delegate;
	private final OperationUser operationUser;
	private final RowAndColumnPrivilegeFetcher rowAndColumnPrivilegeFetcher;

	private final Set<DirectJoinClause> directJoins;
	private final WhereClause userWhereClause;

	private UserQuerySpecs(final UserDataView dataView, final QuerySpecs delegate, final OperationUser operationUser,
			final RowAndColumnPrivilegeFetcher rowAndColumnPrivilegeFetcher) {
		super(delegate);
		this.dataView = dataView;
		this.delegate = delegate;
		this.operationUser = operationUser;
		this.rowAndColumnPrivilegeFetcher = rowAndColumnPrivilegeFetcher;

		userWhereClause = whereClauseForUser();
		directJoins = directJoinClausesForUser(userWhereClause);
	}

	@Override
	public FromClause getFromClause() {
		return dataView.proxy(delegate.getFromClause());
	}

	private WhereClause whereClauseForUser() {
		final WhereClause userWhereClause;
		final CMEntryType fromType = delegate.getFromClause().getType();
		if (fromType instanceof CMClass) {
			final CMClass type = CMClass.class.cast(fromType);
			final WhereClause superClassesWhereClause = safeFilterForSuperclassesOf(type);
			final WhereClause currentClassAndChildrenWhereClause = safeFilterFor(type, type);
			final WhereClause prevExecutorsWhereClause = prevExecutorsFilter(type);
			userWhereClause = and( //
					delegate.getWhereClause(), //
					prevExecutorsWhereClause, //
					currentClassAndChildrenWhereClause, //
					superClassesWhereClause //
			);
		} else {
			userWhereClause = delegate.getWhereClause();
		}
		return userWhereClause;
	}

	/**
	 * As {@link UserQuerySpecs.filterForSuperclassesOf} but returns a
	 * {@link TrueWhereClause} if an error occurs.
	 */
	private WhereClause safeFilterForSuperclassesOf(final CMClass type) {
		try {
			return filterForSuperclassesOf(type);
		} catch (final Exception e) {
			return trueWhereClause();
		}
	}

	/**
	 * Returns the global {@link WhereClause} for the super-classes of the
	 * specified {@link CMClass}.
	 * 
	 * @param type
	 * 
	 * @return the global {@link WhereClause} for the specified {@link CMClass}
	 *         or {@link TrueWhereClause} if there is no filter available.
	 */
	private WhereClause filterForSuperclassesOf(final CMClass type) {
		final List<WhereClause> superClassesWhereClauses = Lists.newArrayList();
		for (CMClass parentType = type.getParent(); parentType != null; parentType = parentType.getParent()) {
			final Iterable<? extends WhereClause> privilegeWhereClause = rowAndColumnPrivilegeFetcher
					.fetchPrivilegeFiltersFor(parentType, type);
			if (!isEmpty(privilegeWhereClause)) {
				superClassesWhereClauses.add(or(privilegeWhereClause));
			}
		}
		return isEmpty(superClassesWhereClauses) ? trueWhereClause() : and(superClassesWhereClauses);
	}

	/**
	 * As {@link UserQuerySpecs.filterFor} but returns a {@code TrueWhereClause}
	 * if an error occurs.
	 */
	private WhereClause safeFilterFor(final CMClass root, final CMClass type) {
		try {
			final WhereClause filter = filterFor(root, type);
			return (filter == null) ? trueWhereClause() : filter;
		} catch (final Exception e) {
			return trueWhereClause();
		}
	}

	/**
	 * Returns the global {@link WhereClause} for the specified {@link CMClass}
	 * including sub-classes.
	 * 
	 * @param root
	 * @param type
	 * 
	 * @return the global {@link WhereClause} for the specified {@link CMClass}
	 *         or {@code null} if the filter is not available.
	 */
	private WhereClause filterFor(final CMClass root, final CMClass type) {
		final Iterable<? extends WhereClause> currentWhereClauses = rowAndColumnPrivilegeFetcher
				.fetchPrivilegeFiltersFor(type);
		final List<WhereClause> childrenWhereClauses = Lists.newArrayList();
		final List<Long> childrenWithNoFilter = Lists.newArrayList();
		for (final CMClass child : type.getChildren()) {
			final WhereClause childWhereClause = filterFor(root, child);
			if (childWhereClause != null) {
				childrenWhereClauses.add(childWhereClause);
			} else {
				childrenWithNoFilter.add(child.getId());
			}
		}
		if (!childrenWithNoFilter.isEmpty()) {
			childrenWhereClauses.add(condition(attribute(root, ID_CLASS), in(childrenWithNoFilter.toArray())));
		}
		final WhereClause whereClause;
		if (isEmpty(currentWhereClauses) && isEmpty(childrenWhereClauses)) {
			whereClause = null;
		} else {
			whereClause = and( //
					isEmpty(currentWhereClauses) ? trueWhereClause() : or(currentWhereClauses), //
					isEmpty(childrenWhereClauses) ? trueWhereClause() : or(childrenWhereClauses) //
			);
		}
		return whereClause;
	}

	/**
	 * Return a where clause to filter the processes: if there is a default
	 * group check that the PrevExecutors is one of the user groups. Otherwise
	 * check for the logged group only
	 * 
	 * @param type
	 * @return
	 */
	private WhereClause prevExecutorsFilter(final CMClass type) {
		final WhereClause whereClause;
		final CMAttribute prevExecutors = type.getAttribute(PREV_EXECUTORS);
		if (operationUser.hasAdministratorPrivileges()) {
			whereClause = trueWhereClause();
		} else if (prevExecutors != null) {
			final String defaultGroupName = operationUser.getAuthenticatedUser().getDefaultGroupName();
			String userGroupsJoined = EMPTY;
			if (isEmpty(defaultGroupName)) {
				userGroupsJoined = operationUser.getPreferredGroup().getName();
			} else {
				userGroupsJoined = on(GROUPS_SEPARATOR).join( //
						operationUser.getAuthenticatedUser().getGroupNames() //
						);
			}

			whereClause = or( //
					condition(attribute(type, prevExecutors), stringArrayOverlap(userGroupsJoined)), //
					/*
					 * the or with empty array is necessary because after the
					 * creation of the the process card (before to say to shark
					 * to advance it) the PrevExecutors is empty
					 */
					condition(attribute(type, prevExecutors), emptyArray()) //
			);
		} else {
			whereClause = trueWhereClause();
		}

		return whereClause;
	}

	private Set<DirectJoinClause> directJoinClausesForUser(final WhereClause userWhereClause) {
		final Set<DirectJoinClause> directJoins = Sets.newHashSet(delegate.getDirectJoins());
		final Map<Alias, CMClass> descendantsByAlias = Maps.newHashMap();
		delegate.getFromClause().getType().accept(new NullEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				for (final CMClass descendant : type.getDescendants()) {
					final Alias alias = EntryTypeAlias.canonicalAlias(descendant);
					descendantsByAlias.put(alias, descendant);
				}
			}

		});
		userWhereClause.accept(new NullWhereClauseVisitor() {

			@Override
			public void visit(final AndWhereClause whereClause) {
				for (final WhereClause subWhereClause : whereClause.getClauses()) {
					subWhereClause.accept(this);
				}
			}

			@Override
			public void visit(final OrWhereClause whereClause) {
				for (final WhereClause subWhereClause : whereClause.getClauses()) {
					subWhereClause.accept(this);
				}
			}

			@Override
			public void visit(final SimpleWhereClause whereClause) {
				final QueryAliasAttribute attribute = whereClause.getAttribute();
				add(directJoins, descendantsByAlias, attribute);
			}

			@Override
			public void visit(final FunctionWhereClause whereClause) {
				final QueryAliasAttribute attribute = whereClause.attribute;
				add(directJoins, descendantsByAlias, attribute);
			}

			private void add(final Set<DirectJoinClause> directJoins, final Map<Alias, CMClass> descendantsByAlias,
					final QueryAliasAttribute attribute) {
				final Alias alias = attribute.getEntryTypeAlias();
				if (descendantsByAlias.containsKey(alias)) {
					final CMClass type = descendantsByAlias.get(alias);
					final DirectJoinClause clause = DirectJoinClause.newInstance() //
							.leftJoin(type) //
							.as(alias) //
							.on(attribute(alias, ID)) //
							.equalsTo(attribute(delegate.getFromClause().getAlias(), ID)) //
							.build();
					directJoins.add(clause);
				}
			}

		});
		return directJoins;
	}

	@Override
	public List<DirectJoinClause> getDirectJoins() {
		return Lists.newArrayList(directJoins);
	}

	@Override
	public WhereClause getWhereClause() {
		return userWhereClause;
	}

}
