package unit.model.data;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Domain.DomainBuilder;
import org.junit.Test;

public class DomainTest {

	private static final String BLANK = " \t";

	private static final String NAME = "foo";
	private static final String NAME_WITH_BLANKS = BLANK + NAME + BLANK;

	private static final String DESCRIPTION = "this is the description";

	private static final long ID_CLASS_1 = 123L;
	private static final long ID_CLASS_2 = 456L;

	private static final long NEGATIVE_ID_CLASS_1 = -ID_CLASS_1;
	private static final long NEGATIVE_ID_CLASS_2 = -ID_CLASS_2;

	@Test
	public void nameIdClass1AndIdClass2AreTheOnlyRequirements() {
		final Domain domain = a(newDomain() //
				.withName(NAME) //
				.withIdClass1(ID_CLASS_1) //
				.withIdClass2(ID_CLASS_2) //
		);
		assertThat(domain.getName(), equalTo(NAME));
		assertThat(domain.getIdClass1(), equalTo(ID_CLASS_1));
		assertThat(domain.getIdClass2(), equalTo(ID_CLASS_2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingNameThowsException() {
		a(newDomain() //
				.withIdClass1(ID_CLASS_1) //
				.withIdClass2(ID_CLASS_2) //
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyNameThowsException() {
		a(newDomain() //
				.withName(EMPTY) //
				.withIdClass1(ID_CLASS_1) //
				.withIdClass2(ID_CLASS_2) //
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void blankNameThowsException() {
		a(newDomain() //
				.withName(BLANK) //
				.withIdClass1(ID_CLASS_1) //
				.withIdClass2(ID_CLASS_2) //
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingIdClass1ThowsException() {
		a(newDomain() //
				.withName(NAME) //
				.withIdClass2(ID_CLASS_2) //
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void zeroIdClass1ThowsException() {
		a(newDomain() //
				.withName(NAME) //
				.withIdClass1(0) //
				.withIdClass2(ID_CLASS_2) //
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeIdClass1ThowsException() {
		a(newDomain() //
				.withName(NAME) //
				.withIdClass1(NEGATIVE_ID_CLASS_1) //
				.withIdClass2(ID_CLASS_2) //
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingIdClass2ThowsException() {
		a(newDomain() //
				.withName(NAME) //
				.withIdClass1(ID_CLASS_1) //
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void zeroIdClass2ThowsException() {
		a(newDomain() //
				.withName(NAME) //
				.withIdClass1(ID_CLASS_1) //
				.withIdClass2(0) //
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeIdClass2ThowsException() {
		a(newDomain() //
				.withName(NAME) //
				.withIdClass1(ID_CLASS_1) //
				.withIdClass2(NEGATIVE_ID_CLASS_2) //
		);
	}

	@Test
	public void nameIsTrimmed() {
		final Domain domain = a(newDomain() //
				.withName(NAME_WITH_BLANKS) //
				.withIdClass1(ID_CLASS_1) //
				.withIdClass2(ID_CLASS_2) //
		);
		assertThat(domain.getName(), equalTo(NAME));
	}

	@Test
	public void descriptionEqualsToTheNameIfNotSpecifiedEmptyOrBlank() {
		assertThat(a(newValidDomain()).getDescription(), equalTo(NAME));
		assertThat(a(newValidDomain().withDescription(EMPTY)).getDescription(), equalTo(NAME));
		assertThat(a(newValidDomain().withDescription(BLANK)).getDescription(), equalTo(NAME));
		assertThat(a(newValidDomain().withDescription(DESCRIPTION)).getDescription(), equalTo(DESCRIPTION));
	}

	/*
	 * Utilities
	 */

	private static Domain a(final DomainBuilder domainBuilder) {
		return domainBuilder.build();
	}

	private DomainBuilder newValidDomain() {
		return newDomain() //
				.withName(NAME_WITH_BLANKS) //
				.withIdClass1(ID_CLASS_1) //
				.withIdClass2(ID_CLASS_2);
	}

	private DomainBuilder newDomain() {
		return Domain.newDomain();
	}

}
