package unit.services.event;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.services.event.Contexts;
import org.cmdbuild.services.event.Contexts.AfterCreate;
import org.cmdbuild.services.event.Contexts.AfterUpdate;
import org.cmdbuild.services.event.Contexts.BeforeDelete;
import org.cmdbuild.services.event.Contexts.BeforeUpdate;
import org.junit.Test;

public class ContextsTest {

	@Test(expected = NullPointerException.class)
	public void afterCreateContextMustSpecifyCard() {
		// when
		Contexts.afterCreate() //
				.build();
	}

	@Test
	public void afterCreateContextCreatedSuccessfully() {
		// given
		final CMCard card = mock(CMCard.class);

		// when
		final AfterCreate context = Contexts.afterCreate() //
				.withCard(card) //
				.build();

		// then
		assertThat(context.card, is(card));
	}

	@Test(expected = NullPointerException.class)
	public void beforeUpdateContextMustSpecifyActualCard() {
		// given
		final CMCard next = mock(CMCard.class);

		// when
		Contexts.beforeUpdate() //
				.withNext(next) //
				.build();
	}

	@Test(expected = NullPointerException.class)
	public void beforeUpdateContextMustSpecifyNextCard() {
		// given
		final CMCard actual = mock(CMCard.class);

		// when
		Contexts.beforeUpdate() //
				.withActual(actual) //
				.build();
	}

	@Test
	public void beforeUpdateContextCreatedSuccessfully() {
		// given
		final CMCard actual = mock(CMCard.class);
		final CMCard next = mock(CMCard.class);

		// when
		final BeforeUpdate context = Contexts.beforeUpdate() //
				.withActual(actual) //
				.withNext(next) //
				.build();

		// then
		assertThat(context.actual, is(actual));
		assertThat(context.next, is(next));
	}

	@Test(expected = NullPointerException.class)
	public void afterUpdateContextMustSpecifyPreviousCard() {
		// given
		final CMCard actual = mock(CMCard.class);

		// when
		Contexts.afterUpdate() //
				.withActual(actual) //
				.build();
	}

	@Test(expected = NullPointerException.class)
	public void afterUpdateContextMustSpecifyActualCard() {
		// given
		final CMCard previous = mock(CMCard.class);

		// when
		Contexts.afterUpdate() //
				.withPrevious(previous) //
				.build();
	}

	@Test
	public void afterUpdateContextCreatedSuccessfully() {
		// given
		final CMCard previous = mock(CMCard.class);
		final CMCard actual = mock(CMCard.class);

		// when
		final AfterUpdate context = Contexts.afterUpdate() //
				.withPrevious(previous) //
				.withActual(actual) //
				.build();

		// then
		assertThat(context.previous, is(previous));
		assertThat(context.actual, is(actual));
	}

	@Test(expected = NullPointerException.class)
	public void beforeDeleteContextMustSpecifyCard() {
		// when
		Contexts.beforeDelete() //
				.build();
	}

	@Test
	public void beforeDeleteContextCreatedSuccessfully() {
		// given
		final CMCard card = mock(CMCard.class);

		// when
		final BeforeDelete context = Contexts.beforeDelete() //
				.withCard(card) //
				.build();

		// then
		assertThat(context.card, is(card));
	}

}
