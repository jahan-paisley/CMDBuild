package org.cmdbuild.dao.entry;

import java.util.Map;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.DBDomain;

import com.google.common.collect.Maps;

public class DBRelation extends DBEntry implements CMRelation, CMRelationDefinition {

	public static final String _1 = "_1";
	public static final String _2 = "_2";

	private final Map<String, CMCard> cards;
	private Long card1Id;
	private Long card2Id;

	public static DBRelation newInstance(final DBDriver driver, final DBDomain type) {
		return new DBRelation(driver, type, null);
	}

	public static DBRelation newInstance(final DBDriver driver, final DBDomain type, final Long id) {
		return new DBRelation(driver, type, id);
	}

	public static DBRelation newInstance(final DBDriver driver, final DBRelation existentRelation) {
		return new DBRelation(driver, existentRelation.getType(), existentRelation.getId());
	}

	private DBRelation(final DBDriver driver, final DBDomain type, final Long id) {
		super(driver, type, id);
		cards = Maps.newHashMap();
	}

	@Override
	public DBDomain getType() {
		return (DBDomain) super.getType();
	}

	public CMCard getCard1() {
		return cards.get(_1);
	}

	@Override
	public DBRelation setCard1(final CMCard card) {
		cards.put(_1, card);
		card1Id = card.getId();
		return this;
	}

	public DBRelation setCard1Id(final Long card1Id) {
		this.card1Id = card1Id;
		return this;
	}

	public CMCard getCard2() {
		return cards.get(_2);
	}

	public DBRelation setCard2Id(final Long card2Id) {
		this.card2Id = card2Id;
		return this;
	}

	@Override
	public DBRelation setCard2(final CMCard card) {
		cards.put(_2, card);
		card2Id = card.getId();
		return this;
	}

	@Override
	public final DBRelation set(final String key, final Object value) {
		if (_1.equals(key)) {
			setCard1(CMCard.class.cast(value));
		} else if (_2.equals(key)) {
			setCard2(CMCard.class.cast(value));
		} else {
			setOnly(key, value);
		}
		return this;
	}

	@Override
	public DBRelation save() {
		saveOnly();
		return this;
	}

	@Override
	public CMRelation create() {
		return save();
	}

	@Override
	public CMRelation update() {
		super.updateOnly();
		return this;
	}

	@Override
	public void delete() {
		super.delete();
	}

	@Override
	public Long getCard1Id() {
		return card1Id;
	}

	@Override
	public Long getCard2Id() {
		return card2Id;
	}

}
