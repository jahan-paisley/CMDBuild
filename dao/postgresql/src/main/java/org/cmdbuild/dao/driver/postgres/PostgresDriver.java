package org.cmdbuild.dao.driver.postgres;

import javax.sql.DataSource;

import org.cmdbuild.dao.TypeObjectCache;
import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.DBQueryResult;
import org.cmdbuild.dao.query.EmptyQuerySpecs;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;
import org.cmdbuild.dao.view.DBDataView.DBDomainDefinition;
import org.joda.time.DateTime;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 20th century driver working with triggers, thus needing a lot more Java hacks
 * 
 * "If all you have is SQL, everything looks like a trigger" A. Maslow
 * (readapted)
 */
public class PostgresDriver extends AbstractDBDriver {

	private static final Marker marker = MarkerFactory.getMarker(PostgresDriver.class.getName());

	private static final DBQueryResult EMPTY_QUERY_RESULT = new DBQueryResult();

	private final JdbcTemplate jdbcTemplate;

	public PostgresDriver(final DataSource datasource, final TypeObjectCache typeObjectCache) {
		super(typeObjectCache);
		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	@Override
	public Iterable<DBClass> findAllClasses() {
		logger.info(marker, "getting all classes");
		if (cache.isEmpty(DBClass.class)) {
			logger.info(marker, "cache is empty, getting all classes from database");
			for (final DBClass element : entryTypeCommands().findAllClasses()) {
				cache.add(element);
			}
		}
		return cache.fetch(DBClass.class);
	}

	@Override
	public DBClass createClass(final DBClassDefinition definition) {
		logger.info(marker, "creating class '{}' within namespace '{}'", //
				definition.getIdentifier().getLocalName(), definition.getIdentifier().getNameSpace());
		final DBClass createdClass = entryTypeCommands().createClass(definition);
		cache.add(createdClass);
		return createdClass;
	}

	@Override
	public DBClass updateClass(final DBClassDefinition definition) {
		logger.info(marker, "updating class '{}' within namespace '{}'", //
				definition.getIdentifier().getLocalName(), definition.getIdentifier().getNameSpace());
		final DBClass updatedClass = entryTypeCommands().updateClass(definition);
		cache.add(updatedClass);
		return updatedClass;
	}

	@Override
	public void deleteClass(final DBClass dbClass) {
		logger.info(marker, "deleting class '{}' within namespace '{}'", //
				dbClass.getIdentifier().getLocalName(), dbClass.getIdentifier().getNameSpace());
		entryTypeCommands().deleteClass(dbClass);
		cache.remove(dbClass);
	}

	@Override
	public DBAttribute createAttribute(final DBAttributeDefinition definition) {
		logger.info(marker, "creating attribute '{}'", definition.getName());
		return entryTypeCommands().createAttribute(definition);
	}

	@Override
	public DBAttribute updateAttribute(final DBAttributeDefinition definition) {
		logger.info(marker, "updating attribute '{}'", definition.getName());
		return entryTypeCommands().updateAttribute(definition);
	}

	@Override
	public void deleteAttribute(final DBAttribute attribute) {
		logger.info(marker, "deleting attribute '{}'", attribute.getName());
		entryTypeCommands().deleteAttribute(attribute);
	}

	@Override
	public void clear(final DBAttribute attribute) {
		logger.info(marker, "clearing attribute '{}'", attribute.getName());
		entryTypeCommands().clear(attribute);
	}

	@Override
	public Iterable<DBDomain> findAllDomains() {
		logger.info(marker, "getting all domains");
		if (cache.isEmpty(DBDomain.class)) {
			logger.info(marker, "cache is empty, getting all domains from database");
			for (final DBDomain element : entryTypeCommands().findAllDomains()) {
				cache.add(element);
			}
		}
		return cache.fetch(DBDomain.class);
	}

	@Override
	public DBDomain createDomain(final DBDomainDefinition definition) {
		logger.info(marker, "creating domain '{}' within namespace '{}'", //
				definition.getIdentifier().getLocalName(), definition.getIdentifier().getNameSpace());
		final DBDomain createdDomain = entryTypeCommands().createDomain(definition);
		cache.add(createdDomain);
		return createdDomain;
	}

	@Override
	public DBDomain updateDomain(final DBDomainDefinition definition) {
		logger.info(marker, "updating domain '{}' within namespace '{}'", //
				definition.getIdentifier().getLocalName(), definition.getIdentifier().getNameSpace());
		final DBDomain updatedDomain = entryTypeCommands().updateDomain(definition);
		cache.add(updatedDomain);
		return updatedDomain;
	}

	@Override
	public void deleteDomain(final DBDomain dbDomain) {
		logger.info(marker, "deleting domain '{}' within namespace '{}'", //
				dbDomain.getIdentifier().getLocalName(), dbDomain.getIdentifier().getNameSpace());
		entryTypeCommands().deleteDomain(dbDomain);
		cache.remove(dbDomain);
	}

	private EntryTypeCommands entryTypeCommands() {
		return new EntryTypeCommands(this, jdbcTemplate);
	}

	@Override
	public Iterable<DBFunction> findAllFunctions() {
		logger.info(marker, "getting all functions");
		if (cache.isEmpty(DBFunction.class)) {
			logger.info(marker, "cache is empty, getting all functions from database");
			for (final DBFunction element : entryTypeCommands().findAllFunctions()) {
				cache.add(element);
			}
		}
		return cache.fetch(DBFunction.class);
	}

	@Override
	public Long create(final DBEntry entry) {
		logger.info(marker, "creating entry for type '{}'", entry.getType().getIdentifier());
		Long id = new EntryInsertCommand(jdbcTemplate, entry).executeAndReturnKey();
		return id;
	}

	@Override
	public void update(final DBEntry entry) {
		logger.info(marker, "updating entry with id '{}' for type '{}'", entry.getId(), entry.getType().getIdentifier());
		new EntryUpdateCommand(jdbcTemplate, entry).execute();
	}

	@Override
	public void delete(final DBEntry entry) {
		logger.info(marker, "deleting entry with id '{}' for type '{}' within namespace '{}'", //
				entry.getId(), entry.getType().getIdentifier());
		new EntryDeleteCommand(jdbcTemplate, entry).execute();
	}

	@Override
	public void clear(final DBEntryType type) {
		logger.info(marker, "clearing type '{}' within namespace '{}'", //
				type.getIdentifier().getLocalName(), type.getIdentifier().getNameSpace());
		// truncate all subclasses as well
		jdbcTemplate.execute("TRUNCATE TABLE " + EntryTypeQuoter.quote(type) + " CASCADE");
	}

	@Override
	public CMQueryResult query(final QuerySpecs query) {
		if (query instanceof EmptyQuerySpecs) {
			return EMPTY_QUERY_RESULT;
		} else {
			return new EntryQueryCommand(this, jdbcTemplate, query).run();
		}
	}

}
