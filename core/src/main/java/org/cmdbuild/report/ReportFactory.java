package org.cmdbuild.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRElementGroup;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRImage;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.base.JRBaseReport;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.type.OnErrorTypeEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;

import org.apache.commons.lang3.ArrayUtils;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.logger.Log;

public abstract class ReportFactory {

	/** Parameter name (replacement) for images name */
	public static final String PARAM_IMAGE = "IMAGE";

	/** Parameter name (replacement) for subreports name */
	public static final String PARAM_SUBREPORT = "SUBREPORT";

	/** report types enum */
	public static enum ReportType {
		CUSTOM
	};

	/** report extensions enum */
	public static enum ReportExtension {
		PDF, CSV, ODT, ZIP, RTF
	};

	protected final DataSource dataSource;
	private final CmdbuildConfiguration configuration;

	private JasperReport jasperReport;
	protected JasperPrint jasperPrint;

	public ReportFactory(final DataSource dataSource, final CmdbuildConfiguration configuration) {
		this.dataSource = dataSource;
		this.configuration = configuration;
	}

	/** get report extension */
	public abstract ReportExtension getReportExtension();

	public abstract JasperPrint fillReport() throws Exception;

	protected JasperPrint fillReport(final JasperReport report, final Map<String, Object> jasperFillManagerParameters)
			throws Exception {
		jasperFillManagerParameters.put(JRParameter.REPORT_LOCALE, getSystemLocale());
		jasperReport = report;
		final long start = System.currentTimeMillis();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			jasperPrint = JasperFillManager.fillReport(report, jasperFillManagerParameters, connection);
		} catch (final Exception exception) {
			if (Log.REPORT.isDebugEnabled()) {
				saveJRXMLTmpFile(report);
			}
			throw exception;
		}
		finally {
			if (connection != null) {
				connection.close();
			}
		}

		Log.REPORT.debug("REPORT fill time: " + (System.currentTimeMillis() - start) + " ms");

