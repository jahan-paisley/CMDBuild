package org.cmdbuild.api.fluent;

public class Relation {

	private final String domainName;
	private CardDescriptor card1;
	private CardDescriptor card2;

	public Relation(final String domainName) {
		this.domainName = domainName;
	}

	public String getDomainName() {
		return domainName;
	}

	public String getClassName1() {
		return card1.getClassName();
	}

	public int getCardId1() {
		return card1.getId();
	}

	public Relation setCard1(final String className, final int id) {
		this.card1 = new CardDescriptor(className, id);
		return this;
	}

	public String getClassName2() {
		return card2.getClassName();
	}

	public int getCardId2() {
		return card2.getId();
	}

	public Relation setCard2(final String className, final int id) {
		this.card2 = new CardDescriptor(className, id);
		return this;
	}

}
