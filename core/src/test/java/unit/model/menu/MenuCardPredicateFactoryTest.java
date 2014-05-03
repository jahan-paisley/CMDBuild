package unit.model.menu;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.TYPE_ATTRIBUTE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.privileges.predicates.IsAlwaysReadable;
import org.cmdbuild.privileges.predicates.IsReadableClass;
import org.cmdbuild.privileges.predicates.IsReadableDashboard;
import org.cmdbuild.services.store.menu.MenuCardPredicateFactory;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;

public class MenuCardPredicateFactoryTest {

	private static final String MOCK_GROUP_NAME = "mock_group";
	private static final Long referencedClassId = 10L;
	private CMDataView view;
	private PrivilegeContextFactory privilegeContextFactory;

	@Before
	public void setUp() {
		this.view = getDataView();
		this.privilegeContextFactory = mock(PrivilegeContextFactory.class);
	}

	private MenuCardPredicateFactory menuCardPredicateFactory(final CMGroup mockGroup) {
		return new MenuCardPredicateFactory(view, mockGroup, privilegeContextFactory, new ViewConverter(view));
	}

	@Test
	public void shouldReturnTheCorrectPredicate() {
		// given
		final CMCard classMockMenuCard = getMockMenuCard(MenuItemType.CLASS);
		final CMCard folderMockMenuCard = getMockMenuCard(MenuItemType.FOLDER);
		final CMCard rootMockMenuCard = getMockMenuCard(MenuItemType.ROOT);
		final CMCard dashboardMockMenuCard = getMockMenuCard(MenuItemType.DASHBOARD);

		final CMGroup mockGroup = mock(CMGroup.class);
		final MenuCardPredicateFactory factory = menuCardPredicateFactory(mockGroup);

		// when
		final Predicate<?> classPredicate = factory.getPredicate(classMockMenuCard);
		final Predicate<?> folderPredicate = factory.getPredicate(folderMockMenuCard);
		final Predicate<?> rootPredicate = factory.getPredicate(rootMockMenuCard);
		final Predicate<?> dashboardPredicate = factory.getPredicate(dashboardMockMenuCard);

		// then
		assertTrue(classPredicate instanceof IsReadableClass);
		assertTrue(dashboardPredicate instanceof IsReadableDashboard);
		assertTrue(folderPredicate instanceof IsAlwaysReadable);
		assertTrue(rootPredicate instanceof IsAlwaysReadable);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfCardIsNotAMenuCard() {
		// given
		final CMCard mockMenuCard = mock(CMCard.class);
		when(mockMenuCard.get(TYPE_ATTRIBUTE)).thenReturn(MenuItemType.CLASS);
		final CMClass mockMenuClass = mock(CMClass.class);
		when(mockMenuClass.getName()).thenReturn("Not_Menu_Class_Name");
		when(mockMenuCard.getType()).thenReturn(mockMenuClass);
		final CMGroup mockGroup = mock(CMGroup.class);
		final MenuCardPredicateFactory factory = menuCardPredicateFactory(mockGroup);

		// when
		factory.getPredicate(mockMenuCard);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfNotValidMenuCardType() {
		// given
		final CMCard mockMenuCard = mock(CMCard.class);
		when(mockMenuCard.get(TYPE_ATTRIBUTE)).thenReturn("not_valid_type");
		final CMClass mockMenuClass = mock(CMClass.class);
		when(mockMenuClass.getName()).thenReturn(MENU_CLASS_NAME);
		when(mockMenuCard.getType()).thenReturn(mockMenuClass);

		final CMGroup mockGroup = mock(CMGroup.class);
		final MenuCardPredicateFactory factory = menuCardPredicateFactory(mockGroup);

		// when
		factory.getPredicate(mockMenuCard);
	}

	@Test
	public void shouldReturnFalseIfNullDashboardDefinition() {
		// given
		final CMCard mockMenuCard = getMockMenuCard(MenuItemType.DASHBOARD);
		when(mockMenuCard.get("Definition")).thenReturn(null);
		final CMGroup mockGroup = mock(CMGroup.class);
		when(mockGroup.getName()).thenReturn(MOCK_GROUP_NAME);
		final MenuCardPredicateFactory factory = menuCardPredicateFactory(mockGroup);

		// when
		final Predicate<CMCard> predicate = factory.getPredicate(mockMenuCard);

		// then
		assertFalse(predicate.apply(mockMenuCard));
	}

	@Test
	public void shouldReturnFalseIfGroupCannotReadTheDashboard() {
		// given
		final CMCard mockMenuCard = getMockMenuCard(MenuItemType.DASHBOARD);
		when(mockMenuCard.get("Definition")).thenReturn("{groups:[group1, group2]}");
		final CMGroup mockGroup = mock(CMGroup.class);
		when(mockGroup.getName()).thenReturn(MOCK_GROUP_NAME);
		final MenuCardPredicateFactory factory = menuCardPredicateFactory(mockGroup);

		// when
		final Predicate<CMCard> predicate = factory.getPredicate(mockMenuCard);

		// then
		assertFalse(predicate.apply(mockMenuCard));
	}

	@Test
	public void shouldReturnFalseIfMalformedDashboardDefinition() {
		// given
		final CMCard mockMenuCard = getMockMenuCard(MenuItemType.DASHBOARD);
		when(mockMenuCard.get("Definition")).thenReturn(
				"{malformed, json, groups:[group1, group2, " + MOCK_GROUP_NAME + "]}");
		final CMGroup mockGroup = mock(CMGroup.class);
		when(mockGroup.getName()).thenReturn(MOCK_GROUP_NAME);
		final MenuCardPredicateFactory factory = menuCardPredicateFactory(mockGroup);

		// when
		final Predicate<CMCard> predicate = factory.getPredicate(mockMenuCard);

		// then
		assertFalse(predicate.apply(mockMenuCard));
	}

	private CMCard getMockMenuCard(final MenuItemType type) {
		final CMCard mockMenuCard = mock(CMCard.class);
		when(mockMenuCard.get(TYPE_ATTRIBUTE)).thenReturn(type.getValue());
		if (type.getValue().equals(MenuItemType.CLASS.getValue())) {
			when(mockMenuCard.get(ELEMENT_CLASS_ATTRIBUTE)).thenReturn(referencedClassId);
		}
		final CMClass mockMenuClass = mock(CMClass.class);
		when(mockMenuClass.getName()).thenReturn(MENU_CLASS_NAME);
		when(mockMenuCard.getType()).thenReturn(mockMenuClass);
		when(mockMenuClass.getId()).thenReturn(referencedClassId);
		return mockMenuCard;
	}

	private CMDataView getDataView() {
		final CMDataView mockView = mock(CMDataView.class);
		return mockView;
	}

}
