package org.cmdbuild.report;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.services.store.report.JDBCReportStore.REPORT_CLASS_NAME;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import net.sf.jasperreports.engine.JRParameter;

import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.ForwardingEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.report.RPReference.ReportReferenceAttributeType;

public class ReportParameterConverter {

	private static class ReportAttribute implements CMAttribute {

		private static final CMEntryType UNSUPPORTED_ENTRY_TYPE = UnsupportedProxyFactory.of(CMEntryType.class)
				.create();
		private static final CMEntryType OWNER = new ForwardingEntryType(UNSUPPORTED_ENTRY_TYPE) {

			private final long FAKE_ID = 0L;

			/**
			 * This {@link CMIdentifier} is completely fake but it's formally
			 * correct. It has been created to avoid problems with attributes
			 * serialization.
			 */
			private final CMIdentifier FAKE_IDENTIFIER = new CMIdentifier() {

				private final String localname = REPORT_CLASS_NAME + "_" + randomNumeric(10);
				private final String namespace = REPORT_CLASS_NAME + "_" + randomNumeric(10);

				@Override
				public String getLocalName() {
					return localname;
				}

				@Override
				public String getNameSpace() {
					return namespace;
				}

			};

			/*
			 * Should be the only methods called.
			 */

			@Override
			public Long getId() {
				return FAKE_ID;
			};

			@Override
			public CMIdentifier getIdentifier() {
				return FAKE_IDENTIFIER;
			};

		};

		private final CMAttributeType<?> type;
		private final ReportParameter rp;

		public ReportAttribute(final CMAttributeType<?> type, final ReportParameter rp) {
			this.type = type;
			this.rp = rp;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public CMEntryType getOwner() {
			return OWNER;
		}

		@Override
		public CMAttributeType<?> getType() {
			return type;
		}

		@Override
		public String getName() {
			return rp.getFullName();
		}

		@Override
		public String getDescription() {
			return rp.getDescription();
		}

		@Override
		public boolean isSystem() {
			return false;
		}

		@Override
		public boolean isInherited() {
			return false;
		}

		@Override
		public boolean isDisplayableInList() {
			return true;
		}

		@Override
		public boolean isMandatory() {
			return rp.isRequired();
		}

		@Override
		public boolean isUnique() {
			return false;
		}

		@Override
		public Mode getMode() {
			return Mode.WRITE;
		}

		@Override
		public int getIndex() {
			return 0;
		}

		@Override
		public String getDefaultValue() {
			if (rp.hasDefaultValue()) {
				return rp.getDefaultValue();
			}
			return EMPTY;
		}

		@Override
		public String getGroup() {
			return null;
		}

		@Override
		public int getClassOrder() {
			return 0;
		}

		@Override
		public String getEditorType() {
			return EMPTY;
		}

		@Override
		public String getFilter() {
			return EMPTY;
		}

		@Override
		public String getForeignKeyDestinationClassName() {
			return EMPTY;
		}

	}

	public static ReportParameterConverter of(final ReportParameter reportParameter) {
		return new ReportParameterConverter(reportParameter);
	}

	private final ReportParameter reportParameter;

	private ReportParameterConverter(final ReportParameter reportParameter) {
		this.reportParameter = reportParameter;
	}

	public CMAttribute toCMAttribute() {
		final CMAttributeType<?> attributeType = new ReportParameterVisitor() {

			private CMAttributeType<?> attributeType;

			public CMAttributeType<?> attributeType() {
				reportParameter.accept(this);
				return attributeType;
			}

			@Override
			public void accept(final RPFake fake) {
				attributeType = new StringAttributeType(100);
			}

			@Override
			public void accept(final RPLookup lookup) {
				attributeType = new LookupAttributeType(lookup.getLookupName());
			}

			@Override
			public void accept(final RPReference reference) {
				attributeType = new ReportReferenceAttributeType(reference.getClassName());
			}

			@Override
			public void accept(final RPSimple simple) {
				final JRParameter jrParameter = simple.getJrParameter();
				final Class<?> valueClass = jrParameter.getValueClass();
				if (asList(String.class).contains(valueClass)) {
					attributeType = new StringAttributeType(100);
				} else if (asList(Integer.class, Long.class, Short.class, BigDecimal.class, Number.class).contains(
						valueClass)) {
					attributeType = new IntegerAttributeType();
				} else if (asList(Date.class).contains(valueClass)) {
					attributeType = new DateAttributeType();
				} else if (asList(Timestamp.class).contains(valueClass)) {
					attributeType = new DateTimeAttributeType();
				} else if (asList(Time.class).contains(valueClass)) {
					attributeType = new TimeAttributeType();
				} else if (asList(Double.class, Float.class).contains(valueClass)) {
					attributeType = new DoubleAttributeType();
				} else if (asList(Boolean.class).contains(valueClass)) {
					attributeType = new BooleanAttributeType();
				} else {
					throw ReportExceptionType.REPORT_INVALID_PARAMETER_CLASS.createException();
				}
			}

		}.attributeType();
		return new ReportAttribute(attributeType, reportParameter);
	}

}
