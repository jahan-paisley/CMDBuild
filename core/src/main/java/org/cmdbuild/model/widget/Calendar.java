package org.cmdbuild.model.widget;

public class Calendar extends Widget {

	private String eventClass;
	private String startDate;
	private String endDate;
	private String eventTitle;
	private String filter;
	private String defaultDate;

	@Override
	public void accept(final WidgetVisitor visitor) {
		visitor.visit(this);
	}

	public String getEventClass() {
		return this.eventClass;
	}

	public void setEventClass(final String eventClass) {
		this.eventClass = eventClass;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	public String getEventTitle() {
		return eventTitle;
	}

	public void setEventTitle(final String eventTitle) {
		this.eventTitle = eventTitle;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public String getDefaultDate() {
		return defaultDate;
	}

	public void setDefaultDate(final String defaultDate) {
		this.defaultDate = defaultDate;
	}

}