		return jasperPrint;
	}

	private Locale getSystemLocale() {
		return configuration.getLocale();
	}

	public void sendReportToStream(final OutputStream outStream) throws Exception {
		if (isReportFilled()) {
			JRExporter exporter = null;

			switch (getReportExtension()) {
			case PDF:
				exporter = new JRPdfExporter();
				break;

			case CSV:
				exporter = new JRCsvExporter();
				exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, ";");
				break;

			case ODT:
				exporter = new JROdtExporter();
				break;

			case RTF:
				exporter = new JRRtfExporter();
				break;
			}

			if (exporter != null) {
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outStream);
				try {
					exporter.exportReport();
				} catch (final Exception exception) {
					if (Log.REPORT.isDebugEnabled()) {
						saveJRXMLTmpFile(jasperReport);
					}
					throw exception;
				}
			}
		}
	}

	public String getContentType() {
		switch (getReportExtension()) {
		case PDF:
			return "application/pdf";

		case CSV:
			return "text/plain";

		case ODT:
			return "application/vnd";

		case RTF:
			return "application/rtf";

		case ZIP:
			return "application/zip";

		default:
			return "";
		}
	}

	public boolean isReportFilled() {
		return jasperPrint != null;
	}

	public JasperPrint getJasperPrint() {
		return jasperPrint;
	}

	/**
	 * JasperReport to JasperDesign converter
	 */
	public static JasperDesign jasperReportToJasperDesign(final JasperReport masterReport) throws Exception {
		JasperDesign jasperDesign = null;
		File masterReportFile = null;
		FileOutputStream fileOutStream = null;
		FileInputStream fileInputStream = null;

		try {
			masterReportFile = File.createTempFile("cmdbuild_jasper_to_design", ".tmp");
			fileOutStream = new FileOutputStream(masterReportFile);
			JRXmlWriter.writeReport(masterReport, fileOutStream, "UTF-8");
			fileOutStream.flush();
			fileOutStream.close();
			fileInputStream = new FileInputStream(masterReportFile);
			jasperDesign = JRXmlLoader.load(fileInputStream);
			fileInputStream.close();
		} finally {
			if (fileOutStream != null) {
				fileOutStream.close();
			}

			if (fileInputStream != null) {
				fileInputStream.close();
			}

			if (masterReportFile != null && masterReportFile.exists()) {
				masterReportFile.delete();
			}
		}

		return jasperDesign;
	}

	/**
	 * Search for subreport expression
	 */
	public static List<JRSubreport> getSubreports(final JasperDesign jd) {
		// Search parameter indicating IReport subreport's directory
		String subreportDir = new String();
		JRDesignParameter subreportDirPar;
		@SuppressWarnings("rawtypes")
		final Map jdMapParameters = jd.getParametersMap();
		if (jdMapParameters.containsKey("SUBREPORT_DIR")) {
			subreportDirPar = (JRDesignParameter) jdMapParameters.get("SUBREPORT_DIR");
			subreportDir = subreportDirPar.getDefaultValueExpression().getText();
		}
		subreportDir = subreportDir.replace("\"", ""); // deleting quotes
		if (!subreportDir.trim().equals("")) {
			Log.REPORT.debug("The directory of subreport is: " + subreportDir);
		}

		// Expressions
		final List<JRSubreport> subreportsList = new LinkedList<JRSubreport>();
		for (final JRBand band : getBands(jd)) {
			if (band != null && band.getChildren() != null) {
				searchSubreports(band.getChildren(), subreportsList); // adds in
																		// subreportList
																		// the
																		// JRSubreport
																		// found
			}
		}
		Log.REPORT.debug("In the report there are " + subreportsList.size() + " subreports");

		return subreportsList;
	}

	/**
	 * Search for images expression
	 */
	public static List<JRDesignImage> getImages(final JRBaseReport report) {
		final List<JRDesignImage> designImagesList = new LinkedList<JRDesignImage>();

		for (final JRBand band : getBands(report)) {
			if (band != null && band.getChildren() != null) {
				searchImages(band.getChildren(), designImagesList); // adds in
																	// imagesList
																	// the
																	// JRImages
																	// founded
			}
		}
		Log.REPORT.debug("In the report there are " + designImagesList.size() + " images");

		return designImagesList;
	}

	public static String getImageFileName(final JRImage jrImage) {
		String filename = "";
		String path = jrImage.getExpression().getText().replaceAll("\"", "");
		if (!path.trim().equals("")) {
			path = path.replaceAll("[\\\\]", "/");
			final StringTokenizer tokenizer = new StringTokenizer(path, "/");
			final int totToken = tokenizer.countTokens();
			for (int i = 0; i < totToken; i++) {
				filename = tokenizer.nextToken();
			}
		}
		return filename;
	}

	public static void setImageFilename(final JRImage jrImage, final String newValue) {
		final JRDesignExpression newImageExpr = new JRDesignExpression();
		newImageExpr.setText(newValue);
		((JRDesignImage) jrImage).setExpression(newImageExpr);
	}

	public static String getSubreportName(final JRSubreport jrSubreport) {
		final String srExpr = jrSubreport.getExpression().getText();
		String subreportPath;
		subreportPath = srExpr.replaceAll("\\$P\\{SUBREPORT_DIR\\}", "");
		subreportPath = subreportPath.replaceAll("\\+", ""); // substituting
																// plus with
																// separator
		subreportPath = subreportPath.replaceAll("[ \"]", ""); // removing
																// spaces,
																// quotes

		return subreportPath;
	}

	/**
	 * Update images expressions before uploading to DB; set names like
	 * "IMAGE1", "IMAGE2" etc
	 * 
	 */
	public static void prepareDesignImagesForUpload(final List<JRDesignImage> designImagesList) {
		for (int i = 0; i < designImagesList.size(); i++) {
			final JRDesignImage jrImage = designImagesList.get(i);

			// set expression
			final JRDesignExpression newImageExpr = new JRDesignExpression();
			final String newImageName = PARAM_IMAGE + i;
			newImageExpr.setText("$P{REPORT_PARAMETERS_MAP}.get(\"" + newImageName + "\")");
			jrImage.setExpression(newImageExpr);

			// set options
			jrImage.setUsingCache(true);
			jrImage.setOnErrorType(OnErrorTypeEnum.BLANK);
		}
	}

	/**
	 * Update images expressions before downloading (zip export); set original
	 * images name
	 * 
	 */
	public static void prepareDesignImagesForZipExport(final List<JRDesignImage> designImagesList,
			final String[] origImagesName) {
		for (int i = 0; i < designImagesList.size(); i++) {
			final JRDesignImage jrImage = designImagesList.get(i);
			final JRDesignExpression newImageExpr = new JRDesignExpression();
			newImageExpr.setText("\"" + origImagesName[i] + "\"");
			jrImage.setExpression(newImageExpr);
		}
	}

	/**
	 * Update subreport expressions before uploadng to DB; set names like
	 * "SUBREPORT1", "SUBREPORT2" etc
	 * 
	 */
	public static void prepareDesignSubreportsForUpload(final List<JRSubreport> subreportsList) {
		for (int i = 0; i < subreportsList.size(); i++) {
			final JRDesignSubreport jrSubreport = (JRDesignSubreport) subreportsList.get(i);
			final JRDesignExpression newExpr = new JRDesignExpression();
			final String newSubreportName = PARAM_SUBREPORT + (i + 1);
			newExpr.setText("$P{REPORT_PARAMETERS_MAP}.get(\"" + newSubreportName + "\")");
			jrSubreport.setExpression(newExpr);
		}
	}

	/**
	 * Update subreport expressions before downloading (zip export); set
	 * original names
	 * 
	 */
	public static void prepareDesignSubreportsForZipExport(final List<JRSubreport> designSubreports,
			final JasperReport[] jasperSubreports) {
		for (int i = 0; i < designSubreports.size(); i++) {
			final JRDesignSubreport jrSubreport = (JRDesignSubreport) designSubreports.get(i);
			final String subreportName = jasperSubreports[i + 1].getName() + ".jasper"; // 0
																						// =
																						// master
																						// report
			final JRDesignExpression newExpr = new JRDesignExpression();
			newExpr.setText("\"" + subreportName + "\"");
			jrSubreport.setExpression(newExpr);
		}
	}

	/**
	 * Check duplicate images
	 */
	public static boolean checkDuplicateImages(final List<JRDesignImage> designImages) {
		for (int i = 0; i < designImages.size(); i++) {
			final JRDesignImage image1 = designImages.get(i);
			final String filename1 = getImageFileName(image1);
			for (int j = (i + 1); j < designImages.size(); j++) {
				final JRDesignImage image2 = designImages.get(j);
				final String filename2 = getImageFileName(image2);
				if (filename1.equalsIgnoreCase(filename2)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Read image and get format name (png,gif,jpg)
	 */
	public static String getImageFormatName(final InputStream is) throws IOException {
		String format = "";
		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(is);
			final Iterator<ImageReader> readerIterator = ImageIO.getImageReaders(iis);
			if (readerIterator.hasNext()) {
				final ImageReader reader = readerIterator.next();
				format = reader.getFormatName();
			}
			iis.flush();
		} finally {
			iis.close();
		}
		is.reset();
		return format;
	}

	/**
	 * Get all the bands of the report
	 */
	public static List<JRBand> getBands(final JRBaseReport jasperDesign) {
		final List<JRBand> bands = new LinkedList<JRBand>();
		bands.add(jasperDesign.getTitle());
		bands.add(jasperDesign.getPageHeader());
		bands.add(jasperDesign.getColumnHeader());
		for (final JRBand detail : jasperDesign.getDetailSection().getBands()) {
			bands.add(detail);
		}
		bands.add(jasperDesign.getColumnFooter());
		bands.add(jasperDesign.getPageFooter());
		bands.add(jasperDesign.getLastPageFooter());
		bands.add(jasperDesign.getSummary());
		for (final JRGroup group : jasperDesign.getGroups()) {
			for (final JRBand band : (JRBand[]) ArrayUtils.addAll(group.getGroupFooterSection().getBands(), group
					.getGroupHeaderSection().getBands())) {
				bands.add(band);
			}
		}
		return bands;
	}

	/**
	 * Search for a JRImage in all the children of the input list and put it
	 * into a List.
	 * 
	 * @param elements
	 *            : the children of a JRElementGroup considered
	 */
	private static void searchImages(final List<JRChild> elements, final List<JRDesignImage> imagesList) {
		final Iterator<JRChild> i = elements.listIterator();
		while (i.hasNext()) {
			final Object jreg = i.next();
			if (jreg instanceof JRDesignImage) {
				imagesList.add((JRDesignImage) jreg);
			} else if (jreg instanceof JRElementGroup) {
				searchImages(((JRElementGroup) jreg).getChildren(), imagesList);
			}
		}
	}

	/**
	 * Search for a JRSubreport in all the children of the input list and put it
	 * into a List.
	 * 
	 * @param elements
	 *            : the children of a JRElementGroup considered
	 */
	private static void searchSubreports(final List<JRChild> elements, final List<JRSubreport> subreportsList) {
		final Iterator<JRChild> i = elements.listIterator();
		while (i.hasNext()) {
			final Object jreg = i.next();
			if (jreg instanceof JRSubreport) {
				subreportsList.add((JRSubreport) jreg);
			} else if (jreg instanceof JRElementGroup) {
				searchSubreports(((JRElementGroup) jreg).getChildren(), subreportsList);
			}
		}
	}

	/**
	 * Save .jrxml file
	 */
	private File saveJRXMLTmpFile(final JasperReport report) throws Exception {
		final File tmpFile = File.createTempFile("cmdbuild_report_debug", ".jrxml");
		final FileOutputStream fos = new FileOutputStream(tmpFile);
		JRXmlWriter.writeReport(report, fos, "UTF-8");
		fos.flush();
		fos.close();
		Log.REPORT.debug("REPORT jrxml file: " + tmpFile.getAbsolutePath());
		return tmpFile;
	}

}
