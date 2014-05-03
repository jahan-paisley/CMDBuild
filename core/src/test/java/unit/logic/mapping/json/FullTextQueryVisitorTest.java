package unit.logic.mapping.json;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue;
import org.cmdbuild.dao.query.clause.where.OperatorAndValue;
import org.cmdbuild.logic.mapping.json.JsonFullTextQueryBuilder.FullTextQueryOperatorVisitor;
import org.junit.Test;

public class FullTextQueryVisitorTest {

	/**
	 * INTEGER
	 */
	@Test
	public void shouldReturnEqualsOperatorAndValueForIntegerType() {
		// given
		final CMAttributeType<?> attributeType = new IntegerAttributeType();
		final String fullText = "-11";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNotNull(operatorAndValue);
		assertTrue(operatorAndValue instanceof EqualsOperatorAndValue);
	}

	@Test
	public void shouldReturnNullIfFullTextQueryIsNotAnInteger() {
		// given
		final CMAttributeType<?> attributeType = new IntegerAttributeType();
		final String fullText = "11abcd";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNull(operatorAndValue);
	}

	/**
	 * DOUBLE
	 */
	@Test
	public void shouldReturnEqualsOperatorAndValueForDoubleType() {
		// given
		final CMAttributeType<?> attributeType = new DoubleAttributeType();
		final String fullText = "-15.456";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNotNull(operatorAndValue);
		assertTrue(operatorAndValue instanceof EqualsOperatorAndValue);
	}

	@Test
	public void shouldReturnNullIfFullTextQueryIsNotADouble() {
		// given
		final CMAttributeType<?> attributeType = new DoubleAttributeType();
		final String fullText = "11abcd";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNull(operatorAndValue);
	}

	/**
	 * DECIMAL
	 */
	@Test
	public void shouldReturnEqualsOperatorAndValueForDecimalType() {
		// given
		final CMAttributeType<?> attributeType = new DecimalAttributeType();
		final String fullText = "12.34";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNotNull(operatorAndValue);
		assertTrue(operatorAndValue instanceof EqualsOperatorAndValue);
	}

	@Test
	public void shouldReturnNullIfFullTextQueryIsNotADecimal() {
		// given
		final CMAttributeType<?> attributeType = new DecimalAttributeType();
		final String fullText = "12,34";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNull(operatorAndValue);
	}

	/**
	 * STRING
	 */
	@Test
	public void shouldReturnContainsOperatorAndValueForStringType() {
		// given
		final CMAttributeType<?> attributeType = new StringAttributeType();
		final String fullText = "bla_ bla   bla";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNotNull(operatorAndValue);
		assertTrue(operatorAndValue instanceof ContainsOperatorAndValue);
	}

	/**
	 * TEXT
	 */
	@Test
	public void shouldReturnContainsOperatorAndValueForTextType() {
		// given
		final CMAttributeType<?> attributeType = new TextAttributeType();
		final String fullText = " text to_search...";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNotNull(operatorAndValue);
		assertTrue(operatorAndValue instanceof ContainsOperatorAndValue);
	}

	/**
	 * INET
	 */
	@Test
	public void shouldReturnEqualOperatorAndValueForInetType() {
		// given
		final CMAttributeType<?> attributeType = new IpAddressAttributeType();
		final String fullText = "192.168.0.2";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNotNull(operatorAndValue);
		assertTrue(operatorAndValue instanceof EqualsOperatorAndValue);
	}

	@Test
	public void shouldReturnNullIfFullTextQueryIsNotAValidIpAddress() {
		// given
		final CMAttributeType<?> attributeType = new IpAddressAttributeType();
		final String fullText = "192.168.024.2554";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNull(operatorAndValue);
	}

	/**
	 * BOOLEAN
	 */
	@Test
	public void shouldReturnEqualOperatorAndValueForBooleanType() {
		// given
		final CMAttributeType<?> attributeType = new BooleanAttributeType();
		final String fullText = "true";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNotNull(operatorAndValue);
		assertTrue(operatorAndValue instanceof EqualsOperatorAndValue);
	}

	@Test
	public void shouldReturnNullIfFullTextQueryIsNotBoolean() {
		// given
		final CMAttributeType<?> attributeType = new BooleanAttributeType();
		final String fullText = "not_boolean_string";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNull(operatorAndValue);
	}

	/**
	 * DATE
	 */
	@Test
	public void shouldReturnEqualOperatorAndValueForDateType() {
		// given
		final CMAttributeType<?> attributeType = new DateAttributeType();
		final String fullText = "08/01/2013";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNotNull(operatorAndValue);
		assertTrue(operatorAndValue instanceof EqualsOperatorAndValue);
	}

	@Test
	public void shouldReturnNullIfFullTextQueryIsNotAValidDate() {
		// given
		final CMAttributeType<?> attributeType = new DateAttributeType();
		final String fullText = "not_date_string";
		final FullTextQueryOperatorVisitor visitor = new FullTextQueryOperatorVisitor(attributeType, fullText);

		// when
		final OperatorAndValue operatorAndValue = visitor.getOperatorAndValue();

		// then
		assertNull(operatorAndValue);
	}

}
