package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.FORMAT;
import static org.cmdbuild.servlets.json.ComunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.JRXML;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.REPORT_ID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JRDesignImage;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ReportException.ReportExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.Report;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.report.ReportFactory.ReportExtension;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.report.ReportFactoryTemplateSchema;
import org.cmdbuild.report.ReportParameter;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.MethodParameterResolver;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.servlets.utils.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModReport extends JSONBaseWithSpringContext {

	@JSONExported
	public JSONArray menuTree() throws JSONException, AuthException {
		final JSONArray serializer = new JSONArray();
		JSONObject item;

		item = new JSONObject();
		item.put("id", "Jasper");
		item.put("text", getTraslation("administration.modreport.importJRFormStep1.menuTitle"));
		item.put("leaf", true);
		item.put("cls", "file");
		item.put("type", "report");
		item.put("selectable", true);
		serializer.put(item);

		return serializer;
	}

	/**
	 * Print a report that lists all the classes
	 * 
	 * @param format
	 * @throws Exception
	 */
	@JSONExported
	public void printSchema( //
			@Parameter(FORMAT) final String format //
	) throws Exception {
		final ReportFactoryTemplateSchema rfts = new ReportFactoryTemplateSchema( //
				dataSource(), //
				ReportExtension.valueOf(format.toUpperCase()), //
				cmdbuildConfiguration(), //
				userDataView() //
		);
		rfts.fillReport();
		sessionVars().setReportFactory(rfts);
	}

	/**
	 * Print a report with the detail of a class
	 * 
	 * @param format
	 * @throws Exception
	 */
	@JSONExported
	public void printClassSchema(@Parameter(CLASS_NAME) final String className, @Parameter(FORMAT) final String format)
			throws Exception {

		final ReportFactoryTemplateSchema rfts = new ReportFactoryTemplateSchema( //
				dataSource(),//
				ReportExtension.valueOf(format.toUpperCase()), //
				className,//
				cmdbuildConfiguration(), //
				userDataView() //
		);

		rfts.fillReport();
		sessionVars().setReportFactory(rfts);
	}

	/**
	 * 
	 * Is the first step of the report upload Analyzes the JRXML and eventually
	 * return the configuration of the second step
	 */

	@Admin
	@JSONExported
	public JSONObject analyzeJasperReport( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(GROUPS) final String groups, //
			@Parameter(REPORT_ID) final int reportId, //
			@Parameter(value = JRXML, required = false) final FileItem file //
	) throws JSONException, NotFoundException {

		resetSession();
		final Report newReport = new Report(userStore());
		setReportSimpleAttributes(name, description, groups, reportId, newReport);

		final JSONObject out = new JSONObject();
		if (file.getSize() > 0) {
			setReportImagesAndSubReports(out, file, newReport);
		} else {
			// there is no second step
			out.put("skipSecondStep", true);
		}

		sessionVars().setNewReport(newReport);
		return out;
	}

	private void setReportImagesAndSubReports(final JSONObject serializer, final FileItem file, final Report newReport)
			throws JSONException {
		String[] imagesNames = null;
		int subreportsNumber = 0;

		final JasperDesign jd = loadJasperDesign(file);
		checkJasperDesignParameters(jd);
		final List<JRDesignImage> designImages = ReportFactory.getImages(jd);

		if (ReportFactory.checkDuplicateImages(designImages)) { // check
																// duplicates
			serializer.put("duplicateimages", true);
			serializer.put("images", "");
			serializer.put("subreports", "");
		} else {
			imagesNames = manageImages(serializer, designImages);
			subreportsNumber = manageSubReports(serializer, jd);
		}

		newReport.setImagesName(imagesNames);
		newReport.setSubreportsNumber(subreportsNumber);
		newReport.setJd(jd);
	}

	private void setReportSimpleAttributes(final String name, final String description, final String groups,
			final int reportId, final Report newReport) {
		newReport.setOriginalId(reportId);
		newReport.setCode(name);
		newReport.setDescription(description);
		newReport.setGroups(parseSelectedGroup(groups));
	}

	private int manageSubReports(final JSONObject serializer, final JasperDesign jd) throws JSONException {
		JSONArray jsonArray;
		JSONObject jsonObject;
		int subreportsNumber = 0;
		final List<JRSubreport> subreports = ReportFactory.getSubreports(jd);
		jsonArray = new JSONArray();
		for (final JRSubreport subreport : subreports) {
			final String subreportName = ReportFactory.getSubreportName(subreport);
			subreportsNumber++;

			// client
			jsonObject = new JSONObject();
			jsonObject.put("name", subreportName);
			jsonArray.put(jsonObject);
		}
		serializer.put("subreports", jsonArray);
		ReportFactory.prepareDesignSubreportsForUpload(subreports); // update
																	// expressions
																	// in design
		return subreportsNumber;
	}

	private String[] manageImages(final JSONObject serializer, final List<JRDesignImage> designImages)
			throws JSONException {
		JSONArray jsonArray;
		JSONObject jsonObject;
		String[] imagesNames;
		jsonArray = new JSONArray();
		imagesNames = new String[designImages.size()];
		for (int i = 0; i < designImages.size(); i++) {
			final String imageFilename = ReportFactory.getImageFileName(designImages.get(i));
			imagesNames[i] = imageFilename;

			// client
			jsonObject = new JSONObject();
			jsonObject.put("name", imageFilename);
			jsonArray.put(jsonObject);
		}
		serializer.put("images", jsonArray);
		ReportFactory.prepareDesignImagesForUpload(designImages); // update
																	// expressions
																	// in design
		return imagesNames;
	}

	private String[] parseSelectedGroup(final String groups) {
		final String[] stringGroups;
		if (groups != null && !groups.equals("")) {
			stringGroups = groups.split(",");
		} else {
			stringGroups = new String[0];
		}

		return stringGroups;
	}

	private void checkJasperDesignParameters(final JasperDesign jd) {
		final JRParameter[] parameters = jd.getParameters();
		for (final JRParameter parameter : parameters) {
			ReportParameter.parseJrParameter(parameter);
		}
	}

	private JasperDesign loadJasperDesign(final FileItem file) {
		JasperDesign jd = null;
		try {
			jd = JRXmlLoader.load(file.getInputStream());
		} catch (final Exception e) {
			Log.REPORT.error("Error loading report", e);
			throw ReportExceptionType.REPORT_INVALID_FILE.createException();
		}
		return jd;
	}

	@Admin
	@JSONExported
	/**
	 * Is the second step of the report
	 * import. Manage the sub reports and
	 * the images
	 * 
	 * @param files
	 * @throws JSONException
	 * @throws AuthException
	 */
	public void importJasperReport(@Request(MethodParameterResolver.MultipartRequest) final List<FileItem> files)
			throws JSONException, AuthException {
		final Report newReport = sessionVars().getNewReport();

		if (newReport.getJd() != null) {
			importSubreportsAndImages(files, newReport);
		}

		saveReport(newReport);
		resetSession();
	}

	@Admin
	@JSONExported
	public void saveJasperReport() {
		final Report newReport = sessionVars().getNewReport();
		saveReport(newReport);
	}

	private void saveReport(final Report newReport) {
		final ReportStore reportStore = reportStore();
		try {
			if (newReport.getOriginalId() < 0) {
				reportStore.insertReport(newReport);
			} else {
				reportStore.updateReport(newReport);
			}
		} catch (final SQLException e) {
			Log.REPORT.error("Error saving report");
		} catch (final IOException e) {
			Log.REPORT.error("Error saving report");
			e.printStackTrace();
		}
	}

	private void importSubreportsAndImages(final List<FileItem> files, final Report newReport) {
		try {

			/*
			 * TODO check images and subreport files - check all elements of
			 * "files" param (ie: files.get(i).isFormField() ) - compare
			 * filename requested and filename uploaded
			 */

			// get IMAGES
			final int nImages = newReport.getImagesName().length;

			// imageByte contains the stream of imagesFiles[]
			final byte[][] imageByte = new byte[nImages][];
			// lengthImageByte contains the lengths of all imageByte[]
			final Integer lengthImagesByte[] = new Integer[nImages];

			for (int i = 0; i < nImages; i++) {
				// loading the image file and putting it in imageByte
				imageByte[i] = files.get(i).get();
			}

			// get REPORTS
			final int nReports = newReport.getSubreportsNumber() + 1; // subreports
																		// + 1
																		// master
																		// report

			// imageByte contains the stream of imagesFiles[]
			final byte[][] reportByte = new byte[nReports][];
			// lengthImageByte contains the lengths of all imageByte[]
			final Integer lengthReportByte[] = new Integer[nReports];

			for (int i = 0; i < nReports - 1; i++) {
				// load the subreport .jasper file and put it in reportByte
				reportByte[i + 1] = files.get(i + nImages).get(); // i+1 because
																	// of the
																	// master
																	// report
																	// with
																	// index 0
			}

			// check if all files have been uploaded
			boolean fileNotUploaded = false;

			for (int i = 0; i < nImages; i++) {
				if (imageByte[i] == null) {
					fileNotUploaded = true;
				}
			}

			for (int i = 1; i < nReports; i++) { // must start at 1 because 0 is
													// master report
				if (reportByte[i] == null) {
					fileNotUploaded = true;
				}
			}

			if (!fileNotUploaded) {

				// IMAGES
				for (int i = 0; i < nImages; i++) {
					lengthImagesByte[i] = imageByte[i].length;
				}

				int totByte = 0; // total n. of bytes needed to store all images
				for (int i = 0; i < nImages; i++) {
					totByte += lengthImagesByte[i];
				}

				// array of bytes to store into db all reports
				final byte[] imagesByte = new byte[totByte];

				int startAt = 0; // determinate position in which starts a new
									// image

				// puts in imageByte all the reports
				for (int i = 0; i < nImages; i++) {
					for (int j = 0; j < lengthImagesByte[i]; j++) {
						imagesByte[startAt + j] = imageByte[i][j];
					}
					startAt += lengthImagesByte[i];
				}

				// REPORTS
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				JasperCompileManager.compileReportToStream(newReport.getJd(), os);
				reportByte[0] = os.toByteArray(); // master report in bytes

				for (int i = 0; i < nReports; i++) {
					lengthReportByte[i] = reportByte[i].length;
				}

				totByte = 0; // total n. of bytes needed to store all reports
								// (master and subreports)
				for (int i = 0; i < nReports; i++) {
					totByte += lengthReportByte[i];
				}

				// array of bytes to store into db
				final byte[] reportsByte = new byte[totByte];

				startAt = 0; // determinate position in which starts a new
								// report

				// puts in reportByte all the reports
				for (int i = 0; i < nReports; i++) {
					for (int j = 0; j < lengthReportByte[i]; j++) {
						reportsByte[startAt + j] = reportByte[i][j];
					}
					startAt += lengthReportByte[i];
				}

				// update report data
				newReport.setType(ReportType.CUSTOM);
				newReport.setStatus("A");
				newReport.setRichReport(reportsByte);
				newReport.setSimpleReport(reportsByte);
				newReport.setReportLength(lengthReportByte);
				newReport.setBeginDate(new Date());

				// update query
				final JRQuery jrQuery = newReport.getJd().getQuery();
				if (jrQuery != null) {
					final String query = jrQuery.getText();
					query.replaceAll("\"", "\\\"");
					newReport.setQuery(query);
				}

				if (imageByte != null) {
					newReport.setImages(imagesByte);
					newReport.setImagesLength(lengthImagesByte);
				}
			} else {
				throw ReportExceptionType.REPORT_UPLOAD_ERROR.createException();
			}
		} catch (final JRException e) {
			Log.REPORT.error("Error compiling report", e);
			throw ReportExceptionType.REPORT_COMPILE_ERROR.createException();
		} catch (final NoClassDefFoundError e) {
			Log.REPORT.error("Class not found error", e);
			throw ReportExceptionType.REPORT_NOCLASS_ERROR.createException(e.getMessage());
		}
	}

	@JSONExported
	public void deleteReport(@Parameter(ID) final int id) throws JSONException {
		reportStore().deleteReport(id);
	}

	/**
	 * Reset session, last "import report" operation
	 * 
	 * @param serializer
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public void resetSession() throws JSONException {
		sessionVars().removeNewReport();
	}
}
