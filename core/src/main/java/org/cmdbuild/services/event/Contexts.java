package org.cmdbuild.services.event;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;

public class Contexts {

	public static class AfterCreate implements Context {

		public static class Builder implements org.cmdbuild.common.Builder<AfterCreate> {

			private CMCard card;

			private Builder() {
				// use factory method
			}

			@Override
			public AfterCreate build() {
				validate();
				return new AfterCreate(this);
			}

			private void validate() {
				Validate.notNull(card, "invalid card");
			}

			public Builder withCard(final CMCard card) {
				this.card = card;
				return this;
			}

		}

		public final CMCard card;

		private AfterCreate(final Builder builder) {
			this.card = builder.card;
		}

		@Override
		public void accept(final ContextVisitor visitor) {
			visitor.visit(this);
		}

	}

	public static class BeforeUpdate implements Context {

		public static class Builder implements org.cmdbuild.common.Builder<BeforeUpdate> {

			private CMCard actual;
			private CMCard next;

			private Builder() {
				// use factory method
			}

			@Override
			public BeforeUpdate build() {
				validate();
				return new BeforeUpdate(this);
			}

			private void validate() {
				Validate.notNull(actual, "invalid actual card");
				Validate.notNull(next, "invalid next card");
			}

			public Builder withActual(final CMCard card) {
				this.actual = card;
				return this;
			}

			public Builder withNext(final CMCard card) {
				this.next = card;
				return this;
			}

		}

		public final CMCard actual;
		public final CMCard next;

		private BeforeUpdate(final Builder builder) {
			this.actual = builder.actual;
			this.next = builder.next;
		}

		@Override
		public void accept(final ContextVisitor visitor) {
			visitor.visit(this);
		}

	}

	public static class AfterUpdate implements Context {

		public static class Builder implements org.cmdbuild.common.Builder<AfterUpdate> {

			private CMCard previous;
			private CMCard actual;

			private Builder() {
				// use factory method
			}

			@Override
			public AfterUpdate build() {
				validate();
				return new AfterUpdate(this);
			}

			private void validate() {
				Validate.notNull(previous, "invalid previous card");
				Validate.notNull(actual, "invalid actual card");
			}

			public Builder withPrevious(final CMCard card) {
				this.previous = card;
				return this;
			}

			public Builder withActual(final CMCard card) {
				this.actual = card;
				return this;
			}

		}

		public final CMCard previous;
		public final CMCard actual;

		private AfterUpdate(final Builder builder) {
			this.previous = builder.previous;
			this.actual = builder.actual;
		}

		@Override
		public void accept(final ContextVisitor visitor) {
			visitor.visit(this);
		}

	}

	public static class BeforeDelete implements Context {

		public static class Builder implements org.cmdbuild.common.Builder<BeforeDelete> {

			private CMCard card;

			private Builder() {
				// use factory method
			}

			@Override
			public BeforeDelete build() {
				validate();
				return new BeforeDelete(this);
			}

			private void validate() {
				Validate.notNull(card, "invalid card");
			}

			public Builder withCard(final CMCard card) {
				this.card = card;
				return this;
			}

		}

		public final CMCard card;

		private BeforeDelete(final Builder builder) {
			this.card = builder.card;
		}

		@Override
		public void accept(final ContextVisitor visitor) {
			visitor.visit(this);
		}

	}

	public static AfterCreate.Builder afterCreate() {
		return new AfterCreate.Builder();
	}

	public static BeforeUpdate.Builder beforeUpdate() {
		return new BeforeUpdate.Builder();
	}

	public static AfterUpdate.Builder afterUpdate() {
		return new AfterUpdate.Builder();
	}

	public static BeforeDelete.Builder beforeDelete() {
		return new BeforeDelete.Builder();
	}

	private Contexts() {
		// prevents instantiation
	}

}
