package org.cmdbuild.dao.query.clause;

import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;

import com.google.common.base.Function;

public class FunctionCall implements CMFunctionCall {

	final Function<CMFunctionParameter, CMAttribute> parameterToAttributeFunction = new Function<CMFunctionParameter, CMAttribute>() {
		@Override
		public CMAttribute apply(final CMFunctionParameter input) {
			return new CMAttribute() {

				@Override
				public CMEntryType getOwner() {
					return FunctionCall.this;
				}

				@Override
				public CMAttributeType<?> getType() {
					return input.getType();
				}

				@Override
				public String getName() {
					return input.getName();
				}

				@Override
				public String getDescription() {
					return input.getName();
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
				public boolean isActive() {
					return true;
				}

				@Override
				public boolean isDisplayableInList() {
					return true;
				}

				@Override
				public boolean isMandatory() {
					return false;
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
					return EMPTY;
				}

				@Override
				public String getGroup() {
					return EMPTY;
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

			};
		}
	};

	final CMFunction function;
	final List<Object> params;

	private FunctionCall(final CMFunction function, final List<Object> params) {
		Validate.notNull(function);
		Validate.isTrue(function.getInputParameters().size() == params.size(),
				"Number of parameters not matching the function signature");
		this.function = function;
		this.params = normalizeFunctionParams(function, params);
	}

	private List<Object> normalizeFunctionParams(final CMFunction function, final List<Object> values) {
		final List<Object> actualParamsNormalized = new ArrayList<Object>(values.size());
		final Iterator<CMFunctionParameter> formalParams = function.getInputParameters().iterator();
		final Iterator<Object> actualParams = values.iterator();
		while (formalParams.hasNext()) {
			final CMAttributeType<?> paramType = formalParams.next().getType();
			final Object value = actualParams.next();
			final Object normalizedValue = paramType.convertValue(value);
			actualParamsNormalized.add(normalizedValue);
		}
		return actualParamsNormalized;
	}

	@Override
	public CMFunction getFunction() {
		return function;
	}

	@Override
	public List<Object> getParams() {
		return params;
	}

	public static FunctionCall call(final CMFunction function, final Object... actualParameters) {
		return new FunctionCall(function, Arrays.asList(actualParameters));
	}

	public static FunctionCall call(final CMFunction function, final Map<String, Object> actualParametersMap) {
		final List<CMFunctionParameter> formalParameters = function.getInputParameters();
		final List<Object> actualParameters = buildActualParametersList(formalParameters, actualParametersMap);
		return new FunctionCall(function, actualParameters);
	}

	private static List<Object> buildActualParametersList(final List<CMFunctionParameter> formalParameters,
			final Map<String, Object> actualParametersMap) {
		final List<Object> actualParameters = new ArrayList<Object>(formalParameters.size());
		for (final CMFunctionParameter fp : formalParameters) {
			final Object ap = actualParametersMap.get(fp.getName());
			actualParameters.add(ap);
		}
		return actualParameters;
	}

	@Override
	public CMIdentifier getIdentifier() {
		return function.getIdentifier();
	}

	@Override
	public Long getId() {
		return function.getId();
	}

	@Override
	public String getName() {
		return getIdentifier().getLocalName();
	}

	@Override
	public String getDescription() {
		return getIdentifier().getLocalName();
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	@Override
	public boolean isSystemButUsable() {
		return false;
	}

	@Override
	public boolean isBaseClass() {
		return false;
	}

	@Override
	public Iterable<? extends CMAttribute> getActiveAttributes() {
		return getAttributes();
	}

	@Override
	public Iterable<? extends CMAttribute> getAttributes() {
		return getAllAttributes();
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		return transform(function.getOutputParameters(), parameterToAttributeFunction);
	}

	@Override
	public CMAttribute getAttribute(final String name) {
		if (name == null) {
			return null;
		}
		for (final CMFunction.CMFunctionParameter param : function.getOutputParameters()) {
			if (name.equals(param.getName())) {
				return parameterToAttributeFunction.apply(param);
			}
		}
		return null;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public String getPrivilegeId() {
		return String.format("Function:%d", getId());
	}

	@Override
	public void accept(final CMEntryTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean holdsHistory() {
		return false;
	}

	@Override
	public String getKeyAttributeName() {
		// TODO really needed here? considering that there is no sense for
		// functions
		return null;
	}

}
