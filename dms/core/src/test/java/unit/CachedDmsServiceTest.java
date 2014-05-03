package unit;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.dms.CachedDmsService;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentTypeDefinition;
import org.junit.Before;
import org.junit.Test;

public class CachedDmsServiceTest {

	private static final String FOO = "foo";

	private DmsService mockedDmsService;
	private CachedDmsService cachedDmsService;

	private DocumentTypeDefinition documentTypeDefinition;

	@Before
	public void setUp() throws Exception {
		mockedDmsService = mock(DmsService.class);
		cachedDmsService = new CachedDmsService(mockedDmsService);

		documentTypeDefinition = mock(DocumentTypeDefinition.class, FOO);
	}

	@Test
	public void documentTypeDefinitionsAreCached() throws Exception {
		when(mockedDmsService.getTypeDefinitions()) //
				.thenReturn(asList(documentTypeDefinition));

		cachedDmsService.getTypeDefinitions();
		cachedDmsService.getTypeDefinitions();

		verify(mockedDmsService, times(1)).getTypeDefinitions();
		verifyNoMoreInteractions(mockedDmsService);
	}

	@Test
	public void cacheCleared() throws Exception {
		when(mockedDmsService.getTypeDefinitions()) //
				.thenReturn(asList(documentTypeDefinition));

		cachedDmsService.getTypeDefinitions();
		cachedDmsService.clearCache();
		cachedDmsService.getTypeDefinitions();

		verify(mockedDmsService, times(2)).getTypeDefinitions();
		verify(mockedDmsService).clearCache();
		verifyNoMoreInteractions(mockedDmsService);
	}

}
