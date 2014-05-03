package org.cmdbuild.cql;

import static org.cmdbuild.cql.CQLParser.ALL;
import static org.cmdbuild.cql.CQLParser.AND;
import static org.cmdbuild.cql.CQLParser.ASC;
import static org.cmdbuild.cql.CQLParser.ATTRIBUTE;
import static org.cmdbuild.cql.CQLParser.ATTRIBUTEAS;
import static org.cmdbuild.cql.CQLParser.ATTRIBUTENAME;
import static org.cmdbuild.cql.CQLParser.ATTRIBUTES;
import static org.cmdbuild.cql.CQLParser.BGN;
import static org.cmdbuild.cql.CQLParser.BTW;
import static org.cmdbuild.cql.CQLParser.CLASS;
import static org.cmdbuild.cql.CQLParser.CLASSALIAS;
import static org.cmdbuild.cql.CQLParser.CLASSDOMREF;
import static org.cmdbuild.cql.CQLParser.CLASSID;
import static org.cmdbuild.cql.CQLParser.CLASSREF;
import static org.cmdbuild.cql.CQLParser.CONT;
import static org.cmdbuild.cql.CQLParser.DEFAULT;
import static org.cmdbuild.cql.CQLParser.DESC;
import static org.cmdbuild.cql.CQLParser.DOM;
import static org.cmdbuild.cql.CQLParser.DOMCARDS;
import static org.cmdbuild.cql.CQLParser.DOMID;
import static org.cmdbuild.cql.CQLParser.DOMMETA;
import static org.cmdbuild.cql.CQLParser.DOMNAME;
import static org.cmdbuild.cql.CQLParser.DOMOBJS;
import static org.cmdbuild.cql.CQLParser.DOMREF;
import static org.cmdbuild.cql.CQLParser.DOMTYPE;
import static org.cmdbuild.cql.CQLParser.DOMVALUE;
import static org.cmdbuild.cql.CQLParser.END;
import static org.cmdbuild.cql.CQLParser.EQ;
import static org.cmdbuild.cql.CQLParser.EXPR;
import static org.cmdbuild.cql.CQLParser.FIELD;
import static org.cmdbuild.cql.CQLParser.FIELDID;
import static org.cmdbuild.cql.CQLParser.FIELDOPERATOR;
import static org.cmdbuild.cql.CQLParser.FIELDVALUE;
import static org.cmdbuild.cql.CQLParser.FROM;
import static org.cmdbuild.cql.CQLParser.FUNCTION;
import static org.cmdbuild.cql.CQLParser.GROUP;
import static org.cmdbuild.cql.CQLParser.GROUPBY;
import static org.cmdbuild.cql.CQLParser.GT;
import static org.cmdbuild.cql.CQLParser.GTEQ;
import static org.cmdbuild.cql.CQLParser.HISTORY;
import static org.cmdbuild.cql.CQLParser.IN;
import static org.cmdbuild.cql.CQLParser.INPUTVAL;
import static org.cmdbuild.cql.CQLParser.ISNOTNULL;
import static org.cmdbuild.cql.CQLParser.ISNULL;
import static org.cmdbuild.cql.CQLParser.LIMIT;
import static org.cmdbuild.cql.CQLParser.LITBOOL;
import static org.cmdbuild.cql.CQLParser.LITDATE;
import static org.cmdbuild.cql.CQLParser.LITNUM;
import static org.cmdbuild.cql.CQLParser.LITSTR;
import static org.cmdbuild.cql.CQLParser.LITTIMESTAMP;
import static org.cmdbuild.cql.CQLParser.LOOKUP;
import static org.cmdbuild.cql.CQLParser.LOOKUPPARENT;
import static org.cmdbuild.cql.CQLParser.LT;
import static org.cmdbuild.cql.CQLParser.LTEQ;
import static org.cmdbuild.cql.CQLParser.NATIVE;
import static org.cmdbuild.cql.CQLParser.NOT;
import static org.cmdbuild.cql.CQLParser.NOTBGN;
import static org.cmdbuild.cql.CQLParser.NOTBTW;
import static org.cmdbuild.cql.CQLParser.NOTCONT;
import static org.cmdbuild.cql.CQLParser.NOTEND;
import static org.cmdbuild.cql.CQLParser.NOTEQ;
import static org.cmdbuild.cql.CQLParser.NOTGROUP;
import static org.cmdbuild.cql.CQLParser.NOTIN;
import static org.cmdbuild.cql.CQLParser.OFFSET;
import static org.cmdbuild.cql.CQLParser.OR;
import static org.cmdbuild.cql.CQLParser.ORDERBY;
import static org.cmdbuild.cql.CQLParser.SELECT;
import static org.cmdbuild.cql.CQLParser.TRUE;
import static org.cmdbuild.cql.CQLParser.WHERE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.runtime.tree.Tree;
import org.cmdbuild.cql.CQLBuilderListener.FieldValueType;
import org.cmdbuild.logger.Log;

