package org.cmdbuild.dao.driver.postgres.query;

import org.cmdbuild.dao.query.clause.alias.Alias;

/**
 * Holder for attributes that must be used within <code>SELECT</code> statement.
 */
public interface SelectAttributesHolder {

	/**
	 * Adds an attribute.
	 * 
	 * @param typeAlias
	 *            is the alias for the type.
	 * @param name
	 *            is the name of the attribute.
	 * @param cast
	 *            is the SQL cast that needs to be used, <code>null</code> if
	 *            not required.
	 * @param alias
	 *            is the alias of the attribute itself.
	 */
	void add(Alias typeAlias, String name, String cast, Alias alias);

	/**
	 * Adds an attribute.
	 * 
	 * @param cast
	 *            is the SQL cast that needs to be used, <code>null</code> if
	 *            not required.
	 * @param alias
	 *            is the alias of the attribute itself.
	 */
	void add(String cast, Alias alias);

}
