package org.cmdbuild.dao.function;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.dao.entrytype.DBIdentifier.fromName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.DBTypeObject;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

public class DBFunction extends DBTypeObject implements CMFunction {

	public static class FunctionMetadata extends EntryTypeMetadata {

		private static Function<String, Category> STRING_TO_CATEGORY = new Function<String, Category>() {

			public Category apply(String input) {
				return Category.of(input);
			};

		};

		public static final String CATEGORIES = BASE_NS + "categories";

		private static final String CATEGORIES_SEPARATOR = ",";

		public Iterable<Category> getCategories() {
			return from(asList(defaultString(get(CATEGORIES)).split(CATEGORIES_SEPARATOR))) //
					.transform(STRING_TO_CATEGORY);

		}
	}

	private static class DBFunctionParameter implements CMFunctionParameter {

		private final String name;
		private final CMAttributeType<?> type;

		DBFunctionParameter(final String name, final CMAttributeType<?> type) {
			Validate.notEmpty(name);
			Validate.notNull(type);
			this.name = name;
			this.type = type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public CMAttributeType<?> getType() {
			return type;
		}

	}

	private final List<CMFunctionParameter> inputParameters;
	private final List<CMFunctionParameter> outputParameters;

	private final List<CMFunctionParameter> unmodifiableInputParameters;
	private final List<CMFunctionParameter> unmodifiableOutputParameters;

	private final boolean returnsSet;

	private final Set<Category> categories;

	public DBFunction(final String name, final Long id, final boolean returnsSet) {
		this(fromName(name), id, returnsSet);
	}

	public DBFunction(final CMIdentifier identifier, final Long id, final boolean returnsSet) {
		super(identifier, id);
		this.inputParameters = new ArrayList<CMFunctionParameter>();
		this.unmodifiableInputParameters = Collections.unmodifiableList(inputParameters);
		this.outputParameters = new ArrayList<CMFunctionParameter>();
		this.unmodifiableOutputParameters = Collections.unmodifiableList(outputParameters);
		this.returnsSet = returnsSet;
		this.categories = Sets.newHashSet();
	}

	@Override
	public boolean returnsSet() {
		return returnsSet;
	}

	@Override
	public List<CMFunctionParameter> getInputParameters() {
		return unmodifiableInputParameters;
	}

	@Override
	public List<CMFunctionParameter> getOutputParameters() {
		return unmodifiableOutputParameters;
	}

	public void addInputParameter(final String name, final CMAttributeType<?> type) {
		inputParameters.add(new DBFunctionParameter(name, type));
	}

	public void addOutputParameter(final String name, final CMAttributeType<?> type) {
		outputParameters.add(new DBFunctionParameter(name, type));
	}

	@Override
	public Iterable<Category> getCategories() {
		return categories;
	}

	public void addCategories(final Iterable<Category> categories) {
		addAll(this.categories, categories);
	}

}