/**
 * This class prvovide a common event-based abstraction to a CQL compiler. <br>
 * It takes a CommonTree expression representation (that comes from the
 * CQLParser) and navigates through the tree, and emits events based on the tree
 * being analyzed.
 * 
 */
@SuppressWarnings("unchecked")
public class CQLCompilerBuilder {
	CQLBuilderListener l;

	Set<String> declaredClassesDomains = new HashSet<String>();

	void addDeclaration(final String... names) {
		for (final String name : names) {
			if (name != null) {
				declaredClassesDomains.add(name);
			}
		}
	}

	boolean hasDeclaration(final String name) {
		return declaredClassesDomains.contains(name);
	}

	private interface TreeWork {
		void work(Tree tree);
	}

	private class HandleFields implements TreeWork {
		boolean first = true;

		@Override
		public void work(final Tree child) {
			if (first) {
				// field, group or domain
				handleElement(child, CQLBuilderListener.WhereType.FIRST);
				first = false;
			} else {
				// and, or
				switch (child.getType()) {
				case AND:
					handleElement(child.getChild(0), CQLBuilderListener.WhereType.AND);
					break;
				case OR:
					handleElement(child.getChild(0), CQLBuilderListener.WhereType.OR);
				default:
					logUnknownTree(child);
				}
			}
		}

		private void handleElement(final Tree child, final CQLBuilderListener.WhereType type) {
			log("handle element " + child.getType() + ", type: " + type.name());
			switch (child.getType()) {
			case GROUP:
				l.startGroup(type, false);
				withChildren(child, new HandleFields());
				l.endGroup();
				break;
			case NOTGROUP:
				l.startGroup(type, true);
				withChildren(child, new HandleFields());
				l.endGroup();
				break;
			case FIELD:
				whereField(child, type);
				break;
			case DOM:
				whereDomain(child, type);
				break;
			case DOMREF:
				whereDomainRef(child, type);
				break;
			default:
				logUnknownTree(child);
			}
		}
	}

	public void setCQLBuilderListener(final CQLBuilderListener listener) {
		this.l = listener;
	}

