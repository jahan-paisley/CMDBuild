package org.cmdbuild.report;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.QuerySpecsBuilderFiller;

public class ReportFactoryTemplateList extends ReportFactoryTemplate {

	private final List<String> attributeNamesSorted;
	private JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final CMClass table;
	private final static String REPORT_PDF = "CMDBuild_list.jrxml";
	private final static String REPORT_CSV = "CMDBuild_list_csv.jrxml";

	public ReportFactoryTemplateList( //
			final DataSource dataSource, //
			final ReportExtension reportExtension, //
			final QueryOptions queryOptions, //
			final List<String> attributeOrder, //
			final String className, //
			final DataAccessLogic dataAccessLogic, //
			final CMDataView dataView, //
			final CmdbuildConfiguration configuration //
	) throws JRException {
		super(dataSource, configuration, dataView);

		this.reportExtension = reportExtension;
		this.attributeNamesSorted = attributeOrder;

		table = dataAccessLogic.findClass(className);
		final QuerySpecsBuilder querySpecsBuilder = new QuerySpecsBuilderFiller(dataView, queryOptions, className) //
				.create();

		final QueryCreator queryCreator = new QueryCreator(querySpecsBuilder.build());
		loadDesign(reportExtension);
		initDesign(queryCreator);
	}

	private void loadDesign(final ReportExtension reportExtension) throws JRException {
		if (reportExtension == ReportExtension.PDF) {
			this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT_PDF);
		} else {
			this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT_CSV);
		}
	}

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}

	private void initDesign(final QueryCreator queryCreator) throws JRException {
		setNameFromTable();
		setQuery(queryCreator);
		setAllTableFields();
		setTextFieldsInDetailBand();
		setColumnHeadersForNewFields();

		if (reportExtension == ReportExtension.PDF) {
			setTitleFromTable();
			updateImagesPath();
		}

		refreshLayout();
	}

	private void setNameFromTable() {
		jasperDesign.setName(table.getIdentifier().getLocalName());
	}

	protected void setQuery(final QueryCreator queryCreator) {
		final String queryString = getQueryString(queryCreator);
		setQuery(queryString);
	}

	private void setTitleFromTable() {
		setTitle(table.getIdentifier().getLocalName());
	}

	private void setAllTableFields() throws JRException {
		final List<CMAttribute> fields = new LinkedList<CMAttribute>();
		for (final String attributeName : attributeNamesSorted) {
			final CMAttribute attribute = table.getAttribute(attributeName);
			// If try to take a reserved
			// attribute form the table
			// it returns null
			if (attribute != null) {
				fields.add(attribute);
			}
		}

		setFields(fields);
	}

	@Override
	protected String fieldNameFromCMAttribute(final CMAttribute cmAttribute) {
		final CMEntryType attributeOwner = cmAttribute.getOwner();
		final String attributeAlias = String.format("%s#%s", attributeOwner.getIdentifier().getLocalName(),
				cmAttribute.getName());
		final String fieldName = getAttributeName(attributeAlias, cmAttribute.getType());

		Log.REPORT.debug(String.format("fieldNameFromCMAttribut: %s", fieldName));
		return fieldName;
	}

	private void setTextFieldsInDetailBand() {
		final JRSection section = jasperDesign.getDetailSection();
		final JRBand band = section.getBands()[0];
		final List<JRChild> graphicVector = new ArrayList<JRChild>();

		for (final JRChild obj : band.getChildren()) {
			if (!(obj instanceof JRDesignTextField)) {
				graphicVector.add(obj);
			}
		}

		final List<JRChild> detailVector = new ArrayList<JRChild>();
		for (final String attributeName : attributeNamesSorted) {
			final CMAttribute attribute = table.getAttribute(attributeName);
			if (attribute != null) {
				final String attributeAlias = String.format("%s#%s", table.getIdentifier().getLocalName(),
						attribute.getName());
				Log.REPORT.debug(String.format("setTextFieldsInDetailBand: %s", attributeAlias));
				detailVector.add(createTextFieldForAttribute(attributeAlias, attribute.getType()));
			}
		}

		band.getChildren().clear();
		band.getChildren().addAll(graphicVector);
		band.getChildren().addAll(detailVector);
	}

	private void setColumnHeadersForNewFields() {
		final JRBand columnHeader = jasperDesign.getColumnHeader();
		final JRElement[] elements = columnHeader.getElements();
		final Vector<JRElement> designHeaders = new Vector<JRElement>();
		final Vector<JRElement> designElements = new Vector<JRElement>();

		// backup existing design elements
		for (int i = 0; i < elements.length; i++) {
			if (!(elements[i] instanceof JRDesignStaticText)) {
				designElements.add(elements[i]);
			}
		}

		// create column headers
		for (final String attribute : attributeNamesSorted) {
			final CMAttribute cmAttribute = table.getAttribute(attribute);
			if (cmAttribute != null) {
				String description = cmAttribute.getDescription();
				if ("".equals(description) || description == null) {
					description = cmAttribute.getName();
				}
				final JRDesignStaticText dst = new JRDesignStaticText();
				dst.setText(description);
				designHeaders.add(dst);
			}
		}

		// save new list of items
		columnHeader.getChildren().clear();
		columnHeader.getChildren().addAll(designElements);
		columnHeader.getChildren().addAll(designHeaders);
	}

	/*
	 * Update position of report elements
	 */
	private void refreshLayout() {
		// calculate weight of all elements
		final Map<String, String> weight = new HashMap<String, String>();
		int virtualWidth = 0;
		int size = 0;
		final int height = 17;

		String key = "";
		CMAttribute attribute = null;
		for (final String attributeName : attributeNamesSorted) {
			attribute = table.getAttribute(attributeName);
			if (attribute == null) {
				continue;
			}

			size = getSizeFromAttribute(attribute);
			virtualWidth += size;

			key = getAttributeName( //
					String.format("%s#%s", table.getIdentifier().getLocalName(), attribute.getName()), //
					attribute.getType() //
			);

			weight.put(attribute.getName(), Integer.toString(size));
			weight.put(key, Integer.toString(size));
			weight.put(attribute.getDescription(), Integer.toString(size));
		}

		final int pageWidth = jasperDesign.getPageWidth();
		final double cx = (pageWidth * 0.95) / virtualWidth;
		Log.REPORT.debug("cx=" + cx + " pageWidth " + (pageWidth * 0.95) + " / virtualWidth " + virtualWidth);
		double doub = 0;
		final JRSection section = jasperDesign.getDetailSection();
		final JRBand detail = section.getBands()[0];
		JRElement[] elements = detail.getElements();
		JRDesignTextField dtf = null;
		int x = 0;
		final int y = 2;
		Log.REPORT.debug("RF updateDesign DESIGN");
		JRDesignExpression varExpr = null;
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof JRDesignTextField) {
				dtf = (JRDesignTextField) elements[i];
				varExpr = (JRDesignExpression) dtf.getExpression();
				key = varExpr.getText();
				Log.REPORT.debug("text=" + key);
				key = key.substring(3, key.length() - 1);
				Log.REPORT.debug("text=" + key);
				key = weight.get(key);
				Log.REPORT.debug("kry=" + key);
				try {
					size = Integer.parseInt(key);
				} catch (final NumberFormatException e) {
					size = 0;
				}
				doub = size * cx;
				size = (int) doub;
				dtf.setX(x);
				dtf.setY(y);
				dtf.setWidth(size);
				dtf.setHeight(height);
				dtf.setBlankWhenNull(true);
				dtf.setStretchWithOverflow(true);
				Log.REPORT.debug("RF updateDesign x=" + dtf.getX() + " Width=" + dtf.getWidth());
				x += size;
			}
		}

		// sizing table headers
		final JRBand columnHeader = jasperDesign.getColumnHeader();
		elements = columnHeader.getElements();
		JRDesignStaticText dst = null;
		x = 0;
		Log.REPORT.debug("RF updateDesign HEADER");
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof JRDesignStaticText) {
				dst = (JRDesignStaticText) elements[i];
				key = dst.getText();
				Log.REPORT.debug("text=" + key);
				key = weight.get(key);
				Log.REPORT.debug("key=" + key);
				size = Integer.parseInt(key);

				doub = size * cx;
				size = (int) doub;
				dst.setForecolor(Color.WHITE);
				dst.setX(x);
				dst.setHeight(height);
				dst.setWidth(size);
				Log.REPORT.debug("RF updateDesign" + dst.getText() + " x=" + dst.getX() + " Width=" + dst.getWidth());
				x += size;
			}
		}
	}

	protected int getSizeFromAttribute(final CMAttribute attribute) {
		return new ReportAttributeSizeVisitor().getSize(attribute.getType());
	}
}
