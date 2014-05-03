package unit.privileges.fetchers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.privileges.fetchers.CMClassPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.ViewPrivilegeFetcher;
import org.cmdbuild.privileges.fetchers.factories.CMClassPrivilegeFetcherFactory;
import org.cmdbuild.privileges.fetchers.factories.ViewPrivilegeFetcherFactory;
import org.junit.Before;
import org.junit.Test;

public class PrivilegeFetcherFactoriesTest {

	private CMDataView dataView;

	@Before
	public void setUp() throws Exception {
		dataView = mock(CMDataView.class);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionIfGroupIsNotSetForCMClass() {
		// given
		final CMClassPrivilegeFetcherFactory classFactory = new CMClassPrivilegeFetcherFactory(dataView);

		// when
		classFactory.create();
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionIfGroupIsNotSetForView() {
		// given
		final ViewPrivilegeFetcherFactory viewFactory = new ViewPrivilegeFetcherFactory(dataView, new ViewConverter(
				dataView));

		// when
		viewFactory.create();
	}

	@Test
	public void eachFactoryShouldReturnTheCorrectTypeOfPrivilegeFetcher() {
		// given
		final CMClassPrivilegeFetcherFactory classFactory = new CMClassPrivilegeFetcherFactory(dataView);
		final ViewPrivilegeFetcherFactory viewFactory = new ViewPrivilegeFetcherFactory(dataView, new ViewConverter(
				dataView));
		classFactory.setGroupId(1L);
		viewFactory.setGroupId(1L);

		// when
		final PrivilegeFetcher classPrivilegeFetcher = classFactory.create();
		final PrivilegeFetcher viewPrivilegeFetcher = viewFactory.create();

		// then
		assertTrue(classPrivilegeFetcher instanceof CMClassPrivilegeFetcher);
		assertTrue(viewPrivilegeFetcher instanceof ViewPrivilegeFetcher);

	}

}
