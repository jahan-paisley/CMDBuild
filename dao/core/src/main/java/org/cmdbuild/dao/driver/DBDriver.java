package org.cmdbuild.dao.driver;

import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;
import org.cmdbuild.dao.view.DBDataView.DBDomainDefinition;

/**
 * Interface for a generic database driver.
 */
public interface DBDriver {

	/**
	 * Finds all available classes.
	 * 
	 * @return all available classes.
	 */
	Iterable<DBClass> findAllClasses();

	/**
	 * Finds a class by its id.
	 * 
	 * @param id
	 *            is the required class's id.
	 * 
	 * @return the requested {@link DBClass} or {@code null} if no class has
	 *         been found.
	 */
	DBClass findClass(Long id);

	/**
	 * Finds a class by its name in the default namespace.
	 * 
	 * @param localname
	 *            is the required class's name.
	 * 
	 * @return the requested {@link DBClass} or {@code null} if no class has
	 *         been found.
	 */
	DBClass findClass(String localname);

	/**
	 * Finds a class by by its local name and namespace.
	 * 
	 * @param localname
	 *            is the required class's local name.
	 * @param localname
	 *            is the required class's namespace.
	 * 
	 * @return the requested {@link DBClass} or {@code null} if no class has
	 *         been found.
	 */
	DBClass findClass(String localname, String namespace);

	/**
	 * Creates a new class.
	 * 
	 * @param definition
	 *            contains the definition needed for creating the new class.
	 * 
	 * @return the created class.
	 */
	DBClass createClass(DBClassDefinition definition);

	/**
	 * Updated an existing class.
	 * 
	 * @param definition
	 *            contains the definition needed for updating the existing
	 *            class.
	 * 
	 * @return the created class.
	 */
	DBClass updateClass(DBClassDefinition definition);

	/**
	 * Creates a new attribute.
	 * 
	 * @param definition
	 *            contains the definition needed for creating the new attribute.
	 * 
	 * @return the created attribute.
	 */
	DBAttribute createAttribute(DBAttributeDefinition definition);

	/**
	 * Updates an existing attribute.
	 * 
	 * @param definition
	 *            contains the definition needed for updating the existing
	 *            attribute.
	 * 
	 * @return the created attribute.
	 */
	DBAttribute updateAttribute(DBAttributeDefinition definition);

	/**
	 * Delete an existing attribute.
	 * 
	 * @param dbAttribute
	 *            the existing attribute.
	 */
	void deleteAttribute(DBAttribute dbAttribute);

	void clear(DBAttribute dbAttribute);

	void deleteClass(DBClass dbClass);

	Iterable<DBDomain> findAllDomains();

	DBDomain findDomain(Long id);

	DBDomain findDomain(String localname);

	/**
	 * Finds a domain by its local name and namespace.
	 * 
	 * @param localname
	 *            is the required domain's local name.
	 * @param localname
	 *            is the required domain's namespace.
	 * 
	 * @return the requested {@link DBDomain} or {@code null} if no class has
	 *         been found.
	 */
	DBDomain findDomain(String localname, String namespace);

	Iterable<DBFunction> findAllFunctions();

	DBFunction findFunction(String localname);

	DBDomain createDomain(DBDomainDefinition domainDefinition);

	DBDomain updateDomain(DBDomainDefinition domainDefinition);

	void deleteDomain(DBDomain dbDomain);

	Long create(DBEntry entry);

	void update(DBEntry entry);

	void delete(DBEntry entry);

	void clear(DBEntryType type);

	CMQueryResult query(QuerySpecs query);

	/*
	 * add parameters for query and executeStatement? note: SQL available only
	 * to the System user, not even to admin!
	 * 
	 * CMQueryResult query(String language, String query); // "CQL",
	 * "from Table ..."
	 * 
	 * void executeStatement(String language, String statement); // "SQL",
	 * "CREATE TABLE ..."
	 * 
	 * void executeScript(String language, String script); // "SQL",
	 * "classpath:/createdb.sql"
	 */

}
