package org.cmdbuild.cql.compiler;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.cmdbuild.cql.CQLBuilderListener;
import org.cmdbuild.cql.CQLCompilerBuilder;
import org.cmdbuild.cql.CQLLexer;
import org.cmdbuild.cql.CQLParser;

public class CQLCompiler {

	public CQLCompiler() {
	}

	public void init() {
	}

	public void compile(final String text, final CQLBuilderListener listener) throws RecognitionException {
		final ANTLRStringStream input = new ANTLRStringStream(text);
		final CQLLexer lexer = new CQLLexer(input);
		final CommonTokenStream tokens = new CommonTokenStream(lexer);
		final CQLParser parser = new CQLParser(tokens);
		final CQLParser.expr_return r = parser.expr();
		final CommonTree t = (CommonTree) r.getTree();
		final CQLCompilerBuilder builder = new CQLCompilerBuilder();
		builder.setCQLBuilderListener(listener);
		builder.compile(t);
	}
}
