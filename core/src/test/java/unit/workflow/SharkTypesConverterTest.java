package unit.workflow;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.workflow.SharkTypesConverter;
import org.cmdbuild.workflow.WorkflowTypesConverter;
import org.cmdbuild.workflow.WorkflowTypesConverter.Lookup;
import org.cmdbuild.workflow.WorkflowTypesConverter.Reference;
import org.cmdbuild.workflow.type.ReferenceType;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

public class SharkTypesConverterTest {

	private static final CMAttributeType<?> NO_ATTRIBUTE_TYPE_PLEASE_FIX_ME = null;

	private final CMDataView dataView = mock(CMDataView.class);
	private final LookupStore lookupStore = mock(LookupStore.class);
	private final WorkflowTypesConverter converter = SharkTypesConverter.newInstance() //
			.withDataView(dataView) //
			.withLookupStore(lookupStore) //
			.build();

	@Test
	public void returnsTheSameObjectIfConversionNotNeeded() {
		assertThat(converter.toWorkflowType(NO_ATTRIBUTE_TYPE_PLEASE_FIX_ME, null), is(nullValue()));
		assertNotConverted("Fifteen");
		assertNotConverted(true);
		assertNotConverted(false);
	}

	@Test
	public void integersAreConvertedToLong() {
		assertConverted(15, 15L);
	}

	@Test
	public void jodaDateTimesAreConvertedToJavaDates() {
		final long instant = 123456L;
		assertConverted(new DateTime(instant), new Date(instant));
	}

	@Test
	public void bigDecimalAreConvertedToDouble() {
		assertConverted(new BigDecimal(1.5d), 1.5d);
	}

	@Test
	public void lookupsAreConvertedToLookupTypeDTOs() {
		final Lookup src = mock(Lookup.class);
		when(src.getId()) //
				.thenReturn(42L);

		when(lookupStore.read(any(Storable.class))) //
				.thenReturn(org.cmdbuild.data.store.lookup.Lookup.newInstance() //
						.withId(42L) //
						.withType(LookupType.newInstance() //
								.withName("t")) //
						.withCode("c") //
						.withDescription("d") //
						.build());

		final org.cmdbuild.workflow.type.LookupType dst = org.cmdbuild.workflow.type.LookupType.class.cast(converter
				.toWorkflowType(NO_ATTRIBUTE_TYPE_PLEASE_FIX_ME, src));

		assertThat(dst.getType(), is("t"));
		assertThat(dst.getId(), is(42));
		assertThat(dst.getCode(), is("c"));
		assertThat(dst.getDescription(), is("d"));
	}

	@Ignore("should find a better way for mock card fetcher")
	@Test
	public void cardReferencesAreConvertedToReferenceTypeDTOs() {
		final CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(12L);

		when(dataView.findClass("Class")) //
				.thenReturn(srcClass);
		// when(dataView.findClass(srcClass.getName())) //
		// .thenReturn(srcClass);

		final Reference src = mock(Reference.class);
		when(src.getId()) //
				.thenReturn(42L);

		final ReferenceType dst = ReferenceType.class.cast(converter.toWorkflowType(NO_ATTRIBUTE_TYPE_PLEASE_FIX_ME,
				src));

		assertThat(dst.getId(), is(42));
		assertThat(dst.getIdClass(), is(12));
		assertThat(dst.getDescription(), is(""));
	}

	@Ignore("should find a better way for mock card fetcher")
	@Test
	public void cardReferenceArraysAreConvertedToReferenceTypeDTOArrays() {
		final CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(12L);

		when(dataView.findClass(srcClass.getName())).thenReturn(srcClass);

		final Reference src0 = mock(Reference.class);
		when(src0.getId()) //
				.thenReturn(42L);
		final Reference[] src = new Reference[] { src0 };
		final ReferenceType[] dst = ReferenceType[].class.cast(converter.toWorkflowType(
				NO_ATTRIBUTE_TYPE_PLEASE_FIX_ME, src));

		assertThat(dst.length, is(src.length));
		final ReferenceType dst0 = dst[0];
		assertThat(dst0.getId(), is(42));
		assertThat(dst0.getIdClass(), is(12));
		assertThat(dst0.getDescription(), is(""));
	}

	@Test
	public void lookupTypesAreConvertedToLong() {
		final org.cmdbuild.workflow.type.LookupType src = new org.cmdbuild.workflow.type.LookupType();
		src.setType("t");
		src.setId(42);
		src.setCode("c");
		src.setDescription("d");

		final Long dst = Long.class.cast(converter.fromWorkflowType(src));

		assertThat(dst, is(42L));

		assertThat(converter.fromWorkflowType(new org.cmdbuild.workflow.type.LookupType()), is(nullValue()));
	}

	@Test
	public void referenceTypesAreConvertedToLong() {
		final ReferenceType src = new ReferenceType();
		src.setIdClass(666);
		src.setId(42);
		final CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(666L);

		final Long dst = Long.class.cast(converter.fromWorkflowType(src));
		assertThat(dst, is(42L));

		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void illegalReferenceTypesAreConvertedToNull() {
		assertThat(converter.fromWorkflowType(new ReferenceType()), is(nullValue()));

		verifyNoMoreInteractions(dataView);
	}

	/*
	 * Utils
	 */

	private void assertNotConverted(final Object src) {
		assertThat(converter.toWorkflowType(NO_ATTRIBUTE_TYPE_PLEASE_FIX_ME, src), is(sameInstance(src)));
	}

	private void assertConverted(final Object src, final Object dst) {
		assertThat(converter.toWorkflowType(NO_ATTRIBUTE_TYPE_PLEASE_FIX_ME, src), is(dst));
	}

}
