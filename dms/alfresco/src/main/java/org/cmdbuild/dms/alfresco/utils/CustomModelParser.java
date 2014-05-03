package org.cmdbuild.dms.alfresco.utils;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CustomModelParser implements LoggingSupport {

	private static final String TYPE_NAMES_EXPRESSION = "/model/types/type/@name";
	private static final String TYPE_TITLE_EXPRESSION = "/model/types/type[@name='%s']/title/text()";
	private static final String ASPECT_NAMES_FOR_TYPE_EXPRESSION_FORMAT = "/model/types/type[@name='%s']/mandatory-aspects/aspect";
	private static final String CONSTRAINT_NAMES_EXPRESSION = "/model/constraints/constraint/@name";
	private static final String CONSTRAINT_VALUES_FOR_CONSTRAINT_NAME_EXPRESSION_FORMAT = "/model/constraints/constraint[@name='%s']/parameter[@name='allowedValues']/list/value";
	private static final String PREFIX_NAME_SEPARATOR = ":";

	private final String content;
	private final String prefix;
	private Document document;

	public CustomModelParser(final String content, final String prefix) {
		this.content = content;
		this.prefix = prefix;
	}

	/**
	 * Gets aspects grouped by type's title.
	 * 
	 * @return the aspect names grouped by type's title.
	 * 
	 * @throws @{@link RuntimeException}
	 */
	public Map<String, List<String>> getAspectsByType() {
		try {
			logger.info("getting all aspects grouped by type (title)");
			return unsafeAspectsByType();
		} catch (final Exception e) {
			logger.error("error getting parsing data, returning and empty map", e);
			throw new RuntimeException(e);
		}
	}

	private Map<String, List<String>> unsafeAspectsByType() throws Exception {
		final Map<String, List<String>> aspectsByType = Maps.newHashMap();
		parseContent();
		for (final String typeName : nodeContentsFrom(typeNamesExpression())) {
			final String typeTitle = evaluateAsString(typeTitlesExpression(addPrefixToName(typeName)));
			if (isNotEmpty(typeTitle)) {
				aspectsByType.put(typeTitle, nodeContentsFrom(aspectNamesForTypeExpression(addPrefixToName(typeName))));
			}
		}
		return aspectsByType;
	}

	/**
	 * Gets constraints grouped by name.
	 * 
	 * @return the constraint values grouped by constraint name.
	 * 
	 * @throws @{@link RuntimeException}
	 */
	public Map<String, List<String>> getConstraintsByMetadata() {
		try {
			logger.info("getting all constraints grouped by metadata");
			return unsafeConstraintsByName();
		} catch (final Exception e) {
			logger.error("error getting parsing data, returning and empty map", e);
			throw new RuntimeException(e);
		}
	}

	private Map<String, List<String>> unsafeConstraintsByName() throws Exception {
		final Map<String, List<String>> constraintsByName = Maps.newHashMap();
		parseContent();
		for (final String constraintName : nodeValuesFrom(constraintNamesExpression())) {
			constraintsByName.put(constraintName,
					nodeContentsFrom(constraintValuesForConstraintNameExpression(addPrefixToName(constraintName))));
		}
		return constraintsByName;
	}

	private List<String> nodeValuesFrom(final String expression) throws XPathExpressionException {
		logger.debug("getting node values using expression '{}'", expression);
		final List<String> values = Lists.newArrayList();
		final NodeList nodeList = evaluateAsNodeList(expression);
		for (int i = 0; i < nodeList.getLength(); i++) {
			final String nodeValue = nodeList.item(i).getNodeValue();
			values.add(stripPrefixFromName(nodeValue));
		}
		return values;
	}

	private List<String> nodeContentsFrom(final String expression) throws XPathExpressionException {
		logger.debug("getting node contents using expression '{}'", expression);
		final List<String> values = new ArrayList<String>();
		final NodeList nodeList = evaluateAsNodeList(expression);
		for (int i = 0; i < nodeList.getLength(); i++) {
			final String nodeValue = nodeList.item(i).getTextContent();
			values.add(stripPrefixFromName(nodeValue));
		}
		return values;
	}

	private NodeList evaluateAsNodeList(final String expression) throws XPathExpressionException {
		final XPathExpression xpathExpression = compileExpression(expression);
		return (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
	}

	private String evaluateAsString(final String expression) throws XPathExpressionException {
		final XPathExpression xpathExpression = compileExpression(expression);
		return (String) xpathExpression.evaluate(document, XPathConstants.STRING);
	}

	private void parseContent() throws ParserConfigurationException, SAXException, IOException {
		logger.debug("parsing content", content);
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(new InputSource(new StringReader(content)));
	}

	private String stripPrefixFromName(final String name) {
		logger.debug("stripping prefix '{}' from name '{}'", prefix, name);
		return name.replaceAll(format("%s%s", prefix, PREFIX_NAME_SEPARATOR), EMPTY);
	}

	private String addPrefixToName(final String name) {
		logger.debug("adding prefix '{}' to name '{}'", prefix, name);
		return format("%s%s%s", prefix, PREFIX_NAME_SEPARATOR, name);
	}

	private static XPathExpression compileExpression(final String expression) throws XPathExpressionException {
		logger.debug("compiling expression", expression);
		final XPathFactory xFactory = XPathFactory.newInstance();
		final XPath xpath = xFactory.newXPath();
		final XPathExpression typeNamesExpression = xpath.compile(expression);
		return typeNamesExpression;
	}

	private static String typeNamesExpression() {
		return TYPE_NAMES_EXPRESSION;
	}

	private static String typeTitlesExpression(final String typeName) {
		return format(TYPE_TITLE_EXPRESSION, typeName);
	}

	private static String aspectNamesForTypeExpression(final String typeName) {
		return format(ASPECT_NAMES_FOR_TYPE_EXPRESSION_FORMAT, typeName);
	}

	private static String constraintNamesExpression() {
		return CONSTRAINT_NAMES_EXPRESSION;
	}

	private static String constraintValuesForConstraintNameExpression(final String constraintName) {
		return format(CONSTRAINT_VALUES_FOR_CONSTRAINT_NAME_EXPRESSION_FORMAT, constraintName);
	}

}