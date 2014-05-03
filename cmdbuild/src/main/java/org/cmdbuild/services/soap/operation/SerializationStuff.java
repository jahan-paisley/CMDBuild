package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.common.Constants.Webservices.BOOLEAN_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.CHAR_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.DATE_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.DECIMAL_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.DOUBLE_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.FOREIGNKEY_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.INET_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.INTEGER_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.LOOKUP_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.REFERENCE_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.STRING_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.TEXT_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.TIMESTAMP_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.TIME_TYPE_NAME;
import static org.cmdbuild.common.Constants.Webservices.UNKNOWN_TYPE_NAME;
import static org.cmdbuild.services.meta.MetadataService.SYSTEM_TEMPLATE_PREFIX;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.types.Metadata;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

class SerializationStuff {

	private static final Logger logger = SoapLogicHelper.logger;
	private static final Marker marker = MarkerFactory.getMarker(SerializationStuff.class.getName());

	private static final Function<org.cmdbuild.model.data.Metadata, Metadata> TO_SOAP_METADATA = new Function<org.cmdbuild.model.data.Metadata, Metadata>() {

		@Override
		public Metadata apply(final org.cmdbuild.model.data.Metadata input) {
			final Metadata element = new Metadata();
			element.setKey(input.name);
			element.setValue(input.value);
			return element;
		}

	};

	private final CMDataView dataView;
	private final MetadataStoreFactory metadataStoreFactory;

	SerializationStuff(final CMDataView dataView, final MetadataStoreFactory metadataStoreFactory) {
		this.dataView = dataView;
		this.metadataStoreFactory = metadataStoreFactory;
	}

	public AttributeSchema serialize(final CMAttribute attribute) {
		logger.info(marker, "serializing attribute '{}'", attribute.getName());
		return serialize(attribute, attribute.getIndex());
	}

	public AttributeSchema serialize(final CMAttribute attribute, final int index) {
		final AttributeSchema schema = new AttributeSchema();
		attribute.getType().accept(new CMAttributeTypeVisitor() {

			@Override
			public void visit(final BooleanAttributeType attributeType) {
				schema.setType(BOOLEAN_TYPE_NAME);
			}

			@Override
			public void visit(final CharAttributeType attributeType) {
				schema.setType(CHAR_TYPE_NAME);
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
				schema.setType(DATE_TYPE_NAME);
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
				schema.setType(TIMESTAMP_TYPE_NAME);
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				schema.setType(DECIMAL_TYPE_NAME);
				schema.setPrecision(attributeType.precision);
				schema.setScale(attributeType.scale);
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
				schema.setType(DOUBLE_TYPE_NAME);
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
				schema.setType(UNKNOWN_TYPE_NAME);
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				schema.setType(LOOKUP_TYPE_NAME);
				schema.setLookupType(attributeType.getLookupTypeName());
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				schema.setType(FOREIGNKEY_TYPE_NAME);
				final CMClass targetClass = dataView.findClass(attributeType.getForeignKeyDestinationClassName());
				schema.setReferencedClassName(targetClass.getName());
				schema.setReferencedIdClass(targetClass.getId().intValue());
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
				schema.setType(INTEGER_TYPE_NAME);
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
				schema.setType(INET_TYPE_NAME);
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				schema.setType(REFERENCE_TYPE_NAME);
				final CMDomain domain = dataView.findDomain(attributeType.getDomainName());
				if (domain == null) {
					logger.error("cannot find domain '{}'", attributeType.getDomainName());
				}
				if (domain.getClass1().getName().equals(attribute.getOwner().getName())) {
					schema.setReferencedClassName(domain.getClass2().getName());
					schema.setReferencedIdClass(domain.getClass2().getId().intValue());
				} else {
					schema.setReferencedClassName(domain.getClass1().getName());
					schema.setReferencedIdClass(domain.getClass1().getId().intValue());
				}
			}

			@Override
			public void visit(final StringArrayAttributeType attributeType) {
				schema.setType(UNKNOWN_TYPE_NAME);
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				schema.setType(STRING_TYPE_NAME);
				schema.setLength(attributeType.length);
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				schema.setType(TEXT_TYPE_NAME);
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
				schema.setType(TIME_TYPE_NAME);
			}

		});
		schema.setIdClass(attribute.getOwner().getId().intValue());
		schema.setName(attribute.getName());
		schema.setDescription(attribute.getDescription());
		schema.setBaseDSP(attribute.isDisplayableInList());
		schema.setUnique(attribute.isUnique());
		schema.setNotnull(attribute.isMandatory());
		schema.setInherited(attribute.isInherited());
		schema.setIndex(index);
		schema.setFieldmode(serialize(attribute.getMode()));
		schema.setDefaultValue(attribute.getDefaultValue());
		schema.setClassorder(attribute.getClassOrder());
		schema.setMetadata(from(concat(storedMetadata(attribute), filterMetadata(attribute))).toArray(Metadata.class) //
		);
		return schema;
	}

	private FluentIterable<Metadata> storedMetadata(final CMAttribute attribute) {
		final Store<org.cmdbuild.model.data.Metadata> store = metadataStoreFactory.storeForAttribute(attribute);
		final Iterable<org.cmdbuild.model.data.Metadata> elements = store.list();
		return from(elements) //
				.transform(TO_SOAP_METADATA);
	}

	private Iterable<Metadata> filterMetadata(final CMAttribute attribute) {
		final List<Metadata> elements = Lists.newArrayList();
		final String filter = attribute.getFilter();
		if (isNotBlank(filter)) {
			final Metadata m = new Metadata();
			m.setKey(SYSTEM_TEMPLATE_PREFIX);
			m.setValue(filter);
			elements.add(m);
		}
		return elements;
	}

	public static String serialize(final Mode mode) {
		switch (mode) {
		case WRITE:
			return "write";
		case READ:
			return "read";
		case HIDDEN:
			return "hidden";
		}
		throw new IllegalArgumentException(format("invalid mode '%s'", mode));
	}

}
