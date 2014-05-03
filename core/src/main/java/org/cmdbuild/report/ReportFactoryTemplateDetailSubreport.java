package org.cmdbuild.report;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.dao.driver.postgres.Const.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;
import static org.cmdbuild.dao.query.clause.AnyDomain.anyDomain;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Utils.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.localization.Localization;
import org.cmdbuild.utils.guava.Functions;

import com.google.common.base.Joiner;

public class ReportFactoryTemplateDetailSubreport extends ReportFactoryTemplate {

	public static enum SubreportType {
		RELATIONS
	}

	private static class SubreportAttribute {

		private final String queryName;
		private final String label;
		private final CMAttribute cmAttribute;

		public SubreportAttribute(final String queryName, final String label, final CMAttribute attribute) {
			super();
			this.queryName = queryName;
			this.label = label;
			cmAttribute = attribute;
		}

		public String getQueryName() {
			return queryName;
		}

		public String getLabel() {
			return label;
		}

		public CMAttribute getCMDBuildAttribute() {
			return cmAttribute;
		}
	}

	private final static String REPORT = "CMDBuild_card_detail_subreport.jrxml";

	public static final String CLASS_DESCRIPTION = "TargetDescription";
	public static final String DOMAIN_DESCRIPTION = "DomainDescription";
	public static final String BEGIN_DATE = "BeginDate";
	public static final String CODE = "Code";
	public static final String DESCRIPTION = "Description";

	private final JasperDesign jasperDesign;
	private final ReportExtension reportExtension;
	private final List<SubreportAttribute> attributes;
	private String designTitle;
	private final Localization localization;

	@Override
	public JasperDesign getJasperDesign() {
		return jasperDesign;
	}

	@Override
	public ReportExtension getReportExtension() {
		return reportExtension;
	}

	public ReportFactoryTemplateDetailSubreport(final DataSource dataSource, final SubreportType subreportType,
			final CMClass table, final Card card, final CMDataView dataView, final Localization localization,
			final CmdbuildConfiguration configuration) throws JRException {
		super(dataSource, configuration, dataView);
		// init vars
		this.reportExtension = ReportExtension.PDF;
		this.attributes = new LinkedList<SubreportAttribute>();
		this.localization = localization;

		final String query;
		switch (subreportType) {
		case RELATIONS:

			designTitle = getTranslation("management.modcard.tabs.relations");
			attributes.add( //
					new SubreportAttribute( //
							DOMAIN_DESCRIPTION, //
							getTranslation("management.modcard.relation_columns.domain"), //
							ReportParameterConverter.of(new RPFake(DESCRIPTION)).toCMAttribute()));
			attributes.add( //
					new SubreportAttribute( //
							CLASS_DESCRIPTION, //
							getTranslation("management.modcard.relation_columns.destclass"), //
							ReportParameterConverter.of(new RPFake(DESCRIPTION)).toCMAttribute()));
			attributes.add( //
					new SubreportAttribute( //
							"begindate", //
							getTranslation("management.modcard.relation_columns.begin_date"), //
							ReportParameterConverter.of(new RPFake(BEGIN_DATE)).toCMAttribute()));
			attributes.add( //
					new SubreportAttribute( //
							CODE, //
							getTranslation("management.modcard.relation_columns.code"), //
							ReportParameterConverter.of(new RPFake(CODE)).toCMAttribute()));
			attributes.add( //
					new SubreportAttribute( //
							DESCRIPTION, //
							getTranslation("management.modcard.relation_columns.description"), //
							ReportParameterConverter.of(new RPFake(DESCRIPTION)).toCMAttribute()));

			final Alias DOM_ALIAS = NameAlias.as("DOM");
			final Alias DST_ALIAS = NameAlias.as("DST");
			final CMClass srcCardType = dataView.findClass(card.getClassName());
			final WhereClause clause = condition(attribute(srcCardType, ID_ATTRIBUTE), eq(card.getId()));

			final QuerySpecs querySpecs = dataView
					.select(attribute(DST_ALIAS, CODE), attribute(DST_ALIAS, DESCRIPTION)) //
					.from(srcCardType) //
					.join(anyClass(), as(DST_ALIAS), over(anyDomain(), as(DOM_ALIAS))) //
					.where(clause) //
					.orderBy(attribute(DST_ALIAS, DESCRIPTION), Direction.ASC).build();
			final QueryCreator queryCreator = new QueryCreator(querySpecs);
			final Iterable<Long> availableClassIds = from(dataView.findClasses()) //
					.transform(Functions.id());
			query = format("" //
					+ "SELECT"
					+ " _cm_read_comment(_cm_comment_for_table_id(\"_DST_IdClass\"), 'DESCR') AS \"%s\","
					+ " _cm_read_comment(_cm_comment_for_table_id(\"_DOM_IdDomain\")," //
					+ " CASE WHEN \"_DOM__Src\" = '_1' THEN 'DESCRDIR' ELSE 'DESCRINV' END" //
					+ " ) AS \"%s\"," //
					+ " \"_DST_BeginDate\" AS \"%s\"," //
					+ " \"DST#Code\" AS \"%s\"," //
					+ " \"DST#Description\" AS \"%s\"" //
					+ " FROM (%s) AS main" //
					+ " WHERE \"_DST_IdClass\" IN (%s)" //
					+ " ORDER BY \"%s\", \"%s\"" //
					+ "", //
					CLASS_DESCRIPTION, //
					DOMAIN_DESCRIPTION, //
					BEGIN_DATE, //
					CODE, //
					DESCRIPTION, //
					getQueryString(queryCreator), //
					Joiner.on(",").join(availableClassIds), //
					DOMAIN_DESCRIPTION, //
					DESCRIPTION);
			break;

		default:
			query = EMPTY;
			break;
		}

		// load design
		jasperDesign = JRXmlLoader.load(getReportDirectory() + REPORT);

		// initialize design
		Log.REPORT.debug(String.format("Report on relations query: %s", query));
		initDesign(query);
	}

