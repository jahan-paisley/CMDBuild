package org.cmdbuild.privileges.predicates;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.model.Report;
import org.cmdbuild.services.store.report.ReportStore;

import com.google.common.base.Predicate;

public class IsReadableReport implements Predicate<CMCard> {

	private final ReportStore reportStore;

	public IsReadableReport(final ReportStore reportStore) {
		this.reportStore = reportStore;
	}

	@Override
	public boolean apply(final CMCard menuCard) {
		final Integer reportId = menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE, Integer.class);
		if (reportId == null) {
			return false;
		}

		final Report fetchedReport = reportStore.findReportById(reportId);
		if (fetchedReport == null) {
			return false;
		}

		return fetchedReport.isUserAllowed();
	}

}
