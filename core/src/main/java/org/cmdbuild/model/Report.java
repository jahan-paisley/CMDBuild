package org.cmdbuild.model;

import static org.cmdbuild.utils.BinaryUtils.fromByte;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.report.ReportFactory.ReportType;

public class Report {

	public static final String REPORT_CLASS_NAME = "Report";

	private final UserStore userStore;

	private int id = 0;
	private String code = "";
	private String description = "";
	private String status = "A";
	private String user = "";
	private Date beginDate = new Date();

	private String query = "";
	private byte[] simpleReport = new byte[0];
	private byte[] richReport = new byte[0];
	private byte[] wizard = new byte[0];
	private byte[] images = new byte[0];
	private Integer[] imagesLength = new Integer[0];
	private Integer[] reportLength = new Integer[0];
	private String[] imagesName = new String[0];
	private String[] groups = new String[0];

	public Report(final UserStore userStore) {
		this.userStore = userStore;
	}

	/**
	 * id of the report we're editing, "-1" if it's a new one (administration
	 * side)
	 */
	private int originalId = -1;

	/**
	 * number of subreport elements (administration side)
	 */
	private int subreportsNumber = -1;

	/**
	 * jasper design created from uploaded file (administration side)
	 */
	private JasperDesign jasperDesign = null;

	public ReportType getType() {
		// There is only one type
		// return it
		return ReportType.CUSTOM;
	}

	public void setType(final ReportType type) {
		// Do nothing
		// there is only one report type
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public String getUser() {
		return user;
	}

	public void setUser(final String user) {
		this.user = user;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(final Date beginDate) {
		this.beginDate = beginDate;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	public int getOriginalId() {
		return originalId;
	}

	public void setOriginalId(final int originalId) {
		this.originalId = originalId;
	}

	public JasperDesign getJd() {
		return jasperDesign;
	}

	public void setJd(final JasperDesign jasperDesign) {
		this.jasperDesign = jasperDesign;
	}

	public void setGroups(final String[] groupNames) {
		this.groups = groupNames;
	}

	public String[] getGroups() {
		return groups;
	}

	/**
	 * Get rich report as byte array
	 */
	public byte[] getRichReportBA() {
		return richReport;
	}

	/**
	 * Get rich report as JasperReport objects array
	 */
	public JasperReport[] getRichReportJRA() throws ClassNotFoundException, IOException {
		int parseLength = 0;
		byte[] singleBin = null;
		final Integer[] length = getReportLength();
		final JasperReport[] obj = new JasperReport[length.length];
		final byte[] bin = getRichReportBA();

		// splits the reports in master and subreports
		for (int i = 0; i < length.length; i++) {
			singleBin = new byte[length[i]];
			for (int j = 0; j < length[i]; j++) {
				singleBin[j] = bin[parseLength + j];
			}
			parseLength += length[i];
			if (singleBin != null && length[i] > 0) {
				obj[i] = (JasperReport) fromByte(singleBin);
			}
		}

		return obj;
	}

	public void setRichReport(final byte[] richReport) {
		this.richReport = richReport;
	}

	public byte[] getSimpleReport() {
		return simpleReport;
	}

	public void setSimpleReport(final byte[] simpleReport) {
		this.simpleReport = simpleReport;
	}

	public byte[] getWizard() {
		return wizard;
	}

	public void setWizard(final byte[] wizard) {
		this.wizard = wizard;
	}

	/**
	 * Get report images as byte array
	 */
	public byte[] getImagesBA() {
		return images;
	}

	/**
	 * Get report images as input stream array
	 */
	public InputStream[] getImagesISA() {
		final byte[] binary = getImagesBA();
		final Integer[] imagesLength = getImagesLength();
		InputStream[] obj = new InputStream[0];
		if (imagesLength != null) {
			obj = new InputStream[imagesLength.length];
			int parseLength = 0;
			byte[] singleBin = null;

			// splits the images
			for (int i = 0; i < imagesLength.length; i++) {
				singleBin = new byte[imagesLength[i]];
				for (int j = 0; j < imagesLength[i]; j++) {
					singleBin[j] = binary[parseLength + j];
				}
				parseLength += imagesLength[i];
				if (singleBin != null && imagesLength[i] > 0) {
					obj[i] = new ByteArrayInputStream(singleBin);
				}
			}
		}
		return obj;
	}

	public void setImages(final byte[] images) {
		this.images = images;
	}

	public Integer[] getReportLength() {
		return reportLength;
	}

	public void setReportLength(final Integer[] reportLength) {
		this.reportLength = reportLength;
	}

	public Integer[] getImagesLength() {
		return imagesLength;
	}

	public void setImagesLength(final Integer[] imagesLength) {
		this.imagesLength = imagesLength;
	}

	public String[] getImagesName() {
		return imagesName;
	}

	public void setImagesName(final String[] imagesNames) {
		this.imagesName = imagesNames;
	}

	public void setSubreportsNumber(final int subreportsNumber) {
		this.subreportsNumber = subreportsNumber;
	}

	public int getSubreportsNumber() {
		return subreportsNumber;
	}

	/**
	 * TODO: create a ReportPrivilegeFetcher. The responsibility of that class
	 * is to fetch privileges from Report table. This class will implement
	 * CMPrivilegedObject and managed like other privilege objects (View, Class
	 * ecc)
	 */
	public boolean isUserAllowed() {
		final OperationUser operationUser = userStore.getUser();
		if (operationUser.hasAdministratorPrivileges()) {
			return true;
		}
		final List<String> allowedGroupIdsForThisReport = Arrays.asList(getGroups());
		final String groupUsedForLogin = operationUser.getPreferredGroup().getName();
		if (allowedGroupIdsForThisReport.contains(groupUsedForLogin.toString())) {
			return true;
		}
		return false;
	}

}
