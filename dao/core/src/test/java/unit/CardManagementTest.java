package unit;

import static org.cmdbuild.dao.entrytype.DBIdentifier.fromName;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.Test;

public class CardManagementTest {

	private final DBDriver driver;
	private final CMDataView view;

	public CardManagementTest() {
		driver = mock(DBDriver.class);
		view = new DBDataView(driver);
	}

	@Test
	public void cardsCanBeCreated() {
		// given
		final String attrName = "A";
		final Object attrValue = "Some text";
		final DBAttribute classAttributes[] = { new DBAttribute(attrName, new TextAttributeType(),
				new AttributeMetadata()) };
		final Long classKey = Long.valueOf(777L);
		final Long cardKey = Long.valueOf(42L);
		given(driver.findClass(classKey)).willReturn(DBClass.newClass() //
				.withIdentifier(fromName("C")) //
				.withId(classKey) //
				.withAllMetadata(new DBClass.ClassMetadata()) //
				.withAllAttributes(Arrays.asList(classAttributes)) //
				.build());
		given(driver.create(any(DBEntry.class))).willReturn(cardKey);

		// when
		final CMClass type = view.findClass(classKey);
		final CMCard card = view.createCardFor(type).set(attrName, attrValue).save();

		// then
		assertThat(card.getId(), is(cardKey));
		assertThat(card.get(attrName), is(attrValue));
	}
}
