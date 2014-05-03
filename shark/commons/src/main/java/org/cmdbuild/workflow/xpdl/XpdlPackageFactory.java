package org.cmdbuild.workflow.xpdl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.enhydra.jxpdl.XMLInterface;
import org.enhydra.jxpdl.XMLInterfaceImpl;
import org.enhydra.jxpdl.XPDLRepositoryHandler;
import org.enhydra.jxpdl.elements.Package;
import org.w3c.dom.Document;

public class XpdlPackageFactory {

	private static final boolean IS_XML_REPRESENTATION = true;

	public static Package readXpdl(final InputStream is) throws XpdlException {
		try {
			byte[] pkgContent = IOUtils.toByteArray(is);
			return readXpdl(pkgContent);
		} catch (Exception e) {
			throw new XpdlException(e);
		}
	}

	public static Package readXpdl(final byte[] pkgContent) throws XpdlException {
		try {
			return xmlInterface().openPackageFromStream(pkgContent, IS_XML_REPRESENTATION);
		} catch (Exception e) {
			throw new XpdlException(e);
		}
	}

	private static XMLInterface xmlInterface() {
		return new XMLInterfaceImpl();
	}

	public static byte[] xpdlByteArray(final Package pkg) throws XpdlException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XpdlPackageFactory.writeXpdl(pkg, os);
		return os.toByteArray();
	}

	public static void writeXpdl(final Package pkg, final OutputStream os) throws XpdlException {
		try {
			final Document document = writeDocument(pkg);
			writeDocumentToOutputStream(document, os);
		} catch (Exception e) {
			throw new XpdlException(e);
		}
	}

	private static void writeDocumentToOutputStream(final Document document, final OutputStream os)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException,
			IOException {
		final DOMSource source = new DOMSource(document);
		final StreamResult result = new StreamResult(os);
		final Transformer transformer =  TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty("encoding", "UTF8");
		transformer.setOutputProperty("indent", "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.transform(source, result);
		os.close();
	}

	private static Document writeDocument(final Package pkg) throws ParserConfigurationException {
		final Document document = newXmlDocument();
		repositoryHandler().toXML(document, pkg);
		return document;
	}

	private static Document newXmlDocument() throws ParserConfigurationException {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		final DocumentBuilder db = dbf.newDocumentBuilder();
		return db.newDocument();
	}

	private static XPDLRepositoryHandler repositoryHandler() {
		final XPDLRepositoryHandler repH = new XPDLRepositoryHandler();
		repH.setXPDLPrefixEnabled(true);
		return repH;
	}
}
