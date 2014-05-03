package org.cmdbuild.model.dashboard;

import java.util.ArrayList;

/*
 * A representation of the definition of a chart
 */
public class ChartDefinition {
	private String name, description, dataSourceName, type, singleSeriesField, labelField, categoryAxisField,
			categoryAxisLabel, valueAxisLabel, fgcolor, bgcolor, chartOrientation;

	private boolean active, autoLoad, legend;

	private int height, maximum, minimum, steps;

	private ArrayList<ChartInput> dataSourceParameters;

	private ArrayList<String> valueAxisFields;

	public ChartDefinition() {
		dataSourceParameters = new ArrayList<ChartInput>();
		valueAxisFields = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(final String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getSingleSeriesField() {
		return singleSeriesField;
	}

	public void setSingleSeriesField(final String singleSeriesField) {
		this.singleSeriesField = singleSeriesField;
	}

	public String getLabelField() {
		return labelField;
	}

	public void setLabelField(final String labelField) {
		this.labelField = labelField;
	}

	public String getCategoryAxisField() {
		return categoryAxisField;
	}

	public void setCategoryAxisField(final String categoryAxisField) {
		this.categoryAxisField = categoryAxisField;
	}

	public String getCategoryAxisLabel() {
		return categoryAxisLabel;
	}

	public void setCategoryAxisLabel(final String categoryAxisLabel) {
		this.categoryAxisLabel = categoryAxisLabel;
	}

	public String getValueAxisLabel() {
		return valueAxisLabel;
	}

	public void setValueAxisLabel(final String valueAxisLabel) {
		this.valueAxisLabel = valueAxisLabel;
	}

	public String getFgcolor() {
		return fgcolor;
	}

	public void setFgcolor(final String fgcolor) {
		this.fgcolor = fgcolor;
	}

	public String getBgcolor() {
		return bgcolor;
	}

	public void setBgcolor(final String bgcolor) {
		this.bgcolor = bgcolor;
	}

	public String getChartOrientation() {
		return chartOrientation;
	}

	public void setChartOrientation(final String chartOrientation) {
		this.chartOrientation = chartOrientation;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public boolean isAutoLoad() {
		return autoLoad;
	}

	public void setAutoLoad(final boolean autoLoad) {
		this.autoLoad = autoLoad;
	}

	public boolean isLegend() {
		return legend;
	}

	public void setLegend(final boolean legend) {
		this.legend = legend;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public int getMaximum() {
		return maximum;
	}

	public void setMaximum(final int maximum) {
		this.maximum = maximum;
	}

	public int getMinimum() {
		return minimum;
	}

	public void setMinimum(final int minimum) {
		this.minimum = minimum;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(final int steps) {
		this.steps = steps;
	}

	// dataSourceParameters
	public ArrayList<ChartInput> getDataSourceParameters() {
		return dataSourceParameters;
	}

	public void setDataSourceParameters(final ArrayList<ChartInput> dataSourceParamenters) {
		this.dataSourceParameters = dataSourceParamenters;
	}

	public void addDataSourceParameter(final ChartInput input) {
		this.dataSourceParameters.add(input);
	}

	public void removeDataSourceParameter(final ChartInput input) {
		this.dataSourceParameters.remove(input);
	}

	// valueAxisFields
	public ArrayList<String> getValueAxisFields() {
		return valueAxisFields;
	}

	public void setValueAxisFields(final ArrayList<String> valueAxisFields) {
		this.valueAxisFields = valueAxisFields;
	}

	public void addValueAxisField(final String field) {
		this.valueAxisFields.add(field);
	}

	public void removeValueAxisField(final String field) {
		this.valueAxisFields.remove(field);
	}

	/*
	 * The representation of how the user could insert the value for a input of
	 * the data source of the chart
	 */

	public static class ChartInput {

		private String name, type, fieldType, defaultValue, lookupType, className, classToUseForReferenceWidget;

		private boolean required;

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(final String type) {
			this.type = type;
		}

		public String getFieldType() {
			return fieldType;
		}

		public void setFieldType(final String fieldType) {
			this.fieldType = fieldType;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
		}

		public String getLookupType() {
			return lookupType;
		}

		public void setLookupType(final String lookupType) {
			this.lookupType = lookupType;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(final String className) {
			this.className = className;
		}

		public String getClassToUseForReferenceWidget() {
			return classToUseForReferenceWidget;
		}

		public void setClassToUseForReferenceWidget(final String classToUseForReferenceWidget) {
			this.classToUseForReferenceWidget = classToUseForReferenceWidget;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(final boolean required) {
			this.required = required;
		}
	}
}
