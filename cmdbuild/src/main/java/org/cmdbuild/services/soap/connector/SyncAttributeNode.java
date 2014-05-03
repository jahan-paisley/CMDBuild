package org.cmdbuild.services.soap.connector;

public enum SyncAttributeNode {
	EXTERNALSYNC("ExternalSync"), //
	ACTIONLIST("actionList"), //
	ACTION("action"), //
	CARDLIST("cardList"), //
	MASTER("cardMaster"), //
	MASTER_CARDID("masterCardId"), //
	MASTER_CLASSNAME("masterClassName"), //
	DETAIL_CARDID("objid"), //
	DOMAIN("domain"), //
	DOMAINDIRECTION("domaindirection"), //
	IDENTIFIERS("identifiers"), //
	ISSHARED("isshared"), //
	;

	private final String attributeName;

	SyncAttributeNode(final String attributeName) {
		this.attributeName = attributeName;
	}

	public String getAttribute() {
		return this.attributeName;
	}
}