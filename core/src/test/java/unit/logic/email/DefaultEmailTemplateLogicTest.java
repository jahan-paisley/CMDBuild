package unit.logic.email;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.logic.email.DefaultEmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEmailTemplateLogicTest {

	private static final List<EmailTemplate> NO_ELEMENTS = Collections.emptyList();

	@Mock
	private Store<EmailTemplate> store;

	private DefaultEmailTemplateLogic logic;

	private final ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);

	@Before
	public void setUp() throws Exception {
		logic = new DefaultEmailTemplateLogic(store);
	}

	@Test
	public void elementCreatedWhenThereAreNoOtherElements() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);
		when(store.read(any(Storable.class))) //
				.thenReturn(EmailTemplate.newInstance() //
						.withId(42L) //
						.withName("foo") //
						.build());
		final Template newOne = mock(Template.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		final Long id = logic.create(newOne);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store).create(captor.capture());
		inOrder.verify(store).read(any(Storable.class));
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
		assertThat(id, equalTo(42L));
	}

	@Test
	public void elementCreatedWhenThereIsNoOtherOneWithSameName() throws Exception {
		// given
		final EmailTemplate stored = EmailTemplate.newInstance() //
				.withName("bar") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));
		when(store.read(any(Storable.class))) //
				.thenReturn(EmailTemplate.newInstance() //
						.withId(42L) //
						.withName("foo") //
						.build());
		final Template newOne = mock(Template.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		final Long id = logic.create(newOne);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store).create(captor.capture());
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
		assertThat(id, equalTo(42L));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateNewElementWhenAnotherWithSameNameExists() throws Exception {
		// given
		final EmailTemplate stored = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));
		final Template newOne = mock(Template.class);
		when(newOne.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.create(newOne);
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotUpdateNonExistingElement() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);
		final Template existing = mock(Template.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.update(existing);
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotUpdateElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final EmailTemplate stored = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored, stored, stored));
		final Template existing = mock(Template.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		try {
			logic.update(existing);
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void elementUpdatedWhenOneIsFound() throws Exception {
		// given
		final EmailTemplate stored = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));
		final Template existing = mock(Template.class);
		when(existing.getName()) //
				.thenReturn("foo");

		// when
		logic.update(existing);

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store).update(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeleteNonExistingAccount() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);

		// when
		try {
			logic.delete("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeleteElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final EmailTemplate stored = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored, stored, stored));

		// when
		try {
			logic.delete("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void elementDeletedWhenAnotherOneWithSameNameIsFound() throws Exception {
		// given
		final EmailTemplate stored = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));

		// when
		logic.delete("foo");

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store).delete(captor.capture());
		verifyNoMoreInteractions(store);
		final EmailTemplate captured = captor.getValue();
		assertThat(captured.getName(), equalTo("foo"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotGetNotExistingElement() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(NO_ELEMENTS);

		// when
		try {
			logic.read("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotGetElementIfItIsStoredMoreThanOnce_thisShouldNeverHappenButWhoKnows() throws Exception {
		// given
		final EmailTemplate stored = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored, stored, stored));

		// when
		try {
			logic.read("foo");
		} finally {
			verify(store).list();
			verifyNoMoreInteractions(store);
		}
	}

	@Test
	public void elementGetWhenOneIsFound() throws Exception {
		// given
		final EmailTemplate stored = EmailTemplate.newInstance() //
				.withName("foo") //
				.build();
		when(store.list()) //
				.thenReturn(asList(stored));

		// when
		logic.read("foo");

		// then
		final InOrder inOrder = inOrder(store);
		inOrder.verify(store).list();
		inOrder.verify(store).read(captor.capture());
		verifyNoMoreInteractions(store);
		assertThat(captor.getValue().getName(), equalTo("foo"));
	}

	@Test
	public void allElementsGet() throws Exception {
		// when
		logic.readAll();

		// then
		verify(store).list();
		verifyNoMoreInteractions(store);
	}

}
