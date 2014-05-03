package org.cmdbuild.cql.facade;

import static java.lang.String.format;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.cql.compiler.CQLCompiler;
import org.cmdbuild.cql.compiler.CQLCompilerListener;
import org.cmdbuild.cql.compiler.impl.FactoryImpl;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.cql.facade.CQLAnalyzer.Callback;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class CQLFacade {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(CQLFacade.class.getName());

	public static QueryImpl compileWithTemplateParams(final String cqlQueryTemplate) throws Exception {
		final String compilableCqlQuery = substituteCqlVariableNames(cqlQueryTemplate);
		return compileAndCheck(compilableCqlQuery);
	}

	/*
	 * {ns:varname} is not parsable, so we need to substitute them with fake
	 * ones to parse the CQL query string
	 */
	private static String substituteCqlVariableNames(final String cqlQuery) {
		final Pattern r = Pattern.compile("\\{[^\\{\\}]+\\}");
		final Matcher m = r.matcher(cqlQuery);
		return m.replaceAll("{fake}");
	}

	public static void compileAndAnalyze( //
			final String query, //
			final Map<String, Object> context, //
			final Callback callback //
	) {
		QueryImpl compiled = null;
		try {
			compiled = compileAndCheck(query);
		} catch (final Throwable e) {
			final String message = format("CQL compilation failed '%s'", query);
			logger.error(marker, message, e);
			throw WorkflowExceptionType.CQL_COMPILATION_FAILED.createException();
		}
		CQLAnalyzer.analyze(compiled, context, callback);
	}

	private static QueryImpl compileAndCheck(final String query) throws Exception {
		final CQLCompiler compiler = new CQLCompiler();
		final CQLCompilerListener listener = new CQLCompilerListener();
		listener.setFactory(new FactoryImpl());
		FactoryImpl.CmdbuildCheck = true;

		compiler.compile(query, listener);

		final QueryImpl compiled = (QueryImpl) listener.getRootQuery();
		compiled.check();
		return compiled;
	}

}
