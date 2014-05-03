package org.cmdbuild.services.soap.types;

import java.util.Calendar;

public class Relation {

	private String domainName;
	private String class1Name;
	private String class2Name;
	private int card1Id;
	private int card2Id;
	private String status;
	private Calendar beginDate;
	private Calendar endDate;

	public Calendar getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Calendar beginDate) {
		this.beginDate = beginDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getClass1Name() {
		return class1Name;
	}

	public void setClass1Name(String class1Name) {
		this.class1Name = class1Name;
	}

	public String getClass2Name() {
		return class2Name;
	}

	public void setClass2Name(String class2Name) {
		this.class2Name = class2Name;
	}

	public int getCard1Id() {
		return card1Id;
	}

	public void setCard1Id(int card1Id) {
		this.card1Id = card1Id;
	}

	public int getCard2Id() {
		return card2Id;
	}

	public void setCard2Id(int card2Id) {
		this.card2Id = card2Id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
