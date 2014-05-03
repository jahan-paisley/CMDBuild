package org.cmdbuild.dao.driver.postgres.quote;

import static java.lang.String.format;
import static org.cmdbuild.dao.driver.postgres.Const.DOMAIN_PREFIX;

import java.util.List;

import org.cmdbuild.dao.driver.postgres.SqlType;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.function.CMFunction;

public class EntryTypeQuoter extends AbstractEntryTypeQuoter implements CMEntryTypeVisitor {

	public static class DomainIdentifier implements CMIdentifier {

		private final CMIdentifier inner;

		public DomainIdentifier(final CMEntryType entryType) {
			this(entryType.getIdentifier());
		}

		public DomainIdentifier(final CMIdentifier identifier) {
			this.inner = identifier;
		}

		@Override
		public String getLocalName() {
			return DOMAIN_PREFIX + inner.getLocalName();
		}

		@Override
		public String getNameSpace() {
			return inner.getNameSpace();
		}

	}

	private static final ParamAdder NULL_PARAM_ADDER = new ParamAdder() {

		@Override
		public void add(final Object value) {
			// nothing to do
		}

	};

	public static String quote(final CMEntryType entryType) {
		return new EntryTypeQuoter(entryType).quote();
	}

	public static String quote(final CMEntryType entryType, final ParamAdder paramAdder) {
		return new EntryTypeQuoter(entryType, paramAdder).quote();
	}

	private final ParamAdder paramAdder;

	private String quotedTypeName;

	public EntryTypeQuoter(final CMEntryType entryType) {
		this(entryType, NULL_PARAM_ADDER);
	}

	/*
	 * TODO params adder is needed for functions... should be done in another
	 * way
	 */
	public EntryTypeQuoter(final CMEntryType entryType, final ParamAdder paramAdder) {
		super(entryType);
		this.paramAdder = paramAdder;
	}

	@Override
	public String quote() {
		entryType.accept(this);
		return quotedTypeName;
	}

	@Override
	public void visit(final CMClass entryType) {
		quotedTypeName = quoteClassOrDomain(entryType.getIdentifier());
	}

	@Override
	public void visit(final CMDomain entryType) {
		final CMIdentifier identifier = entryType.getIdentifier();
		quotedTypeName = quoteClassOrDomain(new DomainIdentifier(identifier));
	}

	@Override
	public void visit(final CMFunctionCall entryType) {
		quotedTypeName = format("%s(%s)", IdentQuoter.quote(entryType.getFunction().getIdentifier().getLocalName()),
				functionParams(entryType));
	}

	private String functionParams(final CMFunctionCall functionCall) {
		final List<CMFunction.CMFunctionParameter> functionParameters = functionCall.getFunction().getInputParameters();
		final List<Object> params = functionCall.getParams();
		for (int i = 0; i < functionParameters.size(); i++) {
			final CMFunction.CMFunctionParameter functionParameter = functionParameters.get(i);
			final Object param = params.get(i);
			final SqlType sqlType = SqlType.getSqlType(functionParameter.getType());
			paramAdder.add(sqlType.javaToSqlValue(param));
		}
		return genQuestionMarks(functionCall.getParams().size());
	}

	private String genQuestionMarks(final int length) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; ++i) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append("?");
		}
		return sb.toString();
	}

}
