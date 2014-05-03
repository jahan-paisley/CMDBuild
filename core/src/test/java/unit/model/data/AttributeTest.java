package unit.model.data;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.junit.Test;

public class AttributeTest {

	private static final String NAME = "foo";
	private static final String BLANK = " \t";
	private static final String NAME_WITH_BLANKS = BLANK + NAME + BLANK;

	private static final String OWNER = "entryType";
	private static final Long NEGATIVE_ID = -42L;
	private static final Long ZERO_ID = 0L;

	private static final String TYPE_THAT_DOES_NOT_REQUIRE_PARAMS = "BOOLEAN";

	private static final String SOMETHING = "something";

	@Test(expected = IllegalArgumentException.class)
	public void missingNameThrowsException() {
		a(newEmptyAttribute() //
				.withOwnerName(OWNER) //
				.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS));
	}

	@Test(expected = NullPointerException.class)
	public void missingOwnerThowsException() {
		a(newEmptyAttribute() //
				.withName(NAME) //
				.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS));
	}

	@Test
	public void missingTypeThowsException() {
		assertThat(a(newEmptyAttribute() //
				.withName(NAME) //
				.withOwnerName(OWNER)) //
				.getType(), is(instanceOf(UndefinedAttributeType.class)));
	}

	@Test
	public void nameIsTrimmed() {
		assertThat(a(newValidAttribute().withName(NAME_WITH_BLANKS)).getName(), equalTo(NAME));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nameCannotBeEmpty() {
		a(newValidAttribute().withName(EMPTY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nameCannotBeBlank() {
		a(newValidAttribute().withName(BLANK));
	}

	@Test
	public void nameOwnerAndTypeAreRequiredAtributes() {
		final Attribute attribute = a(newEmptyAttribute() //
				.withName(NAME) //
				.withOwnerName(OWNER) //
				.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS));
		assertThat(attribute.getName(), equalTo(NAME));
		assertThat(attribute.getOwnerName(), equalTo(OWNER));
		assertThat(attribute.getType(), instanceOf(BooleanAttributeType.class));
	}

	@Test
	public void whenDescriptionIsNotSpecifiedOrEmptyOrBlankThenNameValueIsReturned() {
		assertThat(a(newValidAttribute()).getDescription(), equalTo(NAME));
		assertThat(a(newValidAttribute().withDescription(EMPTY)).getDescription(), equalTo(NAME));
		assertThat(a(newValidAttribute().withDescription(BLANK)).getDescription(), equalTo(NAME));
	}

	@Test
	public void groupValueReturnedAsIs() throws Exception {
		assertThat(a(newValidAttribute()).getGroup(), is(equalTo(StringUtils.EMPTY)));
		assertThat(a(newValidAttribute().withGroup(SOMETHING)).getGroup(), equalTo(SOMETHING));
	}

	@Test
	public void defaultValueReturnedAsIs() throws Exception {
		assertThat(a(newValidAttribute()).getDefaultValue(), is(nullValue(String.class)));
		assertThat(a(newValidAttribute().withDefaultValue(SOMETHING)).getDefaultValue(), equalTo(SOMETHING));
	}

	@Test
	public void attributeIsNotDisplayableInListAsDefault() throws Exception {
		assertThat(a(newValidAttribute()).isDisplayableInList(), equalTo(false));
		assertThat(a(newValidAttribute().thatIsDisplayableInList(false)).isDisplayableInList(), equalTo(false));
		assertThat(a(newValidAttribute().thatIsDisplayableInList(true)).isDisplayableInList(), equalTo(true));
	}

	@Test
	public void attributeIsNotMandatoryAsDefault() throws Exception {
		assertThat(a(newValidAttribute()).isMandatory(), equalTo(false));
		assertThat(a(newValidAttribute().thatIsMandatory(false)).isMandatory(), equalTo(false));
		assertThat(a(newValidAttribute().thatIsMandatory(true)).isMandatory(), equalTo(true));
	}

	@Test
	public void attributeValuesMustNotBeUniqueAsDefault() throws Exception {
		assertThat(a(newValidAttribute()).isUnique(), equalTo(false));
		assertThat(a(newValidAttribute().thatIsUnique(false)).isUnique(), equalTo(false));
		assertThat(a(newValidAttribute().thatIsUnique(true)).isUnique(), equalTo(true));
	}

	@Test
	public void attributeIsActiveAsDefault() throws Exception {
		assertThat(a(newValidAttribute()).isActive(), equalTo(true));
		assertThat(a(newValidAttribute().thatIsActive(false)).isActive(), equalTo(false));
		assertThat(a(newValidAttribute().thatIsActive(true)).isActive(), equalTo(true));
	}

	@Test
	public void booleanAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("BOOLEAN")).getType(), instanceOf(BooleanAttributeType.class));
	}

	@Test
	public void charAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("CHAR")).getType(), instanceOf(CharAttributeType.class));
	}

	@Test
	public void dateAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("DATE")).getType(), instanceOf(DateAttributeType.class));
	}

	@Test(expected = NullPointerException.class)
	public void decimalAttributesRequiresPrecisionAndScaleParameters() throws Exception {
		a(newValidAttribute().withType("DECIMAL"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void decimalAttributesRequiresPrecisionGreaterThanZero() throws Exception {
		a(newValidAttribute().withType("DECIMAL") //
				.withPrecision(0).withScale(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void decimalAttributesRequiresScaleEqualOrLowerThanPrecision() throws Exception {
		a(newValidAttribute().withType("DECIMAL") //
				.withPrecision(5).withScale(6));
	}

	@Test
	public void decimalAttributesRequiresPrecisionGreaterThanZeroAndScaleLowerThanPrecision() throws Exception {
		assertThat(a(newValidAttribute().withType("DECIMAL") //
				.withPrecision(5).withScale(2)).getType(), instanceOf(DecimalAttributeType.class));
	}

	@Test
	public void doubleAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("DOUBLE")).getType(), instanceOf(DoubleAttributeType.class));
	}

	@Test
	public void ipAddressAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("INET")).getType(), instanceOf(IpAddressAttributeType.class));
	}

	@Test
	public void integerAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("INTEGER")).getType(), instanceOf(IntegerAttributeType.class));
	}

	@Test(expected = NullPointerException.class)
	public void stringAttributesRequiresLength() throws Exception {
		a(newValidAttribute().withType("STRING"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void stringAttributesWithNegativeLengthParameterIsNotValid() throws Exception {
		a(newValidAttribute().withType("STRING").withLength(-1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void stringAttributesWithZeroLengthParameterIsNotValid() throws Exception {
		a(newValidAttribute().withType("STRING").withLength(0));
	}

	@Test
	public void stringAttributesWithValidLengthParameter() throws Exception {
		final CMAttributeType<?> attributeType = a(newValidAttribute().withType("STRING").withLength(42)).getType();
		final StringAttributeType stringAttributeType = (StringAttributeType) attributeType;
		assertThat(stringAttributeType.length, equalTo(42));
	}

	@Test
	public void timeAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("TIME")).getType(), instanceOf(TimeAttributeType.class));
	}

	@Test
	public void timestampAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("TIMESTAMP")).getType(), instanceOf(DateTimeAttributeType.class));
	}

	@Test
	public void textAttributesDoesNotRequireParameters() throws Exception {
		assertThat(a(newValidAttribute().withType("TEXT")).getType(), instanceOf(TextAttributeType.class));
	}

	/*
	 * Utilities
	 */

	private static Attribute a(final AttributeBuilder attributeBuilder) {
		return attributeBuilder.build();
	}

	private AttributeBuilder newValidAttribute() {
		return newEmptyAttribute() //
				.withOwnerName(OWNER) //
				.withName(NAME) //
				.withType(TYPE_THAT_DOES_NOT_REQUIRE_PARAMS);
	}

	private AttributeBuilder newEmptyAttribute() {
		return Attribute.newAttribute();
	}

}