	private String getTranslation(final String key) {
		return localization.get(key);
	}

	public JasperReport compileReport() throws JRException {
		JasperReport subreport = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		ByteArrayInputStream byteArrayInputStream = null;
		try {
			byteArrayOutputStream = new ByteArrayOutputStream();
			JasperCompileManager.compileReportToStream(jasperDesign, byteArrayOutputStream);
			byteArrayOutputStream.flush();
			byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			subreport = (JasperReport) JRLoader.loadObject(byteArrayInputStream);
		} catch (final IOException e) {
			try {
				if (byteArrayOutputStream != null) {
					byteArrayOutputStream.close();
				}
			} catch (final IOException e1) {
				// do nothing
			}
		}

		return subreport;
	}

	private void initDesign(final String query) throws JRException {
		// set report query
		setQuery(query);

		// set report fields
		deleteAllFields();
		for (final SubreportAttribute attribute : attributes) {
			final CMAttribute cmAttribute = attribute.getCMDBuildAttribute();
			if (cmAttribute == null) {
				continue;
			}

			String name = "";
			if (attribute.getQueryName() != null) {
				name = attribute.getQueryName();
			} else {
				name = attribute.getCMDBuildAttribute().getName();
			}

			addField(name, attribute.getCMDBuildAttribute().getDescription(), attribute.getCMDBuildAttribute()
					.getType());
		}

		// set column header
		setColumnHeader();

		// set detail
		setDetail();

		// set design parameters
		addDesignParameter("title", designTitle);
	}

	private void setColumnHeader() {
		final JRBand band = jasperDesign.getColumnHeader();

		// clear band
		band.getChildren().clear();

		// add texts
		final int horizontalStep = Math.round(jasperDesign.getPageWidth() / attributes.size());
		int x = 0;
		final int y = 4;
		for (final SubreportAttribute attribute : attributes) {
			final JRDesignStaticText st = createStaticText(attribute.getLabel());
			st.setMode(ModeEnum.OPAQUE);
			st.setBackcolor(new Color(236, 236, 236)); // gray
			st.setHeight(20);
			st.setWidth(horizontalStep);
			st.setX(x);
			st.setY(y);
			band.getChildren().add(st);

			x += horizontalStep;
		}
	}

	private void setDetail() {
		final JRSection section = jasperDesign.getDetailSection();
		final JRBand band = section.getBands()[0];

		// clear band
		band.getChildren().clear();

		// add textfields
		final int horizontalStep = Math.round(jasperDesign.getPageWidth() / attributes.size());
		int x = 0;
		final int y = 2;
		for (final SubreportAttribute attribute : attributes) {
			final CMAttribute cmAttribute = attribute.getCMDBuildAttribute();
			if (cmAttribute == null) {
				continue;
			}

			String name;
			if (attribute.getQueryName() != null) {
				name = attribute.getQueryName();
			} else {
				name = attribute.getCMDBuildAttribute().getName();
			}

			final JRDesignTextField tf = createTextFieldForAttribute(name, attribute.getCMDBuildAttribute().getType());
			tf.setHeight(20);
			tf.setWidth(horizontalStep);
			tf.setX(x);
			tf.setY(y);
			band.getChildren().add(tf);

			x += horizontalStep;
		}
	}

}
