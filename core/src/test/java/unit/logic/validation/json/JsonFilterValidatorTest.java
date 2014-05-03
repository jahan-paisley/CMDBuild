package unit.logic.validation.json;

import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_CLASSNAME_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;

import org.cmdbuild.logic.validation.Validator;
import org.cmdbuild.logic.validation.Validator.ValidationError;
import org.cmdbuild.logic.validation.json.JsonFilterValidator;
import org.json.JSONObject;
import org.junit.Test;

public class JsonFilterValidatorTest {

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfFilterIsNull() throws Exception {
		// given
		final Validator validator = validatorFor(null);

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfFilterKeyDoesNotExist() throws Exception {
		// given
		final Validator validator = validatorFor(json("{not_existent_key: value}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfFilterObjectDoesNotContainCorrectKeys() throws Exception {
		// given
		final Validator validator = validatorFor(json("{not_expected_key: blah, not_expected_2: blahhblahh}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfAttributeObjectIsEmpty() throws Exception {
		// given
		final Validator validator = validatorFor(json("{attribute: {}}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfAttributeObjectDoesNotContainCorrectKeys() throws Exception {
		// given
		final Validator validator = validatorFor(json("{attribute: {not_expected: 1, not_exp: 2}}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfSimpleObjectDoesNotContainCorrectKeys() throws Exception {
		// given
		final Validator validator = validatorFor(json("{attribute: {simple: {}}}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfSimpleObjectDoesNotContainAllCorrectKeys() throws Exception {
		// given
		final Validator validator = validatorFor(json("{attribute: {simple: {not_expected_key: 1, attribute: blahh, operator: equal}}}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateFilterWithNotValidValuesInSimpleConditions() throws Exception {
		// given
		final Validator validator = validatorFor(json("{attribute: {simple: {attribute: blahh, operator: equal, value: blah}}}"));

		// when
		validator.validate();
	}

	@Test
	public void shouldValidateFilterWithAttributeConditions() throws Exception {
		// given
		final Validator validator = validatorFor(json("{attribute: {simple: {attribute: blah, operator: equal, value: [1]}}}"));

		// when
		validator.validate();
	}

	@Test
	public void shouldValidateFilterWithAttributeConditionsAndQueryCondition() throws Exception {
		// given
		final Validator validator = validatorFor(json("{attribute: {simple: {attribute: blah, operator: equal, value: [1]}}, query: full_text_query}"));

		// when
		validator.validate();
	}

	@Test
	public void shouldValidateIfRelationObjectIsEmpty() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: []}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationObjectDoesNotContainCorrectKeys() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{foo: bar}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotContainDomain() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{source: foo, destination: bar, type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotContainSource() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, destination: bar, type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotContainDestination() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotContainType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotHaveDomainValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: '', source: foo, destination: bar, type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleHasBlankDomainValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: ' ', source: foo, destination: bar, type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotHaveSourceValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: '', destination: bar, type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleHasBlankSourceValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: ' ', destination: bar, type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotHaveDestinationValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: '', type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleHasBlankDestinationValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: ' ', type: any}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotHaveTypeValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: ''}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesHasBlankTypeValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: ' '}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotHaveValidTypeValue() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: blah}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleDoesNotHaveCardsWhenSpecifyingOneOfType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: oneof}]}"));

		// when
		validator.validate();
	}

	@Test
	public void shouldValidateIfRelationRuleHasAnyType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: any}]}"));

		// when
		validator.validate();
	}

	@Test
	public void shouldValidateIfRelationRuleHasNoOneType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: noone}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleHasEmptyCardsWhenSpecifyingOneOfType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: oneof, cards: []}]}"));

		// when
		validator.validate();
	}

	@Test
	public void shouldValidateIfRelationRuleHasAtLeastOneValidCardsWhenSpecifyingOneOfType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: oneof, cards: [{"
				+ RELATION_CARD_ID_KEY + ": 42, " + RELATION_CARD_CLASSNAME_KEY + ": bar}]}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleHasCardWithNoIdWhenSpecifyingOneOfType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: oneof, cards: [{"
				+ RELATION_CARD_ID_KEY + ": '', " + RELATION_CARD_CLASSNAME_KEY + ": bar}]}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleHasCardWithBlankIdWhenSpecifyingOneOfType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: oneof, cards: [{"
				+ RELATION_CARD_ID_KEY + ": ' ', " + RELATION_CARD_CLASSNAME_KEY + ": bar}]}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleHasCardWithNoClassNameWhenSpecifyingOneOfType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: oneof, cards: [{"
				+ RELATION_CARD_ID_KEY + ": 42, " + RELATION_CARD_CLASSNAME_KEY + ": ''}]}]}"));

		// when
		validator.validate();
	}

	@Test(expected = ValidationError.class)
	public void shouldNotValidateIfRelationRuleHasCardWithBlankClassNameWhenSpecifyingOneOfType() throws Exception {
		// given
		final Validator validator = validatorFor(json("{relation: [{domain: foo, source: bar, destination: baz, type: oneof, cards: [{"
				+ RELATION_CARD_ID_KEY + ": 42, " + RELATION_CARD_CLASSNAME_KEY + ": ' '}]}]}"));

		// when
		validator.validate();
	}

	/*
	 * Utilities
	 */

	private JsonFilterValidator validatorFor(final JSONObject json) {
		return new JsonFilterValidator(json);
	}

	private JSONObject json(final String string) throws Exception {
		return new JSONObject(string);
	}

}
