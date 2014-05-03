package unit;

import static org.cmdbuild.dao.entrytype.EntryTypeAnalyzer.inspect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBIdentifier;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class EntryTypeAnalyzerTest {

	private CMDataView view;

	@Before
	public void setUp() {
		view = mock(CMDataView.class);
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotCreateAEntryTypeAnalyzerWithNullEntryType() throws Exception {
		inspect(null, null);
	}

	@Test
	public void shouldReturnFalseIfTheEntryTypeDoesNotHaveExternalReferenceActiveAndNotReservedAttributes()
			throws Exception {
		// given
		final CMAttribute integerAttribute = mockAttribute(new IntegerAttributeType(), false, true);
		final CMAttribute booleanAttribute = mockAttribute(new BooleanAttributeType(), false, true);
		final List<CMAttribute> entryTypeAttributes = Lists.newArrayList();
		entryTypeAttributes.add(integerAttribute);
		entryTypeAttributes.add(booleanAttribute);
		final CMClass clazz = mockClass("foo", entryTypeAttributes);

		// when
		final boolean hasExternalReference = inspect(clazz, view).hasExternalReferences();

		// then
		assertFalse(hasExternalReference);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotConsiderInactiveAttributes() throws Exception {
		// given
		final CMAttribute referenceAttribute = mockAttribute(
				new ReferenceAttributeType(DBIdentifier.fromName("ref_attr")), false, false);
		final CMAttribute lookupAttribute = mockAttribute(new LookupAttributeType("lookup_name"), false, false);
		final CMAttribute booleanAttribute = mockAttribute(new BooleanAttributeType(), false, true);
		final List<CMAttribute> entryTypeAttributes = Lists.newArrayList();
		entryTypeAttributes.add(booleanAttribute);
		final CMClass clazz = mockClass("foo", entryTypeAttributes);
		final List<CMAttribute> activeAndInactiveAttributes = Lists.newArrayList();
		activeAndInactiveAttributes.add(booleanAttribute);
		activeAndInactiveAttributes.add(referenceAttribute);
		activeAndInactiveAttributes.add(lookupAttribute);
		when((Iterable<CMAttribute>) clazz.getAttributes()).thenReturn(activeAndInactiveAttributes);

		// when
		final boolean hasExternalReference = inspect(clazz, view).hasExternalReferences();

		// then
		assertFalse(hasExternalReference);
	}

	@Test
	public void shouldReturnCorrectListOfExternalReferenceAttributes() throws Exception {
		// given
		final CMAttribute referenceAttribute = mockAttribute(
				new ReferenceAttributeType(DBIdentifier.fromName("ref_attr")), false, true);
		final CMAttribute lookupAttribute = mockAttribute(new LookupAttributeType("lookup_name"), false, true);
		final CMAttribute booleanAttribute = mockAttribute(new BooleanAttributeType(), false, true);
		final List<CMAttribute> entryTypeAttributes = Lists.newArrayList();
		entryTypeAttributes.add(booleanAttribute);
		entryTypeAttributes.add(referenceAttribute);
		entryTypeAttributes.add(lookupAttribute);
		final CMClass clazz = mockClass("foo", entryTypeAttributes);

		// when
		final Iterable<CMAttribute> foreignKeyAttributes = inspect(clazz, view).getForeignKeyAttributes();
		final Iterable<CMAttribute> referenceAttributes = inspect(clazz, view).getReferenceAttributes();
		final Iterable<CMAttribute> lookupAttributes = inspect(clazz, view).getLookupAttributes();

		// then
		assertEquals(0, Iterables.size(foreignKeyAttributes));
		assertEquals(1, Iterables.size(referenceAttributes));
		assertEquals(1, Iterables.size(lookupAttributes));
	}

	@SuppressWarnings("rawtypes")
	private CMAttribute mockAttribute(final CMAttributeType<?> attributeType, final boolean isSystem,
			final boolean isActive) {
		final CMAttribute attribute = mock(CMAttribute.class);
		when(attribute.isSystem()).thenReturn(isSystem);
		when(attribute.isActive()).thenReturn(isActive);
		when((CMAttributeType) attribute.getType()).thenReturn(attributeType);
		return attribute;
	}

	@SuppressWarnings("unchecked")
	private CMClass mockClass(final String name, final Iterable<CMAttribute> activeAttributes) {
		final CMClass clazz = mock(CMClass.class);
		when(clazz.getName()).thenReturn("foo");
		when((Iterable<CMAttribute>) clazz.getActiveAttributes()).thenReturn(activeAttributes);
		return clazz;
	}

}