	private void withChildren(final Tree parent, final TreeWork work) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			work.work(parent.getChild(i));
		}
	}

	public Tree firstChild(final Tree parent, final int type) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			if (parent.getChild(i).getType() == type) {
				return parent.getChild(i);
			}
		}
		return null;
	}

	public String subtext(final Tree tree) {
		return tree == null ? null : (tree.getChildCount() == 0 ? null : tree.getChild(0).getText());// tree.getText();
	}

	public String text(final Tree tree) {
		return tree == null ? null : tree.getText();
	}

	private void log(final Object o) {
		Log.CMDBUILD.debug(o.toString());
	}

	private void logUnknownTree(final Tree t) {
		log("unknown tree: " + t.getText());
	}

	public void compile(final Tree root) {
		this.declaredClassesDomains.clear();
		log("Start compilation");
		l.globalStart();
		expression(root);
		log("Compilation ended.");
		l.globalEnd();
	}

	public void expression(final Tree expr) {
		log("Start Expression");

		l.startExpression();

		Tree select, from, where, order, group, limit, offset;

		from = firstChild(expr, FROM);
		select = firstChild(expr, SELECT);
		where = firstChild(expr, WHERE);
		order = firstChild(expr, ORDERBY);
		group = firstChild(expr, GROUPBY);
		limit = firstChild(expr, LIMIT);
		offset = firstChild(expr, OFFSET);

		final boolean history = null != firstChild(expr, HISTORY);
		// FROM statement is the only one required.
		from(from, history);

		if (select == null) {
			l.defaultSelect();
		} else {
			select(select);
		}
		if (where == null) {
			l.defaultWhere();
		} else {
			where(where);
		}
		if (order == null) {
			l.defaultOrderBy();
		} else {
			order(order);
		}
		if (group == null) {
			l.defaultGroupBy();
		} else {
			group(group);
		}
		if (limit == null) {
			l.defaultLimit();
		} else {
			limit(limit);
		}
		if (offset == null) {
			l.defaultOffset();
		} else {
			offset(offset);
		}

		log("End Expression");
		l.endExpression();
	}

	public void from(final Tree from, final boolean history) {
		log("Start From");
		l.startFrom(history);
		withChildren(from, new TreeWork() {
			@Override
			public void work(final Tree child) {
				switch (child.getType()) {
				case CLASSREF:
					final String calias = subtext(firstChild(child, CLASSALIAS));
					final String cname = subtext(firstChild(child, CLASS));
					final String cid = subtext(firstChild(child, CLASSID));
					addDeclaration(cname, calias);
					if (cid != null) {
						final int classId = Integer.parseInt(cid);
						l.addFromClass(classId, calias);
					} else {
						l.addFromClass(cname, calias);
					}
					break;
				case DOM:
					fromDomain(child);
					break;
				default:
					logUnknownTree(child);
				}
			}
		});
		log("End From");
		l.endFrom();
	}

	private void fromDomain(final Tree tree) {
		final String classScope = subtext(firstChild(tree, CLASSDOMREF));
		final Tree domType = firstChild(tree, DOMTYPE);
		CQLBuilderListener.DomainDirection dir = null;
		if (null == firstChild(domType, DEFAULT)) {
			dir = CQLBuilderListener.DomainDirection.DEFAULT;
		} else {
			dir = CQLBuilderListener.DomainDirection.INVERSE;
		}
		final String domName = subtext(firstChild(tree, DOMNAME));
		final String domId = subtext(firstChild(tree, DOMID));
		final String dalias = subtext(firstChild(tree, DOMREF));

		addDeclaration(domName, dalias);
		if (domId != null) {
			final int domainId = Integer.parseInt(domId);
			l.startFromDomain(classScope, domainId, dalias, dir);
		} else {
			l.startFromDomain(classScope, domName, dalias, dir);
		}
		final Tree subDomain = firstChild(tree, DOMCARDS);
		if (subDomain != null) {
			fromDomain(subDomain.getChild(0));
		}
		l.endFromDomain();
	}

	public void select(final Tree select) {
		log("Start Select");
		l.startSelect();

		if (null != firstChild(select, ALL)) {
			l.selectAll();
		} else {
			withChildren(select, new TreeWork() {
				@Override
				public void work(final Tree child) {
					switch (child.getType()) {
					case CLASSREF:
						final String classRef = subtext(child);
						l.startSelectFromClass(classRef);
						withChildren(firstChild(child, ATTRIBUTES), new TreeWork() {
							@Override
							public void work(final Tree attr) {
								selectAttribute(attr);
							}
						});
						l.endSelectFromClass();
						break;
					case DOMREF:
						final String domRef = subtext(child);
						l.startSelectFromDomain(domRef);

						Tree meta,
						objs;
						meta = firstChild(child, DOMMETA);
						objs = firstChild(child, DOMOBJS);

						if (meta != null) {
							l.startSelectFromDomainMeta();
							withChildren(meta, new TreeWork() {
								@Override
								public void work(final Tree m) {
									selectAttribute(m);
								}
							});
							l.endSelectFromDomainMeta();
						}
						if (objs != null) {
							l.startSelectFromDomainObjects();
							withChildren(objs, new TreeWork() {
								@Override
								public void work(final Tree m) {
									selectAttribute(m);
								}
							});
							l.endSelectFromDomainObjects();
						}

						l.endSelectFromDomain();
						break;
					case FUNCTION:
						selectFunction(child);
						break;
					case ATTRIBUTE:
						selectAttribute(child);
						break;
					default:
						logUnknownTree(child);
					}
				}
			});
		}

		log("End Select");
		l.endSelect();
	}

	private void selectAttribute(final Tree tree) {
		final String attrName = subtext(firstChild(tree, ATTRIBUTENAME));
		final String attrAs = subtext(firstChild(tree, ATTRIBUTEAS));
		final String clDomRef = subtext(firstChild(tree, CLASSDOMREF));
		l.addSelectAttribute(attrName, attrAs, clDomRef);
	}

	private void selectFunction(final Tree tree) {
		final String funcName = subtext(tree.getChild(0));
		final String funcAs = subtext(firstChild(tree, ATTRIBUTEAS));
		l.startSelectFunction(funcName, funcAs);
		withChildren(firstChild(tree, ATTRIBUTES), new TreeWork() {
			@Override
			public void work(final Tree child) {
				selectAttribute(child);
			}
		});
		l.endSelectFunction();
	}

	public void where(final Tree where) {
		log("Start Where");
		l.startWhere();

		withChildren(where, new HandleFields());

		log("End Where");
		l.endWhere();
	}

	private void whereDomain(final Tree tree, final CQLBuilderListener.WhereType type) {
		final String classScope = subtext(firstChild(tree, CLASSDOMREF));
		final Tree domType = firstChild(tree, DOMTYPE);
		CQLBuilderListener.DomainDirection dir = null;
		if (null == firstChild(domType, DEFAULT)) {
			dir = CQLBuilderListener.DomainDirection.DEFAULT;
		} else {
			dir = CQLBuilderListener.DomainDirection.INVERSE;
		}

		final boolean isNot = null != firstChild(domType, NOT);
		final String domName = subtext(firstChild(tree, DOMNAME));
		final String domId = subtext(firstChild(tree, DOMID));

		if (domId != null) {
			final int domainId = Integer.parseInt(domId);
			l.startDomain(type, classScope, domainId, dir, isNot);
		} else {
			l.startDomain(type, classScope, domName, dir, isNot);
		}

		Tree domMeta, domObjs;
		domMeta = firstChild(tree, DOMVALUE);
		domObjs = firstChild(tree, DOMCARDS);

		if (domMeta != null) {
			l.startDomainMeta();
			withChildren(domMeta, new HandleFields());
			l.endDomainMeta();
		}
		if (domObjs != null) {
			l.startDomainObjects();
			withChildren(domObjs, new HandleFields());
			l.endDomainObjects();
		}
		l.endDomain();
	}

	private void whereDomainRef(final Tree tree, final CQLBuilderListener.WhereType type) {
		final String domRefName = subtext(firstChild(tree, DOMNAME));
		final Tree domType = firstChild(tree, DOMTYPE);
		final boolean isNot = null != firstChild(domType, NOT);

		l.startDomainRef(type, domRefName, isNot);

		Tree domMeta, domObjs;
		domMeta = firstChild(tree, DOMVALUE);
		domObjs = firstChild(tree, DOMCARDS);

		if (domMeta != null) {
			l.startDomainMeta();
			withChildren(domMeta, new HandleFields());
			l.endDomainMeta();
		}
		if (domObjs != null) {
			l.startDomainObjects();
			withChildren(domObjs, new HandleFields());
			l.endDomainObjects();
		}
		l.endDomainRef();
	}

	private void whereField(final Tree tree, final CQLBuilderListener.WhereType type) {
		final Tree id = firstChild(tree, FIELDID);
		final Tree operator = firstChild(tree, FIELDOPERATOR).getChild(0);
		final Tree value = firstChild(tree, FIELDVALUE);

		boolean simple = false;
		boolean lookup;
		// has 1 child or has 2 children, and the first is a domain/class
		if (id.getChildCount() == 1 || id.getChildCount() == 2 && (hasDeclaration(subtext(id.getChild(0))))) {
			simple = true;
		}
		lookup = null != firstChild(id, LOOKUP);

		CQLBuilderListener.FieldOperator fieldop = null;

		boolean isNot = false;
		switch (operator.getType()) {
		case LTEQ:
		case GTEQ:
		case LT:
		case GT:
		case EQ:
		case CONT:
		case BGN:
		case END:
		case IN:
		case BTW:
		case ISNULL:
			fieldop = CQLBuilderListener.FieldOperator.valueOf(operator.getText());
			break;
		case NOTEQ:
		case NOTCONT:
		case NOTBGN:
		case NOTEND:
		case NOTIN:
		case NOTBTW:
			isNot = true;
			fieldop = CQLBuilderListener.FieldOperator.valueOf(operator.getText().substring(3));
			break;
		case ISNOTNULL:
			isNot = true;
			fieldop = CQLBuilderListener.FieldOperator.ISNULL;
			break;
		default:
			logUnknownTree(operator);
		}

		if (simple) {
			String fieldId, cname = null;
			if (id.getChildCount() == 1) {
				fieldId = subtext(id.getChild(0));
			} else {
				cname = subtext(id.getChild(0));
				fieldId = subtext(id.getChild(1));
			}
			l.startSimpleField(type, isNot, cname, fieldId, fieldop);
		} else if (lookup) {
			final List<CQLBuilderListener.LookupOperator> operators = new ArrayList();
			final String classDomRef = subtext(firstChild(id, CLASSDOMREF));
			final String fieldId = id.getChild(0).getText();
			Tree lkpOp = firstChild(id, LOOKUP).getChild(0);
			while (lkpOp != null) {
				final String op = lkpOp.getText();
				final String attr = subtext(firstChild(lkpOp, ATTRIBUTE));
				operators.add(new CQLBuilderListener.LookupOperator(op, attr));

				lkpOp = firstChild(lkpOp, LOOKUPPARENT);
			}
			l.startLookupField(type, isNot, classDomRef, fieldId,
					operators.toArray(new CQLBuilderListener.LookupOperator[] {}), fieldop);
		} else {
			final List<String> path = new ArrayList();

			String classDomRef = null;
			int startIdx = 0;
			if (hasDeclaration(subtext(id.getChild(0)))) {
				classDomRef = subtext(id.getChild(0));
				startIdx = 1;
			}
			for (int i = startIdx; i < id.getChildCount(); i++) {
				path.add(subtext(id.getChild(i)));
			}

			l.startComplexField(type, isNot, classDomRef, path.toArray(new String[] {}), fieldop);
		}

		if (value != null) {
			withChildren(value, new TreeWork() {
				@Override
				public void work(final Tree v) {
					handleFieldValue(v);
				}
			});
		}
	}

	SimpleDateFormat df1 = new SimpleDateFormat("yyyy/MM/DD");
	SimpleDateFormat df2 = new SimpleDateFormat("yy/MM/DD");
	SimpleDateFormat ts1 = new SimpleDateFormat("yyyy/MM/DD'T'HH:mm:ss");
	SimpleDateFormat ts2 = new SimpleDateFormat("yy/MM/DD'T'HH:mm:ss");

	private void handleFieldValue(final Tree value) {
		switch (value.getType()) {
		case LITBOOL:
			l.startValue(FieldValueType.BOOL);
			l.value(null != firstChild(value, TRUE));
			l.endValue();
			break;
		case LITDATE:
			l.startValue(FieldValueType.DATE);
			Date dt = null;
			final String dttxt = value.getChild(0).getText();
			try {
				if (dttxt.length() == 8) {
					dt = df2.parse(dttxt);
				} else {
					dt = df1.parse(dttxt);
				}
			} catch (final Exception e) {
				throw new RuntimeException("Cannot parse date: " + dttxt);
			}
			l.value(dt);
			l.endValue();
			break;
		case LITTIMESTAMP:
			l.startValue(FieldValueType.TIMESTAMP);
			Date ts = null;
			final String tstxt = value.getChild(0).getText();
			try {
				if (tstxt.length() == 17) {
					ts = ts2.parse(tstxt);
				} else {
					ts = ts1.parse(tstxt);
				}
			} catch (final Exception e) {
				throw new RuntimeException("Cannot parse timestamp: " + tstxt);
			}
			l.value(ts);
			l.endValue();
			break;
		case INPUTVAL:
			l.startValue(FieldValueType.INPUT);
			final String varname = text(value.getChild(0));
			l.value(new CQLBuilderListener.FieldInputValue(varname));
			l.endValue();
			break;
		case LITSTR:
			l.startValue(FieldValueType.STRING);
			l.value(extractLiteral(text(value.getChild(0))));
			l.endValue();
			break;
		case LITNUM:
			final String numtxt = subtext(value);
			if (numtxt.indexOf('.') != -1) {
				l.startValue(FieldValueType.FLOAT);
				l.value(Float.parseFloat(numtxt));
			} else {
				l.startValue(FieldValueType.INT);
				l.value(Integer.parseInt(numtxt));
			}
			l.endValue();
			break;
		case EXPR:
			l.startValue(FieldValueType.SUBEXPR);
			this.expression(value);
			l.endValue();
			break;
		case NATIVE:
			l.startValue(FieldValueType.NATIVE);
			String nativetxt = subtext(value);
			nativetxt = nativetxt.substring(1, nativetxt.length() - 1);
			l.value(new CQLBuilderListener.FieldNativeSQLValue(nativetxt));
			l.endValue();
			break;
		default:
			logUnknownTree(value);
		}
	}

	private String extractLiteral(String literal) {
		final char first = literal.charAt(0);
		literal = literal.substring(1, literal.length() - 1);
		if (first == '"') {
			literal = literal.replace("\\\"", "\"");
		} else {
			literal = literal.replace("\\'", "'");
		}

		return literal;
	}

	public void group(final Tree group) {
		log("Start GroupBy");
		l.startGroupBy();

		withChildren(group, new TreeWork() {
			@Override
			public void work(final Tree c) {
				if (c.getType() != ATTRIBUTE) {
					throw new RuntimeException("GroupBy handle only attributes!");
				}
				final String classDomainRef = subtext(firstChild(c, CLASSDOMREF));
				final String attrName = subtext(firstChild(c, ATTRIBUTENAME));

				l.addGroupByElement(classDomainRef, attrName);
			}
		});

		log("End GroupBy");
		l.endGroupBy();
	}

	public void order(final Tree order) {
		log("Start OrderBy");
		l.startOrderBy();

		withChildren(order, new TreeWork() {
			@Override
			public void work(final Tree c) {
				final String classDomRef = subtext(firstChild(c, CLASSDOMREF));
				final String attrName = c.getChild(0).getText();
				final boolean asc = null != firstChild(c, ASC);
				final boolean desc = null != firstChild(c, DESC);

				CQLBuilderListener.OrderByType type = null;
				if (!asc && !desc) {
					type = CQLBuilderListener.OrderByType.DEFAULT;
				} else if (asc) {
					type = CQLBuilderListener.OrderByType.ASC;
				} else {
					type = CQLBuilderListener.OrderByType.DESC;
				}
				l.addOrderByElement(classDomRef, attrName, type);
			}
		});

		log("End OrderBy");
		l.endOrderBy();

	}

	public void limit(final Tree limit) {
		final boolean literal = null != firstChild(limit, LITNUM);
		if (literal) {
			final int limitInt = Integer.parseInt(subtext(firstChild(limit, LITNUM)));
			l.setLimit(limitInt);
		} else {
			l.setLimit(new CQLBuilderListener.FieldInputValue(subtext(firstChild(limit, INPUTVAL))));
		}
	}

	public void offset(final Tree offset) {
		final boolean literal = null != firstChild(offset, LITNUM);
		if (literal) {
			final int offsetInt = Integer.parseInt(subtext(firstChild(offset, LITNUM)));
			l.setOffset(offsetInt);
		} else {
			l.setOffset(new CQLBuilderListener.FieldInputValue(subtext(firstChild(offset, INPUTVAL))));
		}
	}

}
