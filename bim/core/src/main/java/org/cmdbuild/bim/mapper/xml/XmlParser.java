package org.cmdbuild.bim.mapper.xml;

import java.io.File;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cmdbuild.bim.mapper.Parser;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.utils.LoggerSupport;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * This class can read an XML file in which a set of entities is defines as well
 * as their attributes and possible nested entities
 * */
public class XmlParser implements Parser {


	/** the root node of the XML file */
	public static final String ROOT = "bim-conf";
	private static final Logger logger = LoggerSupport.logger;

	private static Document xmlDocument; 

	private File inputFile;
	private static XPath xPath;


	@Deprecated
	public static XmlParser from(final String filename) {
		try {
			final URL url = ClassLoader.getSystemResource(filename);
			final File inputFile = new File(url.toURI());
			return new XmlParser(inputFile);
		} catch (URISyntaxException e) {
			throw new Error(e);
		}
	}

	public XmlParser(File file) {
		this.inputFile = file;

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = domFactory.newDocumentBuilder();
			logger.info(inputFile.getAbsolutePath());
			xmlDocument = builder.parse(inputFile);
			XPathFactory factory = XPathFactory.newInstance();
			xPath = factory.newXPath();
		} catch (Exception e) {
			throw new BimError("Error during setup of parser", e);
		}

	}

	public XmlParser(String xmlString) {
		InputSource xmlSource = new InputSource(new StringReader(xmlString));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			xmlDocument = db.parse(xmlSource);
			xPath = XPathFactory.newInstance().newXPath();
		} catch (Exception e) {
			throw new BimError("Unable to parse export configuration '"+xmlString+"'");
		} 
	}

	/**
	 * @param entityPath
	 *            : a string which identifies the entity to be read, e.g.
	 *            "ROOT/entity[1]"
	 * */
	@Override
	public String getEntityName(String entityPath) {
		String name = "";
		try {
			name = xPath.evaluate(entityPath + "/@name", xmlDocument);
		} catch (XPathExpressionException e) {
			throw new BimError("error in getEntityName", e);
		}
		return name;
	}

	/**
	 * @param entityPath
	 *            : a string which identifies the entity to be read, e.g.
	 *            "ROOT/entity[1]"
	 * */
	@Override
	public String getEntityLabel(String entityPath) {
		String label = "";
		try {
			label = xPath.evaluate(entityPath + "/@label", xmlDocument);
		} catch (XPathExpressionException e) {
			new BimError("error in getEntityLabel", e);
		}
		return label;
	}

	/**
	 * @param entityPath
	 *            : a string which identifies the entity to be read, e.g.
	 *            "ROOT/entity[1]"
	 * */
	@Override
	public String getEntityShape(String entityPath) {
		String shape = "";
		try {
			shape = xPath.evaluate(entityPath + "/@shape", xmlDocument);
		} catch (XPathExpressionException e) {
			new BimError("error in getEntityShape", e);
		}
		return shape;
	}
	

	@Override
	public String getEntityContainerAttribute(String entityPath) {
		String containerAttribute = "";
		try {
			containerAttribute = xPath.evaluate(entityPath + "/@containerAttribute", xmlDocument);
		} catch (XPathExpressionException e) {
			new BimError("error in getEntityContainerAttribute", e);
		}
		return containerAttribute;
	}
	

	/**
	 * @param nestedEntityPath
	 *            : a string which identifies the position of the nested
	 *            entities to be read, e.g.
	 *            "ROOT/entity[1]/attributes/attribute[2]"
	 * */
	@Override
	// FIXME fix the path!!
	public int getNumberOfNestedEntities(String nestedEntityPath) {
		int numberOfNestedEntities = 0;
		try {
			numberOfNestedEntities = Integer.parseInt(xPath.evaluate("count(" + nestedEntityPath + "/entity)",
					xmlDocument));
		} catch (XPathExpressionException e) {
			throw new BimError("error in get entity names", e);
		}
		return numberOfNestedEntities;
	}

	/**
	 * @param entityPath
	 *            : a string which identifies the entity to be read, e.g.
	 *            "ROOT/entity[1]"
	 * */
	@Override
	public Iterable<String> getAttributesNames(String entityPath) {
		int numberOfAttributes = getNumberOfAttributes(entityPath);
		List<String> names = new ArrayList<String>();
		for (int j = 1; j < numberOfAttributes + 1; j++) {
			String name = "";
			try {
				name = xPath.evaluate(entityPath + "/attributes/attribute[" + j + "]/@name", xmlDocument);
				names.add(name);
			} catch (XPathExpressionException e) {
				throw new BimError("error in get entities to read", e);
			}
		}
		return names;
	}

	/**
	 * @param entityPath
	 *            : a string which identifies the entity to be read, e.g.
	 *            "ROOT/entity[1]"
	 * @return the number of attributes of the selected entity
	 * */
	@Override
	public int getNumberOfAttributes(String entityPath) {
		String numberOfAttributesAsString;
		int numberOfAttributes = 0;
		try {
			numberOfAttributesAsString = xPath.evaluate("count(" + entityPath + "/attributes/attribute)", xmlDocument);
			numberOfAttributes = Integer.parseInt(numberOfAttributesAsString);
		} catch (XPathExpressionException e) {
			new BimError("error in get number of attributes", e);
		}
		return numberOfAttributes;
	}

	/**
	 * @param entityPath
	 *            : a string which identifies the entity to be read, e.g.
	 *            "ROOT/entity[1]"
	 * */
	@Override
	public String getAttributeType(String entityPath, String name) {
		String type = "";
		try {
			type = xPath.evaluate(entityPath + "/attributes/attribute[@name='" + name + "']/@type", xmlDocument);
			if (type.equals("")) {
				throw new BimError("error reading attribute type for attribute " + name);
			}
		} catch (XPathExpressionException e) {
			throw new BimError("error reading attribute type for attribute: \"" + name + "\"", e);
		}
		return type;
	}

	@Override
	public String getAttributeType(String entityPath, int i) {
		String type = "";
		try {
			type = xPath.evaluate(entityPath + "/attributes/attribute[" + i + "]/@type", xmlDocument);
			if (type.equals("")) {
				throw new BimError("error reading attribute type for attribute " + i);
			}
		} catch (XPathExpressionException e) {
			throw new BimError("error reading attribute type for attribute: \"" + i + "\"", e);
		}
		return type;
	}

	/**
	 * @param entityPath
	 *            : a string which identifies the entity to be read, e.g.
	 *            "ROOT/entity[1]"
	 * */
	@Override
	public String getAttributeLabel(String entityPath, String name) {
		String label = "";
		try {
			label = xPath.evaluate(entityPath + "/attributes/attribute[@name='" + name + "']/@label", xmlDocument);
		} catch (XPathExpressionException e) {
			throw new BimError("error reading attribute type for attribute: \"" + name + "\"", e);
		}
		return label;
	}

	@Override
	public String getAttributeLabel(String entityPath, int i) {
		String label = "";
		try {
			label = xPath.evaluate(entityPath + "/attributes/attribute[" + i + "]/@label", xmlDocument);
		} catch (XPathExpressionException e) {
			throw new BimError("error reading attribute type for attribute: \"" + i + "\"", e);
		}
		return label;
	}

	/**
	 * @param entityPath
	 *            : a string which identifies the entity to be read, e.g.
	 *            "ROOT/entity[1]"
	 * */
	@Override
	public String getAttributeValue(String entityPath, String name) {
		String value = "";
		try {
			value = xPath.evaluate(entityPath + "/attributes/attribute[@name='" + name + "']/@value", xmlDocument);
		} catch (XPathExpressionException e) {
			throw new BimError("error reading attribute type for attribute: \"" + name + "\"", e);
		}
		return value;
	}

	@Override
	public String getAttributeValue(String entityPath, int i) {
		String value = "";
		try {
			value = xPath.evaluate(entityPath + "/attributes/attribute[" + i + "]/@value", xmlDocument);
		} catch (XPathExpressionException e) {
			throw new BimError("error reading attribute type for attribute: \"" + i + "\"", e);
		}
		return value;
	}

	@Override
	public String getAttributeName(String entityPath, int i) {
		String name = "";
		try {
			name = xPath.evaluate(entityPath + "/attributes/attribute[" + i + "]/@name", xmlDocument);
		} catch (XPathExpressionException e) {
			throw new BimError("error reading attribute name for attribute: \"" + i + "\"", e);
		}
		return name;
	}


}
