package org.cmdbuild.services.event;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.ForwardingCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.ForwardingDataView;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ObservableDataView extends ForwardingDataView {

	private static final Logger logger = Log.PERSISTENCE;
	private static final Marker marker = MarkerFactory.getMarker(ObservableDataView.class.getName());

	private abstract static class ObservableCardDefinition extends ForwardingCardDefinition {

		protected final Observer observer;

		protected ObservableCardDefinition(final CMCardDefinition delegate, final Observer observer) {
			super(delegate);
			this.observer = observer;
		}

	}

	private static class ObservableNewCardDefinition extends ObservableCardDefinition {

		protected ObservableNewCardDefinition(final CMCardDefinition delegate, final Observer observer) {
			super(delegate, observer);
		}

		@Override
		public CMCard save() {
			logger.info(marker, "saving new card");
			final CMCard card = super.save();
			observer.afterCreate(card);
			return card;
		}

	}

	private static class ObservableExistingCardDefinition extends ObservableCardDefinition {

		private final CMCard current;

		protected ObservableExistingCardDefinition(final CMCard current, final CMCardDefinition delegate,
				final Observer observer) {
			super(delegate, observer);
			this.current = current;
		}

		@Override
		public CMCard save() {
			logger.info(marker, "saving existing card");
			final CMCard card = super.save();
			observer.beforeUpdate(current, card);
			observer.afterUpdate(current, card);
			return card;
		}
	}

	private final Observer observer;

	public ObservableDataView(final CMDataView delegate, final Observer observer) {
		super(delegate);
		this.observer = observer;
	}

	@Override
	public CMCardDefinition createCardFor(final CMClass type) {
		return new ObservableNewCardDefinition(super.createCardFor(type), observer);
	}

	@Override
	public CMCardDefinition update(final CMCard card) {
		return new ObservableExistingCardDefinition(card, super.update(card), observer);
	}

	@Override
	public void delete(final CMCard card) {
		logger.info(marker, "deleting existing card");
		observer.beforeDelete(card);
		super.delete(card);
	}

}
