package org.cmdbuild.services.soap.types;

import java.util.ArrayList;
import java.util.List;

public class CardListExt {
	
	private int totalRows;
	private List<CardExt> cards;
	
	public CardListExt(){
		cards = new ArrayList<CardExt>();
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public List<CardExt> getCards() {
		return cards;
	}

	public void setCards(List<CardExt> cards) {
		this.cards = cards;
	}

	public void addCard(CardExt card) {
		this.cards.add(card);
	}
}
