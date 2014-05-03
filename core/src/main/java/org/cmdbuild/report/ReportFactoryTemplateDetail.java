package org.cmdbuild.report;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRCommonText;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.report.ReportFactoryTemplateDetailSubreport.SubreportType;
import org.cmdbuild.report.query.CardReportQuery;
import org.cmdbuild.services.localization.Localization;

public class ReportFactoryTemplateDetail extends ReportFactoryTemplate {

	private static final int PAGE_MARGIN = 30;
	private static final String TRANSLATION_KEY = "management.modcard.tabs.card";
	private final Card card;
	private final String designTitle;
	private final JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final Localization localization;
	private final CmdbuildConfiguration configuration;

	private final static String REPORT = "CMDBuild_card_detail.jrxml";
	private final static String NOTES = "Notes";

	public ReportFactoryTemplateDetail( //
			final DataSource dataSource, //
			final String className, //
			final Long cardId, //
			final ReportExtension reportExtension, //
			final CMDataView dataView, //
			final DataAccessLogic dataAccessLogic, //
			final Localization localization, //
			final CmdbuildConfiguration configuration //
	) throws JRException {
		super(dataSource, configuration, dataView);
		this.reportExtension = reportExtension;
		this.localization = localization;
		this.configuration = configuration;

		card = dataAccessLogic.fetchCard(className, cardId);
		designTitle = localization.get(TRANSLATION_KEY);

		// load design
		this.jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);

		// initialize design with the query
		final String query = new CardReportQuery(card, dataView).toString();
		Log.REPORT.debug(String.format("Card Report Query: %s", query));

		initDesign(query);
	}

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}

	private void initDesign(final String query) throws JRException {
		final CMClass table = card.getType();
		final String tableName = table.getIdentifier().getLocalName();
		jasperDesign.setName(tableName);
		setQuery(query);
		setFields(table.getActiveAttributes());

		// set detail band
		setDetail();

		setTitle(tableName + " - " + card.getAttribute("Description"));
		updateImagesPath();

		// parameters
		addDesignParameter("Card_Detail_Title", designTitle);
		addFillParameter("Card_Detail_Title", designTitle);

		// relations subreport
		setRelationsSubreport();
	}

	/**
	 * Add relations subreport to fill parameters
	 * 
	 * @throws JRException
	 */
	private void setRelationsSubreport() throws JRException {
		final ReportFactoryTemplateDetailSubreport rftds = new ReportFactoryTemplateDetailSubreport( //
				dataSource, //
				SubreportType.RELATIONS, //
				card.getType(), //
				card, //
				dataView, //
				localization, //
				configuration);
		final JasperReport compiledSubreport = rftds.compileReport();
		addFillParameter("relations_subreport", compiledSubreport);
	}

	private void setDetail() {

		// get (sorted) list of attributes
		final List<CMAttribute> attributesToShow = new LinkedList<CMAttribute>();
		CMAttribute notes = null;
		for (final CMAttribute attribute : card.getType().getActiveAttributes()) {
			if (attribute.isActive()) {
				attributesToShow.add(attribute);
				if (isNotesAttribute(attribute)) {
					notes = attribute;
				}
			}
		}

		Collections.sort(attributesToShow, new CMAttributeComparator());

		// place notes at the end
		if (notes != null) {
			attributesToShow.remove(notes);
			attributesToShow.add(notes);
		}

		// clear band
		final JRSection section = getJasperDesign().getDetailSection();
		final JRBand band = section.getBands()[0];
		band.getChildren().clear();

		// add textfields
		final int x = 0;
		int y = 0;
		final int width = jasperDesign.getPageWidth() - (PAGE_MARGIN * 2);
		final int height = 20;
		final int verticalStep = 20;
		for (final CMAttribute attribute : attributesToShow) {

			// print line for notes attribute
			if (isNotesAttribute(attribute)) {
				final JRDesignLine line = new JRDesignLine();
				line.setX(x);
				line.setY(y);
				line.setHeight(1);
				line.setWidth(width);
				line.setPositionType(PositionTypeEnum.FLOAT);
				band.getChildren().add(line);
				y += (verticalStep / 2);
			}

			// print text-field
			final JRDesignTextField tf = createTextFieldForAttribute(attribute);
			tf.setHeight(height);
			tf.setWidth(width);
			tf.setX(x);
			tf.setY(y);
			band.getChildren().add(tf);

			y += verticalStep;
		}

		// update band height
		final int detailHeight = y + 5;
		final JRDesignBand db = (JRDesignBand) band;
		db.setHeight(detailHeight);

		// update page height (if necessary)
		int totBandsHeight = 0;
		for (final JRBand myBand : getBands(jasperDesign)) {
			if (myBand != null) {
				totBandsHeight += myBand.getHeight();
			}
		}
		if (totBandsHeight > jasperDesign.getPageHeight()) {
			jasperDesign.setPageHeight(totBandsHeight);
		}
	}

	/**
	 * Create a texfield with an expression like:
	 * msg("Descrizione : {0}",$F{Computer_Description
	 * }).equals("Descrizione : null"
	 * )?"Descrizione : ":msg("Descrizione : {0}",$F{Computer_Description})
	 * 
	 */
	private JRDesignTextField createTextFieldForAttribute(final CMAttribute attribute) {
		// get default texfield
		final String attributeName = attribute.getOwner().getIdentifier().getLocalName() + "_" + attribute.getName();
		final JRDesignTextField dtf = super.createTextFieldForAttribute(attributeName, attribute.getType());

		// customize expression
		String label;
		if (attribute.getDescription() != null && !attribute.getDescription().equals("")) {

			label = attribute.getDescription();
		} else {
			label = attribute.getName();
		}

		label = label + " : "; // ie - Descrizione : null
		// ie - $F{Computer_Description}
		String fieldattname;
		fieldattname = "$F{"
				+ getAttributeName(card.getType().getIdentifier().getLocalName() + "#" + attribute.getName(),
						attribute.getType()) + "}";

		final String fieldmsg = "msg(\"" + label + "{0}\"," + fieldattname + ")"; // ie
		// - msg("Descrizione : {0}",$F{Computer_Description})
		final String fieldnull = label + "null"; // ie - Descrizione : null
		final String completeexp = fieldmsg + ".equals(\"" + fieldnull + "\")?\"" + label + "\":" + fieldmsg; // ie
		// -
		// msg("Descrizione : {0}",$F{Computer_Description}).equals("Descrizione : null")?"Descrizione : ":msg("Descrizione : {0}",$F{Computer_Description})
		final JRDesignExpression exp = new JRDesignExpression();
		exp.setText(completeexp);
		dtf.setExpression(exp);
		dtf.setMarkup(JRCommonText.MARKUP_HTML);

		return dtf;
	}

	private boolean isNotesAttribute(final CMAttribute attribute) {
		return NOTES.equals(attribute.getName());
	}

	private class CMAttributeComparator implements Comparator<CMAttribute> {
		@Override
		public int compare(final CMAttribute a1, final CMAttribute a2) {
			if (a1.getIndex() > a2.getIndex()) {
				return 1;
			} else if (a1.getIndex() < a2.getIndex()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
