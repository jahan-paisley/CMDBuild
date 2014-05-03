package unit.api.fluent.ws;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.cmdbuild.api.fluent.ExistingRelation;
import org.cmdbuild.services.soap.Relation;
import org.junit.Before;
import org.junit.Test;

public class ExistingRelationTest extends AbstractWsFluentApiTest {

	private ExistingRelation existingRelation;

	@Before
	public void createExistingRelation() throws Exception {
		existingRelation = api().existingRelation(DOMAIN_NAME) //
				.withCard1(CLASS_NAME, CARD_ID) //
				.withCard2(ANOTHER_CLASS_NAME, ANOTHER_CARD_ID);
	}

	@Test
	public void parametersPassedToProxyWhenDeletingExistingRelation() throws Exception {
		existingRelation.delete();

		verify(proxy()).deleteRelation(relationCapturer());
		verifyNoMoreInteractions(proxy());

		final Relation wsRelation = capturedRelation();
		assertThat(wsRelation.getDomainName(), equalTo(existingRelation.getDomainName()));
		assertThat(wsRelation.getClass1Name(), equalTo(existingRelation.getClassName1()));
		assertThat(wsRelation.getCard1Id(), equalTo(existingRelation.getCardId1()));
		assertThat(wsRelation.getClass2Name(), equalTo(existingRelation.getClassName2()));
		assertThat(wsRelation.getCard2Id(), equalTo(existingRelation.getCardId2()));
	}

}
