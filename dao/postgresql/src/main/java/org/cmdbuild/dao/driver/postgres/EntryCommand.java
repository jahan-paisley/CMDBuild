package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.ClassId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.ClassId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;

abstract class EntryCommand {

	private static final IntegerAttributeType INTEGER = new IntegerAttributeType();
	private static final EntryTypeAttributeType ENTRY_TYPE = new EntryTypeAttributeType();

	private final JdbcTemplate jdbcTemplate;
	private final DBEntry entry;
	protected List<SystemAttributes> systemDomainAttributes;

	protected EntryCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		this.jdbcTemplate = jdbcTemplate;
		this.entry = entry;
	}

	protected JdbcTemplate jdbcTemplate() {
		return jdbcTemplate;
	}

	protected DBEntry entry() {
		return entry;
	}

	protected List<AttributeValueType> userAttributesFor(final DBEntry entry) {
		final CMEntryType entryType = entry.getType();
		final List<AttributeValueType> values = Lists.newArrayList();
		for (final Map.Entry<String, Object> v : entry.getAllValues()) {
			final String attributeName = v.getKey();
			final Object value = v.getValue();
			final CMAttributeType<?> attributeType = entryType.getAttribute(attributeName).getType();
			final AttributeValueType attrValueType = new AttributeValueType(attributeName, //
					SqlType.getSqlType(attributeType).javaToSqlValue(value), attributeType);
			values.add(attrValueType);
		}
		// TODO ugly... a visitor is a better idea!

		addSystemAttributesValues(entry, values);
		return values;
	}

	private void addSystemAttributesValues(final DBEntry entry, final List<AttributeValueType> values) {
		values.add(new AttributeValueType(User.getDBName(), entry.getUser(), new StringAttributeType()));
		if (entry instanceof DBRelation) {
			final DBRelation dbRelation = DBRelation.class.cast(entry);
			values.add(new AttributeValueType(DomainId1.getDBName(), dbRelation.getCard1Id(), INTEGER));
			values.add(new AttributeValueType(ClassId1.getDBName(), dbRelation.getCard1().getType()
					.getId(), ENTRY_TYPE));
			values.add(new AttributeValueType(DomainId2.getDBName(), dbRelation.getCard2Id(), INTEGER));
			values.add(new AttributeValueType(ClassId2.getDBName(), dbRelation.getCard2().getType()
					.getId(), ENTRY_TYPE));
		}
	}

}
